package mobi.timon.crypto
actual object Ed25519 {
    init { Enc }
    actual external fun generateKey(): ByteArray
    actual external fun sign(message: ByteArray, privateKey: ByteArray): ByteArray
    actual external fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
}
