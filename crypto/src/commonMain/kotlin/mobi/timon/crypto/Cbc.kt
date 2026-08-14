package mobi.timon.crypto
expect object Cbc {
    fun aesCbcEncrypt(plaintext: ByteArray, key: ByteArray): ByteArray
    fun aesCbcDecrypt(ciphertext: ByteArray, key: ByteArray): ByteArray
    fun desCbcEncrypt(plaintext: ByteArray, key: ByteArray): ByteArray
    fun desCbcDecrypt(ciphertext: ByteArray, key: ByteArray): ByteArray
}
