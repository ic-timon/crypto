package mobi.timon.enc

object Ecdsa {

    init {
        Enc
    }

    external fun generateKey(curveBits: Int): ByteArray

    external fun sign(message: ByteArray, privateKey: ByteArray): ByteArray

    external fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean

    external fun privateKeyToPublicKey(privateKey: ByteArray): ByteArray
}
