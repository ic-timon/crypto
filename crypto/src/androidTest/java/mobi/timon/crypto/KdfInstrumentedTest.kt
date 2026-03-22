package mobi.timon.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KdfInstrumentedTest {

    @Test
    fun bcryptHashAndVerify() {
        val password = "hunter2".toByteArray(Charsets.UTF_8)
        val hash = Kdf.bcryptHash(password, cost = 4)
        assertTrue(hash.isNotEmpty())
        assertTrue(Kdf.bcryptVerify(password, hash))
        assertFalse(Kdf.bcryptVerify("wrong".toByteArray(Charsets.UTF_8), hash))
    }

    @Test
    fun pbkdf2_deterministic() {
        val password = "pw".toByteArray(Charsets.UTF_8)
        val salt = ByteArray(8) { 1 }
        val a = Kdf.pbkdf2(password, salt, iterations = 1000, keyLen = 32)
        val b = Kdf.pbkdf2(password, salt, iterations = 1000, keyLen = 32)
        assertArrayEquals(a, b)
        assertEquals(32, a.size)
    }

    @Test
    fun hkdf_expands() {
        val ikm = Random.bytes(32)
        val salt = Random.bytes(16)
        val info = "ctx".toByteArray(Charsets.UTF_8)
        val key = Kdf.hkdf(ikm, salt, info, keyLen = 48)
        assertEquals(48, key.size)
    }

    @Test
    fun argon2id_lowParams() {
        val password = "p".toByteArray(Charsets.UTF_8)
        val salt = ByteArray(16) { 2 }
        val key = Kdf.argon2idHash(
            password,
            salt,
            timeCost = 1,
            memoryCost = 8,
            parallelism = 1,
            keyLen = 32
        )
        assertEquals(32, key.size)
    }

    @Test
    fun scrypt_derives() {
        val password = "p".toByteArray(Charsets.UTF_8)
        val salt = ByteArray(8) { 3 }
        val key = Kdf.scrypt(password, salt, keyLen = 32)
        assertEquals(32, key.size)
    }
}
