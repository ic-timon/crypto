package mobi.timon.crypto
expect object Aead {
    fun aesGcmEncrypt(plaintext: ByteArray, key: ByteArray): ByteArray
    fun aesGcmDecrypt(ciphertext: ByteArray, key: ByteArray): ByteArray
    fun chacha20Poly1305Encrypt(plaintext: ByteArray, key: ByteArray): ByteArray
    fun chacha20Poly1305Decrypt(ciphertext: ByteArray, key: ByteArray): ByteArray
}
