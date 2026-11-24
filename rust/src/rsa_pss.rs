//! Verify prehashed SHA-2 PSS signatures with the same hash for MGF1.
use rsa::{BigUint, Pss, RsaPublicKey};
use sha2::{Sha256, Sha384, Sha512};

fn unsigned(bytes: &[u8]) -> &[u8] {
    &bytes[bytes.iter().position(|&b| b != 0).unwrap_or(bytes.len())..]
}

fn verify(n: &[u8], e: &[u8], digest: &[u8], signature: &[u8], salt_len: usize) -> i32 {
    let n = unsigned(n);
    if n.len() < 256 || n.len() > 512 || (n.len() == 256 && n[0] < 128) {
        return -5;
    }
    let e = unsigned(e);
    if e.len() > 5 {
        return -1;
    }
    let Ok(key) = RsaPublicKey::new(BigUint::from_bytes_be(n), BigUint::from_bytes_be(e)) else {
        return -1;
    };
    let padding = match digest.len() {
        32 => Pss::new_with_salt::<Sha256>(salt_len),
        48 => Pss::new_with_salt::<Sha384>(salt_len),
        64 => Pss::new_with_salt::<Sha512>(salt_len),
        _ => return -1,
    };
    if salt_len > n.len() || signature.len() != n.len() || signature >= n {
        return 0;
    }
    // For modBits == 1 (mod 8), the PSS encoded message is a byte
    // shorter than the RSA representative. Check the discarded prefix.
    if n[0] == 1 {
        use rsa::traits::PublicKeyParts;
        let representative = BigUint::from_bytes_be(signature).modpow(key.e(), key.n());
        if representative.bits() > (n.len() - 1) * 8 {
            return 0;
        }
    }
    i32::from(key.verify(padding, digest, signature).is_ok())
}

/// Returns 1 for valid, 0 for signature mismatch, -1 for invalid arguments,
/// and -5 for a modulus outside 2048..4096 bits. Digest length selects SHA-2.
/// # Safety
/// Each nonempty input must point to the declared number of readable bytes.
#[no_mangle]
pub unsafe extern "C" fn encrust_rsa_pss_verify_v1(
    n: *const u8,
    n_len: i32,
    e: *const u8,
    e_len: i32,
    digest: *const u8,
    digest_len: i32,
    signature: *const u8,
    signature_len: i32,
    salt_len: i32,
) -> i32 {
    if n.is_null()
        || n_len <= 0
        || e.is_null()
        || e_len <= 0
        || digest.is_null()
        || !matches!(digest_len, 32 | 48 | 64)
        || signature_len < 0
        || (signature_len > 0 && signature.is_null())
        || salt_len < 0
    {
        return -1;
    }
    use std::slice::from_raw_parts;
    let signature = if signature_len == 0 {
        &[]
    } else {
        from_raw_parts(signature, signature_len as usize)
    };
    verify(
        from_raw_parts(n, n_len as usize),
        from_raw_parts(e, e_len as usize),
        from_raw_parts(digest, digest_len as usize),
        signature,
        salt_len as usize,
    )
}

#[cfg(feature = "jni-bridge")]
#[no_mangle]
pub extern "system" fn Java_mobi_timon_crypto_Rsa_verifyPss(
    mut env: jni::JNIEnv,
    _: jni::objects::JObject,
    modulus: jni::objects::JByteArray,
    exponent: jni::objects::JByteArray,
    digest: jni::objects::JByteArray,
    signature: jni::objects::JByteArray,
    salt_len: i32,
) -> jni::sys::jboolean {
    let mut check = || -> jni::errors::Result<i32> {
        let n = env.convert_byte_array(modulus)?;
        let e = env.convert_byte_array(exponent)?;
        let h = env.convert_byte_array(digest)?;
        let sig = env.convert_byte_array(signature)?;
        if salt_len < 0 {
            return Ok(-1);
        }
        Ok(verify(&n, &e, &h, &sig, salt_len as usize))
    };
    match check() {
        Ok(1) => 1,
        Ok(0) | Err(_) => 0,
        Ok(_) => {
            let _ = env.throw_new(
                "java/lang/IllegalArgumentException",
                "invalid RSA-PSS parameters",
            );
            0
        }
    }
}
