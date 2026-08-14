package mobi.timon.crypto
/** CSPRNG。bytes 走 native（getrandom），int/long 纯 Kotlin。 */
expect object Random {
    fun bytes(length: Int): ByteArray

    fun int(): Int = {
        val b = bytes(4)
        ((b[0].toInt() and 0xFF) shl 24) or
        ((b[1].toInt() and 0xFF) shl 16) or
        ((b[2].toInt() and 0xFF) shl 8) or
        (b[3].toInt() and 0xFF)
    }()
}
