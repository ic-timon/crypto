package mobi.timon.android.util

import mobi.timon.android.ui.components.TestStatus
import mobi.timon.crypto.Aead
import mobi.timon.crypto.Bls
import mobi.timon.crypto.Cbc
import mobi.timon.crypto.Codec
import mobi.timon.crypto.Ecdsa
import mobi.timon.crypto.Ed25519
import mobi.timon.crypto.Hash
import mobi.timon.crypto.Hmac
import mobi.timon.crypto.Kdf
import mobi.timon.crypto.Random
import mobi.timon.crypto.Rsa
import mobi.timon.crypto.Secp256k1
import mobi.timon.crypto.Stream
import mobi.timon.crypto.Xts

data class TestResult(
    val name: String,
    val status: TestStatus,
    val output: String? = null,
    val error: String? = null,
    val durationMs: Long = 0
)

object CryptoTester {
    
    fun testSha1(): TestResult = runTest("SHA-1") {
        val data = "Hello, World!".toByteArray()
        val hash = Hash.sha1(data)
        val hex = Codec.toHex(hash)
        val expected = "0a0a9f2a6772942557ab5355d76af442f8f65e01"
        if (hex == expected) "Hash: $hex" else throw AssertionError("Expected $expected, got $hex")
    }
    
    fun testSha256(): TestResult = runTest("SHA-256") {
        val data = "Hello, World!".toByteArray()
        val hash = Hash.sha256(data)
        val hex = Codec.toHex(hash)
        val expected = "dffd6021bb2bd5b0af676290809ec3a53191dd81c7f70a4b28688a362182986f"
        if (hex == expected) "Hash: $hex" else throw AssertionError("Expected $expected, got $hex")
    }
    
    fun testSha512(): TestResult = runTest("SHA-512") {
        val data = "Hello, World!".toByteArray()
        val hash = Hash.sha512(data)
        val hex = Codec.toHex(hash)
        "Hash: $hex (64 bytes)"
    }
    
    fun testBlake2b256(): TestResult = runTest("BLAKE2b-256") {
        val data = "Hello, World!".toByteArray()
        val hash = Hash.blake2b256(data)
        val hex = Codec.toHex(hash)
        "Hash: $hex"
    }
    
    fun testMd5(): TestResult = runTest("MD5") {
        val data = "Hello, World!".toByteArray()
        val hash = Hash.md5(data)
        val hex = Codec.toHex(hash)
        val expected = "65a8e27d8879283831b664bd8b7f0ad4"
        if (hex == expected) "Hash: $hex" else throw AssertionError("Expected $expected, got $hex")
    }
    
    fun testHmacSha256(): TestResult = runTest("HMAC-SHA256") {
        val data = "Hello, World!".toByteArray()
        val key = "secret".toByteArray()
        val mac = Hmac.hmacSha256(data, key)
        val hex = Codec.toHex(mac)
        "HMAC: $hex"
    }
    
    fun testHmacSha512(): TestResult = runTest("HMAC-SHA512") {
        val data = "Hello, World!".toByteArray()
        val key = "secret".toByteArray()
        val mac = Hmac.hmacSha512(data, key)
        val hex = Codec.toHex(mac)
        "HMAC: $hex (64 bytes)"
    }
    
    fun testRipemd160(): TestResult = runTest("RIPEMD-160") {
        val data = "Hello, World!".toByteArray()
        val hash = Hash.ripemd160(data)
        val hex = Codec.toHex(hash)
        "Hash: $hex (20 bytes)"
    }
    
    fun testKeccak256(): TestResult = runTest("Keccak-256") {
        val data = "Hello, World!".toByteArray()
        val hash = Hash.keccak256(data)
        val hex = Codec.toHex(hash)
        "Hash: $hex (32 bytes)"
    }
    
    fun testKeccak512(): TestResult = runTest("Keccak-512") {
        val data = "Hello, World!".toByteArray()
        val hash = Hash.keccak512(data)
        val hex = Codec.toHex(hash)
        "Hash: $hex (64 bytes)"
    }
    
