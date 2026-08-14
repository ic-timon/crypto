//! crypto-native (encrust) — Rust 加密核心。
//!
//! 替代 Go 版（gomobile c-shared）。编译为 cdylib（Android .so）+ staticlib（Apple .a），
//! 通过 C ABI（`#[no_mangle] extern "C"`）暴露给 JNI（Android）和 cinterop（Apple）。
//!
//! 所有导出函数统一约定：
//! - 输入：`*const u8` + `c_int` len（Go 版对齐）
//! - 输出：`*mut u8` + `*mut c_int` outLen（malloc 分配，调用方用 [`enc_free`] 释放）
//! - 失败：返回 null + `*outLen = 0`
//! - verify 类：返回 1 字节（0/1），非 null/null

pub mod secp256k1;
mod utils;

pub use secp256k1::*;
