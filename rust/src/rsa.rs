//! RSA — generateKey / encrypt(OAEP-SHA256) / decrypt / sign(PKCS1v15-SHA256) / verify / privateKeyToPublicKey。
//! 私钥 PKCS#8 DER，公钥 PKIX/SPKI DER。

use crate::utils::{alloc_bool, alloc_copy, ERR_NULL};
use rand::rngs::OsRng;
use rsa::{
    Oaep, RsaPrivateKey, RsaPublicKey,
    pkcs8::{DecodePrivateKey, DecodePublicKey, EncodePrivateKey, EncodePublicKey},
};
use sha2::{Digest, Sha256};
use std::os::raw::c_int;

#[no_mangle]
pub extern "C" fn RsaGenerateKey(bits: c_int, out_len: *mut c_int) -> *mut u8 {
    if !matches!(bits, 2048 | 3072 | 4096) {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let priv_key = match RsaPrivateKey::new(&mut OsRng, bits as usize) {
        Ok(k) => k,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let der = match priv_key.to_pkcs8_der() {
        Ok(d) => d,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    unsafe { alloc_copy(der.as_bytes(), &mut *out_len) }
}

#[no_mangle]
pub extern "C" fn RsaEncrypt(pt: *const u8, pt_len: c_int, pub_key: *const u8, pub_key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if pt.is_null() || pt_len < 0 || pub_key.is_null() || pub_key_len <= 0 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let pt = unsafe { std::slice::from_raw_parts(pt, pt_len as usize) };
    let key_der = unsafe { std::slice::from_raw_parts(pub_key, pub_key_len as usize) };
    let pub_key = match RsaPublicKey::from_public_key_der(key_der) {
        Ok(k) => k,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    match pub_key.encrypt(&mut OsRng, Oaep::new::<Sha256>(), pt) {
        Ok(ct) => unsafe { alloc_copy(&ct, &mut *out_len) },
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    }
}

#[no_mangle]
pub extern "C" fn RsaDecrypt(ct: *const u8, ct_len: c_int, priv_key: *const u8, priv_key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if ct.is_null() || ct_len < 0 || priv_key.is_null() || priv_key_len <= 0 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let ct = unsafe { std::slice::from_raw_parts(ct, ct_len as usize) };
    let key_der = unsafe { std::slice::from_raw_parts(priv_key, priv_key_len as usize) };
    let priv_key = match RsaPrivateKey::from_pkcs8_der(key_der) {
        Ok(k) => k,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    match priv_key.decrypt(Oaep::new::<Sha256>(), ct) {
        Ok(pt) => unsafe { alloc_copy(&pt, &mut *out_len) },
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    }
}

#[no_mangle]
pub extern "C" fn RsaSign(message: *const u8, message_len: c_int, priv_key: *const u8, priv_key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if message.is_null() || message_len < 0 || priv_key.is_null() || priv_key_len <= 0 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let msg = unsafe { std::slice::from_raw_parts(message, message_len as usize) };
    let key_der = unsafe { std::slice::from_raw_parts(priv_key, priv_key_len as usize) };
    let priv_key = match RsaPrivateKey::from_pkcs8_der(key_der) {
        Ok(k) => k,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let hashed = Sha256::digest(msg);
    let signing_key = rsa::pkcs1v15::SigningKey::<Sha256>::new(priv_key);
    use rsa::signature::Signer;
    let sig = signing_key.sign(&hashed);
    let sig_bytes: Box<[u8]> = sig.into();
    unsafe { alloc_copy(&sig_bytes, &mut *out_len) }
}

#[no_mangle]
pub extern "C" fn RsaVerify(message: *const u8, message_len: c_int, signature: *const u8, signature_len: c_int, pub_key: *const u8, pub_key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if message.is_null() || message_len < 0 || signature.is_null() || signature_len <= 0 || pub_key.is_null() || pub_key_len <= 0 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let msg = unsafe { std::slice::from_raw_parts(message, message_len as usize) };
    let sig_bytes = unsafe { std::slice::from_raw_parts(signature, signature_len as usize) };
    let key_der = unsafe { std::slice::from_raw_parts(pub_key, pub_key_len as usize) };
    let pub_key = match RsaPublicKey::from_public_key_der(key_der) {
        Ok(k) => k,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let hashed = Sha256::digest(msg);
    let verifying_key = rsa::pkcs1v15::VerifyingKey::<Sha256>::new(pub_key);
    use rsa::signature::Verifier;
    let sig_arr: &[u8] = sig_bytes;
    // PKCS1v15 signature for RSA: need to parse signature as the right type
    let sig = match rsa::pkcs1v15::Signature::try_from(sig_arr) {
        Ok(s) => s,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let valid = verifying_key.verify(&hashed, &sig).is_ok();
    unsafe { alloc_bool(valid, &mut *out_len) }
}

#[no_mangle]
pub extern "C" fn RsaPrivateKeyToPublicKey(priv_key: *const u8, priv_key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if priv_key.is_null() || priv_key_len <= 0 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let key_der = unsafe { std::slice::from_raw_parts(priv_key, priv_key_len as usize) };
    let priv_key = match RsaPrivateKey::from_pkcs8_der(key_der) {
        Ok(k) => k,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let pub_key = RsaPublicKey::from(&priv_key);
    let der = match pub_key.to_public_key_der() {
        Ok(d) => d,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    unsafe { alloc_copy(der.as_bytes(), &mut *out_len) }
}
