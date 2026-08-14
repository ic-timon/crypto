//! crypto-native (encrust) — Rust 加密核心。
//!
//! 替代 Go 版（gomobile c-shared）。编译为 cdylib（Android .so）+ staticlib（Apple .a），
//! 通过 C ABI（`#[no_mangle] extern "C"`）暴露给 JNI（Android）和 cinterop（Apple）。

pub mod secp256k1;
pub mod hash;
pub mod hmac;
pub mod random;
pub mod aead;
pub mod cbc;
pub mod stream;
pub mod kdf;
pub mod ed25519;
pub mod rsa;
pub mod xts;
pub mod ecdsa;
pub mod bls;
pub mod utils;

#[cfg(feature = "jni-bridge")]
mod jni;
