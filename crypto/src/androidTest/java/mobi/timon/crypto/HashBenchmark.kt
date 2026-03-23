package mobi.timon.crypto

import androidx.benchmark.BenchmarkRule
import androidx.benchmark.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HashBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val context = InstrumentationRegistry.getInstrumentation().context

    @Test
    fun sha256_1mb_throughput() {
        val data = ByteArray(1024 * 1024) { it.toByte() }
        benchmarkRule.measureRepeated {
            Hash.sha256(data)
        }
    }

    @Test
    fun sha512_1mb_throughput() {
        val data = ByteArray(1024 * 1024) { it.toByte() }
        benchmarkRule.measureRepeated {
            Hash.sha512(data)
        }
    }

    @Test
    fun blake2b256_1mb_throughput() {
        val data = ByteArray(1024 * 1024) { it.toByte() }
        benchmarkRule.measureRepeated {
            Hash.blake2b256(data)
        }
    }

    @Test
    fun keccak256_1mb_throughput() {
        val data = ByteArray(1024 * 1024) { it.toByte() }
        benchmarkRule.measureRepeated {
            Hash.keccak256(data)
        }
    }
}
