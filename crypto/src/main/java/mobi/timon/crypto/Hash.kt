package mobi.timon.crypto

/**
 * Cryptographic hash functions.
 * 
 * Provides various hash algorithms including SHA-2 family, Keccak (pre-SHA-3), 
 * Blake2b, RIPEMD-160, and MD5/SHA-1 for legacy compatibility.
 * 
 * **Security Note**: MD5 and SHA-1 are vulnerable to collision attacks. 
 * Use them only for legacy protocol compatibility, not for security-critical applications.
 */
object Hash {

    init {
        Enc
    }

    /**
     * Computes SHA-1 hash.
     * 
     * **Warning**: SHA-1 is cryptographically broken. Use only for legacy compatibility.
     * 
     * @param data Input data to hash
     * @return 20-byte SHA-1 digest
     * @throws EncException if hashing fails
     */
    external fun sha1(data: ByteArray): ByteArray

    /**
     * Computes SHA-256 hash.
     * 
     * @param data Input data to hash
     * @return 32-byte SHA-256 digest
     * @throws EncException if hashing fails
     */
    external fun sha256(data: ByteArray): ByteArray

    /**
     * Computes SHA-512 hash.
     * 
     * @param data Input data to hash
     * @return 64-byte SHA-512 digest
     * @throws EncException if hashing fails
     */
    external fun sha512(data: ByteArray): ByteArray

    /**
     * Computes Blake2b-256 hash.
     * 
     * A modern, fast hash function suitable for cryptographic use.
     * 
     * @param data Input data to hash
     * @return 32-byte Blake2b-256 digest
     * @throws EncException if hashing fails
     */
    external fun blake2b256(data: ByteArray): ByteArray

    /**
     * Computes MD5 hash.
     * 
     * **Warning**: MD5 is cryptographically broken. Use only for legacy compatibility.
     * 
     * @param data Input data to hash
     * @return 16-byte MD5 digest
     * @throws EncException if hashing fails
     */
    external fun md5(data: ByteArray): ByteArray

    /**
     * Computes RIPEMD-160 hash.
     * 
     * Commonly used in Bitcoin and Ethereum for address generation.
     * 
     * @param data Input data to hash
     * @return 20-byte RIPEMD-160 digest
     * @throws EncException if hashing fails
     */
    external fun ripemd160(data: ByteArray): ByteArray

    /**
     * Computes Keccak-256 hash.
     * 
     * This is the original Keccak (pre-NIST modification), commonly used in 
     * Ethereum and other blockchain systems.
     * 
     * @param data Input data to hash
     * @return 32-byte Keccak-256 digest
     * @throws EncException if hashing fails
     */
    external fun keccak256(data: ByteArray): ByteArray

    /**
     * Computes SHA-384 hash.
     * 
     * @param data Input data to hash
     * @return 48-byte SHA-384 digest
     * @throws EncException if hashing fails
     */
    external fun sha384(data: ByteArray): ByteArray

    /**
     * Computes Keccak-512 hash.
     * 
     * This is the original Keccak (pre-NIST modification).
     * 
     * @param data Input data to hash
     * @return 64-byte Keccak-512 digest
     * @throws EncException if hashing fails
     */
    external fun keccak512(data: ByteArray): ByteArray
}
