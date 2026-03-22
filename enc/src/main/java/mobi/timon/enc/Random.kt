package mobi.timon.enc

import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder

object Random {

    init {
        Enc
    }

    external fun bytes(length: Int): ByteArray

    /**
     * 密码学安全随机有符号 32 位整数（均匀分布于完整 [Int.MIN_VALUE, Int.MAX_VALUE] 范围）。
     */
    fun int(): Int {
        val b = bytes(4)
        return ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN).int
    }

    /**
     * 在 **[min, max)** 上均匀随机（左闭右开）；需满足 max > min。
     */
    fun long(min: Long, max: Long): Long {
        val minB = BigInteger.valueOf(min)
        val maxB = BigInteger.valueOf(max)
        if (maxB <= minB) {
            throw EncException("randomLong: max must be greater than min")
        }
        val span = maxB.subtract(minB)
        val offset = uniformBelow(span)
        return minB.add(offset).toLongExact()
    }

    private fun uniformBelow(span: BigInteger): BigInteger {
        val byteLen = (span.bitLength() + 7) / 8 + 1
        val ceiling = BigInteger.ONE.shiftLeft(byteLen * 8)
        val limit = ceiling.divide(span).multiply(span)
        while (true) {
            val r = BigInteger(1, bytes(byteLen))
            if (r < limit) {
                return r.mod(span)
            }
        }
    }

    /**
     * [BigInteger.longValueExact] 需 API 31+；此处用位宽与相等性校验在任意 API 上得到精确 [Long]。
     */
    private fun BigInteger.toLongExact(): Long {
        if (bitLength() > 63) {
            throw EncException("randomLong: result overflow")
        }
        val lv = toLong()
        if (BigInteger.valueOf(lv) != this) {
            throw EncException("randomLong: result overflow")
        }
        return lv
    }
}
