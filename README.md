# Timon Android

**Android 客户端（Jetpack Compose + Material 3）** 与 **原生密码学库 `mobi.timon.enc`** 的同仓工程：应用侧用 Kotlin 写 UI 与业务壳，加解密、哈希、KDF、非对称与签名等能力集中在 `:enc` 模块，经 **Kotlin → JNI（C）→ Go（`crypto/*`、`golang.org/x/crypto`）** 落地，便于与 Go 生态实现、测试向量及多端行为对齐。

---

## 目录

- [仓库里有什么](#仓库里有什么)
- [模块一览](#模块一览)
- [`:enc` 架构与能力](#enc-架构与能力)
- [密码学格式与安全提示](#密码学格式与安全提示)
- [环境与构建](#环境与构建)
- [测试与静态检查](#测试与静态检查)
- [维护 native 时的检查清单](#维护-native-时的检查清单)
- [目录结构](#目录结构)
- [贡献与规范](#贡献与规范)

---

## 仓库里有什么

| 部分 | 作用 |
|------|------|
| **`:app`** | 面向用户的 Android 应用：Compose UI、入口 `Activity` 等。 |
| **`:enc`** | 可复用的 Android Library：对外暴露 Kotlin API，内部通过 JNI 调用 Go 编译出的 `libencgo.so`，与 `libencjni.so` 协同加载。 |

应用模块 Kotlin **命名空间 / 源码包**为 **`mobi.timon.android`**；**`applicationId`**（设备安装后的包名）以 **`app/build.gradle.kts`** 中 `defaultConfig` 为准。

---

## 模块一览

| 模块 | 包名 / 坐标 | 说明 |
|------|-------------|------|
| `:app` | `mobi.timon.android` | 主应用壳工程。 |
| `:enc` | `mobi.timon.enc` | 密码学工具库（见下节）。 |

---

## `:enc` 架构与能力

### 调用链

```mermaid
flowchart LR
  K[Kotlin object / external]
  J[JNI enc_jni.c]
  G[Go //export CGO]
  K --> J --> G
```

文本等价：**Kotlin `external`** → **C（`enc_jni.c`）** → **Go `//export`**，依赖 Go 标准库与 `golang.org/x/crypto`。

- **加载顺序**（见 `enc/.../Enc.kt`）：先 **`encgo`**，再 **`encjni`**。
- **构建**：Gradle 任务（如 `compileGoDebug`）为各 ABI 生成 **`libencgo.so`** 与 **`libencgo.h`**；CMake 按 ABI 链接；输出一般在 `enc` 下 `go/lib/` 等约定目录（勿提交生成的 `.so` 到版本库，以本地/CI 构建为准）。

### API 一览

失败时统一抛出 **`mobi.timon.enc.EncException`**（含 JNI / Go 错误路径）。

| 门面 | 能力摘要 | 实现位置 |
|------|----------|----------|
| **Enc** | `System.loadLibrary` 入口 | native 加载 |
| **Hash** | SHA-1 / SHA-256 / SHA-512；`blake2b256` / `md5` | 多为 native；弱摘要仅兼容旧协议 |
| **Hmac** | HMAC-SHA256、HMAC-SHA512 | native |
| **Random** | `bytes`（CSPRNG）；`int` / `long(min,max)` | `bytes` 为 native；`int`/`long` 为 Kotlin（`long` 为半开区间 `[min,max)`，需 `max > min`） |
| **Codec** | Hex、Base64 | 纯 Kotlin（如 `java.util.Base64`） |
| **Aead** | AES-GCM、ChaCha20-Poly1305 | native |
| **Cbc** | AES-CBC、DES-CBC（PKCS7） | native；DES 仅兼容场景 |
| **Stream** | AES-CTR、ChaCha20（无独立 MAC） | native |
| **Xts** | AES-XTS | native |
| **Kdf** | bcrypt、Argon2id、scrypt、PBKDF2、HKDF | native |
| **Rsa** | 密钥生成、OAEP、PKCS#1 v1.5 签名/验签、公私钥派生 | native；密钥为 **DER 字节**，非 PEM 字符串 |
| **Ecdsa** | P-224 / P-256 / P-384 / P-521 | native |
| **Ed25519** | `generateKey` / `sign` / `verify` | native |

> **弱算法提示**：**MD5、SHA-1、DES** 等仅建议在**校验旧数据或协议兼容**时使用；新设计请优先 **SHA-256、AES、AEAD** 等现代算法。

### 使用示例

```kotlin
import mobi.timon.enc.Aead
import mobi.timon.enc.Codec
import mobi.timon.enc.Hash
import mobi.timon.enc.Kdf
import mobi.timon.enc.Random

val digest = Hash.sha256("hello".toByteArray(Charsets.UTF_8))
println(Codec.toHex(digest))

val key = Random.bytes(32)
val ct = Aead.aesGcmEncrypt("secret".toByteArray(Charsets.UTF_8), key)
val pt = Aead.aesGcmDecrypt(ct, key)

val pw = "password".toByteArray(Charsets.UTF_8)
val bcryptHash = Kdf.bcryptHash(pw, cost = 10)
val ok = Kdf.bcryptVerify(pw, bcryptHash)
```

---

## 密码学格式与安全提示

| 场景 | 约定 |
|------|------|
| **AES-GCM / ChaCha20-Poly1305** | 密文为 **`nonce ‖ ciphertext`（含 Poly1305 tag）**；**不提供 AAD**。 |
| **AES-CBC** | **`iv(16) ‖ ciphertext`**，PKCS7；密钥 16 / 24 / 32 字节。 |
| **DES-CBC** | **`iv(8) ‖ ciphertext`**，PKCS7；密钥 **8 字节**。 |
| **AES-CTR / 纯 ChaCha20** | nonce 与密文拼接方式由实现固定；**无内置完整性认证**，需协议层自行保证。 |
| **AES-XTS** | 密钥 **32 或 64 字节**；明文长度建议为 **16 字节倍数**；`sectorNum` 参与扇区/tweak（见源码）。 |
| **Ed25519** | `generateKey()` 返回 **96 字节**：前 32 公钥，后 64 私钥；`sign` 使用 64 字节私钥（常取 `kp.copyOfRange(32, 96)`）。 |
| **bcrypt** | `bcryptHash` / `bcryptVerify` 的哈希均为 **`ByteArray`**，非字符串。 |
| **scrypt** | Go 侧 **N、r、p 固定**；Kotlin 侧重在 `keyLen`（见 `enc/.../kdf_advanced.go`）。 |

---

## 环境与构建

**通用**

- **Android Studio** / **Android SDK**（根目录 `local.properties` 配置 `sdk.dir`）
- **JDK**（与 Gradle 工具链匹配）

**构建 `:enc` 额外需要**

- **Go**（用于编译 `libencgo.so`）
- **Android NDK**（CMake / JNI）

**常用命令**

```bash
# 全工程调试 APK
./gradlew assembleDebug

./gradlew clean assembleDebug
./gradlew assembleRelease

# 仅编译 enc 的 Go 产物（各 ABI）
./gradlew :enc:compileGoDebug

# 构建依赖 enc 的应用（会触发 preBuild → compileGo*）
./gradlew :app:assembleDebug
```

---

## 测试与静态检查

```bash
# JVM 单元测试（各模块）
./gradlew test

# Android 仪器化测试（需真机或模拟器）
./gradlew connectedAndroidTest

# enc 模块仪器化测试（native）
./gradlew :enc:connectedDebugAndroidTest

# Lint
./gradlew lint
./gradlew :enc:lintDebug
```

`:enc` 中与 JNI、Codec（如 Android `Base64`）相关的用例放在 **`enc/src/androidTest`**；需能加载对应 ABI 的 `.so`。

---

## 维护 native 时的检查清单

修改哈希、加解密、KDF 等 **native 行为**时，请交叉核对：

1. `enc/src/main/java/mobi/timon/enc/*.kt` 中的 **`external fun`**
2. `enc/src/main/cpp/enc_jni.c` 中的 **`Java_mobi_timon_enc_*`**
3. `enc/src/main/go/*.go` 中的 **`//export`** 与构建产物 **`libencgo.h`**

Go 经 C 返回的缓冲区在 JNI 侧应经约定路径（如 **`FreeBytes`**）释放；布尔型 verify 路径与项目中的 **`verifyBoolResult`** 等辅助函数保持一致。空输入等边界语义以 **Go 实现**为准。

---

## 目录结构

```
.
├── app/                          # 主应用（Compose）
├── enc/                          # 密码学 Android Library
│   ├── build.gradle.kts
│   ├── enc.md                    # 算法选型与设计备忘（可选读）
│   ├── src/main/java/mobi/timon/enc/
│   ├── src/main/cpp/             # JNI、CMakeLists.txt
│   ├── src/main/go/              # Go 源码、go.mod；本地构建生成 lib/ 等
│   └── src/androidTest/          # 仪器化测试
├── AGENTS.md                     # 协作与代码风格
└── README.md                     # 本文件
```

---

## 贡献与规范

- 构建命令、Kotlin/Compose 风格、测试目录约定等见 **[AGENTS.md](AGENTS.md)**。

**参考（外部文档）**

- [Go crypto](https://pkg.go.dev/crypto)
- [golang.org/x/crypto](https://pkg.go.dev/golang.org/x/crypto)
