package mobi.timon.crypto

/** 纯 Kotlin 实现（KMP 跨平台，无 android/java 依赖）。 */
object Codec {

    private val HEX_CHARS = "0123456789abcdef".toCharArray()
    private val B64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray()

    /** 转换为十六进制字符串（小写）。 */
    fun toHex(data: ByteArray): String {
        val out = CharArray(data.size * 2)
        for (i in data.indices) {
            val v = data[i].toInt() and 0xFF
            out[i * 2] = HEX_CHARS[v ushr 4]
            out[i * 2 + 1] = HEX_CHARS[v and 0x0F]
        }
        return out.concatToString()
    }

    /** 从十六进制字符串解析（奇数长度或非法字符抛 EncException）。 */
    fun fromHex(hex: String): ByteArray {
        if (hex.length % 2 != 0) throw EncException("fromHex: hex string length must be even")
        val out = ByteArray(hex.length / 2)
        for (i in out.indices) {
            val hi = hexCharValue(hex[i * 2])
            val lo = hexCharValue(hex[i * 2 + 1])
            if (hi < 0 || lo < 0) throw EncException("fromHex: invalid hex character")
            out[i] = ((hi shl 4) or lo).toByte()
        }
        return out
    }

    private fun hexCharValue(c: Char): Int = when (c) {
        in '0'..'9' -> c - '0'
        in 'a'..'f' -> c - 'a' + 10
        in 'A'..'F' -> c - 'A' + 10
        else -> -1
    }

    /** 转换为 Base64 字符串（无换行，标准字母表 + padding）。 */
    fun toBase64(data: ByteArray): String {
        val out = StringBuilder(((data.size + 2) / 3) * 4)
        var i = 0
        while (i + 2 < data.size) {
            val v = ((data[i].toInt() and 0xFF) shl 16) or
                ((data[i + 1].toInt() and 0xFF) shl 8) or
                (data[i + 2].toInt() and 0xFF)
            out.append(B64_CHARS[(v ushr 18) and 0x3F])
            out.append(B64_CHARS[(v ushr 12) and 0x3F])
            out.append(B64_CHARS[(v ushr 6) and 0x3F])
            out.append(B64_CHARS[v and 0x3F])
            i += 3
        }
        when (data.size - i) {
            1 -> {
                val v = (data[i].toInt() and 0xFF) shl 16
                out.append(B64_CHARS[(v ushr 18) and 0x3F])
                out.append(B64_CHARS[(v ushr 12) and 0x3F])
                out.append("==")
            }
            2 -> {
                val v = ((data[i].toInt() and 0xFF) shl 16) or ((data[i + 1].toInt() and 0xFF) shl 8)
                out.append(B64_CHARS[(v ushr 18) and 0x3F])
                out.append(B64_CHARS[(v ushr 12) and 0x3F])
                out.append(B64_CHARS[(v ushr 6) and 0x3F])
                out.append('=')
            }
        }
        return out.toString()
    }

    /** 从 Base64 字符串解析（非法字符抛 EncException）。 */
    fun fromBase64(base64: String): ByteArray {
        var cleaned = base64.replace("\n", "").replace("\r", "")
        var padCount = 0
        while (cleaned.endsWith("=")) {
            cleaned = cleaned.dropLast(1)
            padCount++
        }
        if (padCount > 2) throw EncException("fromBase64: invalid padding")
        val outLen = cleaned.length * 6 / 8
        val out = ByteArray(outLen)
        var buffer = 0
        var bits = 0
        var idx = 0
        for (c in cleaned) {
            val v = b64CharValue(c)
            if (v < 0) throw EncException("fromBase64: invalid character")
            buffer = (buffer shl 6) or v
            bits += 6
            if (bits >= 8) {
                bits -= 8
                if (idx < out.size) {
                    out[idx++] = ((buffer shr bits) and 0xFF).toByte()
                }
            }
        }
        return out
    }

    private fun b64CharValue(c: Char): Int = when (c) {
        in 'A'..'Z' -> c - 'A'
        in 'a'..'z' -> c - 'a' + 26
        in '0'..'9' -> c - '0' + 52
        '+' -> 62
        '/' -> 63
        else -> -1
    }

    /** Constant-time byte array comparison（防时序攻击）。 */
    fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) result = result or (a[i].toInt() xor b[i].toInt())
        return result == 0
    }

    /** 内存擦除（zero-fill）。 */
    fun wipe(data: ByteArray) {
        data.fill(0)
    }
}
