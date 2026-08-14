package mobi.timon.crypto
actual object Stream {
    init { Enc }
    actual external fun aesCtrEncrypt(plaintext: ByteArray, key: ByteArray): ByteArray
    actual external fun aesCtrDecrypt(ciphertext: ByteArray, key: ByteArray): ByteArray
    actual external fun chacha20Encrypt(plaintext: ByteArray, key: ByteArray): ByteArray
    actual external fun chacha20Decrypt(ciphertext: ByteArray, key: ByteArray): ByteArray
}
