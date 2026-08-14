package mobi.timon.crypto

import kotlinx.cinterop.*
import mobi.timon.crypto.native.*

@OptIn(ExperimentalForeignApi::class)
actual object Kdf {
    actual fun bcryptHash(password: ByteArray, cost: Int): ByteArray = memScoped {
        val outLen = alloc<IntVar>()
        val result = password.usePinned { p -> BcryptHash(p.addressOf(0).reinterpret(), password.size, cost, outLen.ptr) }
        NativeBridge.unwrap(result, outLen.value)
    }
    actual fun bcryptVerify(password: ByteArray, hash: ByteArray) = NativeBridge.call2v(password, hash) { p, pl, h, hl, o -> BcryptVerify(p, pl, h, hl, o) }
    actual fun argon2idHash(password: ByteArray, salt: ByteArray, timeCost: Int, memoryCost: Int, parallelism: Int, keyLen: Int): ByteArray = memScoped {
        val outLen = alloc<IntVar>()
        val result = password.usePinned { p ->
            salt.usePinned { s -> Argon2idHash(p.addressOf(0).reinterpret(), password.size, s.addressOf(0).reinterpret(), salt.size, timeCost, memoryCost, parallelism, keyLen, outLen.ptr) }
        }
        NativeBridge.unwrap(result, outLen.value)
    }
    actual fun scrypt(password: ByteArray, salt: ByteArray, keyLen: Int): ByteArray = memScoped {
        val outLen = alloc<IntVar>()
        val result = password.usePinned { p ->
            salt.usePinned { s -> Scrypt(p.addressOf(0).reinterpret(), password.size, s.addressOf(0).reinterpret(), salt.size, keyLen, outLen.ptr) }
        }
        NativeBridge.unwrap(result, outLen.value)
    }
    actual fun pbkdf2(password: ByteArray, salt: ByteArray, iterations: Int, keyLen: Int): ByteArray = memScoped {
        val outLen = alloc<IntVar>()
        val result = password.usePinned { p ->
            salt.usePinned { s -> Pbkdf2(p.addressOf(0).reinterpret(), password.size, s.addressOf(0).reinterpret(), salt.size, iterations, keyLen, outLen.ptr) }
        }
        NativeBridge.unwrap(result, outLen.value)
    }
    actual fun hkdf(ikm: ByteArray, salt: ByteArray, info: ByteArray, keyLen: Int): ByteArray = memScoped {
        val outLen = alloc<IntVar>()
        val result = ikm.usePinned { i ->
            salt.usePinned { s ->
                info.usePinned { f -> Hkdf(i.addressOf(0).reinterpret(), ikm.size, s.addressOf(0).reinterpret(), salt.size, f.addressOf(0).reinterpret(), info.size, keyLen, outLen.ptr) }
            }
        }
        NativeBridge.unwrap(result, outLen.value)
    }
}
