package mobi.timon.crypto

import kotlinx.cinterop.*
import mobi.timon.crypto.native.*

@OptIn(ExperimentalForeignApi::class)
actual object Rsa {
    actual fun generateKey(bits: Int): ByteArray = memScoped {
        val outLen = alloc<IntVar>()
        NativeBridge.unwrap(RsaGenerateKey(bits, outLen.ptr), outLen.value)
    }
    actual fun encrypt(plaintext: ByteArray, publicKey: ByteArray) = NativeBridge.call2(plaintext, publicKey) { p, pl, k, kl, o -> RsaEncrypt(p, pl, k, kl, o) }
    actual fun decrypt(ciphertext: ByteArray, privateKey: ByteArray) = NativeBridge.call2(ciphertext, privateKey) { c, cl, k, kl, o -> RsaDecrypt(c, cl, k, kl, o) }
    actual fun sign(message: ByteArray, privateKey: ByteArray) = NativeBridge.call2(message, privateKey) { m, ml, k, kl, o -> RsaSign(m, ml, k, kl, o) }
    actual fun verify(message: ByteArray, signature: ByteArray, publicKey: ByteArray) = NativeBridge.call3v(message, signature, publicKey) { m, ml, s, sl, p, pl, o -> RsaVerify(m, ml, s, sl, p, pl, o) }
    actual fun privateKeyToPublicKey(privateKey: ByteArray) = NativeBridge.call1(privateKey) { k, kl, o -> RsaPrivateKeyToPublicKey(k, kl, o) }
}
