package mobi.timon.android.ui.screens.sign

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mobi.timon.android.ui.components.TestStatus
import mobi.timon.android.util.TestResult
import mobi.timon.crypto.Bls
import mobi.timon.crypto.Codec
import mobi.timon.crypto.Ecdsa
import mobi.timon.crypto.Ed25519
import mobi.timon.crypto.Rsa
import mobi.timon.crypto.Secp256k1

enum class SignAlgorithm(val displayName: String) {
    ED25519("Ed25519"),
    ECDSA_P256("ECDSA-P256"),
    ECDSA_SECP256K1("ECDSA-secp256k1"),
    SCHNORR("Schnorr (secp256k1)"),
    BLS("BLS12-381"),
    RSA_2048("RSA-2048")
}

data class SignState(
    val message: String = "Hello, World!",
    val privateKeyHex: String = "",
    val publicKeyHex: String = "",
    val signatureHex: String = "",
    val ciphertextHex: String = "",
    val selectedAlgorithm: SignAlgorithm = SignAlgorithm.ED25519,
    val result: String? = null,
    val verifyResult: Boolean? = null,
    val error: String? = null,
    val testResults: List<TestResult> = emptyList(),
    val isRunning: Boolean = false
)

class SignViewModel : ViewModel() {

    private val _state = MutableStateFlow(SignState())
    val state: StateFlow<SignState> = _state.asStateFlow()

    fun updateMessage(message: String) {
        _state.value = _state.value.copy(message = message, result = null, error = null)
    }

    fun updatePrivateKey(hex: String) {
        _state.value = _state.value.copy(privateKeyHex = hex, result = null, error = null)
    }

    fun updatePublicKey(hex: String) {
        _state.value = _state.value.copy(publicKeyHex = hex, result = null, error = null)
    }

    fun updateSignature(hex: String) {
        _state.value = _state.value.copy(signatureHex = hex, result = null, error = null)
    }

    fun updateCiphertext(hex: String) {
        _state.value = _state.value.copy(ciphertextHex = hex, result = null, error = null)
    }

    fun selectAlgorithm(algorithm: SignAlgorithm) {
        _state.value = _state.value.copy(
            selectedAlgorithm = algorithm,
            result = null,
            error = null,
            verifyResult = null,
            privateKeyHex = "",
            publicKeyHex = ""
        )
    }

    fun generateKeyPair() {
        val currentState = _state.value

        try {
            when (currentState.selectedAlgorithm) {
                SignAlgorithm.ED25519 -> {
                    val keyPair = Ed25519.generateKey()
                    val publicKey = keyPair.sliceArray(0 until 32)
                    val privateKey = keyPair.sliceArray(32 until 96)
                    _state.value = currentState.copy(
                        publicKeyHex = Codec.toHex(publicKey),
                        privateKeyHex = Codec.toHex(privateKey),
                        error = null
                    )
                }
                SignAlgorithm.ECDSA_P256 -> {
                    val privateKey = Ecdsa.generateKey(256)
                    val publicKey = Ecdsa.privateKeyToPublicKey(privateKey)
                    _state.value = currentState.copy(
                        privateKeyHex = Codec.toHex(privateKey),
                        publicKeyHex = Codec.toHex(publicKey),
                        error = null
                    )
                }
                SignAlgorithm.ECDSA_SECP256K1 -> {
                    val privateKey = Secp256k1.generateKey()
                    val publicKey = Secp256k1.privateKeyToPublicKey(privateKey, true)
                    _state.value = currentState.copy(
                        privateKeyHex = Codec.toHex(privateKey),
                        publicKeyHex = Codec.toHex(publicKey),
                        error = null
                    )
                }
                SignAlgorithm.SCHNORR -> {
                    val privateKey = Secp256k1.generateKey()
                    val publicKey = Secp256k1.schnorrPrivateKeyToPublicKey(privateKey)
                    _state.value = currentState.copy(
                        privateKeyHex = Codec.toHex(privateKey),
                        publicKeyHex = Codec.toHex(publicKey),
                        error = null
                    )
                }
                SignAlgorithm.BLS -> {
                    val privateKey = Bls.generateKey()
                    val publicKey = Bls.privateKeyToPublicKey(privateKey)
                    _state.value = currentState.copy(
                        privateKeyHex = Codec.toHex(privateKey),
                        publicKeyHex = Codec.toHex(publicKey),
                        error = null
                    )
                }
                SignAlgorithm.RSA_2048 -> {
                    val privateKey = Rsa.generateKey(2048)
                    val publicKey = Rsa.privateKeyToPublicKey(privateKey)
                    _state.value = currentState.copy(
                        privateKeyHex = Codec.toBase64(privateKey),
                        publicKeyHex = Codec.toBase64(publicKey),
                        error = null
                    )
                }
            }
        } catch (e: Exception) {
            _state.value = currentState.copy(error = e.message)
        }
    }

