package mobi.timon.crypto
actual object Cbc {
    init { Enc }
    actual external fun aesCbcEncrypt(plaintext: ByteArray, key: ByteArray): ByteArray
    actual external fun aesCbcDecrypt(ciphertext: ByteArray, key: ByteArray): ByteArray
    actual external fun desCbcEncrypt(plaintext: ByteArray, key: ByteArray): ByteArray
    actual external fun desCbcDecrypt(ciphertext: ByteArray, key: ByteArray): ByteArray
}
