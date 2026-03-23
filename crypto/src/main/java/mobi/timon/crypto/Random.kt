package mobi.timon.crypto

import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Cryptographically secure random number generation.
 * 
 * Uses the underlying native library (via JNI/Go) for CSPRNG.
 * Suitable for generating keys, nonces, and other cryptographic values.
 */
object Random {

    init {
        Enc
    }

    /**
     * Generates cryptographically secure random bytes.
     * 
     * @param length Number of bytes to generate
     * @return Random byte array of specified length
     * @throws EncException if generation fails
     */
    external fun bytes(length: Int): ByteArray

    /**
     * Generates a cryptographically secure random 32-bit integer.
     * 
     * Values are uniformly distributed across the full [Int.MIN_VALUE, Int.MAX_VALUE] range.
     * 
     * @return Random 32-bit integer
     */
    fun int(): Int {
        val b = bytes(4)
        return ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN).int
    }

    /**
     * Generates a cryptographically secure random long in a range [min, max).
     * 
     * The distribution is uniform over [min, max) (half-open interval).
     * 
     * @param min Minimum value (inclusive)
     * @param max Maximum value (exclusive)
     * @return Random long in [min, max)
     * @throws EncException if max <= min or range is invalid
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
