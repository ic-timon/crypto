package mobi.timon.crypto

import kotlinx.cinterop.*
import mobi.timon.crypto.native.*

@OptIn(ExperimentalForeignApi::class)
actual object Ecdsa {
    actual fun generateKey(curveBits: Int): ByteArray = memScoped {
        val outLen = alloc<IntVar>()
        NativeBridge.unwrap(EcdsaGenerateKey(curveBits, outLen.ptr), outLen.value)
    }
    actual fun sign(message: ByteArray, privateKey: ByteArray) = NativeBridge.call2(message, privateKey) { m, ml, k, kl, o -> EcdsaSign(m, ml, k, kl, o) }
    actual fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray) = NativeBridge.call3v(message, signature, publicKey) { m, ml, s, sl, p, pl, o -> EcdsaVerify(m, ml, s, sl, p, pl, o) }
    actual fun privateKeyToPublicKey(privateKey: ByteArray) = NativeBridge.call1(privateKey) { k, kl, o -> EcdsaPrivateKeyToPublicKey(k, kl, o) }
}