    fun sign() {
        val currentState = _state.value

        try {
            val message = currentState.message.toByteArray()

            when (currentState.selectedAlgorithm) {
                SignAlgorithm.ED25519 -> {
                    val privateKey = Codec.fromHex(currentState.privateKeyHex)
                    val signature = Ed25519.sign(message, privateKey)
                    _state.value = currentState.copy(
                        signatureHex = Codec.toHex(signature),
                        result = "Signed: ${signature.size} bytes",
                        error = null
                    )
                }
                SignAlgorithm.ECDSA_P256 -> {
                    val privateKey = Codec.fromHex(currentState.privateKeyHex)
                    val signature = Ecdsa.sign(message, privateKey)
                    _state.value = currentState.copy(
                        signatureHex = Codec.toHex(signature),
                        result = "Signed: ${signature.size} bytes",
                        error = null
                    )
                }
                SignAlgorithm.ECDSA_SECP256K1 -> {
                    val privateKey = Codec.fromHex(currentState.privateKeyHex)
                    val signature = Secp256k1.sign(message, privateKey)
                    _state.value = currentState.copy(
                        signatureHex = Codec.toHex(signature),
                        result = "Signed: ${signature.size} bytes",
                        error = null
                    )
                }
                SignAlgorithm.SCHNORR -> {
                    val privateKey = Codec.fromHex(currentState.privateKeyHex)
                    val signature = Secp256k1.schnorrSign(message, privateKey)
                    _state.value = currentState.copy(
                        signatureHex = Codec.toHex(signature),
                        result = "Signed: ${signature.size} bytes",
                        error = null
                    )
                }
                SignAlgorithm.BLS -> {
                    val privateKey = Codec.fromHex(currentState.privateKeyHex)
                    val signature = Bls.sign(message, privateKey)
                    _state.value = currentState.copy(
                        signatureHex = Codec.toHex(signature),
                        result = "Signed: ${signature.size} bytes",
                        error = null
                    )
                }
                SignAlgorithm.RSA_2048 -> {
                    val privateKey = Codec.fromBase64(currentState.privateKeyHex)
                    val signature = Rsa.sign(message, privateKey)
                    _state.value = currentState.copy(
                        signatureHex = Codec.toBase64(signature),
                        result = "Signed: ${signature.size} bytes",
                        error = null
                    )
                }
            }
        } catch (e: Exception) {
            _state.value = currentState.copy(error = e.message)
        }
    }

    fun verify() {
        val currentState = _state.value

        try {
            val message = currentState.message.toByteArray()

            val verified = when (currentState.selectedAlgorithm) {
                SignAlgorithm.ED25519 -> {
                    val publicKey = Codec.fromHex(currentState.publicKeyHex)
                    val signature = Codec.fromHex(currentState.signatureHex)
                    Ed25519.verify(message, signature, publicKey)
                }
                SignAlgorithm.ECDSA_P256 -> {
                    val publicKey = Codec.fromHex(currentState.publicKeyHex)
                    val signature = Codec.fromHex(currentState.signatureHex)
                    Ecdsa.verify(message, signature, publicKey)
                }
                SignAlgorithm.ECDSA_SECP256K1 -> {
                    val publicKey = Codec.fromHex(currentState.publicKeyHex)
                    val signature = Codec.fromHex(currentState.signatureHex)
                    Secp256k1.verify(message, signature, publicKey)
                }
                SignAlgorithm.SCHNORR -> {
                    val publicKey = Codec.fromHex(currentState.publicKeyHex)
                    val signature = Codec.fromHex(currentState.signatureHex)
                    Secp256k1.schnorrVerify(message, signature, publicKey)
                }
                SignAlgorithm.BLS -> {
                    val publicKey = Codec.fromHex(currentState.publicKeyHex)
                    val signature = Codec.fromHex(currentState.signatureHex)
                    Bls.verify(message, signature, publicKey)
                }
                SignAlgorithm.RSA_2048 -> {
                    val publicKey = Codec.fromBase64(currentState.publicKeyHex)
                    val signature = Codec.fromBase64(currentState.signatureHex)
                    Rsa.verify(message, signature, publicKey)
                }
            }

            _state.value = currentState.copy(verifyResult = verified, error = null)
        } catch (e: Exception) {
            _state.value = currentState.copy(error = e.message)
        }
    }

