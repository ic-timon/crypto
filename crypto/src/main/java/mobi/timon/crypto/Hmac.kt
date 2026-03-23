package mobi.timon.crypto

/**
 * HMAC (Hash-based Message Authentication Code) functions.
 * 
 * Provides message authentication using cryptographic hash functions.
 * HMAC combines a secret key with a hash function to produce a MAC
 * that can verify both data integrity and authenticity.
 */
object Hmac {

    init {
        Enc
    }

    /**
     * Computes HMAC-SHA256.
     * 
     * @param data Input data to authenticate
     * @param key Secret key for authentication
     * @return 32-byte HMAC-SHA256 output
     * @throws EncException if computation fails
     */
    external fun hmacSha256(data: ByteArray, key: ByteArray): ByteArray

    /**
     * Computes HMAC-SHA512.
     * 
     * @param data Input data to authenticate
     * @param key Secret key for authentication
     * @return 64-byte HMAC-SHA512 output
     * @throws EncException if computation fails
     */
    external fun hmacSha512(data: ByteArray, key: ByteArray): ByteArray

    /**
     * Computes HMAC-SHA1.
     * 
     * **Warning**: SHA-1 is cryptographically broken. Use only for legacy compatibility.
     * 
     * @param data Input data to authenticate
     * @param key Secret key for authentication
     * @return 20-byte HMAC-SHA1 output
     * @throws EncException if computation fails
     */
    external fun hmacSha1(data: ByteArray, key: ByteArray): ByteArray
}
