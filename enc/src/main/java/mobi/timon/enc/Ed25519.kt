package mobi.timon.enc

object Ed25519 {

    init {
        Enc
    }

    external fun generateKey(): ByteArray

    external fun sign(message: ByteArray, privateKey: ByteArray): ByteArray

    external fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
}
