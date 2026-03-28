import java.io.File

plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

val ndkVersion: String? = providers.gradleProperty("android.ndkVersion").orNull

val goDir = file("src/main/go")
val goLibDir = file("src/main/go/lib")

val abis = listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")

val goArchMap = mapOf(
    "arm64-v8a" to "arm64",
    "armeabi-v7a" to "arm",
    "x86" to "386",
    "x86_64" to "amd64"
)

val minSdk = 33

android {
    namespace = "mobi.timon.crypto"
    ndkVersion = ndkVersion

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        this.minSdk = minSdk
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        ndk {
            abiFilters += abis
        }

        externalNativeBuild {
            cmake {
                arguments += listOf(
                    "-DANDROID_STL=c++_shared",
                    "-DANDROID_ARM_MODE=arm"
                )
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.directories.add(goLibDir.absolutePath)
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

fun getNdkPath(): File {
    val sdkDir = if (project.hasProperty("android.sdk.path")) {
        file(project.property("android.sdk.path") as String)
    } else {
        val localProps = File(project.rootDir, "local.properties")
        if (localProps.exists()) {
            localProps.readLines()
                .find { it.startsWith("sdk.dir=") }
                ?.substringAfter("=")
                ?.let { file(it) }
        } else {
            null
        } ?: throw GradleException("SDK path not found. Ensure local.properties contains sdk.dir")
    }

    val ndkDir = if (ndkVersion != null) {
        File(sdkDir, "ndk/$ndkVersion")
    } else {
        val ndkRoot = File(sdkDir, "ndk")
        ndkRoot.listFiles()?.maxByOrNull { it.name } ?: throw GradleException("NDK not found")
    }

    if (!ndkDir.exists()) {
        throw GradleException("NDK not found at $ndkDir")
    }
    return ndkDir
}

fun getClangPath(abi: String, ndkPath: File): File {
    val osName = System.getProperty("os.name").lowercase()
    val hostTag = when {
        osName.contains("mac") -> "darwin-x86_64"
        osName.contains("linux") -> "linux-x86_64"
        osName.contains("windows") -> "windows-x86_64"
        else -> throw GradleException("Unsupported OS: $osName")
    }

    val triple = when (abi) {
        "arm64-v8a" -> "aarch64-linux-android"
        "armeabi-v7a" -> "armv7a-linux-androideabi"
        "x86" -> "i686-linux-android"
        "x86_64" -> "x86_64-linux-android"
        else -> throw GradleException("Unknown ABI: $abi")
    }

    val clang = File(ndkPath, "toolchains/llvm/prebuilt/$hostTag/bin/${triple}${minSdk}-clang")
    if (!clang.exists()) {
        throw GradleException("Clang not found at $clang")
    }
    return clang
}

fun compileGoForAbi(abi: String, ndkPath: File) {
    val goArch = goArchMap[abi] ?: throw GradleException("Unknown ABI: $abi")
    val clangPath = getClangPath(abi, ndkPath)

    val outputDir = File(goLibDir, abi)
    outputDir.mkdirs()

    val env = mutableMapOf<String, String>()
    env["CGO_ENABLED"] = "1"
    env["GOOS"] = "android"
    env["GOARCH"] = goArch
    env["CC"] = clangPath.absolutePath
    env["CXX"] = clangPath.absolutePath.replace("-clang", "-clang++")

    val cflags = when (abi) {
        "arm64-v8a" -> "-O3 -march=armv8-a+crypto+simd -mtune=cortex-a76"
        "armeabi-v7a" -> "-O3 -mfpu=neon-vfpv4 -mfloat-abi=softfp -mtune=cortex-a53"
        "x86" -> "-O3 -msse4.2 -mfpmath=sse"
        "x86_64" -> "-O3 -msse4.2 -mavx -mavx2 -mfpmath=sse"
        else -> "-O3"
    }
    env["CGO_CFLAGS"] = cflags
    env["CGO_CXXFLAGS"] = cflags
    env["GODEBUG"] = "gctrace=0"

    if (abi == "armeabi-v7a") {
        env["GOARM"] = "7"
    }

    val outputFile = File(outputDir, "libencgo.so")

    val buildArgs = mutableListOf(
        "go", "build",
        "-buildmode=c-shared",
        "-ldflags", "-s -w",
        "-trimpath",
        "-o", outputFile.absolutePath,
        "."
    )

    val pb = ProcessBuilder(buildArgs)
    pb.directory(goDir)
    pb.environment().putAll(env)
    pb.redirectErrorStream(true)

    val process = pb.start()
    val output = process.inputStream.bufferedReader().readText()
    val exitCode = process.waitFor()

    if (exitCode != 0) {
        throw GradleException("Go compilation failed for $abi:\n$output")
    }
}

tasks.register("compileGoDebug") {
    group = "build"
    description = "Compile Go code for all ABIs (Debug)"

    inputs.dir(goDir).withPropertyName("goSource")
    outputs.dir(goLibDir).withPropertyName("goOutput")

    doLast {
        val ndkPath = getNdkPath()
        println("Using NDK: $ndkPath")

        abis.forEach { abi ->
            println("Compiling Go for $abi...")
            compileGoForAbi(abi, ndkPath)
        }

        println("Go compilation completed for all ABIs")
    }
}

tasks.register("compileGoRelease") {
    group = "build"
    description = "Compile Go code for all ABIs (Release)"

    inputs.dir(goDir).withPropertyName("goSource")
    outputs.dir(goLibDir).withPropertyName("goOutput")

    doLast {
        val ndkPath = getNdkPath()
        println("Using NDK: $ndkPath")

        abis.forEach { abi ->
            println("Compiling Go for $abi (Release)...")
            compileGoForAbi(abi, ndkPath)
        }

        println("Go compilation completed for all ABIs")
    }
}

tasks.named("preBuild") {
    dependsOn("compileGoDebug")
}

tasks.configureEach {
    if (name == "externalNativeBuildRelease") {
        dependsOn("compileGoRelease")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.multidex)
}

fun getVersionFromGitTag(): String {
    val process = ProcessBuilder("git", "describe", "--tags", "--abbrev=0").start()
    val exitCode = process.waitFor()
    return if (exitCode == 0) {
        process.inputStream.bufferedReader().readText().trim().removePrefix("v")
    } else {
        "SNAPSHOT"
    }
}

val libraryVersion = getVersionFromGitTag()

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "io.github.ic-timon.crypto"
            artifactId = "crypto"
            version = libraryVersion

            from(components.findByName("release"))

            pom {
                name.set("crypto")
                description.set("Android cryptographic library with Go backend")
                url.set("https://github.com/ic-timon/crypto")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("ic-timon")
                        name.set("Timon")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/ic-timon/crypto.git")
                    developerConnection.set("scm:git:ssh://github.com/ic-timon/crypto.git")
                    url.set("https://github.com/ic-timon/crypto")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ic-timon/crypto")
            credentials {
                username = project.findProperty("gpr.user") as String?
                    ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.token") as String?
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
