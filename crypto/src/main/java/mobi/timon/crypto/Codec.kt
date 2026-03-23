package mobi.timon.crypto

import android.annotation.SuppressLint
import java.util.Base64

object Codec {

    /**
     * 转换为十六进制字符串
     * @param data 字节数组
     * @return 十六进制字符串（小写）
     */
    fun toHex(data: ByteArray): String {
        return data.joinToString("") { "%02x".format(it) }
    }

    /**
     * 从十六进制字符串解析
     * @param hex 十六进制字符串
     * @return 字节数组
     * @throws EncException 解析失败时抛出
     */
    fun fromHex(hex: String): ByteArray {
        if (hex.length % 2 != 0) {
            throw EncException("fromHex: hex string length must be even")
        }
        return hex.chunked(2).map { 
            it.toIntOrNull(16)?.toByte() 
                ?: throw EncException("fromHex: invalid hex character")
        }.toByteArray()
    }

    /**
     * 转换为 Base64 字符串
     * @param data 字节数组
     * @return Base64 字符串（无换行）
     */
    @SuppressLint("NewApi")
    fun toBase64(data: ByteArray): String {
        return Base64.getEncoder().encodeToString(data)
    }

    /**
     * 从 Base64 字符串解析
     * @param base64 Base64 字符串
     * @return 字节数组
     * @throws EncException 解析失败时抛出
     */
    @SuppressLint("NewApi")
    fun fromBase64(base64: String): ByteArray {
        return Base64.getDecoder().decode(base64)
    }

    /**
     * Constant-time byte array comparison.
     * 
     * Compares two byte arrays in constant time to prevent timing attacks.
     * This should be used when comparing sensitive data such as:
     * - MACs / authentication tags
     * - Password hashes
     * - Cryptographic keys
     * 
     * @param a First byte array
     * @param b Second byte array
     * @return true if arrays are equal, false otherwise
     */
    fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }

    /**
     * Securely wipe a byte array by overwriting with zeros.
     * 
     * Should be called to clear sensitive data from memory after use:
     * - Private keys
     * - Password bytes
     * - Plaintext buffers
     * 
     * Note: This is a best-effort security measure. The JVM may still have
     * copies of the data in memory due to garbage collection behavior.
     * 
     * @param data The byte array to wipe
     */
    fun wipe(data: ByteArray) {
        data.fill(0)
    }
}
