package mobi.timon.crypto

/**
 * Ed25519 (EdDSA over Curve25519) operations.
 * 
 * Ed25519 provides fast, secure digital signatures using the Edwards-curve 
 * Digital Signature Algorithm. It's widely used in modern cryptographic 
 * systems due to its speed and security properties.
 * 
 * **Wire Format**:
 * - Key pair (generateKey): 96 bytes = public key (32) + private key (64)
 * - Signature: 64 bytes
 * - Public key: 32 bytes
 */
object Ed25519 {

    init {
        Enc
    }

    /**
     * Generates a new Ed25519 key pair.
     * 
     * @return 96-byte array: public key (32 bytes) + private key (64 bytes)
     * @throws EncException if key generation fails
     */
    external fun generateKey(): ByteArray

    /**
     * Signs a message using Ed25519.
     * 
     * @param message Message to sign
     * @param privateKey 64-byte private key
     * @return 64-byte signature
     * @throws EncException if signing fails
     */
    external fun sign(message: ByteArray, privateKey: ByteArray): ByteArray

    /**
     * Verifies an Ed25519 signature.
     * 
     * @param message Original message
     * @param signature 64-byte signature to verify
     * @param publicKey 32-byte public key
     * @return true if signature is valid, false otherwise
     * @throws EncException if verification fails
     */
    external fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean
}