    fun testAesGcm(): TestResult = runTest("AES-GCM") {
        val plaintext = "Hello, World!".toByteArray()
        val key = Random.bytes(32)
        val encrypted = Aead.aesGcmEncrypt(plaintext, key)
        val decrypted = Aead.aesGcmDecrypt(encrypted, key)
        if (decrypted.contentEquals(plaintext)) {
            "Encrypt/Decrypt: OK (${encrypted.size} bytes ciphertext)"
        } else {
            throw AssertionError("Decryption failed")
        }
    }
    
    fun testChaCha20Poly1305(): TestResult = runTest("ChaCha20-Poly1305") {
        val plaintext = "Hello, World!".toByteArray()
        val key = Random.bytes(32)
        val encrypted = Aead.chacha20Poly1305Encrypt(plaintext, key)
        val decrypted = Aead.chacha20Poly1305Decrypt(encrypted, key)
        if (decrypted.contentEquals(plaintext)) {
            "Encrypt/Decrypt: OK (${encrypted.size} bytes ciphertext)"
        } else {
            throw AssertionError("Decryption failed")
        }
    }
    
    fun testAesCbc(): TestResult = runTest("AES-CBC") {
        val plaintext = "Hello, World!".toByteArray()
        val key = Random.bytes(16)
        val encrypted = Cbc.aesCbcEncrypt(plaintext, key)
        val decrypted = Cbc.aesCbcDecrypt(encrypted, key)
        if (decrypted.contentEquals(plaintext)) {
            "Encrypt/Decrypt: OK (${encrypted.size} bytes ciphertext)"
        } else {
            throw AssertionError("Decryption failed")
        }
    }
    
    fun testDesCbc(): TestResult = runTest("DES-CBC") {
        val plaintext = "Hello, World!".toByteArray()
        val key = Random.bytes(8)
        val encrypted = Cbc.desCbcEncrypt(plaintext, key)
        val decrypted = Cbc.desCbcDecrypt(encrypted, key)
        if (decrypted.contentEquals(plaintext)) {
            "Encrypt/Decrypt: OK (${encrypted.size} bytes ciphertext)"
        } else {
            throw AssertionError("Decryption failed")
        }
    }
    
    fun testAesCtr(): TestResult = runTest("AES-CTR") {
        val plaintext = "Hello, World!".toByteArray()
        val key = Random.bytes(16)
        val encrypted = Stream.aesCtrEncrypt(plaintext, key)
        val decrypted = Stream.aesCtrDecrypt(encrypted, key)
        if (decrypted.contentEquals(plaintext)) {
            "Encrypt/Decrypt: OK (${encrypted.size} bytes)"
        } else {
            throw AssertionError("Decryption failed")
        }
    }
    
    fun testChaCha20(): TestResult = runTest("ChaCha20") {
        val plaintext = "Hello, World!".toByteArray()
        val key = Random.bytes(32)
        val encrypted = Stream.chacha20Encrypt(plaintext, key)
        val decrypted = Stream.chacha20Decrypt(encrypted, key)
        if (decrypted.contentEquals(plaintext)) {
            "Encrypt/Decrypt: OK (${encrypted.size} bytes)"
        } else {
            throw AssertionError("Decryption failed")
        }
    }
    
    fun testAesXts(): TestResult = runTest("AES-XTS") {
        // XTS 块大小为 16 字节；明文长度必须为 16 的倍数，否则 Go 侧会 panic（已在 xts.go 中前置校验）。
        val plaintext = ByteArray(32) { (0x41 + it % 16).toByte() }
        val key = Random.bytes(32)
        val sectorNum = 0L
        val encrypted = Xts.aesXtsEncrypt(plaintext, key, sectorNum)
        val decrypted = Xts.aesXtsDecrypt(encrypted, key, sectorNum)
        if (decrypted.contentEquals(plaintext)) {
            "Encrypt/Decrypt: OK (${encrypted.size} bytes)"
        } else {
            throw AssertionError("Decryption failed")
        }
    }
    
    fun testBcrypt(): TestResult = runTest("Bcrypt") {
        val password = "password123".toByteArray()
        val hash = Kdf.bcryptHash(password, 10)
        val verified = Kdf.bcryptVerify(password, hash)
        if (verified) "Hash/Verify: OK" else throw AssertionError("Bcrypt verification failed")
    }
    
