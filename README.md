# crypto

[![Release](https://img.shields.io/github/v/release/ic-timon/crypto?include_prereleases)](https://github.com/ic-timon/crypto/releases)
[![License](https://img.shields.io/github/license/ic-timon/crypto)](LICENSE)
[![Rust](https://img.shields.io/badge/Rust-1.75+-CE422B?logo=rust)](https://www.rust-lang.org/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin)](https://kotlinlang.org/)
[![CI](https://github.com/ic-timon/crypto/actions/workflows/ci.yml/badge.svg)](https://github.com/ic-timon/crypto/actions/workflows/ci.yml)

Cross-platform cryptography library exposing a Kotlin API. Built on **Kotlin (KMP) → Rust** — Rust compiles to native static/dynamic libraries, bridged via JNI (Android) and cinterop (Apple platforms).

Powered by RustCrypto ecosystem (`sha2` / `aes-gcm` / `secp256k1` / `ed25519-dalek` / `blst` etc.), all audited implementations. Rust has no runtime, making iOS cinterop first-class.

**中文:** [Readme_CN.md](Readme_CN.md)

---

## Architecture

```
┌─────────────────────────────────┐
│         Kotlin API (KMP)         │
│   mobi.timon.crypto.* (68 fns)   │
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

### Why Rust?

The predecessor was a **Kotlin → JNI(C) → Go** chain. Go's runtime (GC + goroutines) couldn't pass through Kotlin/Native cinterop to iOS — verified crash. Rust has no runtime, compiles to native code identical to C static libraries, enabling both platforms to share one implementation.

## Installation

Published to GitHub Packages.

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/ic-timon/crypto")
            credentials {
                username = "<GitHub username>"
                password = "<Personal Access Token (read:packages)>"
            }
        }
    }
}

// build.gradle.kts
dependencies {
    implementation("io.github.ic-timon.crypto:crypto:<version>")
}
```

## API Overview

| Module | Functions | Algorithms |
|--------|-----------|------------|
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
| Codec | 6 | hex/base64/constantTimeEquals/wipe (pure Kotlin) |

## Build

### Prerequisites

- JDK 17+
- Rust toolchain (`rustup`)
- Android NDK (for Android builds)
- `cargo-ndk`: `cargo install cargo-ndk`
- Apple Xcode (for Apple platform builds, macOS only)

```bash
# Rust core tests
cd rust && cargo test

# Android build (auto-invokes cargo-ndk for 4 ABIs)
./gradlew :crypto:assembleDebug

# Release (tag trigger → GitHub Packages publish)
git tag v2.0.0 && git push origin v2.0.0
```

## License

MIT
