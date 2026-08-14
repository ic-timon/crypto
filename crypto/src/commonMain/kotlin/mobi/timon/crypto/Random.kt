package mobi.timon.crypto

/** 平台随机字节桥（Android=JNI, Apple=cinterop）。 */
internal expect fun platformRandomBytes(length: Int): ByteArray

/** CSPRNG。bytes 走平台 native，int/long 纯 Kotlin。 */
object Random {
    fun bytes(length: Int): ByteArray = platformRandomBytes(length)

    fun int(): Int {
        val b = bytes(4)
        return ((b[0].toInt() and 0xFF) shl 24) or
            ((b[1].toInt() and 0xFF) shl 16) or
            ((b[2].toInt() and 0xFF) shl 8) or
            (b[3].toInt() and 0xFF)
    }
}
