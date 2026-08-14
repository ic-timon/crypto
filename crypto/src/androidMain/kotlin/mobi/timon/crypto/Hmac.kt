package mobi.timon.crypto
actual object Hmac {
    init { Enc }
    actual external fun hmacSha256(data: ByteArray, key: ByteArray): ByteArray
    actual external fun hmacSha512(data: ByteArray, key: ByteArray): ByteArray
    actual external fun hmacSha1(data: ByteArray, key: ByteArray): ByteArray
}
