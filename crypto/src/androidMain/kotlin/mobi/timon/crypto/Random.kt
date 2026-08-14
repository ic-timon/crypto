package mobi.timon.crypto
actual object Random {
    init { Enc }
    actual external fun bytes(length: Int): ByteArray
}
