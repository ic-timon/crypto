package mobi.timon.android.ui.screens.kdf

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mobi.timon.android.ui.components.TestStatus
import mobi.timon.android.util.TestResult
import mobi.timon.crypto.Codec
import mobi.timon.crypto.Kdf
import mobi.timon.crypto.Random

enum class KdfAlgorithm(val displayName: String) {
    BCRYPT("Bcrypt"),
    ARGON2ID("Argon2id"),
    SCRYPT("Scrypt"),
    PBKDF2("PBKDF2"),
    HKDF("HKDF")
}

data class KdfState(
    val password: String = "password123",
    val saltHex: String = "",
    val iterations: String = "10",
    val keyLength: String = "32",
    val selectedAlgorithm: KdfAlgorithm = KdfAlgorithm.BCRYPT,
    val result: String? = null,
    val verifyResult: Boolean? = null,
    val error: String? = null,
    val testResults: List<TestResult> = emptyList(),
    val isRunning: Boolean = false
)

class KdfViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(KdfState())
    val state: StateFlow<KdfState> = _state.asStateFlow()
    
    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password, result = null, error = null, verifyResult = null)
    }
    
    fun updateSalt(hex: String) {
        _state.value = _state.value.copy(saltHex = hex, result = null, error = null)
    }
    
    fun updateIterations(iterations: String) {
        _state.value = _state.value.copy(iterations = iterations, result = null, error = null)
    }
    
    fun updateKeyLength(length: String) {
        _state.value = _state.value.copy(keyLength = length, result = null, error = null)
    }
    
    fun selectAlgorithm(algorithm: KdfAlgorithm) {
        _state.value = _state.value.copy(
            selectedAlgorithm = algorithm,
            result = null,
            error = null,
            verifyResult = null
        )
    }
    
    fun generateSalt() {
        val salt = Random.bytes(16)
        _state.value = _state.value.copy(saltHex = Codec.toHex(salt), result = null, error = null)
    }
    
    fun derive() {
        val currentState = _state.value
        
        try {
            val password = currentState.password.toByteArray()
            val keyLen = currentState.keyLength.toIntOrNull() ?: 32
            
            val result = when (currentState.selectedAlgorithm) {
                KdfAlgorithm.BCRYPT -> {
                    val cost = currentState.iterations.toIntOrNull() ?: 10
                    val hash = Kdf.bcryptHash(password, cost)
                    Codec.toBase64(hash)
                }
                KdfAlgorithm.ARGON2ID -> {
                    val salt = if (currentState.saltHex.isNotEmpty()) {
                        Codec.fromHex(currentState.saltHex)
                    } else {
                        Random.bytes(16)
                    }
                    val timeCost = currentState.iterations.toIntOrNull() ?: 1
                    val hash = Kdf.argon2idHash(password, salt, timeCost, 65536, 1, keyLen)
                    Codec.toHex(hash)
                }
                KdfAlgorithm.SCRYPT -> {
                    val salt = if (currentState.saltHex.isNotEmpty()) {
                        Codec.fromHex(currentState.saltHex)
                    } else {
                        Random.bytes(16)
                    }
                    val key = Kdf.scrypt(password, salt, keyLen)
                    Codec.toHex(key)
                }
                KdfAlgorithm.PBKDF2 -> {
                    val salt = if (currentState.saltHex.isNotEmpty()) {
                        Codec.fromHex(currentState.saltHex)
                    } else {
                        Random.bytes(16)
                    }
                    val iterations = currentState.iterations.toIntOrNull() ?: 10000
                    val key = Kdf.pbkdf2(password, salt, iterations, keyLen)
                    Codec.toHex(key)
                }
                KdfAlgorithm.HKDF -> {
                    val ikm = if (currentState.saltHex.isNotEmpty()) {
                        Codec.fromHex(currentState.saltHex)
                    } else {
                        Random.bytes(32)
                    }
                    val salt = Random.bytes(16)
                    val info = "context".toByteArray()
                    val key = Kdf.hkdf(ikm, salt, info, keyLen)
                    Codec.toHex(key)
                }
            }
            
            _state.value = currentState.copy(result = result, error = null)
        } catch (e: Exception) {
            _state.value = currentState.copy(error = e.message, result = null)
        }
    }
    
    fun verifyBcrypt(hashBase64: String) {
        val currentState = _state.value
        try {
            val password = currentState.password.toByteArray()
            val hash = Codec.fromBase64(hashBase64)
            val verified = Kdf.bcryptVerify(password, hash)
            _state.value = currentState.copy(verifyResult = verified)
        } catch (e: Exception) {
            _state.value = currentState.copy(error = e.message)
        }
    }
    
    fun runAllTests() {
        _state.value = _state.value.copy(isRunning = true)
        
        val results = mutableListOf<TestResult>()
        
        results.add(runKdfTest("Bcrypt") {
            val password = "test".toByteArray()
            val hash = Kdf.bcryptHash(password, 4)
            if (Kdf.bcryptVerify(password, hash)) "OK" else throw Exception("Verify failed")
        })
        
        results.add(runKdfTest("Argon2id") {
            val password = "test".toByteArray()
            val salt = Random.bytes(16)
            val hash = Kdf.argon2idHash(password, salt, 1, 65536, 1, 32)
            "${hash.size} bytes"
        })
        
        results.add(runKdfTest("Scrypt") {
            val password = "test".toByteArray()
            val salt = Random.bytes(16)
            val key = Kdf.scrypt(password, salt, 32)
            "${key.size} bytes"
        })
        
        results.add(runKdfTest("PBKDF2") {
            val password = "test".toByteArray()
            val salt = Random.bytes(16)
            val key = Kdf.pbkdf2(password, salt, 1000, 32)
            "${key.size} bytes"
        })
        
        results.add(runKdfTest("HKDF") {
            val ikm = Random.bytes(32)
            val salt = Random.bytes(16)
            val info = "test".toByteArray()
            val key = Kdf.hkdf(ikm, salt, info, 32)
            "${key.size} bytes"
        })
        
        _state.value = _state.value.copy(testResults = results, isRunning = false)
    }
    
    private inline fun runKdfTest(name: String, block: () -> String): TestResult {
        return try {
            val output = block()
            TestResult(name, TestStatus.SUCCESS, output)
        } catch (e: Exception) {
            TestResult(name, TestStatus.FAILURE, error = e.message)
        }
    }
}
