package mobi.timon.crypto
actual object Kdf {
    init { Enc }
    actual external fun bcryptHash(password: ByteArray, cost: Int): ByteArray
    actual external fun bcryptVerify(password: ByteArray, hash: ByteArray): Boolean
    actual external fun argon2idHash(password: ByteArray, salt: ByteArray, timeCost: Int, memoryCost: Int, parallelism: Int, keyLen: Int): ByteArray
    actual external fun scrypt(password: ByteArray, salt: ByteArray, keyLen: Int): ByteArray
    actual external fun pbkdf2(password: ByteArray, salt: ByteArray, iterations: Int, keyLen: Int): ByteArray
    actual external fun hkdf(ikm: ByteArray, salt: ByteArray, info: ByteArray, keyLen: Int): ByteArray
}
