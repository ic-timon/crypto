package mobi.timon.crypto

import kotlin.math.absoluteValue

/** 平台随机字节桥（Android=JNI, Apple=cinterop）。 */
internal expect fun platformRandomBytes(length: Int): ByteArray

/** CSPRNG。bytes 走平台 native，int/long 纯 Kotlin。 */
object Random {
    fun bytes(length: Int): ByteArray = platformRandomBytes(length)

    fun int(): Int {
        val b = bytes(4)
        return ((b[0].toInt() and 0xFF) shl 24) or
            ((b[1].toInt() and 0xFF) shl 16) or
            ((b[2].toInt() and 0xFF) shl 8) or
            (b[3].toInt() and 0xFF)
    }

    /** 均匀采样半开区间 [min, max)。max <= min 抛 EncException。 */
    fun long(min: Long, max: Long): Long {
        if (max <= min) throw EncException("Random.long: max must be greater than min")
        val range = max - min
        val mask = (Long.SIZE_BITS - 1) - (range - 1).countLeadingZeroBits()
        val bitsNeeded = if (mask < 0) 0 else mask
        if (bitsNeeded <= Long.SIZE_BITS - 1) {
            // rejection sampling within [0, 2^bits)
            val limit = (1L shl bitsNeeded)
            while (true) {
                val v = nextLongBits(bitsNeeded.toInt())
                if (v < range) return min + v
            }
        }
        return min + (nextLongBits(63).let { if (it < 0) it.absoluteValue else it } % range)
    }

    private fun nextLongBits(bits: Int): Long {
        val b = bytes(8)
        var v = 0L
        for (i in 0 until 8) v = (v shl 8) or (b[i].toLong() and 0xFF)
        return v ushr (Long.SIZE_BITS - bits)
    }
}
