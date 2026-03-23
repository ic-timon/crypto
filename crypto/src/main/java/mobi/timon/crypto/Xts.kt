package mobi.timon.crypto

/**
 * AES-XTS (XEX-based Tweaked CodeBook) mode operations.
 * 
 * AES-XTS is designed for disk encryption and providing confidentiality
 * without integrity protection. It's commonly used in 
 * full-disk encryption systems.
 * 
 * **Wire Format**: `ciphertext` (same length as plaintext)
 * 
 * **Security Note**: XTS mode does does NOT provide integrity verification.
 * Consider using an AEAD mode for applications requiring both
 * confidentiality and integrity.
 */
object Xts {

    init {
        Enc
    }

    /**
     * Encrypts data using AES-XTS mode.
     * 
     * @param plaintext Data to encrypt (should be multiple of 16 bytes)
     * @param key Encryption key (32 or 64 bytes for AES-128-XTS or AES-256-XTS)
     * @param sectorNum Sector/tweak number for this block
     * @return Encrypted ciphertext (same length as plaintext)
     * @throws EncException if encryption fails
     */
    external fun aesXtsEncrypt(plaintext: ByteArray, key: ByteArray, sectorNum: Long): ByteArray

    /**
     * Decrypts data using AES-XTS mode.
     * 
     * @param ciphertext Data to decrypt
     * @param key Decryption key (32 or 64 bytes)
     * @param sectorNum Sector/tweak number for this block
     * @return Decrypted plaintext
     * @throws EncException if decryption fails
     */
    external fun aesXtsDecrypt(ciphertext: ByteArray, key: ByteArray, sectorNum: Long): ByteArray
}
