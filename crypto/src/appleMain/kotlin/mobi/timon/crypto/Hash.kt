package mobi.timon.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import mobi.timon.crypto.native.*

@OptIn(ExperimentalForeignApi::class)
actual object Hash {
    actual fun sha1(data: ByteArray) = NativeBridge.call1(data) { d, l, o -> Sha1(d, l, o) }
    actual fun sha256(data: ByteArray) = NativeBridge.call1(data) { d, l, o -> Sha256(d, l, o) }
    actual fun sha384(data: ByteArray) = NativeBridge.call1(data) { d, l, o -> Sha384(d, l, o) }
    actual fun sha512(data: ByteArray) = NativeBridge.call1(data) { d, l, o -> Sha512(d, l, o) }
    actual fun sha512_256(data: ByteArray) = NativeBridge.call1(data) { d, l, o -> Sha512_256(d, l, o) }
    actual fun blake2b256(data: ByteArray) = NativeBridge.call1(data) { d, l, o -> Blake2b256(d, l, o) }
    actual fun md5(data: ByteArray) = NativeBridge.call1(data) { d, l, o -> Md5(d, l, o) }
    actual fun ripemd160(data: ByteArray) = NativeBridge.call1(data) { d, l, o -> Ripemd160(d, l, o) }
    actual fun keccak256(data: ByteArray) = NativeBridge.call1(data) { d, l, o -> Keccak256(d, l, o) }
    actual fun keccak512(data: ByteArray) = NativeBridge.call1(data) { d, l, o -> Keccak512(d, l, o) }
}
