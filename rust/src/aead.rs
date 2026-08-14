//! AEAD — AES-GCM + ChaCha20-Poly1305。wire 格式：nonce(12) ‖ ct ‖ tag(16)，无 AAD。
//! key 长度决定 AES 变体：16 → AES-128-GCM，32 → AES-256-GCM。

use crate::utils::{alloc_copy, ERR_NULL};
use aes_gcm::{Aes128Gcm, Aes256Gcm, aead::{Aead, KeyInit, Payload, generic_array::GenericArray, consts::U12}};
use chacha20poly1305::ChaCha20Poly1305;
use rand::RngCore;
use std::os::raw::c_int;

/// 12B nonce → GenericArray（显式类型，解决宏展开时的类型推断问题）。
fn nonce_ref(slice: &[u8]) -> &GenericArray<u8, U12> {
    GenericArray::from_slice(slice)
}

macro_rules! aead_encrypt {
    ($cipher:expr, $pt:expr, $out:expr) => {{
        let mut nonce = [0u8; 12];
        rand::thread_rng().fill_bytes(&mut nonce);
        match $cipher.encrypt(nonce_ref(&nonce), Payload { msg: $pt, aad: b"" }) {
            Ok(ct) => {
                let mut out = Vec::with_capacity(12 + ct.len());
                out.extend_from_slice(&nonce);
                out.extend_from_slice(&ct);
                alloc_copy(&out, $out)
            }
            Err(_) => { *$out = 0; std::ptr::null_mut() }
        }
    }};
}

macro_rules! aead_decrypt {
    ($cipher:expr, $data:expr, $out:expr) => {{
        if $data.len() < 12 { *$out = 0; std::ptr::null_mut() }
        else {
            let nonce = nonce_ref(&$data[..12]);
            match $cipher.decrypt(nonce, Payload { msg: &$data[12..], aad: b"" }) {
                Ok(pt) => alloc_copy(&pt, $out),
                Err(_) => { *$out = 0; std::ptr::null_mut() }
            }
        }
    }};
}

#[no_mangle]
pub extern "C" fn AesGcmEncrypt(pt: *const u8, pt_len: c_int, key: *const u8, key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if pt.is_null() || pt_len < 0 || key.is_null() { unsafe { *out_len = 0 }; return ERR_NULL; }
    let pt = unsafe { std::slice::from_raw_parts(pt, pt_len as usize) };
    let key = unsafe { std::slice::from_raw_parts(key, key_len as usize) };
    let out = unsafe { &mut *out_len };
    match key.len() {
        16 => { let c = Aes128Gcm::new_from_slice(key).unwrap(); aead_encrypt!(c, pt, out) }
        32 => { let c = Aes256Gcm::new_from_slice(key).unwrap(); aead_encrypt!(c, pt, out) }
        _ => { *out = 0; ERR_NULL }
    }
}

#[no_mangle]
pub extern "C" fn AesGcmDecrypt(ct: *const u8, ct_len: c_int, key: *const u8, key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if ct.is_null() || ct_len < 0 || key.is_null() { unsafe { *out_len = 0 }; return ERR_NULL; }
    let ct = unsafe { std::slice::from_raw_parts(ct, ct_len as usize) };
    let key = unsafe { std::slice::from_raw_parts(key, key_len as usize) };
    let out = unsafe { &mut *out_len };
    match key.len() {
        16 => { let c = Aes128Gcm::new_from_slice(key).unwrap(); aead_decrypt!(c, ct, out) }
        32 => { let c = Aes256Gcm::new_from_slice(key).unwrap(); aead_decrypt!(c, ct, out) }
        _ => { *out = 0; ERR_NULL }
    }
}

#[no_mangle]
pub extern "C" fn ChaCha20Poly1305Encrypt(pt: *const u8, pt_len: c_int, key: *const u8, key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if pt.is_null() || pt_len < 0 || key.is_null() || key_len != 32 { unsafe { *out_len = 0 }; return ERR_NULL; }
    let pt = unsafe { std::slice::from_raw_parts(pt, pt_len as usize) };
    let key = unsafe { std::slice::from_raw_parts(key, 32) };
    let c = ChaCha20Poly1305::new_from_slice(key).unwrap();
    aead_encrypt!(c, pt, unsafe { &mut *out_len })
}

#[no_mangle]
pub extern "C" fn ChaCha20Poly1305Decrypt(ct: *const u8, ct_len: c_int, key: *const u8, key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if ct.is_null() || ct_len < 0 || key.is_null() || key_len != 32 { unsafe { *out_len = 0 }; return ERR_NULL; }
    let ct = unsafe { std::slice::from_raw_parts(ct, ct_len as usize) };
    let key = unsafe { std::slice::from_raw_parts(key, 32) };
    let c = ChaCha20Poly1305::new_from_slice(key).unwrap();
    aead_decrypt!(c, ct, unsafe { &mut *out_len })
}
