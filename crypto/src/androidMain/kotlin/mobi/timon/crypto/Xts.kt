package mobi.timon.crypto
actual object Xts {
    init { Enc }
    actual external fun aesXtsEncrypt(plaintext: ByteArray, key: ByteArray, sectorNum: Long): ByteArray
    actual external fun aesXtsDecrypt(ciphertext: ByteArray, key: ByteArray, sectorNum: Long): ByteArray
}
