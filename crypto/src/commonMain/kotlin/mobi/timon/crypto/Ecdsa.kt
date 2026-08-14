package mobi.timon.crypto
expect object Ecdsa {
    fun generateKey(curveBits: Int): ByteArray
    fun sign(message: ByteArray, privateKey: ByteArray): ByteArray
    fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
    fun privateKeyToPublicKey(privateKey: ByteArray): ByteArray
}
