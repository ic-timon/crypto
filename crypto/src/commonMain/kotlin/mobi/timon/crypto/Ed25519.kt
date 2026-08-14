package mobi.timon.crypto
expect object Ed25519 {
    fun generateKey(): ByteArray
    fun sign(message: ByteArray, privateKey: ByteArray): ByteArray
    fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
}
