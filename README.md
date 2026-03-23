# crypto

[![Release](https://img.shields.io/github/v/release/ic-timon/crypto?include_prereleases)](https://github.com/ic-timon/crypto/releases)
[![License](https://img.shields.io/github/license/ic-timon/crypto)](LICENSE)
[![Go](https://img.shields.io/badge/Go-1.26+-00ADD8?logo=go)](https://go.dev/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-33%2B-3DDC84?logo=android)](https://developer.android.com/)
[![CI](https://github.com/ic-timon/crypto/actions/workflows/release.yml/badge.svg)](https://github.com/ic-timon/crypto/actions/workflows/release.yml)

A native Android cryptography library with a Kotlin API. Under the hood, it's a **Kotlin → JNI (C) → Go** pipeline — because why settle for one language when you can have three?

Powered by the Go standard library and `golang.org/x/crypto`. Battle-tested (by me, mostly).

**简体中文:** [Readme_CN.md](Readme_CN.md)

---

## Contents

- [Installation](#installation)
- [ProGuard / R8](#proguard--r8)
- [Architecture](#architecture)
- [API overview](#api-overview)
- [Usage examples](#usage-examples)
- [Wire formats and security notes](#wire-formats-and-security-notes)
- [Maintenance](#maintenance)
- [Local build](#local-build)
- [Testing](#testing)
- [Repository layout](#repository-layout)
- [Contributing](#contributing)

---

## Installation

`crypto` is published to GitHub Packages.

### 1. Add the repository

In `settings.gradle.kts`:

```kotlin
repositories {
    google()
    mavenCentral()
    maven { url = uri("https://maven.pkg.github.com/ic-timon/crypto") }
}
```

### 2. Add the dependency

```kotlin
implementation("io.github.ic-timon.crypto:crypto:1.3.0")
```

### 3. Authentication

GitHub Packages requires credentials. In `local.properties`:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.token=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN (read:packages)
```

---

## ProGuard / R8

Kotlin `external` functions bind to JNI symbols such as `Java_mobi_timon_crypto_*` in `libencjni`. If **R8 / minification** renames or removes those classes or members, native resolution fails at runtime.

- The library ships **`consumer-rules.pro`**; Gradle **merges** it into consuming apps, so you usually need **no extra rules**.
- If you vendor the module, use an older AAR without consumer rules, or override ProGuard inputs, add the same keeps to your app `proguard-rules.pro`:

```proguard
# mobi.timon.crypto — JNI: keep names visible to libencjni
-keep class mobi.timon.crypto.** { *; }
```

---

## Architecture

```
Kotlin (object / external)
    ↓ JNI
C (enc_jni.c)
    ↓ CGO
Go (//export)
```

- Load order is documented in `Enc.kt`: `encgo` first, then `encjni`.
- Building Go produces per-ABI `libencgo.so` and `libencgo.h`; CMake links them by ABI.

---

## API overview

Failures throw `mobi.timon.crypto.EncException`.

| Facade        | Capabilities |
|---------------|--------------|
| **Hash**      | SHA-1 / SHA-256 / SHA-384 / SHA-512 / SHA-512/256 / blake2b256 / MD5 / RIPEMD-160 / Keccak-256 / Keccak-512 |
| **Hmac**      | HMAC-SHA1 / HMAC-SHA256 / HMAC-SHA512 |
| **Random**    | CSPRNG bytes / int / long |
| **Codec**     | Hex / Base64 / constant-time equals / secure wipe |
| **Aead**      | AES-GCM / ChaCha20-Poly1305 |
| **Cbc**       | AES-CBC / DES-CBC (PKCS#7) |
| **Stream**    | AES-CTR / ChaCha20 |
| **Xts**       | AES-XTS |
| **Kdf**       | bcrypt / Argon2id / scrypt / PBKDF2 / HKDF |
| **Rsa**       | Key generation / OAEP / PKCS#1 v1.5 sign & verify |
| **Ecdsa**     | P-224 / P-256 / P-384 / P-521 |
| **Secp256k1** | Key generation / ECDSA sign & verify / pubkey recovery / Schnorr sign & verify |
| **Bls**       | BLS12-381: keys, sign & verify, signature aggregation, pubkey aggregation |
| **Ed25519**   | Key generation / sign / verify |

---

## Usage examples

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

## Wire formats and security notes

### Symmetric encryption

**AES-GCM / ChaCha20-Poly1305**: Ciphertext layout is `nonce (12 bytes) + ciphertext + tag (16 bytes)`. Nonce and tag are concatenated in the returned blob; callers do not pass them separately for decryption.

**AES-CBC**: Layout is `IV (16 bytes) + ciphertext`. The IV is random and prepended; decryption strips it. PKCS#7 padding. Key sizes 16 / 24 / 32 bytes (AES-128/192/256).

**DES-CBC**: Layout is `IV (8 bytes) + ciphertext`; key length 8 bytes. **Legacy interoperability only.**

**AES-CTR / ChaCha20**: Nonce + ciphertext framing is fixed by the implementation. **No built-in integrity**—use with care.

**AES-XTS**: Keys are 32 or 64 bytes; plaintext length should be a multiple of 16 bytes; `sectorNum` feeds the sector/tweak.

### Asymmetric encryption and signatures

**RSA**: Keys are **DER bytes** (PKCS#8 / PKIX), not PEM strings.

**Ed25519**: Keypairs are serialized as one 96-byte array: first 32 bytes public key, next 64 bytes private key material (seed + public key).

**secp256k1**:
- `generateKey()` returns a 32-byte private key.
- `sign()` returns a 65-byte signature: `r (32) ‖ s (32) ‖ recoveryId (1)`.
- `privateKeyToPublicKey(privateKey, compressed)`: `compressed = true` → 33-byte compressed pubkey; `false` → 65-byte uncompressed.

**Schnorr (secp256k1)**:
- `schnorrSign()` returns a 64-byte signature: `r (32) ‖ s (32)`.
- `schnorrPrivateKeyToPublicKey()` returns a 32-byte x-only public key.

**BLS12-381**:
- Private key 32 bytes; public key 48 bytes (G1 compressed).
- Signature 96 bytes (G2 compressed).
- Aggregated signature remains 96 bytes; aggregated pubkey remains 48 bytes.

### Key derivation

**bcrypt**: Both `bcryptHash` output and the `hash` argument to `bcryptVerify` are `ByteArray` values (not strings).

**scrypt**: N, r, and p are fixed on the Go side; Kotlin only exposes `keyLen`.

### Security note

> **Weak algorithms**: MD5 and SHA-1 are vulnerable to collision attacks; DES has only 56 effective key bits and is brute-forceable. Prefer them only for legacy protocol compatibility.

---

## Maintenance

When changing native behavior, keep these in sync:

1. **`external fun`** declarations in `src/main/java/mobi/timon/crypto/*.kt`
2. **`Java_mobi_timon_crypto_*`** JNI entry points in `src/main/cpp/enc_jni.c`
3. **`//export`** functions in `src/main/go/*.go` and the generated **`libencgo.h`**

C buffers returned from Go are freed in JNI via `FreeBytes`; boolean verify paths use `verifyBoolResult`. Empty-input semantics for hash/HMAC and similar APIs follow the Go implementation.

---

- **Security**: `Codec.constantTimeEquals()` for timing-attack prevention; `Codec.wipe()` for secure memory clearing
- **New algorithms**: SHA-384, SHA-512/256, HMAC-SHA1
- **Documentation**: Full KDoc for all public APIs
- **Quality**: CI workflow, JVM unit tests, ktlint code style
- **UI**: Dashboard now shows 5 modules (Hash, Cipher, KDF, Sign, Utils) with 37 tests total

---

## Local build

Prebuilt `.so` binaries are checked in; you can use them as-is. To rebuild:

**Requirements**: Go 1.26+, Android NDK, and a valid `sdk.dir` in `local.properties`.

```bash
# Go only (per-ABI libencgo.so)
./gradlew :crypto:compileGoDebug

# Full module build
./gradlew :crypto:assembleRelease
```

---

## Testing

```bash
./gradlew :crypto:connectedDebugAndroidTest
```

---

## Repository layout

```
crypto/
├── src/main/java/mobi/timon/crypto/   # Kotlin API
├── src/main/cpp/                       # JNI bridge
├── src/main/go/                        # Go implementation
└── src/androidTest/                    # Instrumented tests
```

---

## References

- [Go crypto](https://pkg.go.dev/crypto)
- [golang.org/x/crypto](https://pkg.go.dev/golang.org/x/crypto)
- [btcd/btcec/v2](https://pkg.go.dev/github.com/btcsuite/btcd/btcec/v2) — secp256k1 / Schnorr
- [kilic/bls12-381](https://pkg.go.dev/github.com/kilic/bls12-381) — BLS12-381

---

## Contributing

Found a bug? Have a feature idea? Want to add a cool new algorithm?

**[Open an issue](https://github.com/ic-timon/crypto/issues)** — I'd love to hear from you!

Whether it's a bug report, feature request, documentation improvement, or just a "hey, this API is confusing" — all feedback is welcome. Don't be shy.

Check out [CONTRIBUTING.md](CONTRIBUTING.md) for the full guide on how to contribute.

---

## License

[MIT](LICENSE) — do whatever you want, just don't blame me if something breaks.
