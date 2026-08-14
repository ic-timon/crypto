//! BLS12-381 签名 — minimal-pubkey-size（pk G1 48B, sig G2 96B）。
//! DST: BLS_SIG_BLS12381G2_XMD:SHA-256_SSWU_RO_POP_

use crate::utils::{alloc_bool, alloc_copy, ERR_NULL};
use blst::min_pk::{PublicKey, SecretKey, Signature};
use blst::BLST_ERROR;
use rand::rngs::OsRng;
use rand::RngCore;
use std::os::raw::c_int;

const DST: &[u8] = b"BLS_SIG_BLS12381G2_XMD:SHA-256_SSWU_RO_POP_";

#[no_mangle]
pub extern "C" fn BlsGenerateKey(out_len: *mut c_int) -> *mut u8 {
    let mut ikm = [0u8; 32];
    OsRng.fill_bytes(&mut ikm);
    let sk = match SecretKey::key_gen(&ikm, &[]) {
        Ok(k) => k,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let sk_bytes = sk.to_bytes();
    unsafe { alloc_copy(&sk_bytes, &mut *out_len) }
}

#[no_mangle]
pub extern "C" fn BlsPrivateKeyToPublicKey(private_key: *const u8, private_key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if private_key.is_null() || private_key_len != 32 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let sk_bytes = unsafe { std::slice::from_raw_parts(private_key, 32) };
    let sk = match SecretKey::from_bytes(sk_bytes) {
        Ok(k) => k,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let pk = sk.sk_to_pk();
    unsafe { alloc_copy(&pk.to_bytes(), &mut *out_len) }
}

#[no_mangle]
pub extern "C" fn BlsSign(message: *const u8, message_len: c_int, private_key: *const u8, private_key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if message.is_null() || message_len < 0 || private_key.is_null() || private_key_len != 32 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let msg = unsafe { std::slice::from_raw_parts(message, message_len as usize) };
    let sk_bytes = unsafe { std::slice::from_raw_parts(private_key, 32) };
    let sk = match SecretKey::from_bytes(sk_bytes) {
        Ok(k) => k,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let sig = sk.sign(msg, DST, &[]);
    unsafe { alloc_copy(&sig.to_bytes(), &mut *out_len) }
}

#[no_mangle]
pub extern "C" fn BlsVerify(message: *const u8, message_len: c_int, signature: *const u8, signature_len: c_int, public_key: *const u8, public_key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if message.is_null() || message_len < 0 || signature.is_null() || signature_len != 96 || public_key.is_null() || public_key_len != 48 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let msg = unsafe { std::slice::from_raw_parts(message, message_len as usize) };
    let sig_bytes = unsafe { std::slice::from_raw_parts(signature, 96) };
    let pk_bytes = unsafe { std::slice::from_raw_parts(public_key, 48) };
    let sig = match Signature::from_bytes(sig_bytes) {
        Ok(s) => s,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let pk = match PublicKey::from_bytes(pk_bytes) {
        Ok(p) => p,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let valid = sig.verify(true, msg, DST, &[], &pk, true) == BLST_ERROR::BLST_SUCCESS;
    unsafe { alloc_bool(valid, &mut *out_len) }
}

#[no_mangle]
pub extern "C" fn BlsAggregateSignatures(signatures: *const u8, signatures_len: c_int, count: c_int, out_len: *mut c_int) -> *mut u8 {
    if signatures.is_null() || signatures_len <= 0 || count <= 0 || signatures_len != count * 96 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let data = unsafe { std::slice::from_raw_parts(signatures, signatures_len as usize) };
    let mut sigs = Vec::with_capacity(count as usize);
    for chunk in data.chunks(96) {
        match Signature::from_bytes(chunk) {
            Ok(s) => sigs.push(s),
            Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
        }
    }
    let refs: Vec<&Signature> = sigs.iter().collect();
    let agg = match blst::min_pk::AggregateSignature::aggregate(&refs, false) {
        Ok(a) => a,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let bytes = agg.to_signature().to_bytes();
    unsafe { alloc_copy(&bytes, &mut *out_len) }
}

#[no_mangle]
pub extern "C" fn BlsAggregatePublicKeys(public_keys: *const u8, public_keys_len: c_int, count: c_int, out_len: *mut c_int) -> *mut u8 {
    if public_keys.is_null() || public_keys_len <= 0 || count <= 0 || public_keys_len != count * 48 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let data = unsafe { std::slice::from_raw_parts(public_keys, public_keys_len as usize) };
    let mut pks = Vec::with_capacity(count as usize);
    for chunk in data.chunks(48) {
        match PublicKey::from_bytes(chunk) {
            Ok(p) => pks.push(p),
            Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
        }
    }
    let refs: Vec<&PublicKey> = pks.iter().collect();
    let agg = match blst::min_pk::AggregatePublicKey::aggregate(&refs, false) {
        Ok(a) => a,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let bytes = agg.to_public_key().to_bytes();
    unsafe { alloc_copy(&bytes, &mut *out_len) }
}
