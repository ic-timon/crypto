package mobi.timon.enc

object Stream {

    init {
        Enc
    }

    external fun aesCtrEncrypt(plaintext: ByteArray, key: ByteArray): ByteArray

    external fun aesCtrDecrypt(ciphertext: ByteArray, key: ByteArray): ByteArray

    external fun chacha20Encrypt(plaintext: ByteArray, key: ByteArray): ByteArray

    external fun chacha20Decrypt(ciphertext: ByteArray, key: ByteArray): ByteArray
}
