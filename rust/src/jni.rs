//! JNI 桥（Android 专用）—— RegisterNatives 模式。
//! 编译为 cdylib 时导出 JNI_OnLoad，注册所有 native 方法。
//! 调用各 crypto 模块的 Rust 实现（不走 C ABI 间接层）。

#![cfg(feature = "jni-bridge")]

use jni::sys::{
    jbyteArray, jint, jlong, JNIBool, JNIEnv as JNIEnvPtr, JavaVM, JNI_VERSION_1_6,
    JNINativeMethod, jclass, jobject,
};
use std::ffi::CString;
use std::os::raw::c_void;

use crate::hash as h;
use crate::hmac as m;
use crate::aead as a;
use crate::cbc as cb;
use crate::stream as st;
use crate::xts as xt;
use crate::kdf as kd;
use crate::ed25519 as ed;
use crate::secp256k1 as sk;
use crate::random as rd;

// JNI 辅助
use jni::objects::{JClass, JByteArray, JValue};
use jni::JNIEnv;

fn get_bytes(env: &mut JNIEnv, arr: jbyteArray) -> Option<Vec<u8>> {
    JByteArray::from(arr).as_ref().ok().and_then(|a| env.convert_byte_array(a).ok())
}

fn make_byte_array(env: &mut JNIEnv, data: &[u8]) -> jbyteArray {
    env.byte_array_from_vec(data).unwrap_or(std::ptr::null_mut())
}

fn make_result(env: &mut JNIEnv, result_ptr: *mut u8, out_len: i32) -> jbyteArray {
    if result_ptr.is_null() || out_len <= 0 {
        // Throw EncException
        let _ = env.throw_new("mobi/timon/crypto/EncException", "operation failed");
        return std::ptr::null_mut();
    }
    let slice = unsafe { std::slice::from_raw_parts(result_ptr, out_len as usize) };
    let arr = make_byte_array(env, slice);
    crate::utils::enc_free(result_ptr, out_len);
    arr
}

fn make_bool(env: &mut JNIEnv, result_ptr: *mut u8, out_len: i32) -> jboolean {
    if result_ptr.is_null() || out_len <= 0 {
        let _ = env.throw_new("mobi/timon/crypto/EncException", "operation failed");
        return 0;
    }
    let val = unsafe { *result_ptr };
    crate::utils::enc_free(result_ptr, out_len);
    val as jboolean
}

type JniFn1 = unsafe extern "system" fn(*mut JNIEnv, jobject, jbyteArray) -> jbyteArray;
type JniFn2 = unsafe extern "system" fn(*mut JNIEnv, jobject, jbyteArray, jbyteArray) -> jbyteArray;
type JniFn0 = unsafe extern "system" fn(*mut JNIEnv, jobject) -> jbyteArray;
type JniBoolFn3 = unsafe extern "system" fn(*mut JNIEnv, jobject, jbyteArray, jbyteArray, jbyteArray) -> JNIBool;

// 宏：生成 JNI wrapper（单 ByteArray 入 → ByteArray 出）
macro_rules! jni_wrap_1 {
    ($name:ident, $rust_fn:path) => {
        unsafe extern "system" fn $name(env: *mut JNIEnv, _obj: jobject, data: jbyteArray) -> jbyteArray {
            let mut env = JNIEnv::from_raw(env).unwrap();
            let data = match get_bytes(&mut env, data) { Some(d) => d, None => return std::ptr::null_mut() };
            let data_ptr = data.as_ptr();
            let data_len = data.len() as std::os::raw::c_int;
            let mut out_len: std::os::raw::c_int = 0;
            let result = $rust_fn(data_ptr, data_len, &mut out_len);
            make_result(&mut env, result, out_len)
        }
    };
}

// 宏：生成 JNI wrapper（双 ByteArray 入 → ByteArray 出）
macro_rules! jni_wrap_2 {
    ($name:ident, $rust_fn:path) => {
        unsafe extern "system" fn $name(env: *mut JNIEnv, _obj: jobject, a1: jbyteArray, a2: jbyteArray) -> jbyteArray {
            let mut env = JNIEnv::from_raw(env).unwrap();
            let a1 = match get_bytes(&mut env, a1) { Some(d) => d, None => return std::ptr::null_mut() };
            let a2 = match get_bytes(&mut env, a2) { Some(d) => d, None => return std::ptr::null_mut() };
            let mut out_len: std::os::raw::c_int = 0;
            let result = $rust_fn(a1.as_ptr(), a1.len() as std::os::raw::c_int,
                                   a2.as_ptr(), a2.len() as std::os::raw::c_int, &mut out_len);
            make_result(&mut env, result, out_len)
        }
    };
}

