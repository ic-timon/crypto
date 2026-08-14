//! secp256k1 — ECDSA + BIP-340 Schnorr（替代 Go 版 btcec）。
//!
//! 对齐 Go 版函数签名和 wire format，修掉 Go 版 sign/verify 格式不一致 bug：
//! sign 出 65B compact（r‖s‖recoveryId），verify 也解析 compact（65B 或 64B），不再用 DER。

use crate::utils::{alloc_bool, alloc_copy};
use rand::rngs::OsRng;
use secp256k1::{ecdsa, schnorr, Keypair, Message, PublicKey, Secp256k1, SecretKey};
use sha2::{Digest, Sha256};
use std::os::raw::c_int;

const ERR_NULL: *mut u8 = std::ptr::null_mut();

fn schnorr_message_hash(message: &[u8]) -> [u8; 32] {
    Sha256::digest(message).into()
}

/// 任意长度消息 → 32B Message（非 32 字节先 SHA-256）。
fn to_message(msg: &[u8]) -> Message {
    if msg.len() == 32 {
        let mut arr = [0u8; 32];
        arr.copy_from_slice(msg);
        Message::from_digest(arr)
    } else {
        Message::from_digest(schnorr_message_hash(msg))
    }
}

// ── ECDSA ──────────────────────────────────────────────────────

/// 生成随机 secp256k1 私钥（32 字节）。
#[no_mangle]
pub extern "C" fn Secp256k1GenerateKey(out_len: *mut c_int) -> *mut u8 {
    let sk = SecretKey::new(&mut OsRng);
    unsafe { alloc_copy(&sk.secret_bytes(), &mut *out_len) }
}

