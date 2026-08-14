package mobi.timon.crypto
expect object Bls {
    fun generateKey(): ByteArray
    fun privateKeyToPublicKey(privateKey: ByteArray): ByteArray
    fun sign(message: ByteArray, privateKey: ByteArray): ByteArray
    fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
    fun aggregateSignatures(signatures: ByteArray, count: Int): ByteArray
    fun aggregatePublicKeys(publicKeys: ByteArray, count: Int): ByteArray
}
