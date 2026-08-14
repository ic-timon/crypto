package mobi.timon.crypto
actual object Hash {
    init { Enc }
    actual external fun sha1(data: ByteArray): ByteArray
    actual external fun sha256(data: ByteArray): ByteArray
    actual external fun sha384(data: ByteArray): ByteArray
    actual external fun sha512(data: ByteArray): ByteArray
    actual external fun sha512_256(data: ByteArray): ByteArray
    actual external fun blake2b256(data: ByteArray): ByteArray
    actual external fun md5(data: ByteArray): ByteArray
    actual external fun ripemd160(data: ByteArray): ByteArray
    actual external fun keccak256(data: ByteArray): ByteArray
    actual external fun keccak512(data: ByteArray): ByteArray
}
