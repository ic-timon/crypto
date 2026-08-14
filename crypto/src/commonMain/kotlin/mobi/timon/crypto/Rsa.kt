package mobi.timon.crypto
expect object Rsa {
    fun generateKey(bits: Int): ByteArray
    fun encrypt(plaintext: ByteArray, publicKey: ByteArray): ByteArray
    fun decrypt(ciphertext: ByteArray, privateKey: ByteArray): ByteArray
    fun sign(message: ByteArray, privateKey: ByteArray): ByteArray
    fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
    fun privateKeyToPublicKey(privateKey: ByteArray): ByteArray
}
