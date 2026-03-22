package mobi.timon.crypto

object Hmac {

    init {
        Enc
    }

    external fun hmacSha256(data: ByteArray, key: ByteArray): ByteArray

    external fun hmacSha512(data: ByteArray, key: ByteArray): ByteArray
}
