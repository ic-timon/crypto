package mobi.timon.crypto

/**
 * Library initializer that loads native libraries.
 * 
 * This object must be referenced first (via `Enc`) before using any 
 * cryptographic function to this library. This triggers loading
 * of both `libencgo.so` and `libencjni.so`.
 * 
 * The library load order is:
 * 1. `libencgo.so` - Go compiled shared library
 * 2. `libencjni.so` - JNI bridge library
 * 
 * Note: All crypto objects in this library have an `init { Enc }` block
 * to ensure the native libraries are loaded before any external function calls.
 */
object Enc {
    init {
        System.loadLibrary("encgo")
        System.loadLibrary("encjni")
    }
}
