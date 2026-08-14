//! JNI 桥（Android 专用）—— 零依赖 raw JNI。
//! JNIEnv 函数表通过指针算术访问（64 位每项 8 字节）。

#![cfg(feature = "jni-bridge")]

use std::os::raw::{c_char, c_int, c_void};

type JNIEnv = *mut c_void;
type JClass = *mut c_void;
type JByteArray = *mut c_void;
type JInt = c_int;
type JLong = i64;
type JBoolean = u8;

/// JNIEnv 函数表索引（jni.h 定义顺序）。
/// env → *env = JNINativeInterface_ * → 函数指针数组。
macro_rules! jni_fn {
    ($env:expr, $idx:expr) => {{
        let env_ptr = $env as *const *const usize;
        let table = *env_ptr; // 函数指针数组首地址
        let fn_ptr = *table.add($idx); // 第 idx 个函数指针
        fn_ptr
    }};
}

unsafe fn get_array_length(env: JNIEnv, arr: JByteArray) -> i32 {
    type Fn = extern "C" fn(JNIEnv, JByteArray) -> i32;
    let f: Fn = std::mem::transmute(jni_fn!(env, 171));
    f(env, arr)
}

unsafe fn get_byte_array_elements(env: JNIEnv, arr: JByteArray) -> (*mut u8, i32) {
    let len = get_array_length(env, arr);
    type Fn = extern "C" fn(JNIEnv, JByteArray, *mut JBoolean) -> *mut i8;
    let f: Fn = std::mem::transmute(jni_fn!(env, 184));
    let elements = f(env, arr, std::ptr::null_mut());
    if elements.is_null() {
        // 获取失败（可能已有 pending exception）——不再触碰 JNI，由调用方统一 throw
        return (std::ptr::null_mut(), 0);
    }
    (elements as *mut u8, len)
}

unsafe fn release_byte_array_elements(env: JNIEnv, arr: JByteArray, ptr: *mut u8) {
    type Fn = extern "C" fn(JNIEnv, JByteArray, *mut i8, i32);
    let f: Fn = std::mem::transmute(jni_fn!(env, 192));
    f(env, arr, ptr as *mut i8, 0); // JNI_ABORT
}

unsafe fn new_byte_array(env: JNIEnv, len: i32) -> JByteArray {
    type Fn = extern "C" fn(JNIEnv, i32) -> JByteArray;
    let f: Fn = std::mem::transmute(jni_fn!(env, 176));
    f(env, len)
}

unsafe fn set_byte_array_region(env: JNIEnv, arr: JByteArray, start: i32, len: i32, buf: *const u8) {
    type Fn = extern "C" fn(JNIEnv, JByteArray, i32, i32, *const i8);
    let f: Fn = std::mem::transmute(jni_fn!(env, 208));
    f(env, arr, start, len, buf as *const i8);
}

unsafe fn throw_enc_exception(env: JNIEnv, msg: &str) {
    let find_class: extern "C" fn(JNIEnv, *const c_char) -> JClass = std::mem::transmute(jni_fn!(env, 6));
    let throw_new: extern "C" fn(JNIEnv, JClass, *const c_char) -> i32 = std::mem::transmute(jni_fn!(env, 14));
    let class_name = b"mobi/timon/crypto/EncException\0";
    let cls = find_class(env, class_name.as_ptr() as *const c_char);
    if !cls.is_null() {  // FindClass 失败时静默（可能已有 pending exception，不能再调 JNI）
        let mut buf = [0u8; 256];
        let copy_len = msg.len().min(254);
        buf[..copy_len].copy_from_slice(&msg.as_bytes()[..copy_len]);
        buf[copy_len] = 0;
        throw_new(env, cls, buf.as_ptr() as *const c_char);
    }
}

// ── 结果包装 ──────────────────────────────────────────────────