// ── Hash ──────────────────────────────────────────────────────
jni_wrap_1!(jni_sha1, h::Sha1);
jni_wrap_1!(jni_sha256, h::Sha256);
jni_wrap_1!(jni_sha384, h::Sha384);
jni_wrap_1!(jni_sha512, h::Sha512);
jni_wrap_1!(jni_sha512_256, h::Sha512_256);
jni_wrap_1!(jni_blake2b256, h::Blake2b256);
jni_wrap_1!(jni_md5, h::Md5);
jni_wrap_1!(jni_ripemd160, h::Ripemd160);
jni_wrap_1!(jni_keccak256, h::Keccak256);
jni_wrap_1!(jni_keccak512, h::Keccak512);

// ── Hmac ──────────────────────────────────────────────────────
jni_wrap_2!(jni_hmac_sha256, m::HmacSha256);
jni_wrap_2!(jni_hmac_sha512, m::HmacSha512);
jni_wrap_2!(jni_hmac_sha1, m::HmacSha1);

// ── Random ────────────────────────────────────────────────────
unsafe extern "system" fn jni_random_bytes(env: *mut JNIEnv, _obj: jobject, length: jint) -> jbyteArray {
    let mut env = JNIEnv::from_raw(env).unwrap();
    let mut out_len: std::os::raw::c_int = 0;
    let result = rd::RandomBytes(length, &mut out_len);
    make_result(&mut env, result, out_len)
}

// ── JNI_OnLoad ────────────────────────────────────────────────

#[no_mangle]
pub extern "system" fn JNI_OnLoad(vm: *mut JavaVM, _reserved: *mut c_void) -> jint {
    let vm = unsafe { jni::JavaVM::from_raw(vm) }.unwrap();
    let mut env = vm.get_env().unwrap();

    // 注册 Hash 方法
    let hash_methods = [
        JNINativeMethod { name: CString::new("sha1").unwrap().into_raw(), sig: CString::new("([B)[B").unwrap().into_raw(), fnPtr: jni_sha1 as *mut c_void },
        JNINativeMethod { name: CString::new("sha256").unwrap().into_raw(), sig: CString::new("([B)[B").unwrap().into_raw(), fnPtr: jni_sha256 as *mut c_void },
        JNINativeMethod { name: CString::new("sha384").unwrap().into_raw(), sig: CString::new("([B)[B").unwrap().into_raw(), fnPtr: jni_sha384 as *mut c_void },
        JNINativeMethod { name: CString::new("sha512").unwrap().into_raw(), sig: CString::new("([B)[B").unwrap().into_raw(), fnPtr: jni_sha512 as *mut c_void },
        JNINativeMethod { name: CString::new("sha512_256").unwrap().into_raw(), sig: CString::new("([B)[B").unwrap().into_raw(), fnPtr: jni_sha512_256 as *mut c_void },
        JNINativeMethod { name: CString::new("blake2b256").unwrap().into_raw(), sig: CString::new("([B)[B").unwrap().into_raw(), fnPtr: jni_blake2b256 as *mut c_void },
        JNINativeMethod { name: CString::new("md5").unwrap().into_raw(), sig: CString::new("([B)[B").unwrap().into_raw(), fnPtr: jni_md5 as *mut c_void },
        JNINativeMethod { name: CString::new("ripemd160").unwrap().into_raw(), sig: CString::new("([B)[B").unwrap().into_raw(), fnPtr: jni_ripemd160 as *mut c_void },
        JNINativeMethod { name: CString::new("keccak256").unwrap().into_raw(), sig: CString::new("([B)[B").unwrap().into_raw(), fnPtr: jni_keccak256 as *mut c_void },
        JNINativeMethod { name: CString::new("keccak512").unwrap().into_raw(), sig: CString::new("([B)[B").unwrap().into_raw(), fnPtr: jni_keccak512 as *mut c_void },
    ];
    let _ = env.register_native_methods("mobi/timon/crypto/Hash", &hash_methods);

    // Hmac
    let hmac_methods = [
        JNINativeMethod { name: CString::new("hmacSha256").unwrap().into_raw(), sig: CString::new("([B[B)[B").unwrap().into_raw(), fnPtr: jni_hmac_sha256 as *mut c_void },
        JNINativeMethod { name: CString::new("hmacSha512").unwrap().into_raw(), sig: CString::new("([B[B)[B").unwrap().into_raw(), fnPtr: jni_hmac_sha512 as *mut c_void },
        JNINativeMethod { name: CString::new("hmacSha1").unwrap().into_raw(), sig: CString::new("([B[B)[B").unwrap().into_raw(), fnPtr: jni_hmac_sha1 as *mut c_void },
    ];
    let _ = env.register_native_methods("mobi/timon/crypto/Hmac", &hmac_methods);

    // Random
    let random_methods = [
        JNINativeMethod { name: CString::new("bytes").unwrap().into_raw(), sig: CString::new("(I)[B").unwrap().into_raw(), fnPtr: jni_random_bytes as *mut c_void },
    ];
    let _ = env.register_native_methods("mobi/timon/crypto/Random", &random_methods);

    // TODO: register remaining objects (Aead, Cbc, Stream, Xts, Kdf, Ed25519, Secp256k1, Rsa, Ecdsa, Bls)

    JNI_VERSION_1_6
}
