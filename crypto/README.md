# Enc — Android 密码学库（`mobi.timon.crypto`）

基于 **Go（CGO）+ NDK + CMake + JNI** 的 Android Library：Kotlin `external` → `enc_jni.c` → Go `crypto/*` 与 `golang.org/x/crypto/*`。

## 架构

```
Kotlin (object / external)
    ↓ JNI
C (enc_jni.c)
    ↓ CGO
Go (//export)
```

- 加载顺序见 `Enc.kt`：先 `encgo`，再 `encjni`。
- 编译 Go 会生成各 ABI 的 `libencgo.so` 与 `libencgo.h`（输出目录见下文 `go/lib/`），CMake 按 ABI 链接。

## 功能一览

| 门面 | 说明 | 状态 |
|------|------|------|
| **Enc** | `System.loadLibrary` 入口 | 已实现 |
| **Hash** | SHA-1 / SHA-256 / SHA-512 | 已实现（native） |
| **Hash** | `blake2b256` / `md5` | 已实现（native）；**MD5 仅兼容旧协议**，新设计请用 SHA-256 等 |
| **Hash** | `ripemd160` / `keccak256` / `keccak512` | 已实现（native） |
| **Hmac** | HMAC-SHA256 / HMAC-SHA512 | 已实现（native） |
| **Random** | `bytes` | 已实现（native） |
| **Random** | `int` / `long` | 已实现（纯 Kotlin；`long` 为半开区间 `[min, max)`，需 `max > min`） |
| **Codec** | Hex、Base64（`java.util.Base64`） | 已实现（纯 Kotlin） |
| **Aead** | AES-GCM、ChaCha20-Poly1305 | 已实现（native） |
| **Cbc** | AES-CBC（PKCS7） | 已实现（native） |
| **Cbc** | DES-CBC（PKCS7，**`iv(8) ‖ ciphertext`**，密钥 **8 字节**） | 已实现（native）；**DES 仅兼容场景**，新设计请用 AES |
| **Stream** | AES-CTR、ChaCha20（无独立 MAC） | 已实现（native） |
| **Xts** | AES-XTS | 已实现（native） |
| **Kdf** | bcrypt、Argon2id、scrypt、PBKDF2、HKDF | 已实现（native） |
| **Rsa** | 密钥生成、OAEP 加解密、PKCS#1 v1.5 签名/验签、公私钥派生 | 已实现（native）；密钥为 **DER 字节**（PKCS#8 / PKIX），非 PEM 字符串 |
| **Ecdsa** | P-224 / P-256 / P-384 / P-521，`curveBits` 取值 224/256/384/521 | 已实现（native） |
| **Secp256k1** | 密钥生成 / ECDSA 签名验签 / 公钥恢复 / Schnorr 签名验签 | 已实现（native） |
| **Bls** | BLS12-381：密钥生成、签名验签、签名聚合、公钥聚合 | 已实现（native） |
| **Ed25519** | `generateKey` / `sign` / `verify` | 已实现（native） |

失败统一抛出 **`EncException`**（含 native 与纯 Kotlin 占位）。

## 密码学格式约定

- **AES-GCM / ChaCha20-Poly1305**：密文为 **`nonce ‖ ciphertext`（含 Poly1305 tag）**；**不提供 AAD**。
- **AES-CBC**：**`iv(16) ‖ ciphertext`**，PKCS7 填充；密钥长度 16/24/32 字节（AES-128/192/256）。
- **DES-CBC**：**`iv(8) ‖ ciphertext`**，PKCS7 填充；密钥长度 **8 字节**。
- **AES-CTR / 纯 ChaCha20**：nonce 与密文的拼接方式由实现固定，解密端与加密端成对使用；**无内置完整性认证**。
- **AES-XTS**：密钥 **32 或 64 字节**；明文长度建议为 **16 字节的倍数**；`sectorNum` 参与扇区/tweak（见实现）。
- **Ed25519**：`generateKey()` 返回 **96 字节**：前 **32** 为公钥，后 **64** 为私钥；`sign` 需传入 **64 字节**私钥（通常为 `kp.copyOfRange(32, 96)`）。
- **secp256k1**：`generateKey()` 返回 **32 字节**私钥；`sign()` 返回 **65 字节**签名（`r ‖ s ‖ recoveryId`）；`privateKeyToPublicKey(privateKey, compressed)` 返回 **33 字节**压缩或 **65 字节**未压缩公钥。
- **Schnorr**：`schnorrSign()` 返回 **64 字节**签名；`schnorrPrivateKeyToPublicKey()` 返回 **32 字节** x-only 公钥。
- **BLS12-381**：私钥 **32 字节**；公钥 **48 字节**（G1 压缩）；签名 **96 字节**（G2 压缩）。
- **bcrypt**：`bcryptHash` 返回的哈希与 `bcryptVerify` 的 `hash` 均为 **`ByteArray`**（非字符串）。
- **Kdf.scrypt**：Go 侧 **N、r、p 为固定参数**，Kotlin 仅暴露 `keyLen`（详见 `kdf_advanced.go`）。