unsafe fn result_to_jbytes(env: JNIEnv, result: *mut u8, out_len: c_int) -> JByteArray {
    if result.is_null() || out_len <= 0 {
        throw_enc_exception(env, "operation failed");
        return std::ptr::null_mut();
    }
    let arr = new_byte_array(env, out_len);
    if arr.is_null() {
        // NewByteArray 失败（OOM，pending exception 已设置）——只释放内存退出
        crate::utils::enc_free(result, out_len);
        return std::ptr::null_mut();
    }
    set_byte_array_region(env, arr, 0, out_len, result);
    crate::utils::enc_free(result, out_len);
    arr
}

unsafe fn result_to_jbool(env: JNIEnv, result: *mut u8, out_len: c_int) -> JBoolean {
    if result.is_null() || out_len <= 0 {
        throw_enc_exception(env, "operation failed");
        return 0;
    }
    let val = *result;
    crate::utils::enc_free(result, out_len);
    val
}

// ── crypto 模块引用 ───────────────────────────────────────────

use crate::hash as h;
use crate::hmac as m;
use crate::aead as a;
use crate::cbc as cb;
use crate::stream as st;
use crate::xts as xt;
use crate::kdf as kd;
use crate::ed25519 as ed;
use crate::secp256k1 as sk;
use crate::rsa as rs;
use crate::ecdsa as ec;
use crate::bls as bl;
use crate::random as rd;

// ── JNI 导出函数 ──────────────────────────────────────────────

// 宏：单 ByteArray 入 → ByteArray 出
macro_rules! jni_1 {
    ($name:ident, $rust:path) => {
        #[no_mangle]
        pub unsafe extern "system" fn $name(env: JNIEnv, _cls: JClass, data: JByteArray) -> JByteArray {
            if data.is_null() { throw_enc_exception(env, "null"); return std::ptr::null_mut(); }
            let (ptr, len) = get_byte_array_elements(env, data);
            let mut out: c_int = 0;
            let r = $rust(ptr, len, &mut out);
            release_byte_array_elements(env, data, ptr);
            result_to_jbytes(env, r, out)
        }
    };
}

// 宏：双 ByteArray 入 → ByteArray 出
macro_rules! jni_2 {
    ($name:ident, $rust:path) => {
        #[no_mangle]
        pub unsafe extern "system" fn $name(env: JNIEnv, _cls: JClass, a1: JByteArray, a2: JByteArray) -> JByteArray {
            if a1.is_null() || a2.is_null() { throw_enc_exception(env, "null"); return std::ptr::null_mut(); }
            let (p1, l1) = get_byte_array_elements(env, a1);
            let (p2, l2) = get_byte_array_elements(env, a2);
            let mut out: c_int = 0;
            let r = $rust(p1, l1, p2, l2, &mut out);
            release_byte_array_elements(env, a1, p1);
            release_byte_array_elements(env, a2, p2);
            result_to_jbytes(env, r, out)
        }
    };
}

// 宏：三 ByteArray 入 → Boolean 出
macro_rules! jni_3v {
    ($name:ident, $rust:path) => {
        #[no_mangle]
        pub unsafe extern "system" fn $name(env: JNIEnv, _cls: JClass, a1: JByteArray, a2: JByteArray, a3: JByteArray) -> JBoolean {
            if a1.is_null() || a2.is_null() || a3.is_null() { return 0; }
            let (p1, l1) = get_byte_array_elements(env, a1);
            let (p2, l2) = get_byte_array_elements(env, a2);
            let (p3, l3) = get_byte_array_elements(env, a3);
            let mut out: c_int = 0;
            let r = $rust(p1, l1, p2, l2, p3, l3, &mut out);
            release_byte_array_elements(env, a1, p1);
            release_byte_array_elements(env, a2, p2);
            release_byte_array_elements(env, a3, p3);
            result_to_jbool(env, r, out)
        }
    };
}

