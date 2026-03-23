package mobi.timon.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BlsInstrumentedTest {

    @Test
    fun bls_generateKey_returns32Bytes() {
        val privateKey = Bls.generateKey()
        assertNotNull(privateKey)
        assertEquals(32, privateKey.size)
    }

    @Test
    fun bls_privateKeyToPublicKey_returns48Bytes() {
        val privateKey = Bls.generateKey()
        val publicKey = Bls.privateKeyToPublicKey(privateKey)
        assertNotNull(publicKey)
        assertEquals(48, publicKey.size)
    }

    @Test
    fun bls_sign_returns96Bytes() {
        val privateKey = Bls.generateKey()
        val message = "test message for BLS".toByteArray(Charsets.UTF_8)
        val signature = Bls.sign(message, privateKey)
        assertNotNull(signature)
        assertEquals(96, signature.size)
    }

    @Test
    fun bls_signAndVerify() {
        val privateKey = Bls.generateKey()
        val publicKey = Bls.privateKeyToPublicKey(privateKey)
        val message = "test message for BLS signature".toByteArray(Charsets.UTF_8)

        val signature = Bls.sign(message, privateKey)
        val verified = Bls.verify(message, signature, publicKey)
        assertTrue(verified)
    }

    @Test
    fun bls_aggregateSignatures() {
        val privateKey1 = Bls.generateKey()
        val privateKey2 = Bls.generateKey()
        val message = "same message for aggregation".toByteArray(Charsets.UTF_8)

        val sig1 = Bls.sign(message, privateKey1)
        val sig2 = Bls.sign(message, privateKey2)

        val concatenated = sig1 + sig2
        val aggSig = Bls.aggregateSignatures(concatenated, 2)

        assertNotNull(aggSig)
        assertEquals(96, aggSig.size)
    }

    @Test
    fun bls_aggregatePublicKeys() {
        val privateKey1 = Bls.generateKey()
        val privateKey2 = Bls.generateKey()

        val pubKey1 = Bls.privateKeyToPublicKey(privateKey1)
        val pubKey2 = Bls.privateKeyToPublicKey(privateKey2)

        val concatenated = pubKey1 + pubKey2
        val aggPubKey = Bls.aggregatePublicKeys(concatenated, 2)

        assertNotNull(aggPubKey)
        assertEquals(48, aggPubKey.size)
    }

    @Test
    fun bls_verifyAggregatedSignature() {
        val privateKey1 = Bls.generateKey()
        val privateKey2 = Bls.generateKey()
        val message = "aggregated message test".toByteArray(Charsets.UTF_8)

        val pubKey1 = Bls.privateKeyToPublicKey(privateKey1)
        val pubKey2 = Bls.privateKeyToPublicKey(privateKey2)

        val sig1 = Bls.sign(message, privateKey1)
        val sig2 = Bls.sign(message, privateKey2)

        val aggSig = Bls.aggregateSignatures(sig1 + sig2, 2)
        val aggPubKey = Bls.aggregatePublicKeys(pubKey1 + pubKey2, 2)

        val verified = Bls.verify(message, aggSig, aggPubKey)
        assertTrue(verified)
    }
}
