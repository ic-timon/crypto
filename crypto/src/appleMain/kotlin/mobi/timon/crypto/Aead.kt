package mobi.timon.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import mobi.timon.crypto.native.*

@OptIn(ExperimentalForeignApi::class)
actual object Aead {
    actual fun aesGcmEncrypt(plaintext: ByteArray, key: ByteArray) = NativeBridge.call2(plaintext, key) { p, pl, k, kl, o -> AesGcmEncrypt(p, pl, k, kl, o) }
    actual fun aesGcmDecrypt(ciphertext: ByteArray, key: ByteArray) = NativeBridge.call2(ciphertext, key) { c, cl, k, kl, o -> AesGcmDecrypt(c, cl, k, kl, o) }
    actual fun chacha20Poly1305Encrypt(plaintext: ByteArray, key: ByteArray) = NativeBridge.call2(plaintext, key) { p, pl, k, kl, o -> ChaCha20Poly1305Encrypt(p, pl, k, kl, o) }
    actual fun chacha20Poly1305Decrypt(ciphertext: ByteArray, key: ByteArray) = NativeBridge.call2(ciphertext, key) { c, cl, k, kl, o -> ChaCha20Poly1305Decrypt(c, cl, k, kl, o) }
}
