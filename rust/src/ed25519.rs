//! Ed25519 — generateKey / sign / verify。
//! Go 格式：generateKey 出 pub(32) ‖ priv(64)=seed(32)+pub(32) 共 96B。
//! sign 接受 64-byte private key（Go priv 格式）。

use crate::utils::{alloc_bool, alloc_copy, ERR_NULL};
use ed25519_dalek::{Signature, Signer, SigningKey, Verifier, VerifyingKey};
use rand::rngs::OsRng;
use std::os::raw::c_int;

#[no_mangle]
pub extern "C" fn Ed25519GenerateKey(out_len: *mut c_int) -> *mut u8 {
    let sk = SigningKey::generate(&mut OsRng);
    let vk = sk.verifying_key();
    let mut out = Vec::with_capacity(96);
    out.extend_from_slice(&vk.to_bytes());       // pub(32)
    out.extend_from_slice(&sk.to_keypair_bytes()); // priv(64) = seed(32)+pub(32)
    unsafe { alloc_copy(&out, &mut *out_len) }
}

#[no_mangle]
pub extern "C" fn Ed25519Sign(message: *const u8, message_len: c_int, private_key: *const u8, private_key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if message.is_null() || message_len < 0 || private_key.is_null() || private_key_len != 64 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let msg = unsafe { std::slice::from_raw_parts(message, message_len as usize) };
    let priv_bytes = unsafe { std::slice::from_raw_parts(private_key, 64) };
    let mut kp = [0u8; 64];
    kp.copy_from_slice(priv_bytes);
    let sk = match SigningKey::from_keypair_bytes(&kp) {
        Ok(k) => k,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let sig: Signature = sk.sign(msg);
    unsafe { alloc_copy(&sig.to_bytes(), &mut *out_len) }
}

#[no_mangle]
pub extern "C" fn Ed25519Verify(message: *const u8, message_len: c_int, signature: *const u8, signature_len: c_int, public_key: *const u8, public_key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if message.is_null() || message_len < 0 || signature.is_null() || signature_len != 64 || public_key.is_null() || public_key_len != 32 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let msg = unsafe { std::slice::from_raw_parts(message, message_len as usize) };
    let sig_bytes = unsafe { std::slice::from_raw_parts(signature, 64) };
    let pk_bytes = unsafe { std::slice::from_raw_parts(public_key, 32) };
    let mut pk = [0u8; 32];
    pk.copy_from_slice(pk_bytes);
    let vk = match VerifyingKey::from_bytes(&pk) {
        Ok(k) => k,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let mut sig_arr = [0u8; 64];
    sig_arr.copy_from_slice(sig_bytes);
    let sig = Signature::from_bytes(&sig_arr);
    let valid = vk.verify(msg, &sig).is_ok();
    unsafe { alloc_bool(valid, &mut *out_len) }
}
