package mobi.timon.crypto

/** RFC 7748. All keys are 32 bytes; low-order peer keys are rejected. */
expect object X25519 {
    fun publicKey(privateKey: ByteArray): ByteArray
    fun sharedSecret(privateKey: ByteArray, peerPublic: ByteArray): ByteArray
}