// Hash
jni_1!(Java_mobi_timon_crypto_Hash_sha1, h::Sha1);
jni_1!(Java_mobi_timon_crypto_Hash_sha256, h::Sha256);
jni_1!(Java_mobi_timon_crypto_Hash_sha384, h::Sha384);
jni_1!(Java_mobi_timon_crypto_Hash_sha512, h::Sha512);
jni_1!(Java_mobi_timon_crypto_Hash_sha512_1256, h::Sha512_256);
jni_1!(Java_mobi_timon_crypto_Hash_blake2b256, h::Blake2b256);
jni_1!(Java_mobi_timon_crypto_Hash_md5, h::Md5);
jni_1!(Java_mobi_timon_crypto_Hash_ripemd160, h::Ripemd160);
jni_1!(Java_mobi_timon_crypto_Hash_keccak256, h::Keccak256);
jni_1!(Java_mobi_timon_crypto_Hash_keccak512, h::Keccak512);

// Hmac
jni_2!(Java_mobi_timon_crypto_Hmac_hmacSha256, m::HmacSha256);
jni_2!(Java_mobi_timon_crypto_Hmac_hmacSha512, m::HmacSha512);
jni_2!(Java_mobi_timon_crypto_Hmac_hmacSha1, m::HmacSha1);

// Aead
jni_2!(Java_mobi_timon_crypto_Aead_aesGcmEncrypt, a::AesGcmEncrypt);
jni_2!(Java_mobi_timon_crypto_Aead_aesGcmDecrypt, a::AesGcmDecrypt);
jni_2!(Java_mobi_timon_crypto_Aead_chacha20Poly1305Encrypt, a::ChaCha20Poly1305Encrypt);
jni_2!(Java_mobi_timon_crypto_Aead_chacha20Poly1305Decrypt, a::ChaCha20Poly1305Decrypt);

// Cbc
jni_2!(Java_mobi_timon_crypto_Cbc_aesCbcEncrypt, cb::AesCbcEncrypt);
jni_2!(Java_mobi_timon_crypto_Cbc_aesCbcDecrypt, cb::AesCbcDecrypt);
jni_2!(Java_mobi_timon_crypto_Cbc_desCbcEncrypt, cb::DesCbcEncrypt);
jni_2!(Java_mobi_timon_crypto_Cbc_desCbcDecrypt, cb::DesCbcDecrypt);

// Stream
jni_2!(Java_mobi_timon_crypto_Stream_aesCtrEncrypt, st::AesCtrEncrypt);
jni_2!(Java_mobi_timon_crypto_Stream_aesCtrDecrypt, st::AesCtrDecrypt);
jni_2!(Java_mobi_timon_crypto_Stream_chacha20Encrypt, st::ChaCha20Encrypt);
jni_2!(Java_mobi_timon_crypto_Stream_chacha20Decrypt, st::ChaCha20Decrypt);

// Random
#[no_mangle]
pub unsafe extern "system" fn Java_mobi_timon_crypto_RandomJni_bytes(env: JNIEnv, _cls: JClass, length: JInt) -> JByteArray {
    let mut out: c_int = 0;
    result_to_jbytes(env, rd::RandomBytes(length, &mut out), out)
}

// Ed25519
#[no_mangle]
pub unsafe extern "system" fn Java_mobi_timon_crypto_Ed25519_generateKey(env: JNIEnv, _cls: JClass) -> JByteArray {
    let mut out: c_int = 0;
    result_to_jbytes(env, ed::Ed25519GenerateKey(&mut out), out)
}
jni_2!(Java_mobi_timon_crypto_Ed25519_sign, ed::Ed25519Sign);
jni_3v!(Java_mobi_timon_crypto_Ed25519_verify, ed::Ed25519Verify);

// Secp256k1
#[no_mangle]
pub unsafe extern "system" fn Java_mobi_timon_crypto_Secp256k1_generateKey(env: JNIEnv, _cls: JClass) -> JByteArray {
    let mut out: c_int = 0;
    result_to_jbytes(env, sk::Secp256k1GenerateKey(&mut out), out)
}

