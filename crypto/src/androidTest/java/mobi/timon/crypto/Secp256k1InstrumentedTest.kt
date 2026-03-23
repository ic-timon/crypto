package mobi.timon.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Secp256k1InstrumentedTest {

    @Test
    fun secp256k1_generateKey_returns32Bytes() {
        val privateKey = Secp256k1.generateKey()
        assertNotNull(privateKey)
        assertEquals(32, privateKey.size)
    }

    @Test
    fun secp256k1_privateKeyToPublicKey_compressed_returns33Bytes() {
        val privateKey = Secp256k1.generateKey()
        val publicKey = Secp256k1.privateKeyToPublicKey(privateKey, true)
        assertNotNull(publicKey)
        assertEquals(33, publicKey.size)
    }

    @Test
    fun secp256k1_privateKeyToPublicKey_uncompressed_returns65Bytes() {
        val privateKey = Secp256k1.generateKey()
        val publicKey = Secp256k1.privateKeyToPublicKey(privateKey, false)
        assertNotNull(publicKey)
        assertEquals(65, publicKey.size)
    }

    @Test
    fun secp256k1_signAndVerify() {
        val privateKey = Secp256k1.generateKey()
        val publicKey = Secp256k1.privateKeyToPublicKey(privateKey, true)
        val message = "test message for secp256k1".toByteArray(Charsets.UTF_8)

        val signature = Secp256k1.sign(message, privateKey)
        assertNotNull(signature)
        assertEquals(65, signature.size)

        val verified = Secp256k1.verify(message, signature, publicKey)
        assertTrue(verified)
    }

    @Test
    fun secp256k1_recoverPublicKey() {
        val privateKey = Secp256k1.generateKey()
        val expectedPublicKey = Secp256k1.privateKeyToPublicKey(privateKey, true)
        val message = "test message for recovery".toByteArray(Charsets.UTF_8)

        val signature = Secp256k1.sign(message, privateKey)
        val recoveredPublicKey = Secp256k1.recoverPublicKey(message, signature, true)

        assertNotNull(recoveredPublicKey)
        assertArrayEquals(expectedPublicKey, recoveredPublicKey)
    }

    @Test
    fun schnorr_signAndVerify() {
        val privateKey = Secp256k1.generateKey()
        val publicKey = Secp256k1.schnorrPrivateKeyToPublicKey(privateKey)
        val message = "test message for schnorr".toByteArray(Charsets.UTF_8)

        val signature = Secp256k1.schnorrSign(message, privateKey)
        assertNotNull(signature)
        assertEquals(64, signature.size)

        val verified = Secp256k1.schnorrVerify(message, signature, publicKey)
        assertTrue(verified)
    }

    @Test
    fun schnorr_publicKey_is32Bytes() {
        val privateKey = Secp256k1.generateKey()
        val publicKey = Secp256k1.schnorrPrivateKeyToPublicKey(privateKey)
        assertEquals(32, publicKey.size)
    }

    @Test
    fun keccak256_knownVector() {
        val input = "".toByteArray(Charsets.UTF_8)
        val hash = Hash.keccak256(input)
        val expected = Codec.fromHex("c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470")
        assertArrayEquals(expected, hash)
    }

    @Test
    fun keccak256_helloWorld() {
        val input = "hello world".toByteArray(Charsets.UTF_8)
        val hash = Hash.keccak256(input)
        val expected = Codec.fromHex("47173285a8d7341e5e972fc677286384f802f8ef42a5ec5f03bbfa254cb01fad")
        assertArrayEquals(expected, hash)
    }

    @Test
    fun keccak512_knownVector() {
        val input = "".toByteArray(Charsets.UTF_8)
        val hash = Hash.keccak512(input)
        val expected = Codec.fromHex(
            "0eab42de4c3ceb9235fc91acffe746b29c29a8c366b7c60e4e67c466f36a4304" +
                "c00fa9caf9d87976ba469bcbe06713b435f091ef2769fb160cdab33d3670680e",
        )
        assertArrayEquals(expected, hash)
    }

    @Test
    fun ripemd160_knownVector() {
        val input = "".toByteArray(Charsets.UTF_8)
        val hash = Hash.ripemd160(input)
        val expected = Codec.fromHex("9c1185a5c5e9fc54612808977ee8f548b2258d31")
        assertArrayEquals(expected, hash)
    }

    @Test
    fun ripemd160_abc() {
        val input = "abc".toByteArray(Charsets.UTF_8)
        val hash = Hash.ripemd160(input)
        val expected = Codec.fromHex("8eb208f7e05d987a9b044a8e98c6b087f15a0bfc")
        assertArrayEquals(expected, hash)
    }
}
