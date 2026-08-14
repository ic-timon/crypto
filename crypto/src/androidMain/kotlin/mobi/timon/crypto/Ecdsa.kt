package mobi.timon.crypto
actual object Ecdsa {
    init { Enc }
    actual external fun generateKey(curveBits: Int): ByteArray
    actual external fun sign(message: ByteArray, privateKey: ByteArray): ByteArray
    actual external fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
    actual external fun privateKeyToPublicKey(privateKey: ByteArray): ByteArray
}
