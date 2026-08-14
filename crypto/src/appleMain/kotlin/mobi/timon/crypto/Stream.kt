package mobi.timon.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import mobi.timon.crypto.native.*

@OptIn(ExperimentalForeignApi::class)
actual object Stream {
    actual fun aesCtrEncrypt(plaintext: ByteArray, key: ByteArray) = NativeBridge.call2(plaintext, key) { p, pl, k, kl, o -> AesCtrEncrypt(p, pl, k, kl, o) }
    actual fun aesCtrDecrypt(ciphertext: ByteArray, key: ByteArray) = NativeBridge.call2(ciphertext, key) { c, cl, k, kl, o -> AesCtrDecrypt(c, cl, k, kl, o) }
    actual fun chacha20Encrypt(plaintext: ByteArray, key: ByteArray) = NativeBridge.call2(plaintext, key) { p, pl, k, kl, o -> ChaCha20Encrypt(p, pl, k, kl, o) }
    actual fun chacha20Decrypt(ciphertext: ByteArray, key: ByteArray) = NativeBridge.call2(ciphertext, key) { c, cl, k, kl, o -> ChaCha20Decrypt(c, cl, k, kl, o) }
}
