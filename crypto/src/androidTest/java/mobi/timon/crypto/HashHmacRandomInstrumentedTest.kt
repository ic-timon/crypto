package mobi.timon.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HashHmacRandomInstrumentedTest {

    @Test
    fun sha256_empty_matchesKnownVector() {
        val expected = Codec.fromHex(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        )
        assertArrayEquals(expected, Hash.sha256(ByteArray(0)))
    }

    @Test
    fun sha256_abc_matchesKnownVector() {
        val expected = Codec.fromHex(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
        )
        assertArrayEquals(expected, Hash.sha256("abc".toByteArray(Charsets.UTF_8)))
    }

    @Test
    fun sha1_sha512_outputLengths() {
        assertEquals(20, Hash.sha1(byteArrayOf(1)).size)
        assertEquals(32, Hash.sha256(byteArrayOf(1)).size)
        assertEquals(64, Hash.sha512(byteArrayOf(1)).size)
    }

    @Test
    fun hmacSha256_rfc4231_case1() {
        val key = ByteArray(20) { 0x0b.toByte() }
        val data = "Hi There".toByteArray(Charsets.UTF_8)
        val expected = Codec.fromHex(
            "b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9376c2e32cff7"
        )
        assertArrayEquals(expected, Hmac.hmacSha256(data, key))
    }

    @Test
    fun hmacSha512_rfc4231_case1() {
        val key = ByteArray(20) { 0x0b.toByte() }
        val data = "Hi There".toByteArray(Charsets.UTF_8)
        val expected = Codec.fromHex(
            "87aa7cdea5ef619d4ff0b4241a1d6cb02379f4e2ce4ec2787ad0b30545e17cdedaa833b7d6b8a702038b274eaea3f4e4be9d914eeb61f1702e696c203a126854"
        )
        assertArrayEquals(expected, Hmac.hmacSha512(data, key))
    }

    @Test
    fun blake2b256_empty_matchesKnownVector() {
        val expected = Codec.fromHex(
            "0e5751c026e543b2e8ab2eb06099daa1d1e5df47778f7787faab45cdf12fe3a8"
        )
        assertArrayEquals(expected, Hash.blake2b256(ByteArray(0)))
    }

    @Test
    fun md5_empty_matchesKnownVector() {
        val expected = Codec.fromHex("d41d8cd98f00b204e9800998ecf8427e")
        assertArrayEquals(expected, Hash.md5(ByteArray(0)))
    }

    @Test
    fun randomBytes_lengthAndNonTrivial() {
        val a = Random.bytes(32)
        val b = Random.bytes(32)
        assertEquals(32, a.size)
        assertEquals(32, b.size)
        assertNotEquals(a.toList(), b.toList())
        assertTrue(a.any { it != 0.toByte() })
    }

    @Test
    fun randomInt_samples_notAllIdentical() {
        val samples = List(16) { Random.int() }
        assertTrue(
            samples.distinct().size >= 2,
            "CSPRNG samples should not all be identical",
        )
    }

    @Test
    fun randomLong_range_halfOpen() {
        repeat(20) {
            val v = Random.long(0L, 100L)
            assertTrue(v >= 0L)
            assertTrue(v < 100L)
        }
    }

    @Test
    fun randomLong_invalidRange_throws() {
        assertThrows(EncException::class.java) {
            Random.long(10L, 10L)
        }
        assertThrows(EncException::class.java) {
            Random.long(10L, 5L)
        }
    }
}
