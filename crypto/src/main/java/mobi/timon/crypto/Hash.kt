package mobi.timon.crypto

object Hash {

    init {
        Enc
    }

    external fun sha1(data: ByteArray): ByteArray

    external fun sha256(data: ByteArray): ByteArray

    external fun sha512(data: ByteArray): ByteArray

    external fun blake2b256(data: ByteArray): ByteArray

    external fun md5(data: ByteArray): ByteArray

    external fun ripemd160(data: ByteArray): ByteArray

    external fun keccak256(data: ByteArray): ByteArray

    external fun keccak512(data: ByteArray): ByteArray
}
