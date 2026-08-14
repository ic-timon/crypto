package mobi.timon.crypto

import kotlinx.cinterop.*
import mobi.timon.crypto.native.*

@OptIn(ExperimentalForeignApi::class)
actual object Secp256k1 {
    actual fun generateKey() = NativeBridge.call0 { o -> Secp256k1GenerateKey(o) }
    actual fun privateKeyToPublicKey(privateKey: ByteArray, compressed: Boolean): ByteArray = memScoped {
        val outLen = alloc<IntVar>()
        val result = privateKey.usePinned { p ->
            Secp256k1PrivateKeyToPublicKey(p.addressOf(0).reinterpret(), privateKey.size, if (compressed) 1 else 0, outLen.ptr)
        }
        NativeBridge.unwrap(result, outLen.value)
    }
    actual fun sign(message: ByteArray, privateKey: ByteArray) = NativeBridge.call2(message, privateKey) { m, ml, k, kl, o -> Secp256k1Sign(m, ml, k, kl, o) }
    actual fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray) = NativeBridge.call3v(message, signature, publicKey) { m, ml, s, sl, p, pl, o -> Secp256k1Verify(m, ml, s, sl, p, pl, o) }
    actual fun recoverPublicKey(message: ByteArray, signature: ByteArray, compressed: Boolean): ByteArray = memScoped {
        val outLen = alloc<IntVar>()
        val result = message.usePinned { m ->
            signature.usePinned { s -> Secp256k1RecoverPublicKey(m.addressOf(0).reinterpret(), message.size, s.addressOf(0).reinterpret(), signature.size, if (compressed) 1 else 0, outLen.ptr) }
        }
        NativeBridge.unwrap(result, outLen.value)
    }
    actual fun schnorrSign(message: ByteArray, privateKey: ByteArray) = NativeBridge.call2(message, privateKey) { m, ml, k, kl, o -> SchnorrSign(m, ml, k, kl, o) }
    actual fun schnorrVerify(message: ByteArray, signature: ByteArray, publicKey: ByteArray) = NativeBridge.call3v(message, signature, publicKey) { m, ml, s, sl, p, pl, o -> SchnorrVerify(m, ml, s, sl, p, pl, o) }
    actual fun schnorrPrivateKeyToPublicKey(privateKey: ByteArray) = NativeBridge.call1(privateKey) { k, kl, o -> SchnorrPrivateKeyToPublicKey(k, kl, o) }
    actual fun schnorrSignHash(hash: ByteArray, privateKey: ByteArray) = NativeBridge.call2(hash, privateKey) { h, hl, k, kl, o -> SchnorrSignHash(h, hl, k, kl, o) }
    actual fun schnorrVerifyHash(hash: ByteArray, signature: ByteArray, publicKey: ByteArray) = NativeBridge.call3v(hash, signature, publicKey) { h, hl, s, sl, p, pl, o -> SchnorrVerifyHash(h, hl, s, sl, p, pl, o) }
}
