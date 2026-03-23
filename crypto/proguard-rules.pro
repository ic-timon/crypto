# ProGuard rules for mobi.timon.crypto

# Keep JNI native methods - these are referenced by native .so libraries
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep all public native methods in crypto package
-keep class mobi.timon.crypto.** {
    *;
}

# Keep Kotlin external functions (these reference native methods)
-keep class kotlin.jvm.**
-keepclassmembers class * {
    @kotlin.jvm.* <fields>;
}

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
