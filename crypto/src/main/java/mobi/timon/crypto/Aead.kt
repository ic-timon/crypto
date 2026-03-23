package mobi.timon.crypto

/**
 * Authenticated Encryption with Associated Data (AEAD) ciphers.
 * 
 * AEAD ciphers provide both confidentiality and authenticity guarantees.
 * They encrypt data and produce an authentication tag that verifies
 * the ciphertext hasn't been tampered with.
 * 
 * **Wire Format**: All encrypted outputs include nonce and tag prepended/appended:
 * - AES-GCM: `nonce (12 bytes) + ciphertext + tag (16 bytes)`
 * - ChaCha20-Poly1305: `nonce (12 bytes) + ciphertext + tag (16 bytes)`
 */
object Aead {

    init {
        Enc
    }

    /**
     * Encrypts data using AES-GCM.
     * 
     * @param plaintext Data to encrypt
     * @param key Encryption key (16/24/32 bytes for AES-128/192/256)
     * @return Ciphertext = `nonce (12) + ciphertext + tag (16)`
     * @throws EncException if encryption fails
     */
    external fun aesGcmEncrypt(plaintext: ByteArray, key: ByteArray): ByteArray

    /**
     * Decrypts data using AES-GCM.
     * 
     * @param ciphertext Ciphertext = `nonce (12) + ciphertext + tag (16)`
     * @param key Decryption key (16/24/32 bytes for AES-128/192/256)
     * @return Decrypted plaintext
     * @throws EncException if decryption or authentication fails
     */
    external fun aesGcmDecrypt(ciphertext: ByteArray, key: ByteArray): ByteArray

    /**
     * Encrypts data using ChaCha20-Poly1305.
     * 
     * @param plaintext Data to encrypt
     * @param key Encryption key (32 bytes)
     * @return Ciphertext = `nonce (12) + ciphertext + tag (16)`
     * @throws EncException if encryption fails
     */
    external fun chacha20Poly1305Encrypt(plaintext: ByteArray, key: ByteArray): ByteArray

    /**
     * Decrypts data using ChaCha20-Poly1305.
     * 
     * @param ciphertext Ciphertext = `nonce (12) + ciphertext + tag (16)`
     * @param key Decryption key (32 bytes)
     * @return Decrypted plaintext
     * @throws EncException if decryption or authentication fails
     */
    external fun chacha20Poly1305Decrypt(ciphertext: ByteArray, key: ByteArray): ByteArray
}
