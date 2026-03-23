# crypto

[![Release](https://img.shields.io/github/v/release/ic-timon/crypto?include_prereleases)](https://github.com/ic-timon/crypto/releases)
[![License](https://img.shields.io/github/license/ic-timon/crypto)](LICENSE)
[![Go](https://img.shields.io/badge/Go-1.26+-00ADD8?logo=go)](https://go.dev/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-33%2B-3DDC84?logo=android)](https://developer.android.com/)
[![CI](https://github.com/ic-timon/crypto/actions/workflows/release.yml/badge.svg)](https://github.com/ic-timon/crypto/actions/workflows/release.yml)

Android 原生密码学库，对外暴露 Kotlin API，内部通过 **Kotlin → JNI（C）→ Go** 调用链实现，依赖 Go 标准库与 `golang.org/x/crypto`。

**English:** [README.md](README.md)

---

## 目录

- [安装](#安装)
- [架构](#架构)
- [API 一览](#api-一览)
- [使用示例](#使用示例)
- [密码学格式与安全提示](#密码学格式与安全提示)
- [维护指南](#维护指南)
- [本地构建](#本地构建)
- [测试](#测试)
- [目录结构](#目录结构)

---

## 安装

`crypto` 已发布到 GitHub Packages。

### 1. 配置仓库

在 `settings.gradle.kts` 中添加：

```kotlin
repositories {
    google()
    mavenCentral()
    maven { url = uri("https://maven.pkg.github.com/ic-timon/crypto") }
}
```

### 2. 添加依赖

```kotlin
implementation("io.github.ic-timon.crypto:crypto:1.1.0")
```

### 3. 认证配置

GitHub Packages 需要认证。在 `local.properties` 中添加：

```properties
gpr.user=你的GitHub用户名
gpr.token=你的GitHub Personal Access Token (read:packages权限)
```

---

## 架构

```
Kotlin (object / external)
    ↓ JNI
C (enc_jni.c)
    ↓ CGO
Go (//export)
```

- 加载顺序见 `Enc.kt`：先 `encgo`，再 `encjni`。
- 编译 Go 会生成各 ABI 的 `libencgo.so` 与 `libencgo.h`，CMake 按 ABI 链接。

---

## API 一览

失败时统一抛出 `mobi.timon.crypto.EncException`。

| 门面          | 能力                                           |
|-------------|----------------------------------------------|
| **Hash**    | SHA-1 / SHA-256 / SHA-512 / blake2b256 / MD5 / RIPEMD-160 / Keccak-256 / Keccak-512 |
| **Hmac**    | HMAC-SHA256 / HMAC-SHA512                    |
| **Random**  | CSPRNG bytes / int / long                    |
| **Codec**   | Hex / Base64                                 |
| **Aead**    | AES-GCM / ChaCha20-Poly1305                  |
| **Cbc**     | AES-CBC / DES-CBC（PKCS7）                     |
| **Stream**  | AES-CTR / ChaCha20                           |
| **Xts**     | AES-XTS                                      |
| **Kdf**     | bcrypt / Argon2id / scrypt / PBKDF2 / HKDF   |
| **Rsa**     | 密钥生成 / OAEP / PKCS#1 v1.5 签名验签               |
| **Ecdsa**   | P-224 / P-256 / P-384 / P-521                |
| **Secp256k1** | 密钥生成 / ECDSA 签名验签 / 公钥恢复 / Schnorr 签名验签 |
| **Bls**     | BLS12-381：密钥生成、签名验签、签名聚合、公钥聚合 |
| **Ed25519** | 密钥生成 / 签名 / 验签                               |

---

## 使用示例

```kotlin
import mobi.timon.crypto.*

// Hash
val digest = Hash.sha256("hello".toByteArray())
println(Codec.toHex(digest))

val keccak = Hash.keccak256("hello".toByteArray())
val ripe = Hash.ripemd160("hello".toByteArray())

// AES-GCM
val key = Random.bytes(32)
val ct = Aead.aesGcmEncrypt("secret".toByteArray(), key)
val pt = Aead.aesGcmDecrypt(ct, key)

// bcrypt
val bcryptHash = Kdf.bcryptHash("password".toByteArray(), cost = 10)
val ok = Kdf.bcryptVerify("password".toByteArray(), bcryptHash)

// secp256k1 ECDSA
val sk = Secp256k1.generateKey()
val pk = Secp256k1.privateKeyToPublicKey(sk, true)
val sig = Secp256k1.sign("message".toByteArray(), sk)
val valid = Secp256k1.verify("message".toByteArray(), sig, pk)

// Schnorr (secp256k1)
val schnorrPk = Secp256k1.schnorrPrivateKeyToPublicKey(sk)
val schnorrSig = Secp256k1.schnorrSign("message".toByteArray(), sk)
val schnorrValid = Secp256k1.schnorrVerify("message".toByteArray(), schnorrSig, schnorrPk)

// BLS12-381
val blsSk = Bls.generateKey()
val blsPk = Bls.privateKeyToPublicKey(blsSk)
val blsSig = Bls.sign("message".toByteArray(), blsSk)
val blsValid = Bls.verify("message".toByteArray(), blsSig, blsPk)
```

---

## 密码学格式与安全提示

### 对称加密

**AES-GCM / ChaCha20-Poly1305**：输出格式 `nonce(12字节) + ciphertext + tag(16字节)`，nonce 和 tag 自动拼接返回，解密时无需单独传入。

**AES-CBC**：输出格式 `iv(16字节) + ciphertext`，IV 随机生成并拼在密文前，解密时自动提取。使用 PKCS7 填充。密钥长度 16/24/32 字节（AES-128/192/256）。

**DES-CBC**：输出格式 `iv(8字节) + ciphertext`，密钥长度 8 字节。**仅兼容旧系统**。

**AES-CTR / ChaCha20**：nonce 与密文拼接方式由实现固定，**无内置完整性认证**，需谨慎使用。

**AES-XTS**：密钥 32 或 64 字节；明文长度建议为 16 字节的倍数；`sectorNum` 参与扇区/tweak。

### 非对称加密与签名

**RSA**：密钥为 **DER 字节**（PKCS#8 / PKIX），非 PEM 字符串。

**Ed25519**：密钥对序列化为一整个 96 字节数组，前 32 字节是公钥，后 64 字节是私钥（种子+公钥）。

**secp256k1**：
- `generateKey()` 返回 32 字节私钥
- `sign()` 返回 65 字节签名：`r(32) ‖ s(32) ‖ recoveryId(1)`
- `privateKeyToPublicKey(privateKey, compressed)`：`compressed=true` 返回 33 字节压缩公钥，`false` 返回 65 字节未压缩公钥

**Schnorr (secp256k1)**：
- `schnorrSign()` 返回 64 字节签名：`r(32) ‖ s(32)`
- `schnorrPrivateKeyToPublicKey()` 返回 32 字节 x-only 公钥

**BLS12-381**：
- 私钥 32 字节，公钥 48 字节（G1 点压缩）
- 签名 96 字节（G2 点压缩）
- 聚合签名仍为 96 字节，聚合公钥仍为 48 字节

### 密钥派生

**bcrypt**：`bcryptHash` 返回的哈希与 `bcryptVerify` 的 `hash` 均为 `ByteArray`（非字符串）。

**scrypt**：Go 侧 N、r、p 为固定参数，Kotlin 仅暴露 `keyLen`。

### 安全提示

> **弱算法**：MD5、SHA-1 存在碰撞攻击风险，DES 密钥仅 56 位可被暴力破解。仅建议在兼容旧协议时使用。

---

## 维护指南

修改 native 行为时，请同时核对：

1. `src/main/java/mobi/timon/crypto/*.kt` 中的 **`external fun`**
2. `src/main/cpp/enc_jni.c` 中的 **`Java_mobi_timon_crypto_*`**
3. `src/main/go/*.go` 中的 **`//export`** 与构建产物中的 **`libencgo.h`**

Go 返回的 C 分配缓冲区在 JNI 中经 `FreeBytes` 释放；返回布尔类的 Verify 路径使用统一 `verifyBoolResult`。空长度输入在哈希/HMAC 等 API 中的语义以 Go 实现为准。

---

## 本地构建

预编译的 `.so` 文件已包含在仓库中，可直接使用。如需自行编译：

**前置要求**：Go 1.26+、Android NDK，且 `local.properties` 中 `sdk.dir` 正确。

```bash
# 仅编译 Go（生成各 ABI 的 libencgo.so）
./gradlew :crypto:compileGoDebug

# 完整构建
./gradlew :crypto:assembleRelease
```

---

## 测试

```bash
./gradlew :crypto:connectedDebugAndroidTest
```

---

## 目录结构

```
crypto/
├── src/main/java/mobi/timon/crypto/   # Kotlin API
├── src/main/cpp/                       # JNI 桥接
├── src/main/go/                        # Go 实现
└── src/androidTest/                    # 仪器化测试
```

---

## 参考链接

- [Go crypto](https://pkg.go.dev/crypto)
- [golang.org/x/crypto](https://pkg.go.dev/golang.org/x/crypto)
- [btcd/btcec/v2](https://pkg.go.dev/github.com/btcsuite/btcd/btcec/v2) - secp256k1 / Schnorr
- [kilic/bls12-381](https://pkg.go.dev/github.com/kilic/bls12-381) - BLS12-381

---

## License

[MIT](LICENSE)
