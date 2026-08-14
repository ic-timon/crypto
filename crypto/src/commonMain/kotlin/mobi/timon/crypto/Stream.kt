package mobi.timon.crypto
expect object Stream {
    fun aesCtrEncrypt(plaintext: ByteArray, key: ByteArray): ByteArray
    fun aesCtrDecrypt(ciphertext: ByteArray, key: ByteArray): ByteArray
    fun chacha20Encrypt(plaintext: ByteArray, key: ByteArray): ByteArray
    fun chacha20Decrypt(ciphertext: ByteArray, key: ByteArray): ByteArray
}
