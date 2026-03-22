package mobi.timon.android.ui.screens.utils

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mobi.timon.android.ui.components.TestStatus
import mobi.timon.android.util.TestResult
import mobi.timon.crypto.Codec
import mobi.timon.crypto.Random

enum class UtilsMode {
    RANDOM_BYTES,
    HEX_ENCODE,
    HEX_DECODE,
    BASE64_ENCODE,
    BASE64_DECODE
}

data class UtilsState(
    val input: String = "",
    val output: String? = null,
    val randomLength: String = "32",
    val selectedMode: UtilsMode = UtilsMode.HEX_ENCODE,
    val error: String? = null,
    val testResults: List<TestResult> = emptyList(),
    val isRunning: Boolean = false
)

class UtilsViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(UtilsState())
    val state: StateFlow<UtilsState> = _state.asStateFlow()
    
    fun updateInput(input: String) {
        _state.value = _state.value.copy(input = input, output = null, error = null)
    }
    
    fun updateRandomLength(length: String) {
        _state.value = _state.value.copy(randomLength = length, output = null, error = null)
    }
    
    fun selectMode(mode: UtilsMode) {
        _state.value = _state.value.copy(selectedMode = mode, output = null, error = null)
    }
    
    fun execute() {
        val currentState = _state.value
        
        try {
            val output = when (currentState.selectedMode) {
                UtilsMode.RANDOM_BYTES -> {
                    val length = currentState.randomLength.toIntOrNull() ?: 32
                    val bytes = Random.bytes(length)
                    Codec.toHex(bytes)
                }
                UtilsMode.HEX_ENCODE -> {
                    val data = currentState.input.toByteArray()
                    Codec.toHex(data)
                }
                UtilsMode.HEX_DECODE -> {
                    val bytes = Codec.fromHex(currentState.input)
                    String(bytes)
                }
                UtilsMode.BASE64_ENCODE -> {
                    val data = currentState.input.toByteArray()
                    Codec.toBase64(data)
                }
                UtilsMode.BASE64_DECODE -> {
                    val bytes = Codec.fromBase64(currentState.input)
                    String(bytes)
                }
            }
            
            _state.value = currentState.copy(output = output, error = null)
        } catch (e: Exception) {
            _state.value = currentState.copy(error = e.message)
        }
    }
    
    fun runAllTests() {
        _state.value = _state.value.copy(isRunning = true)
        
        val results = mutableListOf<TestResult>()
        
        results.add(runUtilsTest("Random Bytes") {
            val bytes1 = Random.bytes(32)
            val bytes2 = Random.bytes(32)
            if (bytes1.size == 32 && bytes2.size == 32 && !bytes1.contentEquals(bytes2)) {
                "OK"
            } else {
                throw Exception("Random generation failed")
            }
        })
        
        results.add(runUtilsTest("Random Int") {
            val int = Random.int()
            "OK: $int"
        })
        
        results.add(runUtilsTest("Random Long Range") {
            val long = Random.long(0, 1000)
            if (long in 0 until 1000) "OK: $long" else throw Exception("Out of range")
        })
        
        results.add(runUtilsTest("Hex Encode/Decode") {
            val data = "Hello".toByteArray()
            val hex = Codec.toHex(data)
            val decoded = Codec.fromHex(hex)
            if (decoded.contentEquals(data)) "OK" else throw Exception("Mismatch")
        })
        
        results.add(runUtilsTest("Base64 Encode/Decode") {
            val data = "Hello, World!".toByteArray()
            val base64 = Codec.toBase64(data)
            val decoded = Codec.fromBase64(base64)
            if (decoded.contentEquals(data)) "OK" else throw Exception("Mismatch")
        })
        
        _state.value = _state.value.copy(testResults = results, isRunning = false)
    }
    
    private inline fun runUtilsTest(name: String, block: () -> String): TestResult {
        return try {
            val output = block()
            TestResult(name, TestStatus.SUCCESS, output)
        } catch (e: Exception) {
            TestResult(name, TestStatus.FAILURE, error = e.message)
        }
    }
}
