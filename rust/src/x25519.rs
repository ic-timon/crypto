//! X25519 fixed-buffer ABI for Native callers.
use std::ptr;
use x25519_dalek::{PublicKey, StaticSecret};

pub(crate) fn public_key(secret: [u8; 32]) -> [u8; 32] {
    PublicKey::from(&StaticSecret::from(secret)).to_bytes()
}

pub(crate) fn shared_secret(secret: [u8; 32], peer: [u8; 32]) -> Option<[u8; 32]> {
    let shared = StaticSecret::from(secret).diffie_hellman(&PublicKey::from(peer));
    shared.was_contributory().then(|| shared.to_bytes())
}

/// # Safety
/// Each input must point to 32 readable bytes and output to 32 writable bytes.
#[no_mangle]
pub unsafe extern "C" fn encrust_x25519_public_v1(secret: *const u8, out: *mut u8) -> i32 {
    if secret.is_null() || out.is_null() {
        return -1;
    }
    let value = public_key(ptr::read(secret.cast::<[u8; 32]>()));
    ptr::copy_nonoverlapping(value.as_ptr(), out, 32);
    0
}

/// # Safety
/// Same buffer contract as encrust_x25519_public_v1. A low-order peer
/// clears the output and returns -2.
#[no_mangle]
pub unsafe extern "C" fn encrust_x25519_shared_v1(
    secret: *const u8,
    peer: *const u8,
    out: *mut u8,
) -> i32 {
    if secret.is_null() || peer.is_null() || out.is_null() {
        return -1;
    }
    match shared_secret(ptr::read(secret.cast()), ptr::read(peer.cast())) {
        Some(value) => {
            ptr::copy_nonoverlapping(value.as_ptr(), out, 32);
            0
        }
        None => {
            ptr::write_bytes(out, 0, 32);
            -2
        }
    }
}

#[cfg(feature = "jni-bridge")]
mod android {
    use super::*;
    use jni::{
        objects::{JByteArray, JObject},
        sys::jbyteArray,
        JNIEnv,
    };

    fn key(env: &mut JNIEnv, input: JByteArray) -> Option<[u8; 32]> {
        let bytes = env.convert_byte_array(input).ok()?;
        if let Ok(value) = bytes.try_into() {
            return Some(value);
        }
        let _ = env.throw_new(
            "java/lang/IllegalArgumentException",
            "X25519 keys must be 32 bytes",
        );
        None
    }

    #[no_mangle]
    pub extern "system" fn Java_mobi_timon_crypto_X25519_publicKey(
        mut env: JNIEnv,
        _: JObject,
        secret: JByteArray,
    ) -> jbyteArray {
        let Some(secret) = key(&mut env, secret) else {
            return ptr::null_mut();
        };
        env.byte_array_from_slice(&public_key(secret))
            .map(|arr| arr.into_raw())
            .unwrap_or(ptr::null_mut())
    }

    #[no_mangle]
    pub extern "system" fn Java_mobi_timon_crypto_X25519_sharedSecret(
        mut env: JNIEnv,
        _: JObject,
        secret: JByteArray,
        peer: JByteArray,
    ) -> jbyteArray {
        let Some(secret) = key(&mut env, secret) else {
            return ptr::null_mut();
        };
        let Some(peer) = key(&mut env, peer) else {
            return ptr::null_mut();
        };
        let Some(shared) = shared_secret(secret, peer) else {
            let _ = env.throw_new(
                "java/lang/IllegalArgumentException",
                "low-order X25519 public key",
            );
            return ptr::null_mut();
        };
        env.byte_array_from_slice(&shared)
            .map(|arr| arr.into_raw())
            .unwrap_or(ptr::null_mut())
    }
}