#[no_mangle]
pub unsafe extern "system" fn Java_mobi_timon_crypto_Secp256k1_privateKeyToPublicKey(env: JNIEnv, _cls: JClass, pk: JByteArray, compressed: JInt) -> JByteArray {
    if pk.is_null() { throw_enc_exception(env, "null"); return std::ptr::null_mut(); }
    let (ptr, len) = get_byte_array_elements(env, pk);
    let mut out: c_int = 0;
    let r = sk::Secp256k1PrivateKeyToPublicKey(ptr, len, compressed, &mut out);
    release_byte_array_elements(env, pk, ptr);
    result_to_jbytes(env, r, out)
}

jni_2!(Java_mobi_timon_crypto_Secp256k1_sign, sk::Secp256k1Sign);
jni_3v!(Java_mobi_timon_crypto_Secp256k1_verify, sk::Secp256k1Verify);

#[no_mangle]
pub unsafe extern "system" fn Java_mobi_timon_crypto_Secp256k1_recoverPublicKey(env: JNIEnv, _cls: JClass, msg: JByteArray, sig: JByteArray, compressed: JInt) -> JByteArray {
    if msg.is_null() || sig.is_null() { throw_enc_exception(env, "null"); return std::ptr::null_mut(); }
    let (m, ml) = get_byte_array_elements(env, msg);
    let (s, sl) = get_byte_array_elements(env, sig);
    let mut out: c_int = 0;
    let r = sk::Secp256k1RecoverPublicKey(m, ml, s, sl, compressed, &mut out);
    release_byte_array_elements(env, msg, m);
    release_byte_array_elements(env, sig, s);
    result_to_jbytes(env, r, out)
}

jni_2!(Java_mobi_timon_crypto_Secp256k1_schnorrSign, sk::SchnorrSign);
jni_3v!(Java_mobi_timon_crypto_Secp256k1_schnorrVerify, sk::SchnorrVerify);
jni_1!(Java_mobi_timon_crypto_Secp256k1_schnorrPrivateKeyToPublicKey, sk::SchnorrPrivateKeyToPublicKey);
jni_2!(Java_mobi_timon_crypto_Secp256k1_schnorrSignHash, sk::SchnorrSignHash);
jni_3v!(Java_mobi_timon_crypto_Secp256k1_schnorrVerifyHash, sk::SchnorrVerifyHash);

// Kdf
#[no_mangle]
pub unsafe extern "system" fn Java_mobi_timon_crypto_Kdf_bcryptHash(env: JNIEnv, _cls: JClass, pw: JByteArray, cost: JInt) -> JByteArray {
    if pw.is_null() { throw_enc_exception(env, "null"); return std::ptr::null_mut(); }
    let (ptr, len) = get_byte_array_elements(env, pw);
    let mut out: c_int = 0;
    let r = kd::BcryptHash(ptr, len, cost, &mut out);
    release_byte_array_elements(env, pw, ptr);
    result_to_jbytes(env, r, out)
}

#[no_mangle]
pub unsafe extern "system" fn Java_mobi_timon_crypto_Kdf_bcryptVerify(env: JNIEnv, _cls: JClass, pw: JByteArray, hash: JByteArray) -> JBoolean {
    if pw.is_null() || hash.is_null() { return 0; }
    let (p, pl) = get_byte_array_elements(env, pw);
    let (h, hl) = get_byte_array_elements(env, hash);
    let mut out: c_int = 0;
    let r = kd::BcryptVerify(p, pl, h, hl, &mut out);
    release_byte_array_elements(env, pw, p);
    release_byte_array_elements(env, hash, h);
    result_to_jbool(env, r, out)
}

// Xts
#[no_mangle]
pub unsafe extern "system" fn Java_mobi_timon_crypto_Xts_aesXtsEncrypt(env: JNIEnv, _cls: JClass, pt: JByteArray, key: JByteArray, sector: JLong) -> JByteArray {
    if pt.is_null() || key.is_null() { throw_enc_exception(env, "null"); return std::ptr::null_mut(); }
    let (p, pl) = get_byte_array_elements(env, pt);
    let (k, kl) = get_byte_array_elements(env, key);
    let mut out: c_int = 0;
    let r = xt::AesXtsEncrypt(p, pl, k, kl, sector, &mut out);
    release_byte_array_elements(env, pt, p);
    release_byte_array_elements(env, key, k);
    result_to_jbytes(env, r, out)
}

