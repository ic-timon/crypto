package mobi.timon.crypto
expect object Secp256k1 {
    fun generateKey(): ByteArray
    fun privateKeyToPublicKey(privateKey: ByteArray, compressed: Boolean): ByteArray
    fun sign(message: ByteArray, privateKey: ByteArray): ByteArray
    fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
    fun recoverPublicKey(message: ByteArray, signature: ByteArray, compressed: Boolean): ByteArray
    fun schnorrSign(message: ByteArray, privateKey: ByteArray): ByteArray
    fun schnorrVerify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
    fun schnorrPrivateKeyToPublicKey(privateKey: ByteArray): ByteArray
    fun schnorrSignHash(hash: ByteArray, privateKey: ByteArray): ByteArray
    fun schnorrVerifyHash(hash: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
}
