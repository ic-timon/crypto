package mobi.timon.crypto

import kotlinx.cinterop.*
import mobi.timon.crypto.native.*

@OptIn(ExperimentalForeignApi::class)
internal actual fun platformRandomBytes(length: Int): ByteArray = memScoped {
    val outLen = alloc<IntVar>()
    val result = RandomBytes(length, outLen.ptr)
    if (result == null || outLen.value <= 0) throw EncException("random failed")
    val out = ByteArray(outLen.value)
    out.usePinned { pinned ->
        for (i in 0 until outLen.value) pinned.addressOf(i)[0] = result[i].toByte()
    }
    enc_free(result, outLen.value)
    out
}
