package mobi.timon.crypto
expect object Hmac {
    fun hmacSha256(data: ByteArray, key: ByteArray): ByteArray
    fun hmacSha512(data: ByteArray, key: ByteArray): ByteArray
    fun hmacSha1(data: ByteArray, key: ByteArray): ByteArray
}