    fun testArgon2id(): TestResult = runTest("Argon2id") {
        val password = "password123".toByteArray()
        val salt = Random.bytes(16)
        val hash = Kdf.argon2idHash(password, salt, 1, 65536, 1, 32)
        "Hash: ${Codec.toHex(hash)} (32 bytes)"
    }
    
    fun testScrypt(): TestResult = runTest("Scrypt") {
        val password = "password123".toByteArray()
        val salt = Random.bytes(16)
        val key = Kdf.scrypt(password, salt, 32)
        "Key: ${Codec.toHex(key)} (32 bytes)"
    }
    
    fun testPbkdf2(): TestResult = runTest("PBKDF2") {
        val password = "password123".toByteArray()
        val salt = Random.bytes(16)
        val key = Kdf.pbkdf2(password, salt, 10000, 32)
        "Key: ${Codec.toHex(key)} (32 bytes)"
    }
    
    fun testHkdf(): TestResult = runTest("HKDF") {
        val ikm = Random.bytes(32)
        val salt = Random.bytes(16)
        val info = "context".toByteArray()
        val key = Kdf.hkdf(ikm, salt, info, 32)
        "Key: ${Codec.toHex(key)} (32 bytes)"
    }
    
    fun testEd25519(): TestResult = runTest("Ed25519") {
        val keyPair = Ed25519.generateKey()
        val publicKey = keyPair.sliceArray(0 until 32)
        val privateKey = keyPair.sliceArray(32 until 96)
        val message = "Hello, World!".toByteArray()
        val signature = Ed25519.sign(message, privateKey)
        val verified = Ed25519.verify(message, signature, publicKey)
        if (verified) "Sign/Verify: OK (${signature.size} bytes signature)" 
        else throw AssertionError("Ed25519 verification failed")
    }
    
    fun testEcdsa(): TestResult = runTest("ECDSA-P256") {
        val privateKey = Ecdsa.generateKey(256)
        val publicKey = Ecdsa.privateKeyToPublicKey(privateKey)
        val message = "Hello, World!".toByteArray()
        val signature = Ecdsa.sign(message, privateKey)
        val verified = Ecdsa.verify(message, signature, publicKey)
        if (verified) "Sign/Verify: OK" else throw AssertionError("ECDSA verification failed")
    }
    
    fun testRsa(): TestResult = runTest("RSA-2048") {
        val keyPair = Rsa.generateKey(2048)
        val privateKey = keyPair
        val publicKey = Rsa.privateKeyToPublicKey(privateKey)
        val message = "Hello!".toByteArray()
        val encrypted = Rsa.encrypt(message, publicKey)
        val decrypted = Rsa.decrypt(encrypted, privateKey)
        if (decrypted.contentEquals(message)) {
            "Encrypt/Decrypt: OK (${encrypted.size} bytes ciphertext)"
        } else {
            throw AssertionError("RSA decryption failed")
        }
    }
    
    fun testSecp256k1(): TestResult = runTest("ECDSA-secp256k1") {
        val privateKey = Secp256k1.generateKey()
        val publicKey = Secp256k1.privateKeyToPublicKey(privateKey, true)
        val message = "Hello, World!".toByteArray()
        val signature = Secp256k1.sign(message, privateKey)
        val verified = Secp256k1.verify(message, signature, publicKey)
        if (verified) "Sign/Verify: OK (${signature.size} bytes signature)" 
        else throw AssertionError("secp256k1 verification failed")
    }
    
    fun testSchnorr(): TestResult = runTest("Schnorr (secp256k1)") {
        val privateKey = Secp256k1.generateKey()
        val publicKey = Secp256k1.schnorrPrivateKeyToPublicKey(privateKey)
        val message = "Hello, World!".toByteArray()
        val signature = Secp256k1.schnorrSign(message, privateKey)
        val verified = Secp256k1.schnorrVerify(message, signature, publicKey)
        if (verified) "Sign/Verify: OK (${signature.size} bytes signature)" 
        else throw AssertionError("Schnorr verification failed")
    }
    
