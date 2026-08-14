package mobi.timon.crypto
expect object Hash {
    fun sha1(data: ByteArray): ByteArray
    fun sha256(data: ByteArray): ByteArray
    fun sha384(data: ByteArray): ByteArray
    fun sha512(data: ByteArray): ByteArray
    fun sha512_256(data: ByteArray): ByteArray
    fun blake2b256(data: ByteArray): ByteArray
    fun md5(data: ByteArray): ByteArray
    fun ripemd160(data: ByteArray): ByteArray
    fun keccak256(data: ByteArray): ByteArray
    fun keccak512(data: ByteArray): ByteArray
}
