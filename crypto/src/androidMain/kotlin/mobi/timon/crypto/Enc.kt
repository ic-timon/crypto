package mobi.timon.crypto
actual object Enc {
    init { System.loadLibrary("encrust") }
}
