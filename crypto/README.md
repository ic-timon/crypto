# Enc — 跨平台密码学库（`mobi.timon.crypto`）

基于 **Kotlin Multiplatform + Rust**：Kotlin API（commonMain expect）→ Android JNI / Apple cinterop → Rust 加密核心（`rust/` crate，RustCrypto 生态）。

## 架构

```
Kotlin (commonMain expect object)
    ↓ androidMain: JNI          ↓ appleMain: cinterop
Rust (#[no_mangle] extern "C")  ← 同一份实现
```

- Android 加载见 androidMain `Enc.kt`：`System.loadLibrary("encrust")`。
- Apple 平台由 cinterop 自动链接 `libencrust.a`（`.def` 见 `src/nativeInterop/cinterop/`）。
- Rust FFI 变更后需 cbindgen 重新生成 `encrust.h`（见根目录 CONTRIBUTING）。

## 功能一览

| 门面 | 说明 | 状态 |
|------|------|------|
| **Enc** | Android 原生库加载入口 | 已实现 |
| **Hash** | SHA-1 / SHA-256 / SHA-384 / SHA-512 / SHA-512-256 | 已实现（native） |
| **Hash** | `blake2b256` / `md5` | 已实现（native）；**MD5 仅兼容旧协议**，新设计请用 SHA-256 等 |
| **Hash** | `ripemd160` / `keccak256` / `keccak512` | 已实现（native） |
| **Hmac** | HMAC-SHA256 / SHA-512 / SHA-1 | 已实现（native） |
| **Random** | `bytes` | 已实现（native） |
| **Random** | `int` / `long` | 已实现（纯 Kotlin；`long` 为半开区间 `[min, max)`，需 `max > min`） |
| **Codec** | Hex、Base64 | 已实现（纯 Kotlin，KMP 兼容） |
| **Aead** | AES-GCM、ChaCha20-Poly1305 | 已实现（native） |
| **Cbc** | AES-CBC、DES-CBC（PKCS7） | 已实现（native）；**DES 仅兼容旧系统** |
| **Stream** | AES-CTR、ChaCha20 | 已实现（native，无 MAC——需认证请用 Aead） |
| **Xts** | AES-XTS | 已实现（native，磁盘加密用） |
| **Kdf** | bcrypt / Argon2id / scrypt / PBKDF2 / HKDF | 已实现（native） |
| **Ed25519** | generateKey / sign / verify | 已实现（native） |
| **Secp256k1** | ECDSA + BIP-340 Schnorr | 已实现（native）；sign/verify 统一 65B compact（v2.0 修复 DER 不一致） |
| **Rsa** | 2048/3072/4096，OAEP-SHA256 / PKCS1v15-SHA256 | 已实现（native） |
| **Ecdsa** | P-256 / P-384 | 已实现（native；P-224/P-521 暂不支持） |
| **Bls** | BLS12-381（pk G1 48B / sig G2 96B） | 已实现（native） |

## 与 v1（Go 版）的差异

| 项 | v1 (Go) | v2 (Rust) |
|----|---------|-----------|
| 平台 | 仅 Android | Android + iOS/macOS/tvOS/watchOS |
| ECDSA 私钥格式 | SEC1 DER | PKCS#8 DER（**breaking**） |
| Secp256k1 verify | 解析 DER（与 sign 的 compact 输出不兼容，bug） | 解析 compact 65B/64B（**修复**） |
| P-224 / P-521 | 支持 | 暂不支持 |
| BLS aggregate | 支持 | 支持（blst） |

## 构建

```bash
# Android AAR（自动 cargo-ndk 编 4 ABI）
./gradlew :crypto:assembleDebug

# Rust 单测
cd ../rust && cargo test

# Apple target
./gradlew :crypto:compileKotlinIosArm64
```

## 测试

```bash
# Android instrumented（需模拟器/真机）
./gradlew :crypto:connectedDebugAndroidTest
```
