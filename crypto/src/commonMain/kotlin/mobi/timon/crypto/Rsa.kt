package mobi.timon.crypto
expect object Rsa {
    fun generateKey(bits: Int): ByteArray
    fun encrypt(plaintext: ByteArray, publicKey: ByteArray): ByteArray
    fun decrypt(ciphertext: ByteArray, privateKey: ByteArray): ByteArray
    fun sign(message: ByteArray, privateKey: ByteArray): ByteArray
    fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
    /** Verify a prehashed SHA-256/384/512 digest with matching MGF1 and exact salt length. */
    fun verifyPss(modulus: ByteArray, exponent: ByteArray, digest: ByteArray, signature: ByteArray, saltLength: Int): Boolean
    fun privateKeyToPublicKey(privateKey: ByteArray): ByteArray
}
