package mobi.timon.crypto
actual object Rsa {
    init { Enc }
    actual external fun generateKey(bits: Int): ByteArray
    actual external fun encrypt(plaintext: ByteArray, publicKey: ByteArray): ByteArray
    actual external fun decrypt(ciphertext: ByteArray, privateKey: ByteArray): ByteArray
    actual external fun sign(message: ByteArray, privateKey: ByteArray): ByteArray
    actual external fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
    actual external fun privateKeyToPublicKey(privateKey: ByteArray): ByteArray
}
