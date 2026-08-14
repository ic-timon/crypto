//! 直接调 C ABI 函数的集成测试（host 验证，分离 Android 环境变量）。

#[test]
fn random_bytes_works() {
    let mut out_len: i32 = 0;
    let r = unsafe { encrust::random::RandomBytes(32, &mut out_len) };
    assert!(!r.is_null(), "RandomBytes returned null");
    assert_eq!(out_len, 32);
    unsafe { encrust::utils::enc_free(r, out_len) };
}

#[test]
fn aes_gcm_roundtrip() {
    let key = [7u8; 32];
    let pt = b"hello rust gcm".to_vec();
    let mut out_len: i32 = 0;
    let ct = unsafe { encrust::aead::AesGcmEncrypt(pt.as_ptr(), pt.len() as i32, key.as_ptr(), 32, &mut out_len) };
    if ct.is_null() { panic!("AesGcmEncrypt returned null, out_len={out_len}"); }
    let ct_len = out_len;
    let ct_vec = unsafe { std::slice::from_raw_parts(ct, ct_len as usize).to_vec() };
    unsafe { encrust::utils::enc_free(ct, ct_len) };
    assert_eq!(ct_len as usize, 12 + pt.len() + 16, "nonce+ct+tag");

    let mut out2: i32 = 0;
    let dec = unsafe { encrust::aead::AesGcmDecrypt(ct_vec.as_ptr(), ct_len, key.as_ptr(), 32, &mut out2) };
    assert!(!dec.is_null(), "AesGcmDecrypt returned null");
    let dec_vec = unsafe { std::slice::from_raw_parts(dec, out2 as usize).to_vec() };
    unsafe { encrust::utils::enc_free(dec, out2) };
    assert_eq!(dec_vec, pt);
}

#[test]
fn aes_cbc_roundtrip() {
    let key = [3u8; 16];
    let pt = b"0123456789abcdef0".to_vec();
    let mut out_len: i32 = 0;
    let ct = unsafe { encrust::cbc::AesCbcEncrypt(pt.as_ptr(), pt.len() as i32, key.as_ptr(), 16, &mut out_len) };
    assert!(!ct.is_null(), "AesCbcEncrypt null");
    let ct_len = out_len;
    let ct_vec = unsafe { std::slice::from_raw_parts(ct, ct_len as usize).to_vec() };
    unsafe { encrust::utils::enc_free(ct, ct_len) };

    let mut out2: i32 = 0;
    let dec = unsafe { encrust::cbc::AesCbcDecrypt(ct_vec.as_ptr(), ct_len, key.as_ptr(), 16, &mut out2) };
    assert!(!dec.is_null(), "AesCbcDecrypt null");
    let dec_vec = unsafe { std::slice::from_raw_parts(dec, out2 as usize).to_vec() };
    unsafe { encrust::utils::enc_free(dec, out2) };
    assert_eq!(dec_vec, pt);
}

#[test]
fn hkdf_works() {
    let ikm = [0x0bu8; 22];
    let salt = [0x0bu8; 13];
    let mut out_len: i32 = 0;
    let r = unsafe {
        encrust::kdf::Hkdf(ikm.as_ptr(), 22, salt.as_ptr(), 13, std::ptr::null(), 0, 42, &mut out_len)
    };
    assert!(!r.is_null(), "Hkdf null");
    unsafe { encrust::utils::enc_free(r, out_len) };
    assert_eq!(out_len, 42);
}
