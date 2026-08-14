package mobi.timon.crypto

internal actual fun platformRandomBytes(length: Int): ByteArray = RandomJni.bytes(length)

internal object RandomJni {
    init { Enc }
    external fun bytes(length: Int): ByteArray
}
