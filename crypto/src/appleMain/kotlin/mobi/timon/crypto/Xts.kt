package mobi.timon.crypto

import kotlinx.cinterop.*
import mobi.timon.crypto.native.*

@OptIn(ExperimentalForeignApi::class)
actual object Xts {
    actual fun aesXtsEncrypt(plaintext: ByteArray, key: ByteArray, sectorNum: Long): ByteArray = memScoped {
        val outLen = alloc<IntVar>()
        val result = plaintext.usePinned { p ->
            key.usePinned { k -> AesXtsEncrypt(p.addressOf(0).reinterpret(), plaintext.size, k.addressOf(0).reinterpret(), key.size, sectorNum, outLen.ptr) }
        }
        NativeBridge.unwrap(result, outLen.value)
    }
    actual fun aesXtsDecrypt(ciphertext: ByteArray, key: ByteArray, sectorNum: Long): ByteArray = memScoped {
        val outLen = alloc<IntVar>()
        val result = ciphertext.usePinned { c ->
            key.usePinned { k -> AesXtsDecrypt(c.addressOf(0).reinterpret(), ciphertext.size, k.addressOf(0).reinterpret(), key.size, sectorNum, outLen.ptr) }
        }
        NativeBridge.unwrap(result, outLen.value)
    }
}
