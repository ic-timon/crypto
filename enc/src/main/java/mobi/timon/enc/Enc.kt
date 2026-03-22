package mobi.timon.enc

object Enc {
    init {
        System.loadLibrary("encgo")
        System.loadLibrary("encjni")
    }
}
