package mobi.timon.crypto

object Bls {

    init {
        Enc
    }

    external fun generateKey(): ByteArray

    external fun privateKeyToPublicKey(privateKey: ByteArray): ByteArray

    external fun sign(message: ByteArray, privateKey: ByteArray): ByteArray

    external fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean

    external fun aggregateSignatures(signatures: ByteArray, count: Int): ByteArray

    external fun aggregatePublicKeys(publicKeys: ByteArray, count: Int): ByteArray
}
