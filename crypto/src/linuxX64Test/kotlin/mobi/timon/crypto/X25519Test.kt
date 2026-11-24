package mobi.timon.crypto

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

class X25519Test {
    @Test
    fun rfc7748AndInvalidInputs() {
        val alice = "77076d0a7318a57d3c16c17251b26645df4c2f87ebc0992ab177fba51db92c2a".hexToByteArray()
        val bobPublic = "de9edb7d7b7dc1b4d35b61c2ece435373f8343c85b78674dadfc7e146f882b4f".hexToByteArray()
        assertContentEquals("8520f0098930a754748b7ddcb43ef75a0dbf3a0d26381af4eba4a98eaa9b4e6a".hexToByteArray(), X25519.publicKey(alice))
        assertContentEquals("4a5d9d5ba4ce2de1728e3bf480350f25e07e21c947d19e3376f09b3c1e161742".hexToByteArray(), X25519.sharedSecret(alice, bobPublic))
        assertFailsWith<IllegalArgumentException> { X25519.sharedSecret(alice, ByteArray(32)) }
        assertFailsWith<IllegalArgumentException> { X25519.publicKey(ByteArray(31)) }
    }
}
