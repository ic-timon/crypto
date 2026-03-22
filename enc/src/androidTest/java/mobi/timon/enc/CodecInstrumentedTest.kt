package mobi.timon.enc

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CodecInstrumentedTest {

    @Test
    fun toHex_fromHex_roundTrip() {
        val raw = byteArrayOf(0x00, 0x0f, 0xff.toByte(), 0x10)
        val hex = Codec.toHex(raw)
        assertEquals("000fff10", hex)
        assertArrayEquals(raw, Codec.fromHex(hex))
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
    fun base64_roundTrip() {
        val raw = "hello enc".toByteArray(Charsets.UTF_8)
        val b64 = Codec.toBase64(raw)
        assertArrayEquals(raw, Codec.fromBase64(b64))
    }
}
