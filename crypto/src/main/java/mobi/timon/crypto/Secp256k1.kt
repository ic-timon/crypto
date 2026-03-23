package mobi.timon.crypto

/**
 * secp256k1 elliptic curve operations.
 * 
 * secp256k1 is the curve used in Bitcoin, Ethereum, and many other blockchain systems.
 * Provides both ECDSA and Schnorr signature support.
 * 
 * **Wire Format**:
 * - Private key: 32 bytes (raw)
 * - Public key (compressed): 33 bytes, (uncompressed): 65 bytes
 * - ECDSA signature: 65 bytes = r (32) + s (32) + recoveryId (1)
 * - Schnorr signature: 64 bytes = r (32) + s (32)
 * - Schnorr public key: 32 bytes (x-only)
 */
object Secp256k1 {

    init {
        Enc
    }

    /**
     * Generates a new secp256k1 private key.
     * 
     * @return 32-byte private key
     * @throws EncException if key generation fails
     */
    external fun generateKey(): ByteArray

    /**
     * Derives the public key from a private key.
     * 
     * @param privateKey 32-byte private key
     * @param compressed If true, returns 33-byte compressed key; otherwise 65-byte uncompressed
     * @return Public key (33 or 65 bytes depending on compression)
     * @throws EncException if derivation fails
     */
    external fun privateKeyToPublicKey(privateKey: ByteArray, compressed: Boolean): ByteArray

    /**
     * Signs a message using ECDSA.
     * 
     * @param message Message to sign (typically a hash)
     * @param privateKey 32-byte private key
     * @return 65-byte signature = r (32) + s (32) + recoveryId (1)
     * @throws EncException if signing fails
     */
    external fun sign(message: ByteArray, privateKey: ByteArray): ByteArray

    /**
     * Verifies an ECDSA signature.
     * 
     * @param message Original message
     * @param signature 65-byte signature
     * @param publicKey 33 or 65-byte public key
     * @return true if signature is valid, false otherwise
     * @throws EncException if verification fails
     */
    external fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean

    /**
     * Recovers the public key from a signature.
     * 
     * @param message Original message
     * @param signature 65-byte signature (with recovery id)
     * @param compressed If true, returns 33-byte compressed key; otherwise 65-byte uncompressed
     * @return Recovered public key
     * @throws EncException if recovery fails
     */
    external fun recoverPublicKey(message: ByteArray, signature: ByteArray, compressed: Boolean): ByteArray

    /**
     * Signs a message using Schnorr (BIP-340).
     * 
     * @param message Message to sign (typically a 32-byte hash)
     * @param privateKey 32-byte private key
     * @return 64-byte signature = r (32) + s (32)
     * @throws EncException if signing fails
     */
    external fun schnorrSign(message: ByteArray, privateKey: ByteArray): ByteArray

    /**
     * Verifies a Schnorr signature (BIP-340).
     * 
     * @param message Original message
     * @param signature 64-byte signature
     * @param publicKey 32-byte x-only public key
     * @return true if signature is valid, false otherwise
     * @throws EncException if verification fails
     */
    external fun schnorrVerify(message: ByteArray, signature: ByteArray, publicKey: ByteArray): Boolean

    /**
     * Derives the x-only public key from a private key for Schnorr.
     * 
     * @param privateKey 32-byte private key
     * @return 32-byte x-only public key
     * @throws EncException if derivation fails
     */
    external fun schnorrPrivateKeyToPublicKey(privateKey: ByteArray): ByteArray
}
