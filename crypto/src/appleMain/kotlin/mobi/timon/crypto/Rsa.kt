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
    actual fun verifyPss(modulus: ByteArray, exponent: ByteArray, digest: ByteArray, signature: ByteArray, saltLength: Int): Boolean {
        require(modulus.isNotEmpty() && exponent.isNotEmpty() && digest.size in setOf(32, 48, 64) && saltLength >= 0)
        val status = modulus.usePinned { n -> exponent.usePinned { e ->
            digest.usePinned { hash -> signature.usePinned { sig ->
                encrust_rsa_pss_verify_v1(n.addressOf(0).reinterpret(), modulus.size,
                    e.addressOf(0).reinterpret(), exponent.size, hash.addressOf(0).reinterpret(), digest.size,
                    if (signature.isEmpty()) null else sig.addressOf(0).reinterpret(), signature.size, saltLength)
            } }
        } }
        require(status >= 0) { "invalid RSA-PSS parameters ($status)" }
        return status == 1
    }
    actual fun privateKeyToPublicKey(privateKey: ByteArray) = NativeBridge.call1(privateKey) { k, kl, o -> RsaPrivateKeyToPublicKey(k, kl, o) }
}
