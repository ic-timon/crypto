package mobi.timon.crypto

object Secp256k1 {

    init {
        Enc
    }

    external fun generateKey(): ByteArray

    external fun privateKeyToPublicKey(privateKey: ByteArray, compressed: Boolean): ByteArray

    external fun sign(message: ByteArray, privateKey: ByteArray): ByteArray

    external fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean

    external fun recoverPublicKey(message: ByteArray, signature: ByteArray, compressed: Boolean): ByteArray

    external fun schnorrSign(message: ByteArray, privateKey: ByteArray): ByteArray

    external fun schnorrVerify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean

    external fun schnorrPrivateKeyToPublicKey(privateKey: ByteArray): ByteArray
}
