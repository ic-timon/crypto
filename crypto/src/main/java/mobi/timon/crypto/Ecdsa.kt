package mobi.timon.crypto

/**
 * ECDSA (Elliptic Curve Digital Signature Algorithm) operations.
 * 
 * Supports NIST curves P-224, P-256, P-384, and P-521.
 * 
 * **Wire Format**:
 * - Private key: DER-encoded PKCS#8
 * - Public key: DER-encoded PKIX/SPKI
 * - Signature: DER-encoded ASN.1 SEQUENCE (r, s)
 */
object Ecdsa {

    init {
        Enc
    }

    /**
     * Generates a new ECDSA key pair.
     * 
     * @param curveBits Curve size in bits (224, 256, 384, or 521)
     * @return DER-encoded private key (PKCS#8 format)
     * @throws EncException if key generation fails
     */
    external fun generateKey(curveBits: Int): ByteArray

    /**
     * Signs a message using ECDSA.
     * 
     * @param message Message to sign
     * @param privateKey DER-encoded private key
     * @return DER-encoded signature
     * @throws EncException if signing fails
     */
    external fun sign(message: ByteArray, privateKey: ByteArray): ByteArray

    /**
     * Verifies an ECDSA signature.
     * 
     * @param message Original message
     * @param signature DER-encoded signature to verify
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
