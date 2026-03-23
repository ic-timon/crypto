package mobi.timon.android.ui.screens.hash

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mobi.timon.android.ui.components.TestStatus
import mobi.timon.android.util.TestResult
import mobi.timon.crypto.Codec
import mobi.timon.crypto.Hash
import mobi.timon.crypto.Hmac

enum class HashAlgorithm(val displayName: String) {
    SHA1("SHA-1"),
    SHA256("SHA-256"),
    SHA384("SHA-384"),
    SHA512("SHA-512"),
    SHA512_256("SHA-512/256"),
    BLAKE2B256("BLAKE2b-256"),
    MD5("MD5"),
    RIPEMD160("RIPEMD-160"),
    KECCAK256("Keccak-256"),
    KECCAK512("Keccak-512"),
    HMAC_SHA1("HMAC-SHA1"),
    HMAC_SHA256("HMAC-SHA256"),
    HMAC_SHA512("HMAC-SHA512")
}

data class HashState(
    val input: String = "Hello, World!",
    val keyInput: String = "",
    val selectedAlgorithm: HashAlgorithm = HashAlgorithm.SHA256,
    val result: String? = null,
    val error: String? = null,
    val testResults: List<TestResult> = emptyList(),
    val isRunning: Boolean = false
)

class HashViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(HashState())
    val state: StateFlow<HashState> = _state.asStateFlow()
    
    fun updateInput(input: String) {
        _state.value = _state.value.copy(input = input, result = null, error = null)
    }
    
    fun updateKey(key: String) {
        _state.value = _state.value.copy(keyInput = key, result = null, error = null)
    }
    
    fun selectAlgorithm(algorithm: HashAlgorithm) {
        _state.value = _state.value.copy(
            selectedAlgorithm = algorithm,
            result = null,
            error = null
        )
    }
    
    fun compute() {
        val currentState = _state.value
        val data = currentState.input.toByteArray()
        
        try {
            val result = when (currentState.selectedAlgorithm) {
                HashAlgorithm.SHA1 -> Codec.toHex(Hash.sha1(data))
                HashAlgorithm.SHA256 -> Codec.toHex(Hash.sha256(data))
                HashAlgorithm.SHA384 -> Codec.toHex(Hash.sha384(data))
                HashAlgorithm.SHA512 -> Codec.toHex(Hash.sha512(data))
                HashAlgorithm.SHA512_256 -> Codec.toHex(Hash.sha512_256(data))
                HashAlgorithm.BLAKE2B256 -> Codec.toHex(Hash.blake2b256(data))
                HashAlgorithm.MD5 -> Codec.toHex(Hash.md5(data))
                HashAlgorithm.RIPEMD160 -> Codec.toHex(Hash.ripemd160(data))
                HashAlgorithm.KECCAK256 -> Codec.toHex(Hash.keccak256(data))
                HashAlgorithm.KECCAK512 -> Codec.toHex(Hash.keccak512(data))
                HashAlgorithm.HMAC_SHA1 -> {
                    val key = currentState.keyInput.ifEmpty { "default" }.toByteArray()
                    Codec.toHex(Hmac.hmacSha1(data, key))
                }
                HashAlgorithm.HMAC_SHA256 -> {
                    val key = currentState.keyInput.ifEmpty { "default" }.toByteArray()
                    Codec.toHex(Hmac.hmacSha256(data, key))
                }
                HashAlgorithm.HMAC_SHA512 -> {
                    val key = currentState.keyInput.ifEmpty { "default" }.toByteArray()
                    Codec.toHex(Hmac.hmacSha512(data, key))
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
        val testInput = "test".toByteArray()
        
        results.add(runSingleTest("SHA-1") {
            Codec.toHex(Hash.sha1(testInput))
        })
        results.add(runSingleTest("SHA-256") {
            Codec.toHex(Hash.sha256(testInput))
        })
        results.add(runSingleTest("SHA-384") {
            Codec.toHex(Hash.sha384(testInput))
        })
        results.add(runSingleTest("SHA-512") {
            Codec.toHex(Hash.sha512(testInput))
        })
        results.add(runSingleTest("SHA-512/256") {
            Codec.toHex(Hash.sha512_256(testInput))
        })
        results.add(runSingleTest("BLAKE2b-256") {
            Codec.toHex(Hash.blake2b256(testInput))
        })
        results.add(runSingleTest("MD5") {
            Codec.toHex(Hash.md5(testInput))
        })
        results.add(runSingleTest("RIPEMD-160") {
            Codec.toHex(Hash.ripemd160(testInput))
        })
        results.add(runSingleTest("Keccak-256") {
            Codec.toHex(Hash.keccak256(testInput))
        })
        results.add(runSingleTest("Keccak-512") {
            Codec.toHex(Hash.keccak512(testInput))
        })
        results.add(runSingleTest("HMAC-SHA1") {
            Codec.toHex(Hmac.hmacSha1(testInput, "key".toByteArray()))
        })
        results.add(runSingleTest("HMAC-SHA256") {
            Codec.toHex(Hmac.hmacSha256(testInput, "key".toByteArray()))
        })
        results.add(runSingleTest("HMAC-SHA512") {
            Codec.toHex(Hmac.hmacSha512(testInput, "key".toByteArray()))
        })
        
        _state.value = _state.value.copy(testResults = results, isRunning = false)
    }
    
    private inline fun runSingleTest(name: String, block: () -> String): TestResult {
        return try {
            val output = block()
            TestResult(name, TestStatus.SUCCESS, output)
        } catch (e: Exception) {
            TestResult(name, TestStatus.FAILURE, error = e.message)
        }
    }
}
