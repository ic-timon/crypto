//! ECDSA — P-256/384（P-224 无 Rust crate，P-521 pkcs8 不支持，暂不支持）。
//! 私钥/公钥 PKCS#8 DER。签名格式：[4B rLen（BE）][r BE][s BE]，消息先 SHA-256。

use crate::utils::{alloc_bool, alloc_copy, ERR_NULL};
use elliptic_curve::pkcs8::{DecodePrivateKey, DecodePublicKey, EncodePrivateKey, EncodePublicKey};
use rand::rngs::OsRng;
use sha2::{Digest, Sha256};
use signature::{Signer, Verifier};
use std::os::raw::c_int;

fn strip_leading_zeros(bytes: &[u8]) -> &[u8] {
    let mut s = 0;
    while s + 1 < bytes.len() && bytes[s] == 0 { s += 1; }
    &bytes[s..]
}

fn encode_sig(r: &[u8], s: &[u8]) -> Vec<u8> {
    let r = strip_leading_zeros(r);
    let s = strip_leading_zeros(s);
    let mut out = Vec::with_capacity(4 + r.len() + s.len());
    out.extend_from_slice(&(r.len() as u32).to_be_bytes());
    out.extend_from_slice(r);
    out.extend_from_slice(s);
    out
}

fn decode_sig(sig: &[u8]) -> Option<(Vec<u8>, Vec<u8>)> {
    if sig.len() < 5 { return None; }
    let r_len = u32::from_be_bytes([sig[0], sig[1], sig[2], sig[3]]) as usize;
    if r_len == 0 || 4 + r_len > sig.len() { return None; }
    Some((sig[4..4 + r_len].to_vec(), sig[4 + r_len..].to_vec()))
}

// ── P-256 ──────────────────────────────────────────────

fn p256_gen() -> Option<Vec<u8>> {
    let sk = p256::ecdsa::SigningKey::random(&mut OsRng);
    sk.to_pkcs8_der().ok().map(|d| d.as_bytes().to_vec())
}
fn p256_sign(msg: &[u8], der: &[u8]) -> Option<Vec<u8>> {
    let sk = p256::ecdsa::SigningKey::from_pkcs8_der(der).ok()?;
    let hashed = Sha256::digest(msg);
    let sig: p256::ecdsa::Signature = sk.sign(&hashed);
    let bytes = sig.to_bytes();
    Some(encode_sig(&bytes[..32], &bytes[32..]))
}
fn p256_verify(msg: &[u8], sig_bytes: &[u8], pub_der: &[u8]) -> bool {
    let vk = match p256::ecdsa::VerifyingKey::from_public_key_der(pub_der) { Ok(k) => k, Err(_) => return false };
    let (r, s) = match decode_sig(sig_bytes) { Some(x) => x, None => return false };
    let mut fixed = [0u8; 64];
    let ro = 32 - r.len().min(32);
    fixed[ro..ro + r.len().min(32)].copy_from_slice(&r[..r.len().min(32)]);
    let so = 64 - s.len().min(32);
    fixed[so..so + s.len().min(32)].copy_from_slice(&s[..s.len().min(32)]);
    let sig = match p256::ecdsa::Signature::from_slice(&fixed) { Ok(s) => s, Err(_) => return false };
    let hashed = Sha256::digest(msg);
    vk.verify(&hashed, &sig).is_ok()
}
fn p256_to_pub(der: &[u8]) -> Option<Vec<u8>> {
    let sk = p256::ecdsa::SigningKey::from_pkcs8_der(der).ok()?;
    p256::ecdsa::VerifyingKey::from(&sk).to_public_key_der().ok().map(|d| d.to_vec())
}

// ── P-384 ──────────────────────────────────────────────