    fun encrypt() {
        val currentState = _state.value

        if (currentState.selectedAlgorithm != SignAlgorithm.RSA_2048) {
            _state.value = currentState.copy(error = "Encryption only supported for RSA")
            return
        }

        try {
            val message = currentState.message.toByteArray()
            val publicKey = Codec.fromBase64(currentState.publicKeyHex)
            val encrypted = Rsa.encrypt(message, publicKey)
            _state.value = currentState.copy(
                ciphertextHex = Codec.toBase64(encrypted),
                result = "Encrypted: ${encrypted.size} bytes",
                error = null
            )
        } catch (e: Exception) {
            _state.value = currentState.copy(error = e.message)
        }
    }

    fun decrypt() {
        val currentState = _state.value

        if (currentState.selectedAlgorithm != SignAlgorithm.RSA_2048) {
            _state.value = currentState.copy(error = "Decryption only supported for RSA")
            return
        }

        try {
            val ciphertext = Codec.fromBase64(currentState.ciphertextHex)
            val privateKey = Codec.fromBase64(currentState.privateKeyHex)
            val decrypted = Rsa.decrypt(ciphertext, privateKey)
            _state.value = currentState.copy(
                result = "Decrypted: ${String(decrypted)}",
                error = null
            )
        } catch (e: Exception) {
            _state.value = currentState.copy(error = e.message)
        }
    }

    fun runAllTests() {
        _state.value = _state.value.copy(isRunning = true)

        val results = mutableListOf<TestResult>()

        results.add(runSignTest("Ed25519") {
            val keyPair = Ed25519.generateKey()
            val publicKey = keyPair.sliceArray(0 until 32)
            val privateKey = keyPair.sliceArray(32 until 96)
            val message = "test".toByteArray()
            val signature = Ed25519.sign(message, privateKey)
            if (Ed25519.verify(message, signature, publicKey)) "OK" else throw Exception("Verify failed")
        })

        results.add(runSignTest("ECDSA-P256") {
            val privateKey = Ecdsa.generateKey(256)
            val publicKey = Ecdsa.privateKeyToPublicKey(privateKey)
            val message = "test".toByteArray()
            val signature = Ecdsa.sign(message, privateKey)
            if (Ecdsa.verify(message, signature, publicKey)) "OK" else throw Exception("Verify failed")
        })

        results.add(runSignTest("ECDSA-secp256k1") {
            val privateKey = Secp256k1.generateKey()
            val publicKey = Secp256k1.privateKeyToPublicKey(privateKey, true)
            val message = "test".toByteArray()
            val signature = Secp256k1.sign(message, privateKey)
            if (Secp256k1.verify(message, signature, publicKey)) "OK" else throw Exception("Verify failed")
        })

        results.add(runSignTest("Schnorr") {
            val privateKey = Secp256k1.generateKey()
            val publicKey = Secp256k1.schnorrPrivateKeyToPublicKey(privateKey)
            val message = "test".toByteArray()
            val signature = Secp256k1.schnorrSign(message, privateKey)
            if (Secp256k1.schnorrVerify(message, signature, publicKey)) "OK" else throw Exception("Verify failed")
        })

        results.add(runSignTest("BLS12-381") {
            val privateKey = Bls.generateKey()
            val publicKey = Bls.privateKeyToPublicKey(privateKey)
            val message = "test".toByteArray()
            val signature = Bls.sign(message, privateKey)
            if (Bls.verify(message, signature, publicKey)) "OK" else throw Exception("Verify failed")
        })

        results.add(runSignTest("RSA-2048") {
            val privateKey = Rsa.generateKey(2048)
            val publicKey = Rsa.privateKeyToPublicKey(privateKey)
            val message = "test".toByteArray()
            val encrypted = Rsa.encrypt(message, publicKey)
            val decrypted = Rsa.decrypt(encrypted, privateKey)
            if (decrypted.contentEquals(message)) "OK" else throw Exception("Decrypt failed")
        })

        _state.value = _state.value.copy(testResults = results, isRunning = false)
    }

    private inline fun runSignTest(name: String, block: () -> String): TestResult {
        return try {
            val output = block()
            TestResult(name, TestStatus.SUCCESS, output)
        } catch (e: Exception) {
            TestResult(name, TestStatus.FAILURE, error = e.message)
        }
    }
}
