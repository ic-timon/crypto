package mobi.timon.crypto
expect object Kdf {
    fun bcryptHash(password: ByteArray, cost: Int): ByteArray
    fun bcryptVerify(password: ByteArray, hash: ByteArray): Boolean
    fun argon2idHash(password: ByteArray, salt: ByteArray, timeCost: Int, memoryCost: Int, parallelism: Int, keyLen: Int): ByteArray
    fun scrypt(password: ByteArray, salt: ByteArray, keyLen: Int): ByteArray
    fun pbkdf2(password: ByteArray, salt: ByteArray, iterations: Int, keyLen: Int): ByteArray
    fun hkdf(ikm: ByteArray, salt: ByteArray, info: ByteArray, keyLen: Int): ByteArray
}
