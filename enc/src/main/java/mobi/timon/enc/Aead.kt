package mobi.timon.enc

object Aead {

    init {
        Enc
    }

    external fun aesGcmEncrypt(plaintext: ByteArray, key: ByteArray): ByteArray

    external fun aesGcmDecrypt(ciphertext: ByteArray, key: ByteArray): ByteArray

    external fun chacha20Poly1305Encrypt(plaintext: ByteArray, key: ByteArray): ByteArray

    external fun chacha20Poly1305Decrypt(ciphertext: ByteArray, key: ByteArray): ByteArray
}