#[no_mangle]
pub unsafe extern "system" fn Java_mobi_timon_crypto_Xts_aesXtsDecrypt(env: JNIEnv, _cls: JClass, ct: JByteArray, key: JByteArray, sector: JLong) -> JByteArray {
    if ct.is_null() || key.is_null() { throw_enc_exception(env, "null"); return std::ptr::null_mut(); }
    let (c, cl) = get_byte_array_elements(env, ct);
    let (k, kl) = get_byte_array_elements(env, key);
    let mut out: c_int = 0;
    let r = xt::AesXtsDecrypt(c, cl, k, kl, sector, &mut out);
    release_byte_array_elements(env, ct, c);
    release_byte_array_elements(env, key, k);
    result_to_jbytes(env, r, out)
}

// Rsa
#[no_mangle]
pub unsafe extern "system" fn Java_mobi_timon_crypto_Rsa_generateKey(env: JNIEnv, _cls: JClass, bits: JInt) -> JByteArray {
    let mut out: c_int = 0;
    result_to_jbytes(env, rs::RsaGenerateKey(bits, &mut out), out)
}
jni_2!(Java_mobi_timon_crypto_Rsa_encrypt, rs::RsaEncrypt);
jni_2!(Java_mobi_timon_crypto_Rsa_decrypt, rs::RsaDecrypt);
jni_2!(Java_mobi_timon_crypto_Rsa_sign, rs::RsaSign);
jni_3v!(Java_mobi_timon_crypto_Rsa_verify, rs::RsaVerify);
jni_1!(Java_mobi_timon_crypto_Rsa_privateKeyToPublicKey, rs::RsaPrivateKeyToPublicKey);

// Ecdsa
#[no_mangle]
pub unsafe extern "system" fn Java_mobi_timon_crypto_Ecdsa_generateKey(env: JNIEnv, _cls: JClass, curve: JInt) -> JByteArray {
    let mut out: c_int = 0;
    result_to_jbytes(env, ec::EcdsaGenerateKey(curve, &mut out), out)
}
jni_2!(Java_mobi_timon_crypto_Ecdsa_sign, ec::EcdsaSign);
jni_3v!(Java_mobi_timon_crypto_Ecdsa_verify, ec::EcdsaVerify);
jni_1!(Java_mobi_timon_crypto_Ecdsa_privateKeyToPublicKey, ec::EcdsaPrivateKeyToPublicKey);

// Bls
#[no_mangle]
pub unsafe extern "system" fn Java_mobi_timon_crypto_Bls_generateKey(env: JNIEnv, _cls: JClass) -> JByteArray {
    let mut out: c_int = 0;
    result_to_jbytes(env, bl::BlsGenerateKey(&mut out), out)
}
jni_1!(Java_mobi_timon_crypto_Bls_privateKeyToPublicKey, bl::BlsPrivateKeyToPublicKey);
jni_2!(Java_mobi_timon_crypto_Bls_sign, bl::BlsSign);
jni_3v!(Java_mobi_timon_crypto_Bls_verify, bl::BlsVerify);

#[no_mangle]
pub unsafe extern "system" fn Java_mobi_timon_crypto_Bls_aggregateSignatures(env: JNIEnv, _cls: JClass, sigs: JByteArray, count: JInt) -> JByteArray {
    if sigs.is_null() { throw_enc_exception(env, "null"); return std::ptr::null_mut(); }
    let (ptr, len) = get_byte_array_elements(env, sigs);
    let mut out: c_int = 0;
    let r = bl::BlsAggregateSignatures(ptr, len, count, &mut out);
    release_byte_array_elements(env, sigs, ptr);
    result_to_jbytes(env, r, out)
}

