package mobi.timon.enc

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SymmetricCipherInstrumentedTest {

    @Test
    fun aesGcm_roundTrip() {
        val key = Random.bytes(32)
        val plain = "plain-gcm".toByteArray(Charsets.UTF_8)
        val ct = Aead.aesGcmEncrypt(plain, key)
        assertArrayEquals(plain, Aead.aesGcmDecrypt(ct, key))
    }

    @Test
    fun aesGcm_wrongKey_fails() {
        val key = Random.bytes(32)
        val wrong = Random.bytes(32)
        val ct = Aead.aesGcmEncrypt("x".toByteArray(), key)
        try {
            Aead.aesGcmDecrypt(ct, wrong)
            throw AssertionError("expected EncException")
        } catch (e: EncException) {
            // ok
        }
    }

    @Test
    fun chacha20Poly1305_roundTrip() {
        val key = Random.bytes(32)
        val plain = "plain-c20p".toByteArray(Charsets.UTF_8)
        val ct = Aead.chacha20Poly1305Encrypt(plain, key)
        assertArrayEquals(plain, Aead.chacha20Poly1305Decrypt(ct, key))
    }

    @Test
    fun aesCbc_roundTrip() {
        val key = Random.bytes(32)
        val plain = "cbc-block".toByteArray(Charsets.UTF_8)
        val ct = Cbc.aesCbcEncrypt(plain, key)
        assertArrayEquals(plain, Cbc.aesCbcDecrypt(ct, key))
    }

    @Test
    fun desCbc_roundTrip() {
        val key = Random.bytes(8)
        val plain = "des8".toByteArray(Charsets.UTF_8)
        val ct = Cbc.desCbcEncrypt(plain, key)
        assertArrayEquals(plain, Cbc.desCbcDecrypt(ct, key))
    }

    @Test
    fun desCbc_wrongKeyLength_throws() {
        val shortKey = ByteArray(7) { 1 }
        assertThrows(EncException::class.java) {
            Cbc.desCbcEncrypt(byteArrayOf(1), shortKey)
        }
    }

    @Test
    fun aesCtr_roundTrip() {
        val key = Random.bytes(32)
        val plain = "ctr-stream".toByteArray(Charsets.UTF_8)
        val ct = Stream.aesCtrEncrypt(plain, key)
        assertArrayEquals(plain, Stream.aesCtrDecrypt(ct, key))
    }

    @Test
    fun chacha20_roundTrip() {
        val key = Random.bytes(32)
        val plain = "chacha".toByteArray(Charsets.UTF_8)
        val ct = Stream.chacha20Encrypt(plain, key)
        assertArrayEquals(plain, Stream.chacha20Decrypt(ct, key))
    }

    @Test
    fun aesXts_roundTrip() {
        val key = Random.bytes(32)
        val plain = ByteArray(16) { it.toByte() }
        val ct = Xts.aesXtsEncrypt(plain, key, 0L)
        assertArrayEquals(plain, Xts.aesXtsDecrypt(ct, key, 0L))
    }

    @Test
    fun aesXts_sectorNum1_roundTrip() {
        val key = Random.bytes(32)
        val plain = ByteArray(32) { (it + 3).toByte() }
        val ct = Xts.aesXtsEncrypt(plain, key, 1L)
        assertArrayEquals(plain, Xts.aesXtsDecrypt(ct, key, 1L))
    }
}
