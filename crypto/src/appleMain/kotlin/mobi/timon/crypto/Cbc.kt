package mobi.timon.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import mobi.timon.crypto.native.*

@OptIn(ExperimentalForeignApi::class)
actual object Cbc {
    actual fun aesCbcEncrypt(plaintext: ByteArray, key: ByteArray) = NativeBridge.call2(plaintext, key) { p, pl, k, kl, o -> AesCbcEncrypt(p, pl, k, kl, o) }
    actual fun aesCbcDecrypt(ciphertext: ByteArray, key: ByteArray) = NativeBridge.call2(ciphertext, key) { c, cl, k, kl, o -> AesCbcDecrypt(c, cl, k, kl, o) }
    actual fun desCbcEncrypt(plaintext: ByteArray, key: ByteArray) = NativeBridge.call2(plaintext, key) { p, pl, k, kl, o -> DesCbcEncrypt(p, pl, k, kl, o) }
    actual fun desCbcDecrypt(ciphertext: ByteArray, key: ByteArray) = NativeBridge.call2(ciphertext, key) { c, cl, k, kl, o -> DesCbcDecrypt(c, cl, k, kl, o) }
}
