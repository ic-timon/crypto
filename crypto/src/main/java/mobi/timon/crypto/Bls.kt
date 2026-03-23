package mobi.timon.crypto

/**
 * BLS12-381 signature operations.
 * 
 * BLS (Boneh-Lynn-Shacham) signatures allow for signature aggregation, 
 * enabling multiple signatures to be combined into a single short signature.
 * Uses the BLS12-381 pairing-friendly curve.
 * 
 * **Wire Format**:
 * - Private key: 32 bytes
 * - Public key: 48 bytes (G1 compressed)
 * - Signature: 96 bytes (G2 compressed)
 * - Aggregated signature: 96 bytes
 * - Aggregated public key: 48 bytes
 */
object Bls {

    init {
        Enc
    }

    /**
     * Generates a new BLS private key.
     * 
     * @return 32-byte private key
     * @throws EncException if key generation fails
     */
    external fun generateKey(): ByteArray

    /**
     * Derives the public key from a private key.
     * 
     * @param privateKey 32-byte private key
     * @return 48-byte compressed public key (G1)
     * @throws EncException if derivation fails
     */
    external fun privateKeyToPublicKey(privateKey: ByteArray): ByteArray

    /**
     * Signs a message using BLS.
     * 
     * @param message Message to sign
     * @param privateKey 32-byte private key
     * @return 96-byte compressed signature (G2)
     * @throws EncException if signing fails
     */
    external fun sign(message: ByteArray, privateKey: ByteArray): ByteArray

    /**
     * Verifies a BLS signature.
     * 
     * @param message Original message
     * @param signature 96-byte signature
     * @param publicKey 48-byte public key
     * @return true if signature is valid, false otherwise
     * @throws EncException if verification fails
     */
    external fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean

    /**
     * Aggregates multiple BLS signatures into one.
     * 
     * @param signatures Concatenated signatures (count * 96 bytes)
     * @param count Number of signatures
     * @return 96-byte aggregated signature
     * @throws EncException if aggregation fails
     */
    external fun aggregateSignatures(signatures: ByteArray, count: Int): ByteArray

    /**
     * Aggregates multiple BLS public keys into one.
     * 
     * @param publicKeys Concatenated public keys (count * 48 bytes)
     * @param count Number of public keys
     * @return 48-byte aggregated public key
     * @throws EncException if aggregation fails
     */
    external fun aggregatePublicKeys(publicKeys: ByteArray, count: Int): ByteArray
}