fn p384_gen() -> Option<Vec<u8>> {
    let sk = p384::ecdsa::SigningKey::random(&mut OsRng);
    sk.to_pkcs8_der().ok().map(|d| d.as_bytes().to_vec())
}
fn p384_sign(msg: &[u8], der: &[u8]) -> Option<Vec<u8>> {
    let sk = p384::ecdsa::SigningKey::from_pkcs8_der(der).ok()?;
    let hashed = Sha256::digest(msg);
    let sig: p384::ecdsa::Signature = sk.sign(&hashed);
    let bytes = sig.to_bytes();
    Some(encode_sig(&bytes[..48], &bytes[48..]))
}
fn p384_verify(msg: &[u8], sig_bytes: &[u8], pub_der: &[u8]) -> bool {
    let vk = match p384::ecdsa::VerifyingKey::from_public_key_der(pub_der) { Ok(k) => k, Err(_) => return false };
    let (r, s) = match decode_sig(sig_bytes) { Some(x) => x, None => return false };
    let mut fixed = [0u8; 96];
    let ro = 48 - r.len().min(48);
    fixed[ro..ro + r.len().min(48)].copy_from_slice(&r[..r.len().min(48)]);
    let so = 96 - s.len().min(48);
    fixed[so..so + s.len().min(48)].copy_from_slice(&s[..s.len().min(48)]);
    let sig = match p384::ecdsa::Signature::from_slice(&fixed) { Ok(s) => s, Err(_) => return false };
    let hashed = Sha256::digest(msg);
    vk.verify(&hashed, &sig).is_ok()
}
fn p384_to_pub(der: &[u8]) -> Option<Vec<u8>> {
    let sk = p384::ecdsa::SigningKey::from_pkcs8_der(der).ok()?;
    p384::ecdsa::VerifyingKey::from(&sk).to_public_key_der().ok().map(|d| d.to_vec())
}

// ── FFI ────────────────────────────────────────────────

#[no_mangle]
pub extern "C" fn EcdsaGenerateKey(curve_bits: c_int, out_len: *mut c_int) -> *mut u8 {
    let der = match curve_bits {
        256 => p256_gen(),
        384 => p384_gen(),
        _ => None,
    };
    match der {
        Some(d) => unsafe { alloc_copy(&d, &mut *out_len) },
        None => unsafe { *out_len = 0; ERR_NULL },
    }
}

#[no_mangle]
pub extern "C" fn EcdsaSign(message: *const u8, message_len: c_int, private_key: *const u8, private_key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if message.is_null() || message_len <= 0 || private_key.is_null() || private_key_len <= 0 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let msg = unsafe { std::slice::from_raw_parts(message, message_len as usize) };
    let der = unsafe { std::slice::from_raw_parts(private_key, private_key_len as usize) };
    let sig = p256_sign(msg, der).or_else(|| p384_sign(msg, der));
    match sig {
        Some(s) => unsafe { alloc_copy(&s, &mut *out_len) },
        None => unsafe { *out_len = 0; ERR_NULL },
    }
}

#[no_mangle]
pub extern "C" fn EcdsaVerify(message: *const u8, message_len: c_int, signature: *const u8, signature_len: c_int, public_key: *const u8, public_key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if message.is_null() || message_len <= 0 || signature.is_null() || signature_len < 5 || public_key.is_null() || public_key_len <= 0 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let msg = unsafe { std::slice::from_raw_parts(message, message_len as usize) };
    let sig = unsafe { std::slice::from_raw_parts(signature, signature_len as usize) };
    let pub_der = unsafe { std::slice::from_raw_parts(public_key, public_key_len as usize) };
    let valid = p256_verify(msg, sig, pub_der) || p384_verify(msg, sig, pub_der);
    unsafe { alloc_bool(valid, &mut *out_len) }
}

#[no_mangle]
pub extern "C" fn EcdsaPrivateKeyToPublicKey(private_key: *const u8, private_key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if private_key.is_null() || private_key_len <= 0 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let der = unsafe { std::slice::from_raw_parts(private_key, private_key_len as usize) };
    let pub_der = p256_to_pub(der).or_else(|| p384_to_pub(der));
    match pub_der {
        Some(d) => unsafe { alloc_copy(&d, &mut *out_len) },
        None => unsafe { *out_len = 0; ERR_NULL },
    }
}
