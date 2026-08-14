package mobi.timon.crypto

import kotlinx.cinterop.*
import mobi.timon.crypto.native.*

@OptIn(ExperimentalForeignApi::class)
actual object Bls {
    actual fun generateKey() = NativeBridge.call0 { o -> BlsGenerateKey(o) }
    actual fun privateKeyToPublicKey(privateKey: ByteArray) = NativeBridge.call1(privateKey) { k, kl, o -> BlsPrivateKeyToPublicKey(k, kl, o) }
    actual fun sign(message: ByteArray, privateKey: ByteArray) = NativeBridge.call2(message, privateKey) { m, ml, k, kl, o -> BlsSign(m, ml, k, kl, o) }
    actual fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray) = NativeBridge.call3v(message, signature, publicKey) { m, ml, s, sl, p, pl, o -> BlsVerify(m, ml, s, sl, p, pl, o) }
    actual fun aggregateSignatures(signatures: ByteArray, count: Int): ByteArray = memScoped {
        val outLen = alloc<IntVar>()
        val result = signatures.usePinned { s -> BlsAggregateSignatures(s.addressOf(0).reinterpret(), signatures.size, count, outLen.ptr) }
        NativeBridge.unwrap(result, outLen.value)
    }
    actual fun aggregatePublicKeys(publicKeys: ByteArray, count: Int): ByteArray = memScoped {
        val outLen = alloc<IntVar>()
        val result = publicKeys.usePinned { p -> BlsAggregatePublicKeys(p.addressOf(0).reinterpret(), publicKeys.size, count, outLen.ptr) }
        NativeBridge.unwrap(result, outLen.value)
    }
}
