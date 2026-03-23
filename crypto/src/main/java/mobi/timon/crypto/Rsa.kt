package mobi.timon.crypto

/**
 * RSA (Rivest-Shamir-Adleman) operations.
 * 
 * Provides RSA encryption, decryption, and signatures with OAEP and PKCS#1 v1.5 padding.
 * 
 * **Wire Format**:
 * - Keys: DER-encoded (PKCS#8 for private, PKIX/SPKI for public)
 * - NOT PEM strings (no `-----BEGIN...` headers)
 */
object Rsa {

    init {
        Enc
    }

    /**
     * Generates a new RSA key pair.
     * 
     * @param bits Key size in bits (e.g., 2048, 3072, 4096)
     * @return DER-encoded private key (PKCS#8 format)
     * @throws EncException if key generation fails
     */
    external fun generateKey(bits: Int): ByteArray

    /**
     * Encrypts data using RSA-OAEP.
     * 
     * @param plaintext Data to encrypt (limited by key size and padding)
     * @param publicKey DER-encoded public key
     * @return Encrypted ciphertext
     * @throws EncException if encryption fails
     */
    external fun encrypt(plaintext: ByteArray, publicKey: ByteArray): ByteArray

    /**
     * Decrypts data using RSA-OAEP.
     * 
     * @param ciphertext Encrypted data
     * @param privateKey DER-encoded private key
     * @return Decrypted plaintext
     * @throws EncException if decryption fails
     */
    external fun decrypt(ciphertext: ByteArray, privateKey: ByteArray): ByteArray

    /**
     * Signs a message using RSASSA-PKCS1-v1_5.
     * 
     * The message is hashed internally with SHA-256 before signing.
     * 
     * @param message Message to sign
     * @param privateKey DER-encoded private key
     * @return Signature bytes
     * @throws EncException if signing fails
     */
    external fun sign(message: ByteArray, privateKey: ByteArray): ByteArray

    /**
     * Verifies an RSASSA-PKCS1-v1_5 signature.
     * 
     * @param message Original message
     * @param signature Signature to verify
     * @param publicKey DER-encoded public key
     * @return true if signature is valid, false otherwise
     * @throws EncException if verification fails
     */
    external fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean

    /**
     * Extracts the public key from a private key.
     * 
     * @param privateKey DER-encoded private key
     * @return DER-encoded public key (PKIX/SPKI format)
     * @throws EncException if extraction fails
     */
    external fun privateKeyToPublicKey(privateKey: ByteArray): ByteArray
}
