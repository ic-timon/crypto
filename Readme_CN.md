# crypto

[![Release](https://img.shields.io/github/v/release/ic-timon/crypto?include_prereleases)](https://github.com/ic-timon/crypto/releases)
[![License](https://img.shields.io/github/license/ic-timon/crypto)](LICENSE)
[![Rust](https://img.shields.io/badge/Rust-1.75+-CE422B?logo=rust)](https://www.rust-lang.org/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin)](https://kotlinlang.org/)
[![CI](https://github.com/ic-timon/crypto/actions/workflows/ci.yml/badge.svg)](https://github.com/ic-timon/crypto/actions/workflows/ci.yml)

跨平台密码学库，对外暴露 Kotlin API。内部采用 **Kotlin (KMP) → Rust** 架构 —— Rust 编译为原生静态/动态库，通过 JNI（Android）和 cinterop（Apple 平台）桥接。

底层依赖 RustCrypto 生态（`sha2` / `aes-gcm` / `secp256k1` / `ed25519-dalek` / `blst` 等），全部审计过的成熟实现。Rust 无 runtime，iOS cinterop 一等公民。

**English:** [README.md](README.md)

---

## 架构

```
┌─────────────────────────────────┐
│         Kotlin API (KMP)         │
│   mobi.timon.crypto.* (68 函数)   │
├────────────┬────────────────────┤
│ Android    │ Apple (iOS/macOS)  │
│ JNI        │ cinterop           │
├────────────┴────────────────────┤
│          Rust Core (encrust)     │
│   sha2/hmac/aes-gcm/secp256k1/  │
│   ed25519/rsa/ecdsa/bls12-381   │
│   argon2/bcrypt/scrypt/hkdf     │
└─────────────────────────────────┘
```

### 为什么是 Rust？

前身是 **Kotlin → JNI(C) → Go** 三层链。Go runtime（GC + goroutine）无法通过 Kotlin/Native cinterop 进 iOS，已验证崩溃。Rust 无 runtime，编译产物与 C 静态库行为一致，两端共用一套实现。

## 安装

`crypto` 已发布到 GitHub Packages。

### 1. 配置仓库

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/ic-timon/crypto")
            credentials {
                username = "<GitHub 用户名>"
                password = "<Personal Access Token (read:packages)>"
            }
        }
    }
}
```

### 2. 添加依赖

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.ic-timon.crypto:crypto:<version>")
}
```

## API 一览

| 模块 | 函数数 | 算法 |
|------|--------|------|
| Hash | 10 | SHA-1/256/384/512/512_256, BLAKE2b256, MD5, RIPEMD160, Keccak256/512 |
| Hmac | 3 | HMAC-SHA256/512/SHA1 |
| Aead | 4 | AES-GCM, ChaCha20-Poly1305 |
| Cbc | 4 | AES-CBC, DES-CBC (PKCS7) |
| Stream | 4 | AES-CTR, ChaCha20 |
| Xts | 2 | AES-XTS |
| Kdf | 6 | bcrypt, Argon2id, scrypt, PBKDF2, HKDF |
| Ed25519 | 3 | generateKey, sign, verify |
| Secp256k1 | 10 | ECDSA + BIP-340 Schnorr |
| Rsa | 6 | 2048/3072/4096, OAEP-SHA256, PKCS1v15-SHA256 |
| Ecdsa | 4 | P-256/384 |
| Bls | 6 | BLS12-381 (minimal-pubkey-size) |
| Random | 1 | CSPRNG |
| Codec | 6 | hex/base64/constantTimeEquals/wipe (纯 Kotlin) |

## 使用示例

```kotlin
import mobi.timon.crypto.Hash
import mobi.timon.crypto.Aead
import mobi.timon.crypto.Secp256k1

// SHA-256
val digest = Hash.sha256("hello".toByteArray())

// AES-256-GCM（nonce 自动生成并 prepend）
val key = Random.bytes(32)
val ciphertext = Aead.aesGcmEncrypt(plaintext, key)
val decrypted = Aead.aesGcmDecrypt(ciphertext, key)

// BIP-340 Schnorr（Nostr 签名）
val privKey = Secp256k1.generateKey()
val pubKey = Secp256k1.schnorrPrivateKeyToPublicKey(privKey)
val signature = Secp256k1.schnorrSign(message, privKey)
val valid = Secp256k1.schnorrVerify(message, signature, pubKey)
```

## 本地构建

### 前置依赖

- JDK 17+
- Rust toolchain (`rustup`)
- Android NDK（Android 构建）
- `cargo-ndk`：`cargo install cargo-ndk`
- Apple Xcode（Apple 平台构建，仅 macOS）

### 构建

```bash
# Rust core 测试
cd rust && cargo test

# Android 构建（自动调 cargo-ndk 构建 4 ABI）
./gradlew :crypto:assembleDebug

# Release（tag 触发 → GitHub Packages 发布）
git tag v2.0.0 && git push origin v2.0.0
```

## 目录结构

```
crypto/
├── rust/                    # Rust 加密核心
│   ├── Cargo.toml           # 依赖声明
│   ├── src/                 # 15 模块 + FFI 导出
│   ├── cbindgen.toml        # C 头文件生成配置
│   └── encrust.h            # cbindgen 生成的头文件（供 cinterop 用）
├── crypto/                  # KMP 模块
│   ├── build.gradle.kts     # KMP + cargo 构建任务
│   ├── src/commonMain/      # expect 声明 + 纯 Kotlin
│   ├── src/androidMain/     # JNI actual
│   └── src/nativeInterop/   # cinterop .def + encrust.h
├── app/                     # Demo app
└── .github/workflows/       # CI + Release
```

## 许可证

MIT
