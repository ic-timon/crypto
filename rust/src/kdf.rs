//! KDF — bcrypt + argon2id + scrypt + PBKDF2 + HKDF。
//! scrypt 固定 N=32768/r=8/p=1；PBKDF2 固定 SHA-256；HKDF 固定 SHA-256（空 salt → 32 零）。

use crate::utils::{alloc_bool, alloc_copy, ERR_NULL};
use hkdf::Hkdf;
use pbkdf2::pbkdf2_hmac;
use scrypt::scrypt as scrypt_impl;
use sha2::Sha256;
use std::os::raw::c_int;

#[no_mangle]
pub extern "C" fn BcryptHash(password: *const u8, password_len: c_int, cost: c_int, out_len: *mut c_int) -> *mut u8 {
    if password.is_null() || password_len < 0 || cost < 4 || cost > 31 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let password = unsafe { std::slice::from_raw_parts(password, password_len as usize) };
    // bcrypt crate 需要 str，但 Go 版接受 bytes。我们用 modular crypt format。
    match bcrypt::hash(password, (cost as u32).min(31)) {
        Ok(hashed) => unsafe { alloc_copy(hashed.as_bytes(), &mut *out_len) },
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    }
}

#[no_mangle]
pub extern "C" fn BcryptVerify(password: *const u8, password_len: c_int, hash: *const u8, hash_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if password.is_null() || password_len < 0 || hash.is_null() || hash_len <= 0 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let password = unsafe { std::slice::from_raw_parts(password, password_len as usize) };
    let hash_bytes = unsafe { std::slice::from_raw_parts(hash, hash_len as usize) };
    let hash_str = match std::str::from_utf8(hash_bytes) { Ok(s) => s, Err(_) => unsafe { *out_len = 0; return ERR_NULL; } };
    let valid = bcrypt::verify(password, hash_str).unwrap_or(false);
    unsafe { alloc_bool(valid, &mut *out_len) }
}

#[no_mangle]
pub extern "C" fn Argon2idHash(
    password: *const u8, password_len: c_int,
    salt: *const u8, salt_len: c_int,
    time_cost: c_int, memory_cost: c_int, parallelism: c_int, key_len: c_int,
    out_len: *mut c_int,
) -> *mut u8 {
    if password.is_null() || password_len < 0 || salt.is_null() || salt_len < 8 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let password = unsafe { std::slice::from_raw_parts(password, password_len as usize) };
    let salt = unsafe { std::slice::from_raw_parts(salt, salt_len as usize) };
    let params = argon2::Params::new(
        (memory_cost as u32 * 1024).max(8 * 1024).try_into().unwrap_or(8 * 1024),
        time_cost.max(1) as u32,
        parallelism.max(1) as u32,
        Some(key_len.max(1) as usize),
    ).unwrap();
    let argon = argon2::Argon2::new(argon2::Algorithm::Argon2id, argon2::Version::V0x13, params);
    let mut out = vec![0u8; key_len.max(1) as usize];
    match argon.hash_password_into(password, salt, &mut out) {
        Ok(_) => unsafe { alloc_copy(&out, &mut *out_len) },
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    }
}

#[no_mangle]
pub extern "C" fn Scrypt(password: *const u8, password_len: c_int, salt: *const u8, salt_len: c_int, key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if password.is_null() || password_len < 0 || salt.is_null() || salt_len < 0 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let password = unsafe { std::slice::from_raw_parts(password, password_len as usize) };
    let salt = unsafe { std::slice::from_raw_parts(salt, salt_len as usize) };
    let mut out = vec![0u8; key_len.max(1) as usize];
    let params = scrypt::Params::new(15, 8, 1, key_len.max(1) as usize).unwrap(); // log2(32768)=15
    match scrypt_impl(password, salt, &params, &mut out) {
        Ok(_) => unsafe { alloc_copy(&out, &mut *out_len) },
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    }
}

#[no_mangle]
pub extern "C" fn Pbkdf2(password: *const u8, password_len: c_int, salt: *const u8, salt_len: c_int, iterations: c_int, key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if password.is_null() || password_len < 0 || salt.is_null() || salt_len < 0 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let password = unsafe { std::slice::from_raw_parts(password, password_len as usize) };
    let salt = unsafe { std::slice::from_raw_parts(salt, salt_len as usize) };
    let mut out = vec![0u8; key_len.max(1) as usize];
    pbkdf2_hmac::<Sha256>(password, salt, iterations.max(1) as u32, &mut out);
    unsafe { alloc_copy(&out, &mut *out_len) }
}

#[no_mangle]
pub extern "C" fn Hkdf(ikm: *const u8, ikm_len: c_int, salt: *const u8, salt_len: c_int, info: *const u8, info_len: c_int, key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if ikm.is_null() || ikm_len < 0 { unsafe { *out_len = 0 }; return ERR_NULL; }
    let ikm = unsafe { std::slice::from_raw_parts(ikm, ikm_len as usize) };
    let salt = if salt.is_null() || salt_len == 0 { None } else { Some(unsafe { std::slice::from_raw_parts(salt, salt_len as usize) }) };
    let info = if info.is_null() || info_len == 0 { b"" } else { unsafe { std::slice::from_raw_parts(info, info_len as usize) } };
    let hk = match salt {
        Some(s) => Hkdf::<Sha256>::new(Some(s), ikm),
        None => Hkdf::<Sha256>::new(Some(&[0u8; 32]), ikm), // Go: empty salt → 32 zeros
    };
    let mut out = vec![0u8; key_len.max(1) as usize];
    match hk.expand(info, &mut out) {
        Ok(_) => unsafe { alloc_copy(&out, &mut *out_len) },
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    }
}
