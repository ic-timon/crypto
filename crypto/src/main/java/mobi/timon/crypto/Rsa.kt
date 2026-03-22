package mobi.timon.crypto

object Rsa {

    init {
        Enc
    }

    external fun generateKey(bits: Int): ByteArray

    external fun encrypt(plaintext: ByteArray, publicKey: ByteArray): ByteArray

    external fun decrypt(ciphertext: ByteArray, privateKey: ByteArray): ByteArray

    external fun sign(message: ByteArray, privateKey: ByteArray): ByteArray

    external fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean

    external fun privateKeyToPublicKey(privateKey: ByteArray): ByteArray
}
