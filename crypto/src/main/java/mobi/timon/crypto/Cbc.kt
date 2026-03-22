package mobi.timon.crypto

object Cbc {

    init {
        Enc
    }

    /**
     * AES-CBC 加密（自动 PKCS7 填充）
     * @param plaintext 明文
     * @param key 密钥（16/24/32 字节对应 AES-128/192/256）
     * @return 密文 = iv(16) + ciphertext
     * @throws EncException 加密失败时抛出
     */
    external fun aesCbcEncrypt(plaintext: ByteArray, key: ByteArray): ByteArray

    /**
     * AES-CBC 解密(自动 PKCS7 去填充)
     * @param ciphertext 密文 = iv(16) + ciphertext
     * @param key 密钥（16/24/32 字节）
     * @return 明文
     * @throws EncException 解密失败时抛出
     */
    external fun aesCbcDecrypt(ciphertext: ByteArray, key: ByteArray): ByteArray

    /**
     * DES-CBC 加密（仅用于遗留系统兼容）
     * @param plaintext 明文
     * @param key 密钥（8 字节）
     * @return 密文 = iv(8) + ciphertext
     * @throws EncException 加密失败时抛出
     */
    external fun desCbcEncrypt(plaintext: ByteArray, key: ByteArray): ByteArray

    /**
     * DES-CBC 解密（仅用于遗留系统兼容）
     * @param ciphertext 密文 = iv(8) + ciphertext
     * @param key 密钥（8 字节）
     * @return 明文
     * @throws EncException 解密失败时抛出
     */
    external fun desCbcDecrypt(ciphertext: ByteArray, key: ByteArray): ByteArray
}