    fun testBls(): TestResult = runTest("BLS12-381") {
        val privateKey = Bls.generateKey()
        val publicKey = Bls.privateKeyToPublicKey(privateKey)
        val message = "Hello, World!".toByteArray()
        val signature = Bls.sign(message, privateKey)
        val verified = Bls.verify(message, signature, publicKey)
        if (verified) "Sign/Verify: OK (${signature.size} bytes signature)" 
        else throw AssertionError("BLS verification failed")
    }
    
    fun testBlsAggregation(): TestResult = runTest("BLS Aggregation") {
        val sk1 = Bls.generateKey()
        val sk2 = Bls.generateKey()
        val pk1 = Bls.privateKeyToPublicKey(sk1)
        val pk2 = Bls.privateKeyToPublicKey(sk2)
        val message = "Same message".toByteArray()
        
        val sig1 = Bls.sign(message, sk1)
        val sig2 = Bls.sign(message, sk2)
        
        val aggSig = Bls.aggregateSignatures(sig1 + sig2, 2)
        val aggPk = Bls.aggregatePublicKeys(pk1 + pk2, 2)
        
        val verified = Bls.verify(message, aggSig, aggPk)
        if (verified) "Aggregate Sign/Verify: OK" 
        else throw AssertionError("BLS aggregation verification failed")
    }
    
    fun testRandomBytes(): TestResult = runTest("Random Bytes") {
        val bytes1 = Random.bytes(32)
        val bytes2 = Random.bytes(32)
        if (bytes1.size == 32 && bytes2.size == 32 && !bytes1.contentEquals(bytes2)) {
            "Generated 32 random bytes: ${Codec.toHex(bytes1)}"
        } else {
            throw AssertionError("Random generation failed")
        }
    }
    
    fun testCodecHex(): TestResult = runTest("Hex Codec") {
        val data = "Hello".toByteArray()
        val hex = Codec.toHex(data)
        val decoded = Codec.fromHex(hex)
        if (decoded.contentEquals(data)) "Encode/Decode: $hex" 
        else throw AssertionError("Hex codec failed")
    }
    
    fun testCodecBase64(): TestResult = runTest("Base64 Codec") {
        val data = "Hello, World!".toByteArray()
        val base64 = Codec.toBase64(data)
        val decoded = Codec.fromBase64(base64)
        if (decoded.contentEquals(data)) "Encode/Decode: $base64" 
        else throw AssertionError("Base64 codec failed")
    }
    
    fun runAllHashTests(): List<TestResult> = listOf(
        testSha1(),
        testSha256(),
        testSha512(),
        testBlake2b256(),
        testMd5(),
        testRipemd160(),
        testKeccak256(),
        testKeccak512(),
        testHmacSha256(),
        testHmacSha512()
    )
    
    fun runAllCipherTests(): List<TestResult> = listOf(
        testAesGcm(),
        testChaCha20Poly1305(),
        testAesCbc(),
        testDesCbc(),
        testAesCtr(),
        testChaCha20(),
        testAesXts()
    )
    
    fun runAllKdfTests(): List<TestResult> = listOf(
        testBcrypt(),
        testArgon2id(),
        testScrypt(),
        testPbkdf2(),
        testHkdf()
    )
    
    fun runAllSignTests(): List<TestResult> = listOf(
        testEd25519(),
        testEcdsa(),
        testSecp256k1(),
        testSchnorr(),
        testBls(),
        testBlsAggregation(),
        testRsa()
    )
    
    fun runAllUtilsTests(): List<TestResult> = listOf(
        testRandomBytes(),
        testCodecHex(),
        testCodecBase64()
    )
    
    fun runAllTests(): List<TestResult> = runAllHashTests() + 
        runAllCipherTests() + 
        runAllKdfTests() + 
        runAllSignTests() + 
        runAllUtilsTests()
    
    private inline fun runTest(name: String, block: () -> String): TestResult {
        val startTime = System.currentTimeMillis()
        return try {
            val output = block()
            val duration = System.currentTimeMillis() - startTime
            TestResult(name, TestStatus.SUCCESS, output, durationMs = duration)
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            TestResult(name, TestStatus.FAILURE, error = e.message, durationMs = duration)
        }
    }
}
