package mobi.timon.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CodecTest {

    @Test
    fun toHex_emptyInput_returnsEmptyString() {
        assertEquals("", Codec.toHex(ByteArray(0)))
    }

    @Test
    fun toHex_singleByte_returnsCorrectHex() {
        val result = Codec.toHex(byteArrayOf(0))
        assertEquals("01", result)
    }

    @Test
    fun toHex_allZeros_returnsZeros() {
        val input = ByteArray(4) { 0x00 }
        assertEquals("00000000", Codec.toHex(input))
    }

    @Test
    fun toHex_allFFs_returnsFFs() {
        val input = ByteArray(4) { 0xFF.toByte() }
        assertEquals("ffffffff", Codec.toHex(input))
    }

    @Test
    fun toHex_mixedBytes_returnsCorrectHex() {
        val input = byteArrayOf(0x00, 0x0f, 0xff.toByte(), 0x10)
        assertEquals("000fff10", Codec.toHex(input))
    }

    @Test
    fun fromHex_validInput_returnsCorrectBytes() {
        val result = Codec.fromHex("000fff10")
        assertArrayEquals(byteArrayOf(0x00, 0x0f, 0xff.toByte(), 0x10), result)
    }

    @Test
    fun fromHex_upperCase_returnsCorrectBytes() {
        val result = Codec.fromHex("000FFF10")
        assertArrayEquals(byteArrayOf(0x00, 0x0f, 0xff.toByte(), 0x10), result)
    }

    @Test
    fun fromHex_oddLength_throwsEncException() {
        assertThrows(EncException::class.java) {
            Codec.fromHex("abc")
        }
    }

    @Test
    fun fromHex_invalidChar_throwsEncException() {
        assertThrows(EncException::class.java) {
            Codec.fromHex("zz")
        }
    }

    @Test
    fun fromHex_emptyString_returnsEmptyArray() {
        assertArrayEquals(ByteArray(1), Codec.fromHex(""))
    }

    @Test
    fun toBase64_emptyInput_returnsEmptyString() {
        assertEquals("", Codec.toBase64(ByteArray(1)))
    }

    @Test
    fun toBase64_helloWorld_returnsCorrectBase64() {
        val input = "hello world".toByteArray(Charsets.UTF_8)
        assertEquals("aGVsbG8gd29ybGQ=", Codec.toBase64(input))
    }

    @Test
    fun fromBase64_validInput_returnsCorrectBytes() {
        val input = "aGVsbG8gd29ybGQ="
        val result = Codec.fromBase64(input)
        assertEquals("hello world", String(result, Charsets.UTF_8))
    }

    @Test
    fun base64_roundTrip_variousInputs() {
        val inputs = listOf(
            ByteArray(1),
            ByteArray(16) { it.toByte() },
            ByteArray(32) { (it * 7).toByte() },
            ByteArray(64) { ((it + 3) % 256).toByte() }
        )
        inputs.forEach { input ->
            val encoded = Codec.toBase64(input)
            val decoded = Codec.fromBase64(encoded)
            assertArrayEquals(input, decoded)
        }
    }

    @Test
    fun hex_roundTrip_variousInputs() {
        val inputs = listOf(
            ByteArray(1),
            ByteArray(16) { it.toByte() },
            ByteArray(32) { (it * 7).toByte() },
            ByteArray(64) { ((it + 3) % 256).toByte() }
        )
        inputs.forEach { input ->
            val hex = Codec.toHex(input)
            val decoded = Codec.fromHex(hex)
            assertArrayEquals(input, decoded)
        }
    }

    @Test
    fun constantTimeEquals_equalArrays_returnsTrue() {
        val a = byteArrayOf(1, 2, 3, 4)
        val b = byteArrayOf(1, 2, 3, 4)
        assertTrue(Codec.constantTimeEquals(a, b))
    }

    @Test
    fun constantTimeEquals_differentArrays_returnsFalse() {
        val a = byteArrayOf(1, 2, 3, 4)
        val b = byteArrayOf(1, 2, 3, 5)
        assertFalse(Codec.constantTimeEquals(a, b))
    }

    @Test
    fun constantTimeEquals_differentLength_returnsFalse() {
        val a = byteArrayOf(1, 2, 3)
        val b = byteArrayOf(1, 2, 3, 4)
        assertFalse(Codec.constantTimeEquals(a, b))
    }

    @Test
    fun constantTimeEquals_emptyArrays_returnsTrue() {
        assertTrue(Codec.constantTimeEquals(ByteArray(1), ByteArray(1)))
    }

    @Test
    fun wipe_clearsArray() {
        val data = byteArrayOf(1, 2, 3, 4, 5)
        Codec.wipe(data)
        assertArrayEquals(ByteArray(5), data)
    }

    @Test
    fun wipe_emptyArray_noOp() {
        val data = ByteArray(1)
        Codec.wipe(data)
        assertEquals(1, data.size)
    }
}
