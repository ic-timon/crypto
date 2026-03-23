package mobi.timon.android.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobi.timon.android.R
import mobi.timon.android.ui.components.ApiTestInfo
import mobi.timon.android.ui.components.TestStatus
import mobi.timon.android.util.CryptoTester
import mobi.timon.android.util.TestResult

data class DashboardState(
    val hashTests: List<TestResult> = emptyList(),
    val cipherTests: List<TestResult> = emptyList(),
    val kdfTests: List<TestResult> = emptyList(),
    val signTests: List<TestResult> = emptyList(),
    val utilsTests: List<TestResult> = emptyList(),
    val isRunning: Boolean = false,
    val lastRunTime: Long = 0
) {
    val totalTests: Int
        get() = hashTests.size + cipherTests.size + kdfTests.size + signTests.size + utilsTests.size
    
    val passedTests: Int
        get() = hashTests.count { it.status == TestStatus.SUCCESS } +
                cipherTests.count { it.status == TestStatus.SUCCESS } +
                kdfTests.count { it.status == TestStatus.SUCCESS } +
                signTests.count { it.status == TestStatus.SUCCESS } +
                utilsTests.count { it.status == TestStatus.SUCCESS }
    
    val successRate: Float
        get() = if (totalTests == 0) 0f else passedTests.toFloat() / totalTests
    
    val hashApiInfo: ApiTestInfo
        get() = ApiTestInfo(
            nameResId = R.string.module_hash_name,
            descriptionResId = R.string.module_hash_desc,
            testCount = hashTests.size,
            passedCount = hashTests.count { it.status == TestStatus.SUCCESS },
            status = when {
                hashTests.isEmpty() -> TestStatus.PENDING
                hashTests.all { it.status == TestStatus.SUCCESS } -> TestStatus.SUCCESS
                else -> TestStatus.FAILURE
            }
        )
    
    val cipherApiInfo: ApiTestInfo
        get() = ApiTestInfo(
            nameResId = R.string.module_cipher_name,
            descriptionResId = R.string.module_cipher_desc,
            testCount = cipherTests.size,
            passedCount = cipherTests.count { it.status == TestStatus.SUCCESS },
            status = when {
                cipherTests.isEmpty() -> TestStatus.PENDING
                cipherTests.all { it.status == TestStatus.SUCCESS } -> TestStatus.SUCCESS
                else -> TestStatus.FAILURE
            }
        )
    
    val kdfApiInfo: ApiTestInfo
        get() = ApiTestInfo(
            nameResId = R.string.module_kdf_name,
            descriptionResId = R.string.module_kdf_desc,
            testCount = kdfTests.size,
            passedCount = kdfTests.count { it.status == TestStatus.SUCCESS },
            status = when {
                kdfTests.isEmpty() -> TestStatus.PENDING
                kdfTests.all { it.status == TestStatus.SUCCESS } -> TestStatus.SUCCESS
                else -> TestStatus.FAILURE
            }
        )

    val signApiInfo: ApiTestInfo
        get() = ApiTestInfo(
            nameResId = R.string.module_sign_name,
            descriptionResId = R.string.module_sign_desc,
            testCount = signTests.size,
            passedCount = signTests.count { it.status == TestStatus.SUCCESS },
            status = when {
                signTests.isEmpty() -> TestStatus.PENDING
                signTests.all { it.status == TestStatus.SUCCESS } -> TestStatus.SUCCESS
                else -> TestStatus.FAILURE
            }
        )

    val utilsApiInfo: ApiTestInfo
        get() = ApiTestInfo(
            nameResId = R.string.module_utils_name,
            descriptionResId = R.string.module_utils_desc,
            testCount = utilsTests.size,
            passedCount = utilsTests.count { it.status == TestStatus.SUCCESS },
            status = when {
                utilsTests.isEmpty() -> TestStatus.PENDING
                utilsTests.all { it.status == TestStatus.SUCCESS } -> TestStatus.SUCCESS
                else -> TestStatus.FAILURE
            }
        )
}

class DashboardViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()
    
    fun runAllTests() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRunning = true)
            
            val startTime = System.currentTimeMillis()
            
            val hashTests = CryptoTester.runAllHashTests()
            _state.value = _state.value.copy(hashTests = hashTests)
            
            val cipherTests = CryptoTester.runAllCipherTests()
            _state.value = _state.value.copy(cipherTests = cipherTests)
            
            val kdfTests = CryptoTester.runAllKdfTests()
            _state.value = _state.value.copy(kdfTests = kdfTests)
            
            val signTests = CryptoTester.runAllSignTests()
            _state.value = _state.value.copy(signTests = signTests)
            
            val utilsTests = CryptoTester.runAllUtilsTests()
            _state.value = _state.value.copy(
                utilsTests = utilsTests,
                isRunning = false,
                lastRunTime = System.currentTimeMillis() - startTime
            )
        }
    }
    
    fun runHashTests() {
        viewModelScope.launch {
            _state.value = _state.value.copy(hashTests = CryptoTester.runAllHashTests())
        }
    }
    
    fun runCipherTests() {
        viewModelScope.launch {
            _state.value = _state.value.copy(cipherTests = CryptoTester.runAllCipherTests())
        }
    }
    
    fun runKdfTests() {
        viewModelScope.launch {
            _state.value = _state.value.copy(kdfTests = CryptoTester.runAllKdfTests())
        }
    }
    
    fun runSignTests() {
        viewModelScope.launch {
            _state.value = _state.value.copy(signTests = CryptoTester.runAllSignTests())
        }
    }
    
    fun runUtilsTests() {
        viewModelScope.launch {
            _state.value = _state.value.copy(utilsTests = CryptoTester.runAllUtilsTests())
        }
    }
}