/// 从私钥派生公钥。compressed=1 → 33B，=0 → 65B。
#[no_mangle]
pub extern "C" fn Secp256k1PrivateKeyToPublicKey(
    private_key: *const u8, private_key_len: c_int,
    compressed: c_int,
    out_len: *mut c_int,
) -> *mut u8 {
    if private_key.is_null() || private_key_len != 32 {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let secp = Secp256k1::new();
    let sk_bytes = unsafe { std::slice::from_raw_parts(private_key, 32) };
    let sk = match SecretKey::from_slice(sk_bytes) {
        Ok(k) => k,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let pk = PublicKey::from_secret_key(&secp, &sk);
    let serialized = if compressed == 1 { pk.serialize().to_vec() } else { pk.serialize_uncompressed().to_vec() };
    unsafe { alloc_copy(&serialized, &mut *out_len) }
}

/// ECDSA 签名。出 65B compact（r‖s‖recoveryId）。
#[no_mangle]
pub extern "C" fn Secp256k1Sign(
    message: *const u8, message_len: c_int,
    private_key: *const u8, private_key_len: c_int,
    out_len: *mut c_int,
) -> *mut u8 {
    if message.is_null() || message_len < 0 || private_key.is_null() || private_key_len != 32 {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let secp = Secp256k1::new();
    let msg = unsafe { std::slice::from_raw_parts(message, message_len as usize) };
    let sk_bytes = unsafe { std::slice::from_raw_parts(private_key, 32) };
    let sk = match SecretKey::from_slice(sk_bytes) {
        Ok(k) => k,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let msg = to_message(msg);
    let rsig = secp.sign_ecdsa_recoverable(&msg, &sk);
    // 0.29: serialize_compact 返回 (RecoveryId, [u8; 64])
    let (recid, compact) = rsig.serialize_compact();
    let mut out = [0u8; 65];
    out[..64].copy_from_slice(&compact);
    out[64] = recid.to_i32() as u8;
    unsafe { alloc_copy(&out, &mut *out_len) }
}

/// ECDSA 验签。接受 65B compact（r‖s‖recoveryId）或 64B compact（r‖s）。
#[no_mangle]
pub extern "C" fn Secp256k1Verify(
    message: *const u8, message_len: c_int,
    signature: *const u8, signature_len: c_int,
    public_key: *const u8, public_key_len: c_int,
    out_len: *mut c_int,
) -> *mut u8 {
    if message.is_null() || message_len < 0
        || signature.is_null() || signature_len < 64
        || public_key.is_null() || public_key_len == 0
    {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let secp = Secp256k1::new();
    let msg = unsafe { std::slice::from_raw_parts(message, message_len as usize) };
    let sig_bytes = unsafe { std::slice::from_raw_parts(signature, signature_len as usize) };
    let pk_bytes = unsafe { std::slice::from_raw_parts(public_key, public_key_len as usize) };

    let pk = match PublicKey::from_slice(pk_bytes) {
        Ok(p) => p,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };

    // 65B → recoverable compact → standard；64B → standard compact
    let sig = if sig_bytes.len() == 65 {
        let recid = match ecdsa::RecoveryId::from_i32(sig_bytes[64] as i32) {
            Ok(r) => r,
            Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
        };
        let mut data64 = [0u8; 64];
        data64.copy_from_slice(&sig_bytes[..64]);
        match ecdsa::RecoverableSignature::from_compact(&data64, recid) {
            Ok(rs) => rs.to_standard(),
            Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
        }
    } else {
        let mut data64 = [0u8; 64];
        data64.copy_from_slice(sig_bytes);
        match ecdsa::Signature::from_compact(&data64) {
            Ok(s) => s,
            Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
        }
    };

    let msg = to_message(msg);
    let valid = secp.verify_ecdsa(&msg, &sig, &pk).is_ok();
    unsafe { alloc_bool(valid, &mut *out_len) }
}

/// 从签名恢复公钥。输入 65B compact + 消息。
#[no_mangle]
pub extern "C" fn Secp256k1RecoverPublicKey(
    message: *const u8, message_len: c_int,
    signature: *const u8, signature_len: c_int,
    compressed: c_int,
    out_len: *mut c_int,
) -> *mut u8 {
    if message.is_null() || message_len < 0 || signature.is_null() || signature_len != 65 {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let msg = unsafe { std::slice::from_raw_parts(message, message_len as usize) };
    let sig_bytes = unsafe { std::slice::from_raw_parts(signature, 65) };

    let recid = match ecdsa::RecoveryId::from_i32(sig_bytes[64] as i32) {
        Ok(r) => r,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let mut data64 = [0u8; 64];
    data64.copy_from_slice(&sig_bytes[..64]);
    let rsig = match ecdsa::RecoverableSignature::from_compact(&data64, recid) {
        Ok(s) => s,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };

    let msg = to_message(msg);
    let pk = match rsig.recover(&msg) {
        Ok(p) => p,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let serialized = if compressed == 1 { pk.serialize().to_vec() } else { pk.serialize_uncompressed().to_vec() };
    unsafe { alloc_copy(&serialized, &mut *out_len) }
}

// ── Schnorr (BIP-340) ──────────────────────────────────────────

/// BIP-340 Schnorr 签名。消息先 SHA-256。出 64B。
#[no_mangle]
pub extern "C" fn SchnorrSign(
    message: *const u8, message_len: c_int,
    private_key: *const u8, private_key_len: c_int,
    out_len: *mut c_int,
) -> *mut u8 {
    if message.is_null() || message_len < 0 || private_key.is_null() || private_key_len != 32 {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let secp = Secp256k1::new();
    let msg = unsafe { std::slice::from_raw_parts(message, message_len as usize) };
    let sk_bytes = unsafe { std::slice::from_raw_parts(private_key, 32) };
    let sk = match SecretKey::from_slice(sk_bytes) {
        Ok(k) => k,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let keypair = Keypair::from_secret_key(&secp, &sk);
    let hash = schnorr_message_hash(msg);
    let message = Message::from_digest(hash);
    let sig = secp.sign_schnorr_with_rng(&message, &keypair, &mut OsRng);
    unsafe { alloc_copy(&sig.serialize(), &mut *out_len) }
}

/// BIP-340 Schnorr 验签。消息先 SHA-256。publicKey 32B x-only。
#[no_mangle]
pub extern "C" fn SchnorrVerify(
    message: *const u8, message_len: c_int,
    signature: *const u8, signature_len: c_int,
    public_key: *const u8, public_key_len: c_int,
    out_len: *mut c_int,
) -> *mut u8 {
    if message.is_null() || message_len < 0
        || signature.is_null() || signature_len != 64
        || public_key.is_null() || public_key_len != 32
    {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let secp = Secp256k1::new();
    let msg = unsafe { std::slice::from_raw_parts(message, message_len as usize) };
    let sig_bytes = unsafe { std::slice::from_raw_parts(signature, 64) };
    let pk_bytes = unsafe { std::slice::from_raw_parts(public_key, 32) };

    let xonly = match secp256k1::XOnlyPublicKey::from_slice(pk_bytes) {
        Ok(p) => p,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let sig = match schnorr::Signature::from_slice(sig_bytes) {
        Ok(s) => s,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let hash = schnorr_message_hash(msg);
    let message = Message::from_digest(hash);
    let valid = secp.verify_schnorr(&sig, &message, &xonly).is_ok();
    unsafe { alloc_bool(valid, &mut *out_len) }
}

/// 从私钥派生 x-only 公钥（32B）。
#[no_mangle]
pub extern "C" fn SchnorrPrivateKeyToPublicKey(
    private_key: *const u8, private_key_len: c_int,
    out_len: *mut c_int,
) -> *mut u8 {
    if private_key.is_null() || private_key_len != 32 {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let secp = Secp256k1::new();
    let sk_bytes = unsafe { std::slice::from_raw_parts(private_key, 32) };
    let sk = match SecretKey::from_slice(sk_bytes) {
        Ok(k) => k,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let pk = PublicKey::from_secret_key(&secp, &sk);
    let serialized = pk.serialize(); // 33B
    unsafe { alloc_copy(&serialized[1..], &mut *out_len) } // x-only = compressed[1..]
}

/// Schnorr 对 32B hash 直接签名（Nostr event id 用）。
#[no_mangle]
pub extern "C" fn SchnorrSignHash(
    hash: *const u8, hash_len: c_int,
    private_key: *const u8, private_key_len: c_int,
    out_len: *mut c_int,
) -> *mut u8 {
    if hash.is_null() || hash_len != 32 || private_key.is_null() || private_key_len != 32 {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let secp = Secp256k1::new();
    let hash_bytes = unsafe { std::slice::from_raw_parts(hash, 32) };
    let sk_bytes = unsafe { std::slice::from_raw_parts(private_key, 32) };
    let sk = match SecretKey::from_slice(sk_bytes) {
        Ok(k) => k,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let keypair = Keypair::from_secret_key(&secp, &sk);
    let mut arr = [0u8; 32];
    arr.copy_from_slice(hash_bytes);
    let message = Message::from_digest(arr);
    let sig = secp.sign_schnorr_with_rng(&message, &keypair, &mut OsRng);
    unsafe { alloc_copy(&sig.serialize(), &mut *out_len) }
}

/// Schnorr 对 32B hash 直接验签。
#[no_mangle]
pub extern "C" fn SchnorrVerifyHash(
    hash: *const u8, hash_len: c_int,
    signature: *const u8, signature_len: c_int,
    public_key: *const u8, public_key_len: c_int,
    out_len: *mut c_int,
) -> *mut u8 {
    if hash.is_null() || hash_len != 32
        || signature.is_null() || signature_len != 64
        || public_key.is_null() || public_key_len != 32
    {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let secp = Secp256k1::new();
    let hash_bytes = unsafe { std::slice::from_raw_parts(hash, 32) };
    let sig_bytes = unsafe { std::slice::from_raw_parts(signature, 64) };
    let pk_bytes = unsafe { std::slice::from_raw_parts(public_key, 32) };

    let xonly = match secp256k1::XOnlyPublicKey::from_slice(pk_bytes) {
        Ok(p) => p,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let sig = match schnorr::Signature::from_slice(sig_bytes) {
        Ok(s) => s,
        Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
    };
    let mut arr = [0u8; 32];
    arr.copy_from_slice(hash_bytes);
    let message = Message::from_digest(arr);
    let valid = secp.verify_schnorr(&sig, &message, &xonly).is_ok();
    unsafe { alloc_bool(valid, &mut *out_len) }
}

// ── 单元测试 ────────────────────────────────────────────────────

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_schnorr_sign_verify() {
        let secp = Secp256k1::new();
        let sk = SecretKey::new(&mut OsRng);
        let keypair = Keypair::from_secret_key(&secp, &sk);
        let xonly = keypair.public_key().x_only_public_key().0;

        let msg = b"hello nostr";
        let hash = schnorr_message_hash(msg);
        let message = Message::from_digest(hash);
        let sig = secp.sign_schnorr_with_rng(&message, &keypair, &mut OsRng);
        assert_eq!(sig.serialize().len(), 64);
        assert!(secp.verify_schnorr(&sig, &message, &xonly).is_ok());

        let wrong_hash = schnorr_message_hash(b"wrong");
        let wrong_msg = Message::from_digest(wrong_hash);
        assert!(secp.verify_schnorr(&sig, &wrong_msg, &xonly).is_err());
    }

    #[test]
    fn test_ecdsa_sign_recover() {
        let secp = Secp256k1::new();
        let sk = SecretKey::new(&mut OsRng);
        let pk = PublicKey::from_secret_key(&secp, &sk);

        let msg_bytes = [0xabu8; 32];
        let msg = Message::from_digest(msg_bytes);
        let rsig = secp.sign_ecdsa_recoverable(&msg, &sk);
        let (_recid, compact) = rsig.serialize_compact();
        assert_eq!(compact.len(), 64);

        // 65B compact → recover → same pubkey
        let recovered = rsig.recover(&msg).unwrap();
        assert_eq!(recovered.serialize(), pk.serialize());
    }

    #[test]
    fn test_ecdsa_sign_verify_compact() {
        let secp = Secp256k1::new();
        let sk = SecretKey::new(&mut OsRng);
        let pk = PublicKey::from_secret_key(&secp, &sk);

        let msg_bytes = [0xcdu8; 32];
        let msg = Message::from_digest(msg_bytes);
        let rsig = secp.sign_ecdsa_recoverable(&msg, &sk);
        let sig = rsig.to_standard();
        assert!(secp.verify_ecdsa(&msg, &sig, &pk).is_ok());
    }

    #[test]
    fn test_schnorr_hash_sign_verify() {
        let secp = Secp256k1::new();
        let sk = SecretKey::new(&mut OsRng);
        let keypair = Keypair::from_secret_key(&secp, &sk);
        let xonly = keypair.public_key().x_only_public_key().0;

        let event_id = [0x42u8; 32];
        let msg = Message::from_digest(event_id);
        let sig = secp.sign_schnorr_with_rng(&msg, &keypair, &mut OsRng);
        assert!(secp.verify_schnorr(&sig, &msg, &xonly).is_ok());
    }

    #[test]
    fn test_xonly_pubkey() {
        let secp = Secp256k1::new();
        let sk = SecretKey::new(&mut OsRng);
        let pk = PublicKey::from_secret_key(&secp, &sk);
        let compressed = pk.serialize(); // 33B
        let xonly = &compressed[1..]; // 32B
        assert_eq!(xonly.len(), 32);

        // x_only_public_key() 应与 compressed[1..] 一致
        let (xpk, _) = pk.x_only_public_key();
        assert_eq!(xpk.serialize(), xonly);
    }
}
