package mobi.timon.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import mobi.timon.crypto.native.*

@OptIn(ExperimentalForeignApi::class)
actual object Ed25519 {
    actual fun generateKey() = NativeBridge.call0 { o -> Ed25519GenerateKey(o) }
    actual fun sign(message: ByteArray, privateKey: ByteArray) = NativeBridge.call2(message, privateKey) { m, ml, k, kl, o -> Ed25519Sign(m, ml, k, kl, o) }
    actual fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray) = NativeBridge.call3v(message, signature, publicKey) { m, ml, s, sl, p, pl, o -> Ed25519Verify(m, ml, s, sl, p, pl, o) }
}
