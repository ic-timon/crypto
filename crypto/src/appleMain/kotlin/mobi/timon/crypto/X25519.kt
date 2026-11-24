package mobi.timon.crypto

import kotlinx.cinterop.*
import mobi.timon.crypto.native.encrust_x25519_public_v1
import mobi.timon.crypto.native.encrust_x25519_shared_v1

/** RFC 7748 on Kotlin/Native. Private keys and wire public keys are 32 bytes. */
@OptIn(ExperimentalForeignApi::class)
actual object X25519 {
    actual fun publicKey(privateKey: ByteArray): ByteArray {
        require(privateKey.size == 32)
        return ByteArray(32).also { output ->
            privateKey.usePinned { secret -> output.usePinned { out ->
                check(encrust_x25519_public_v1(secret.addressOf(0).reinterpret(), out.addressOf(0).reinterpret()) == 0)
            } }
        }
    }

    actual fun sharedSecret(privateKey: ByteArray, peerPublic: ByteArray): ByteArray {
        require(privateKey.size == 32 && peerPublic.size == 32)
        return ByteArray(32).also { output ->
            privateKey.usePinned { secret -> peerPublic.usePinned { peer -> output.usePinned { out ->
                val status = encrust_x25519_shared_v1(secret.addressOf(0).reinterpret(),
                    peer.addressOf(0).reinterpret(), out.addressOf(0).reinterpret())
                require(status != -2) { "low-order X25519 public key" }
                check(status == 0)
            } } }
        }
    }
}
