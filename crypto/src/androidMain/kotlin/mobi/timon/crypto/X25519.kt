package mobi.timon.crypto

actual object X25519 {
    init { Enc }
    actual external fun publicKey(privateKey: ByteArray): ByteArray
    actual external fun sharedSecret(privateKey: ByteArray, peerPublic: ByteArray): ByteArray
}
