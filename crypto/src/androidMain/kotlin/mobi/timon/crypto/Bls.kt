package mobi.timon.crypto
actual object Bls {
    init { Enc }
    actual external fun generateKey(): ByteArray
    actual external fun privateKeyToPublicKey(privateKey: ByteArray): ByteArray
    actual external fun sign(message: ByteArray, privateKey: ByteArray): ByteArray
    actual external fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
    actual external fun aggregateSignatures(signatures: ByteArray, count: Int): ByteArray
    actual external fun aggregatePublicKeys(publicKeys: ByteArray, count: Int): ByteArray
}
