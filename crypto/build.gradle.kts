import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

// :crypto — KMP 加密库（Rust core via JNI/cinterop）。
// Android: JNI → libencrust.so（cargo-ndk 构建 4 ABI）
// Apple: cinterop → libencrust.a（cargo 构建各 Apple target）
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    `maven-publish`
}

kotlin {
    applyDefaultHierarchyTemplate()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        compilerOptions { jvmTarget = JvmTarget.JVM_11 }
    }

    // Apple targets
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    macosArm64()
    macosX64()
    tvosArm64()
    watchosArm64()

    // cinterop for each Apple target
    fun KotlinNativeTarget.cryptoCInterop() {
        compilations["main"].cinterops {
            val cryptoNative by creating {
                includeDirs.headerFilterOnly(project.file("src/nativeInterop/cinterop"))
                tasks[interopProcessingTaskName].dependsOn("buildRustApple")
            }
        }
    }

    iosArm64 { cryptoCInterop() }
    iosSimulatorArm64 { cryptoCInterop() }
    iosX64 { cryptoCInterop() }
    macosArm64 { cryptoCInterop() }
    macosX64 { cryptoCInterop() }
    tvosArm64 { cryptoCInterop() }
    watchosArm64 { cryptoCInterop() }

    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("stdlib"))
        }
    }
}

android {
    namespace = "mobi.timon.crypto"
    compileSdk = 36

    defaultConfig {
        minSdk = 33
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets["main"].jniLibs.srcDirs("src/androidMain/jniLibs")
}

// ── Rust build tasks ──────────────────────────────────────────

fun runCargo(dir: File, ndkHome: String?, args: List<String>) {
    val pb = ProcessBuilder(listOf("cargo") + args)
        .directory(dir)
        .inheritIO()
    if (ndkHome != null) pb.environment()["ANDROID_NDK_HOME"] = ndkHome
    pb.environment()["IPHONEOS_DEPLOYMENT_TARGET"] = "13.0"
    val rc = pb.start().waitFor()
    if (rc != 0) throw GradleException("cargo ${args.joinToString(" ")} failed with exit code $rc")
}



val rustDir = file("../rust")
val ndkHome = System.getenv("ANDROID_NDK_HOME")
    ?: file("${System.getProperty("user.home")}/Library/Android/sdk/ndk").takeIf { it.exists() }
        ?.listFiles()?.maxOrNull()?.toString()

// Android: cargo-ndk build for 4 ABIs → libencrust.so
val buildRustAndroid by tasks.registering {
    group = "build"
    val abis = mapOf(
        "arm64-v8a" to "aarch64-linux-android",
        "armeabi-v7a" to "armv7-linux-androideabi",
        "x86" to "i686-linux-android",
        "x86_64" to "x86_64-linux-android",
    )
    doLast {
        abis.forEach { (abi, target) ->
            val outDir = file("src/androidMain/jniLibs/$abi")
            outDir.mkdirs()
            runCargo(rustDir, ndkHome, listOf("ndk", "-t", target, "-P", "33", "build", "--release", "--features", "jni-bridge"))
            project.copy {
                from(rustDir.resolve("target/$target/release/libencrust.so"))
                into(outDir)
            }
        }
    }
}

// Apple: cargo build for each Apple target → libencrust.a
val buildRustApple by tasks.registering {
    group = "build"
    val targets = listOf(
        "aarch64-apple-ios",
        "aarch64-apple-ios-sim",
        "x86_64-apple-darwin",
    )
    doLast {
        targets.forEach { target ->
            runCargo(rustDir, null, listOf("build", "--release", "--target", target))
        }
    }
}

// Ensure native libs are built before Android assemble
tasks.named("preBuild") { dependsOn("buildRustAndroid") }

// ── Maven publish ─────────────────────────────────────────────

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ic-timon/crypto")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.token").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

// 诊断：打印 source sets
tasks.register("printSourceSets") {
    doLast {
        kotlin.sourceSets.forEach { ss ->
            println("SS: ${ss.name} -> ${ss.kotlin.srcDirs}")
        }
    }
}

dependencies {
    "androidTestImplementation"(libs.junit)
    "androidTestImplementation"(libs.androidx.junit)
    "androidTestImplementation"(libs.androidx.runner.alias)
    "androidTestImplementation"(libs.kotlin.testJunit)
}
