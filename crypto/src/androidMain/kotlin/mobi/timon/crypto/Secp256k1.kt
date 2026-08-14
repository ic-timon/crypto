package mobi.timon.crypto
actual object Secp256k1 {
    init { Enc }
    actual external fun generateKey(): ByteArray
    actual external fun privateKeyToPublicKey(privateKey: ByteArray, compressed: Boolean): ByteArray
    actual external fun sign(message: ByteArray, privateKey: ByteArray): ByteArray
    actual external fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
    actual external fun recoverPublicKey(message: ByteArray, signature: ByteArray, compressed: Boolean): ByteArray
    actual external fun schnorrSign(message: ByteArray, privateKey: ByteArray): ByteArray
    actual external fun schnorrVerify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
    actual external fun schnorrPrivateKeyToPublicKey(privateKey: ByteArray): ByteArray
    actual external fun schnorrSignHash(hash: ByteArray, privateKey: ByteArray): ByteArray
    actual external fun schnorrVerifyHash(hash: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
}
