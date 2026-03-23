package mobi.timon.crypto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class RandomTest {

    @Test
    fun int_returnsValueInValidRange() {
        repeat(100) {
            val value = Random.int()
            assertTrue(value >= Int.MIN_VALUE)
            assertTrue(value <= Int.MAX_VALUE)
        }
    }

    @Test
    fun int_samplesAreNotAllIdentical() {
        val samples = List(100) { Random.int() }
        assertTrue(samples.distinct().size > 1, "Random int should produce varied values")
    }

    @Test
    fun long_validRange_returnsValueWithinRange() {
        repeat(100) {
            val value = Random.long(0L, 100L)
            assertTrue(value >= 0L, "Value should be >= min")
            assertTrue(value < 100L, "Value should be < max")
        }
    }

    @Test
    fun long_negativeRange_returnsValueWithinRange() {
        repeat(100) {
            val value = Random.long(-1000L, 1000L)
            assertTrue(value >= -1000L)
            assertTrue(value < 1000L)
        }
    }

    @Test
    fun long_largeRange_returnsValueWithinRange() {
        val min = Long.MIN_VALUE
        val max = Long.MAX_VALUE
        repeat(10) {
            val value = Random.long(min, max)
            assertTrue(value >= min)
            assertTrue(value < max)
        }
    }

    @Test
    fun long_equalMinAndMax_throwsEncException() {
        assertThrows(EncException::class.java) {
            Random.long(10L, 10L)
        }
    }

    @Test
    fun long_maxLessThanMin_throwsEncException() {
        assertThrows(EncException::class.java) {
            Random.long(100L, 10L)
        }
    }

    @Test
    fun long_singleValueRange_throwsEncException() {
        assertThrows(EncException::class.java) {
            Random.long(42L, 42L)
        }
    }

    @Test
    fun bytes_requestedLength_returnsCorrectLength() {
        val lengths = listOf(1, 16, 32, 64, 128, 256)
        lengths.forEach { len ->
            val result = Random.bytes(len)
            assertEquals(len, result.size)
        }
    }

    @Test
    fun bytes_multipleCalls_returnsDifferentValues() {
        val a = Random.bytes(32)
        val b = Random.bytes(32)
        assertFalse(a.contentEquals(b), "Two random byte arrays should differ")
    }

    @Test
    fun bytes_largeRequest_returnsCorrectLength() {
        val result = Random.bytes(1024)
        assertEquals(1024, result.size)
    }

    private fun repeat(times: Int, block: () -> Unit) {
        repeat(times) { block() }
    }
}
