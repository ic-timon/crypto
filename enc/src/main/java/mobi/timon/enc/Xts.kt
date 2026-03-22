package mobi.timon.enc

object Xts {

    init {
        Enc
    }

    external fun aesXtsEncrypt(plaintext: ByteArray, key: ByteArray, sectorNum: Long): ByteArray

    external fun aesXtsDecrypt(ciphertext: ByteArray, key: ByteArray, sectorNum: Long): ByteArray
}