## 使用示例

```kotlin
import mobi.timon.crypto.*

// Hash
val digest = Hash.sha256("hello".toByteArray(Charsets.UTF_8))
println(Codec.toHex(digest))

val keccak = Hash.keccak256("hello".toByteArray(Charsets.UTF_8))
val ripe = Hash.ripemd160("hello".toByteArray(Charsets.UTF_8))

// AES-GCM
val key = Random.bytes(32)
val ct = Aead.aesGcmEncrypt("secret".toByteArray(Charsets.UTF_8), key)
val pt = Aead.aesGcmDecrypt(ct, key)

// bcrypt
val pw = "password".toByteArray(Charsets.UTF_8)
val bcryptHash = Kdf.bcryptHash(pw, cost = 10)
val ok = Kdf.bcryptVerify(pw, bcryptHash)

// secp256k1 ECDSA
val sk = Secp256k1.generateKey()
val pk = Secp256k1.privateKeyToPublicKey(sk, true)
val sig = Secp256k1.sign("message".toByteArray(Charsets.UTF_8), sk)
val valid = Secp256k1.verify("message".toByteArray(Charsets.UTF_8), sig, pk)

// Schnorr
val schnorrPk = Secp256k1.schnorrPrivateKeyToPublicKey(sk)
val schnorrSig = Secp256k1.schnorrSign("message".toByteArray(Charsets.UTF_8), sk)
val schnorrValid = Secp256k1.schnorrVerify("message".toByteArray(Charsets.UTF_8), schnorrSig, schnorrPk)

// BLS12-381
val blsSk = Bls.generateKey()
val blsPk = Bls.privateKeyToPublicKey(blsSk)
val blsSig = Bls.sign("message".toByteArray(Charsets.UTF_8), blsSk)
val blsValid = Bls.verify("message".toByteArray(Charsets.UTF_8), blsSig, blsPk)
```

## 维护：Kotlin / JNI / Go 对齐

修改 native 行为时，请同时核对：

1. `src/main/java/mobi/timon/crypto/*.kt` 中的 **`external fun`**
2. `src/main/cpp/enc_jni.c` 中的 **`Java_mobi_timon_crypto_*`**
3. `src/main/go/*.go` 中的 **`//export`** 与构建产物中的 **`libencgo.h`**

Go 返回的 C 分配缓冲区在 JNI 中经 **`FreeBytes`** 释放；返回布尔类的 Verify 路径使用统一 **`verifyBoolResult`**。空长度输入在哈希/HMAC 等 API 中的语义以 Go 实现为准（例如空消息的 SHA-256）。

## 测试

- 可执行测试在 **`src/androidTest`**（需真机或模拟器；模块已启用 **multiDex** 以容纳测试依赖）。
- 运行：`./gradlew :crypto:connectedDebugAndroidTest`
- Lint：`./gradlew :crypto:lintDebug`

## 构建

```bash
# 仅编译 Go（生成各 ABI 的 libencgo.so）
./gradlew :crypto:compileGoDebug

# 依赖 crypto 的调试构建（会触发 preBuild → compileGoDebug）
./gradlew :app:assembleDebug
```

需本机已安装 **Go**、**Android NDK**，且 `local.properties` 中 `sdk.dir` 正确。

## 目录结构（概要）

```
crypto/
├── build.gradle.kts
├── README.md
├── src/main/java/mobi/timon/crypto/   # Kotlin API
├── src/main/cpp/                       # JNI（enc_jni.c、CMakeLists.txt）
├── src/main/go/                        # Go 源码、go.mod；lib/ 为编译输出（勿提交二进制）
└── src/androidTest/                    # Instrumented 测试
```

## 参考链接

- [Go crypto](https://pkg.go.dev/crypto)
- [golang.org/x/crypto](https://pkg.go.dev/golang.org/x/crypto)
- [btcd/btcec/v2](https://pkg.go.dev/github.com/btcsuite/btcd/btcec/v2) - secp256k1 / Schnorr
- [kilic/bls12-381](https://pkg.go.dev/github.com/kilic/bls12-381) - BLS12-381
