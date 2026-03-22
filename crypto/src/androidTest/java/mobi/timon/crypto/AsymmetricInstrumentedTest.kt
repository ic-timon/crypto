package mobi.timon.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AsymmetricInstrumentedTest {

    @Test
    fun ed25519_signAndVerify() {
        val kp = Ed25519.generateKey()
        assertEquals(96, kp.size)
        val pub = kp.copyOfRange(0, 32)
        val priv = kp.copyOfRange(32, 96)
        val msg = "msg".toByteArray(Charsets.UTF_8)
        val sig = Ed25519.sign(msg, priv)
        assertEquals(64, sig.size)
        assertTrue(Ed25519.verify(msg, sig, pub))
    }

    @Test
    fun ecdsa_p256_signAndVerify() {
        val sk = Ecdsa.generateKey(256)
        assertNotNull(sk)
        val msg = "ecdsa-msg".toByteArray(Charsets.UTF_8)
        val sig = Ecdsa.sign(msg, sk)
        val pk = Ecdsa.privateKeyToPublicKey(sk)
        assertTrue(Ecdsa.verify(msg, sig, pk))
    }

    @Test(timeout = 240_000)
    fun rsa2048_encryptAndSign() {
        val privDer = Rsa.generateKey(2048)
        val pubDer = Rsa.privateKeyToPublicKey(privDer)
        val plain = "rsa-plain".toByteArray(Charsets.UTF_8)
        val ct = Rsa.encrypt(plain, pubDer)
        assertArrayEquals(plain, Rsa.decrypt(ct, privDer))

        val msg = "sign-me".toByteArray(Charsets.UTF_8)
        val sig = Rsa.sign(msg, privDer)
        assertTrue(Rsa.verify(msg, sig, pubDer))
    }
}
