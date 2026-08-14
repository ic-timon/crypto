package mobi.timon.crypto

import kotlinx.cinterop.*
import mobi.timon.crypto.native.enc_free

/**
 * cinterop 调用辅助——pinned ByteArray → C 指针 → 结果拷回 → enc_free。
 * 所有 actual 实现共用这五个模式。
 */
@OptIn(ExperimentalForeignApi::class)
internal object NativeBridge {

    /** 无参 → ByteArray（generateKey 类）。 */
    fun call0(fn: (CPointer<IntVar>?) -> CPointer<UByteVar>?): ByteArray = memScoped {
        val outLen = alloc<IntVar>()
        val result = fn(outLen.ptr)
        unwrap(result, outLen.value)
    }

    /** 单 ByteArray → ByteArray（hash 类）。 */
    fun call1(data: ByteArray, fn: (CPointer<UByteVar>?, Int, CPointer<IntVar>?) -> CPointer<UByteVar>?): ByteArray = memScoped {
        val outLen = alloc<IntVar>()
        val result = data.usePinned { fn(it.addressOf(0).reinterpret(), data.size, outLen.ptr) }
        unwrap(result, outLen.value)
    }

    /** 双 ByteArray → ByteArray（hmac/encrypt 类）。 */
    fun call2(a1: ByteArray, a2: ByteArray, fn: (CPointer<UByteVar>?, Int, CPointer<UByteVar>?, Int, CPointer<IntVar>?) -> CPointer<UByteVar>?): ByteArray = memScoped {
        val outLen = alloc<IntVar>()
        val result = a1.usePinned { p1 ->
            a2.usePinned { p2 -> fn(p1.addressOf(0).reinterpret(), a1.size, p2.addressOf(0).reinterpret(), a2.size, outLen.ptr) }
        }
        unwrap(result, outLen.value)
    }

    /** 双 ByteArray → Boolean（bcryptVerify 类）。 */
    fun call2v(a1: ByteArray, a2: ByteArray, fn: (CPointer<UByteVar>?, Int, CPointer<UByteVar>?, Int, CPointer<IntVar>?) -> CPointer<UByteVar>?): Boolean = memScoped {
        val outLen = alloc<IntVar>()
        val result = a1.usePinned { p1 ->
            a2.usePinned { p2 -> fn(p1.addressOf(0).reinterpret(), a1.size, p2.addressOf(0).reinterpret(), a2.size, outLen.ptr) }
        }
        if (result == null || outLen.value <= 0) throw EncException("operation failed")
        val v = result.pointed.value
        enc_free(result, outLen.value)
        v != 0.toUByte()
    }

    /** 三 ByteArray → Boolean（verify 类）。 */
    fun call3v(a1: ByteArray, a2: ByteArray, a3: ByteArray, fn: (CPointer<UByteVar>?, Int, CPointer<UByteVar>?, Int, CPointer<UByteVar>?, Int, CPointer<IntVar>?) -> CPointer<UByteVar>?): Boolean = memScoped {
        val outLen = alloc<IntVar>()
        val result = a1.usePinned { p1 ->
            a2.usePinned { p2 ->
                a3.usePinned { p3 -> fn(p1.addressOf(0).reinterpret(), a1.size, p2.addressOf(0).reinterpret(), a2.size, p3.addressOf(0).reinterpret(), a3.size, outLen.ptr) }
            }
        }
        if (result == null || outLen.value <= 0) throw EncException("operation failed")
        val v = result.pointed.value
        enc_free(result, outLen.value)
        v != 0.toUByte()
    }

    /** 结果指针 → ByteArray（拷贝 + 释放）。 */
    internal fun unwrap(result: CPointer<UByteVar>?, len: Int): ByteArray {
        if (result == null || len <= 0) throw EncException("operation failed")
        val out = ByteArray(len)
        out.usePinned { pinned ->
            memcpyC(result, pinned.addressOf(0).reinterpret(), len)
        }
        enc_free(result, len)
        return out
    }

    private fun memcpyC(src: CPointer<UByteVar>, dst: CPointer<UByteVar>, len: Int) {
        for (i in 0 until len) dst[i] = src[i]
    }
}
