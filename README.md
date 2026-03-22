# crypto

[![Release](https://img.shields.io/github/v/release/ic-timon/crypto?include_prereleases)](https://github.com/ic-timon/crypto/releases)
[![License](https://img.shields.io/github/license/ic-timon/crypto)](LICENSE)
[![Go](https://img.shields.io/badge/Go-1.26+-00ADD8?logo=go)](https://go.dev/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-33%2B-3DDC84?logo=android)](https://developer.android.com/)
[![CI](https://github.com/ic-timon/crypto/actions/workflows/release.yml/badge.svg)](https://github.com/ic-timon/crypto/actions/workflows/release.yml)

Android 原生密码学库，对外暴露 Kotlin API，内部通过 **Kotlin → JNI（C）→ Go** 调用链实现，依赖 Go 标准库与 `golang.org/x/crypto`。

---

## 目录

- [安装](#安装)
- [API 一览](#api-一览)
- [使用示例](#使用示例)
- [密码学格式与安全提示](#密码学格式与安全提示)
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
implementation("io.github.ic-timon.crypto:crypto:1.0.4")
```

### 3. 认证配置

GitHub Packages 需要认证。在 `local.properties` 中添加：

```properties
gpr.user=你的GitHub用户名
gpr.token=你的GitHub Personal Access Token (read:packages权限)
```

---

## API 一览

失败时统一抛出 `mobi.timon.crypto.EncException`。

| 门面          | 能力                                           |
|-------------|----------------------------------------------|
| **Hash**    | SHA-1 / SHA-256 / SHA-512 / blake2b256 / MD5 |
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
| **Ed25519** | 密钥生成 / 签名 / 验签                               |

---

## 使用示例

```kotlin
import mobi.timon.crypto.*

val digest = Hash.sha256("hello".toByteArray())
println(Codec.toHex(digest))

val key = Random.bytes(32)
val ct = Aead.aesGcmEncrypt("secret".toByteArray(), key)
val pt = Aead.aesGcmDecrypt(ct, key)

val bcryptHash = Kdf.bcryptHash("password".toByteArray(), cost = 10)
val ok = Kdf.bcryptVerify("password".toByteArray(), bcryptHash)
```

---

## 密码学格式与安全提示

| 算法 | 格式说明 |
|------|----------|
| AES-GCM / ChaCha20-Poly1305 | `nonce ‖ ciphertext`（含 tag） |
| AES-CBC | `iv(16) ‖ ciphertext`，PKCS7 |
| Ed25519 | 密钥对 96 字节：前 32 公钥，后 64 私钥 |

> **弱算法提示**：MD5、SHA-1、DES 仅建议在兼容旧协议时使用。

---

## 本地构建

需要 Go 1.26+ 和 Android NDK。

```bash
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

本项目是 CI 自动构建并发布到 GitHub Packages 与 Releases。

---

## License

[MIT](LICENSE)
