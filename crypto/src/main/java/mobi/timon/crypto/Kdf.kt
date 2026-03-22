package mobi.timon.crypto

object Kdf {

    init {
        Enc
    }

    external fun bcryptHash(password: ByteArray, cost: Int): ByteArray

    external fun bcryptVerify(password: ByteArray, hash: ByteArray): Boolean

    external fun argon2idHash(
        password: ByteArray,
        salt: ByteArray,
        timeCost: Int,
        memoryCost: Int,
        parallelism: Int,
        keyLen: Int
    ): ByteArray

    external fun scrypt(password: ByteArray, salt: ByteArray, keyLen: Int): ByteArray

    external fun pbkdf2(
        password: ByteArray,
        salt: ByteArray,
        iterations: Int,
        keyLen: Int
    ): ByteArray

    external fun hkdf(ikm: ByteArray, salt: ByteArray, info: ByteArray, keyLen: Int): ByteArray
}
