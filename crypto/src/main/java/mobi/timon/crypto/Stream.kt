package mobi.timon.crypto

/**
 * Stream cipher operations.
 * 
 * Provides stream ciphers that encrypt data byte-by-byte without built-in
 * integrity protection. Unlike AEAD ciphers, these do NOT authenticate the ciphertext.
 * 
 * **Security Note**: Stream ciphers provide confidentiality only. For authenticated
 * encryption, use [Aead] instead.
 * 
 * **Wire Format**: All outputs include nonce prepended:
 * - AES-CTR: `nonce (16 bytes) + ciphertext`
 * - ChaCha20: `nonce (12 bytes) + ciphertext`
 */
object Stream {

    init {
        Enc
    }

    /**
     * Encrypts data using AES-CTR mode.
     * 
     * @param plaintext Data to encrypt
     * @param key Encryption key (16/24/32 bytes for AES-128/192/256)
     * @return Ciphertext = `nonce (16) + encrypted data`
     * @throws EncException if encryption fails
     */
    external fun aesCtrEncrypt(plaintext: ByteArray, key: ByteArray): ByteArray

    /**
     * Decrypts data using AES-CTR mode.
     * 
     * @param ciphertext Ciphertext = `nonce (16) + encrypted data`
     * @param key Decryption key (16/24/32 bytes)
     * @return Decrypted plaintext
     * @throws EncException if decryption fails
     */
    external fun aesCtrDecrypt(ciphertext: ByteArray, key: ByteArray): ByteArray

    /**
     * Encrypts data using ChaCha20 stream cipher.
     * 
     * Note: This is plain ChaCha20 without Poly1305 authentication.
     * For authenticated encryption, use [Aead.chacha20Poly1305Encrypt].
     * 
     * @param plaintext Data to encrypt
     * @param key Encryption key (32 bytes)
     * @return Ciphertext = `nonce (12) + encrypted data`
     * @throws EncException if encryption fails
     */
    external fun chacha20Encrypt(plaintext: ByteArray, key: ByteArray): ByteArray

    /**
     * Decrypts data using ChaCha20 stream cipher.
     * 
     * @param ciphertext Ciphertext = `nonce (12) + encrypted data`
     * @param key Decryption key (32 bytes)
     * @return Decrypted plaintext
     * @throws EncException if decryption fails
     */
    external fun chacha20Decrypt(ciphertext: ByteArray, key: ByteArray): ByteArray
}