#[no_mangle]
pub unsafe extern "system" fn Java_mobi_timon_crypto_Bls_aggregatePublicKeys(env: JNIEnv, _cls: JClass, pks: JByteArray, count: JInt) -> JByteArray {
    if pks.is_null() { throw_enc_exception(env, "null"); return std::ptr::null_mut(); }
    let (ptr, len) = get_byte_array_elements(env, pks);
    let mut out: c_int = 0;
    let r = bl::BlsAggregatePublicKeys(ptr, len, count, &mut out);
    release_byte_array_elements(env, pks, ptr);
    result_to_jbytes(env, r, out)
}

// Kdf remaining (Argon2id, scrypt, pbkdf2, hkdf — multi-arg functions)
#[no_mangle]
pub unsafe extern "system" fn Java_mobi_timon_crypto_Kdf_argon2idHash(
    env: JNIEnv, _cls: JClass, password: JByteArray, salt: JByteArray,
    time_cost: JInt, mem_cost: JInt, parallelism: JInt, key_len: JInt,
) -> JByteArray {
    if password.is_null() || salt.is_null() { throw_enc_exception(env, "null"); return std::ptr::null_mut(); }
    let (p, pl) = get_byte_array_elements(env, password);
    let (s, sl) = get_byte_array_elements(env, salt);
    let mut out: c_int = 0;
    let r = kd::Argon2idHash(p, pl, s, sl, time_cost, mem_cost, parallelism, key_len, &mut out);
    release_byte_array_elements(env, password, p);
    release_byte_array_elements(env, salt, s);
    result_to_jbytes(env, r, out)
}

#[no_mangle]
pub unsafe extern "system" fn Java_mobi_timon_crypto_Kdf_scrypt(env: JNIEnv, _cls: JClass, password: JByteArray, salt: JByteArray, key_len: JInt) -> JByteArray {
    if password.is_null() || salt.is_null() { throw_enc_exception(env, "null"); return std::ptr::null_mut(); }
    let (p, pl) = get_byte_array_elements(env, password);
    let (s, sl) = get_byte_array_elements(env, salt);
    let mut out: c_int = 0;
    let r = kd::Scrypt(p, pl, s, sl, key_len, &mut out);
    release_byte_array_elements(env, password, p);
    release_byte_array_elements(env, salt, s);
    result_to_jbytes(env, r, out)
}

#[no_mangle]
pub unsafe extern "system" fn Java_mobi_timon_crypto_Kdf_pbkdf2(env: JNIEnv, _cls: JClass, password: JByteArray, salt: JByteArray, iterations: JInt, key_len: JInt) -> JByteArray {
    if password.is_null() || salt.is_null() { throw_enc_exception(env, "null"); return std::ptr::null_mut(); }
    let (p, pl) = get_byte_array_elements(env, password);
    let (s, sl) = get_byte_array_elements(env, salt);
    let mut out: c_int = 0;
    let r = kd::Pbkdf2(p, pl, s, sl, iterations, key_len, &mut out);
    release_byte_array_elements(env, password, p);
    release_byte_array_elements(env, salt, s);
    result_to_jbytes(env, r, out)
}

#[no_mangle]
pub unsafe extern "system" fn Java_mobi_timon_crypto_Kdf_hkdf(env: JNIEnv, _cls: JClass, ikm: JByteArray, salt: JByteArray, info: JByteArray, key_len: JInt) -> JByteArray {
    if ikm.is_null() { throw_enc_exception(env, "null"); return std::ptr::null_mut(); }
    let (i, il) = get_byte_array_elements(env, ikm);
    let (s, sl) = if salt.is_null() { (std::ptr::null_mut(), 0) } else { get_byte_array_elements(env, salt) };
    let (f, fl) = if info.is_null() { (std::ptr::null_mut(), 0) } else { get_byte_array_elements(env, info) };
    let mut out: c_int = 0;
    let r = kd::Hkdf(i, il, s, sl, f, fl, key_len, &mut out);
    release_byte_array_elements(env, ikm, i);
    if !salt.is_null() { release_byte_array_elements(env, salt, s); }
    if !info.is_null() { release_byte_array_elements(env, info, f); }
    result_to_jbytes(env, r, out)
}
