package mobi.timon.android.ui.screens.cipher

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mobi.timon.android.ui.components.TestStatus
import mobi.timon.android.util.TestResult
import mobi.timon.crypto.Aead
import mobi.timon.crypto.Cbc
import mobi.timon.crypto.Codec
import mobi.timon.crypto.Random
import mobi.timon.crypto.Stream
import mobi.timon.crypto.EncException
import mobi.timon.crypto.Xts

enum class CipherMode {
    ENCRYPT,
    DECRYPT
}

enum class CipherAlgorithm(val displayName: String, val keySize: Int) {
    AES_GCM("AES-GCM", 32),
    CHACHA20_POLY1305("ChaCha20-Poly1305", 32),
    AES_CBC("AES-CBC", 16),
    DES_CBC("DES-CBC", 8),
    AES_CTR("AES-CTR", 16),
    CHACHA20("ChaCha20", 32),
    AES_XTS("AES-XTS", 32)
}

data class CipherState(
    val input: String = "Hello, World!",
    val inputHex: String = "",
    val useHexInput: Boolean = false,
    val keyHex: String = "",
    val sectorNum: String = "0",
    val selectedAlgorithm: CipherAlgorithm = CipherAlgorithm.AES_GCM,
    val mode: CipherMode = CipherMode.ENCRYPT,
    val result: String? = null,
    val error: String? = null,
    val testResults: List<TestResult> = emptyList(),
    val isRunning: Boolean = false
)

class CipherViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(CipherState())
    val state: StateFlow<CipherState> = _state.asStateFlow()
    
    fun updateInput(input: String) {
        _state.value = _state.value.copy(input = input, result = null, error = null)
    }
    
    fun updateInputHex(hex: String) {
        _state.value = _state.value.copy(inputHex = hex, result = null, error = null)
    }
    
    fun toggleHexInput(useHex: Boolean) {
        _state.value = _state.value.copy(useHexInput = useHex, result = null, error = null)
    }
    
    fun updateKey(hex: String) {
        _state.value = _state.value.copy(keyHex = hex, result = null, error = null)
    }
    
    fun updateSectorNum(num: String) {
        _state.value = _state.value.copy(sectorNum = num, result = null, error = null)
    }
    
    fun selectAlgorithm(algorithm: CipherAlgorithm) {
        _state.value = _state.value.copy(
            selectedAlgorithm = algorithm,
            result = null,
            error = null
        )
    }
    
    fun setMode(mode: CipherMode) {
        _state.value = _state.value.copy(mode = mode, result = null, error = null)
    }
    
    fun generateKey() {
        val key = Random.bytes(_state.value.selectedAlgorithm.keySize)
        _state.value = _state.value.copy(keyHex = Codec.toHex(key), result = null, error = null)
    }
    
    fun execute() {
        val currentState = _state.value
        
        try {
            val data = if (currentState.useHexInput) {
                Codec.fromHex(currentState.inputHex)
            } else {
                currentState.input.toByteArray()
            }
            
            val key = if (currentState.keyHex.isNotEmpty()) {
                Codec.fromHex(currentState.keyHex)
            } else {
                Random.bytes(currentState.selectedAlgorithm.keySize)
            }
            
            val result = when (currentState.selectedAlgorithm) {
                CipherAlgorithm.AES_GCM -> {
                    if (currentState.mode == CipherMode.ENCRYPT) {
                        Codec.toHex(Aead.aesGcmEncrypt(data, key))
                    } else {
                        String(Aead.aesGcmDecrypt(data, key))
                    }
                }
                CipherAlgorithm.CHACHA20_POLY1305 -> {
                    if (currentState.mode == CipherMode.ENCRYPT) {
                        Codec.toHex(Aead.chacha20Poly1305Encrypt(data, key))
                    } else {
                        String(Aead.chacha20Poly1305Decrypt(data, key))
                    }
                }
                CipherAlgorithm.AES_CBC -> {
                    if (currentState.mode == CipherMode.ENCRYPT) {
                        Codec.toHex(Cbc.aesCbcEncrypt(data, key))
                    } else {
                        String(Cbc.aesCbcDecrypt(data, key))
                    }
                }
                CipherAlgorithm.DES_CBC -> {
                    if (currentState.mode == CipherMode.ENCRYPT) {
                        Codec.toHex(Cbc.desCbcEncrypt(data, key))
                    } else {
                        String(Cbc.desCbcDecrypt(data, key))
                    }
                }
                CipherAlgorithm.AES_CTR -> {
                    if (currentState.mode == CipherMode.ENCRYPT) {
                        Codec.toHex(Stream.aesCtrEncrypt(data, key))
                    } else {
                        String(Stream.aesCtrDecrypt(data, key))
                    }
                }
                CipherAlgorithm.CHACHA20 -> {
                    if (currentState.mode == CipherMode.ENCRYPT) {
                        Codec.toHex(Stream.chacha20Encrypt(data, key))
                    } else {
                        String(Stream.chacha20Decrypt(data, key))
                    }
                }
                CipherAlgorithm.AES_XTS -> {
                    if (data.size % 16 != 0) {
                        throw EncException(
                            "AES-XTS 要求明文/密文长度为 16 字节的倍数（当前 ${data.size} 字节）"
                        )
                    }
                    val sector = currentState.sectorNum.toLongOrNull() ?: 0L
                    if (currentState.mode == CipherMode.ENCRYPT) {
                        Codec.toHex(Xts.aesXtsEncrypt(data, key, sector))
                    } else {
                        String(Xts.aesXtsDecrypt(data, key, sector))
                    }
                }
            }
            
            _state.value = currentState.copy(result = result, error = null)
        } catch (e: Exception) {
            _state.value = currentState.copy(error = e.message, result = null)
        }
    }
    
    fun runAllTests() {
        _state.value = _state.value.copy(isRunning = true)
        
        val results = mutableListOf<TestResult>()
        
        results.add(runCipherTest("AES-GCM") {
            val plaintext = "test".toByteArray()
            val key = Random.bytes(32)
            val encrypted = Aead.aesGcmEncrypt(plaintext, key)
            val decrypted = Aead.aesGcmDecrypt(encrypted, key)
            if (decrypted.contentEquals(plaintext)) "OK" else throw Exception("Mismatch")
        })
        
        results.add(runCipherTest("ChaCha20-Poly1305") {
            val plaintext = "test".toByteArray()
            val key = Random.bytes(32)
            val encrypted = Aead.chacha20Poly1305Encrypt(plaintext, key)
            val decrypted = Aead.chacha20Poly1305Decrypt(encrypted, key)
            if (decrypted.contentEquals(plaintext)) "OK" else throw Exception("Mismatch")
        })
        
        results.add(runCipherTest("AES-CBC") {
            val plaintext = "test".toByteArray()
            val key = Random.bytes(16)
            val encrypted = Cbc.aesCbcEncrypt(plaintext, key)
            val decrypted = Cbc.aesCbcDecrypt(encrypted, key)
            if (decrypted.contentEquals(plaintext)) "OK" else throw Exception("Mismatch")
        })
        
        results.add(runCipherTest("DES-CBC") {
            val plaintext = "test".toByteArray()
            val key = Random.bytes(8)
            val encrypted = Cbc.desCbcEncrypt(plaintext, key)
            val decrypted = Cbc.desCbcDecrypt(encrypted, key)
            if (decrypted.contentEquals(plaintext)) "OK" else throw Exception("Mismatch")
        })
        
        results.add(runCipherTest("AES-CTR") {
            val plaintext = "test".toByteArray()
            val key = Random.bytes(16)
            val encrypted = Stream.aesCtrEncrypt(plaintext, key)
            val decrypted = Stream.aesCtrDecrypt(encrypted, key)
            if (decrypted.contentEquals(plaintext)) "OK" else throw Exception("Mismatch")
        })
        
        results.add(runCipherTest("ChaCha20") {
            val plaintext = "test".toByteArray()
            val key = Random.bytes(32)
            val encrypted = Stream.chacha20Encrypt(plaintext, key)
            val decrypted = Stream.chacha20Decrypt(encrypted, key)
            if (decrypted.contentEquals(plaintext)) "OK" else throw Exception("Mismatch")
        })
        
        results.add(runCipherTest("AES-XTS") {
            val plaintext = ByteArray(32) { it.toByte() }
            val key = Random.bytes(32)
            val encrypted = Xts.aesXtsEncrypt(plaintext, key, 0)
            val decrypted = Xts.aesXtsDecrypt(encrypted, key, 0)
            if (decrypted.contentEquals(plaintext)) "OK" else throw Exception("Mismatch")
        })
        
        _state.value = _state.value.copy(testResults = results, isRunning = false)
    }
    
    private inline fun runCipherTest(name: String, block: () -> String): TestResult {
        return try {
            val output = block()
            TestResult(name, TestStatus.SUCCESS, output)
        } catch (e: Exception) {
            TestResult(name, TestStatus.FAILURE, error = e.message)
        }
    }
}
