package mobi.timon.crypto

object Enc {
    init {
        System.loadLibrary("encgo")
        System.loadLibrary("encjni")
    }
}
