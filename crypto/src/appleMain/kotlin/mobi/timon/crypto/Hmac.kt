package mobi.timon.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import mobi.timon.crypto.native.*

@OptIn(ExperimentalForeignApi::class)
actual object Hmac {
    actual fun hmacSha256(data: ByteArray, key: ByteArray) = NativeBridge.call2(data, key) { d, dl, k, kl, o -> HmacSha256(d, dl, k, kl, o) }
    actual fun hmacSha512(data: ByteArray, key: ByteArray) = NativeBridge.call2(data, key) { d, dl, k, kl, o -> HmacSha512(d, dl, k, kl, o) }
    actual fun hmacSha1(data: ByteArray, key: ByteArray) = NativeBridge.call2(data, key) { d, dl, k, kl, o -> HmacSha1(d, dl, k, kl, o) }
}
