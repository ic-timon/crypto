package mobi.timon.crypto

/**
 * Key Derivation Functions (KDFs).
 * 
 * Provides various password-based key derivation functions for securely
 * deriving cryptographic keys from passwords.
 * 
 * **Security Recommendations**:
 * - bcrypt: Use cost >= 10 for production, >= 4 for development
 * - Argon2id: Recommended for new applications
 * - scrypt: Good balance of security and performance
 * - PBKDF2: Use at least 100,000 iterations for modern applications
 */
object Kdf {

    init {
        Enc
    }

    /**
     * Derives a key from a password using bcrypt.
     * 
     * @param password Password bytes
     * @param cost Work factor (4-31, exponential). Higher = slower.
     * @return bcrypt hash (60 bytes, modular crypt format)
     * @throws EncException if hashing fails
     */
    external fun bcryptHash(password: ByteArray, cost: Int): ByteArray

    /**
     * Verifies a password against a bcrypt hash.
     * 
     * Uses constant-time comparison internally.
     * 
     * @param password Password to verify
     * @param hash bcrypt hash from [bcryptHash]
     * @return true if password matches, false otherwise
     * @throws EncException if verification fails
     */
    external fun bcryptVerify(password: ByteArray, hash: ByteArray): Boolean

    /**
     * Derives a key using Argon2id.
     * 
     * Argon2id is the winner of the Argon2 Password Hashing Competition
     * and recommended for new applications.
     * 
     * @param password Password bytes
     * @param salt Salt bytes (16 bytes recommended)
     * @param timeCost Number of iterations (1-10)
     * @param memoryCost Memory cost in KiB (8-65536)
     * @param parallelism Number of parallel threads (1-8)
     * @param keyLen Desired output key length in bytes
     * @return Derived key
     * @throws EncException if derivation fails
     */
    external fun argon2idHash(
        password: ByteArray,
        salt: ByteArray,
        timeCost: Int,
        memoryCost: Int,
        parallelism: Int,
        keyLen: Int
    ): ByteArray

    /**
     * Derives a key using scrypt.
     * 
     * @param password Password bytes
     * @param salt Salt bytes
     * @param keyLen Desired output key length in bytes
     * @return Derived key
     * @throws EncException if derivation fails
     */
    external fun scrypt(password: ByteArray, salt: ByteArray, keyLen: Int): ByteArray

    /**
     * Derives a key using PBKDF2-HMAC-SHA256.
     * 
     * @param password Password bytes
     * @param salt Salt bytes
     * @param iterations Number of iterations (min 1,000 for production)
     * @param keyLen Desired output key length in bytes
     * @return Derived key
     * @throws EncException if derivation fails
     */
    external fun pbkdf2(
        password: ByteArray,
        salt: ByteArray,
        iterations: Int,
        keyLen: Int
    ): ByteArray

    /**
     * Derives a key using HKDF (HMAC-based KDF).
     * 
     * @param ikm Input key material
     * @param salt Optional salt (can be empty)
     * @param info Optional context info (can be empty)
     * @param keyLen Desired output key length in bytes
     * @return Derived key
     * @throws EncException if derivation fails
     */
    external fun hkdf(ikm: ByteArray, salt: ByteArray, info: ByteArray, keyLen: Int): ByteArray
}
