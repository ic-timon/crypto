//! Stream ciphers — AES-CTR（nonce(16)‖ct）+ ChaCha20（nonce(12)‖ct，无 MAC）。

use crate::utils::{alloc_copy, ERR_NULL};
use aes::cipher::{KeyInit, KeyIvInit, StreamCipher};
use rand::RngCore;
use std::os::raw::c_int;

fn make_aes_ctr(key: &[u8], nonce: &[u8]) -> Option<Box<dyn StreamCipher>> {
    use ctr::Ctr128BE;
    macro_rules! make {
        ($aes:ty) => {
            Some(Box::new(Ctr128BE::<$aes>::new(key.into(), nonce.into())))
        }
    }
    match key.len() {
        16 => make!(aes::Aes128),
        24 => make!(aes::Aes192),
        32 => make!(aes::Aes256),
        _ => None,
    }
}

#[no_mangle]
pub extern "C" fn AesCtrEncrypt(pt: *const u8, pt_len: c_int, key: *const u8, key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if pt.is_null() || pt_len < 0 || key.is_null() { unsafe { *out_len = 0 }; return ERR_NULL; }
    let pt = unsafe { std::slice::from_raw_parts(pt, pt_len as usize) };
    let key = unsafe { std::slice::from_raw_parts(key, key_len as usize) };
    let mut nonce = [0u8; 16];
    rand::thread_rng().fill_bytes(&mut nonce);
    let mut cipher = match make_aes_ctr(key, &nonce) { Some(c) => c, None => unsafe { *out_len = 0; return ERR_NULL; } };
    let mut buf = pt.to_vec();
    cipher.apply_keystream(&mut buf);
    let mut out = Vec::with_capacity(16 + buf.len());
    out.extend_from_slice(&nonce);
    out.extend_from_slice(&buf);
    unsafe { alloc_copy(&out, &mut *out_len) }
}

#[no_mangle]
pub extern "C" fn AesCtrDecrypt(ct: *const u8, ct_len: c_int, key: *const u8, key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if ct.is_null() || ct_len < 16 || key.is_null() { unsafe { *out_len = 0 }; return ERR_NULL; }
    let data = unsafe { std::slice::from_raw_parts(ct, ct_len as usize) };
    let key = unsafe { std::slice::from_raw_parts(key, key_len as usize) };
    let nonce = &data[..16];
    let ct = &data[16..];
    let mut cipher = match make_aes_ctr(key, nonce) { Some(c) => c, None => unsafe { *out_len = 0; return ERR_NULL; } };
    let mut buf = ct.to_vec();
    cipher.apply_keystream(&mut buf);
    unsafe { alloc_copy(&buf, &mut *out_len) }
}

#[no_mangle]
pub extern "C" fn ChaCha20Encrypt(pt: *const u8, pt_len: c_int, key: *const u8, key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if pt.is_null() || pt_len < 0 || key.is_null() || key_len != 32 { unsafe { *out_len = 0 }; return ERR_NULL; }
    let pt = unsafe { std::slice::from_raw_parts(pt, pt_len as usize) };
    let key = unsafe { std::slice::from_raw_parts(key, 32) };
    let mut nonce = [0u8; 12];
    rand::thread_rng().fill_bytes(&mut nonce);
    let mut cipher = chacha20::ChaCha20::new(key.into(), &nonce.into());
    let mut buf = pt.to_vec();
    cipher.apply_keystream(&mut buf);
    let mut out = Vec::with_capacity(12 + buf.len());
    out.extend_from_slice(&nonce);
    out.extend_from_slice(&buf);
    unsafe { alloc_copy(&out, &mut *out_len) }
}

#[no_mangle]
pub extern "C" fn ChaCha20Decrypt(ct: *const u8, ct_len: c_int, key: *const u8, key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if ct.is_null() || ct_len < 12 || key.is_null() || key_len != 32 { unsafe { *out_len = 0 }; return ERR_NULL; }
    let data = unsafe { std::slice::from_raw_parts(ct, ct_len as usize) };
    let key = unsafe { std::slice::from_raw_parts(key, 32) };
    let nonce = &data[..12];
    let ct = &data[12..];
    let mut cipher = chacha20::ChaCha20::new(key.into(), nonce.into());
    let mut buf = ct.to_vec();
    cipher.apply_keystream(&mut buf);
    unsafe { alloc_copy(&buf, &mut *out_len) }
}
