package mobi.timon.crypto
actual object Aead {
    init { Enc }
    actual external fun aesGcmEncrypt(plaintext: ByteArray, key: ByteArray): ByteArray
    actual external fun aesGcmDecrypt(ciphertext: ByteArray, key: ByteArray): ByteArray
    actual external fun chacha20Poly1305Encrypt(plaintext: ByteArray, key: ByteArray): ByteArray
    actual external fun chacha20Poly1305Decrypt(ciphertext: ByteArray, key: ByteArray): ByteArray
}
