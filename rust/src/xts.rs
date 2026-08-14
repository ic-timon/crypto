//! AES-XTS — 磁盘加密模式。sectorNum 作为 tweak，key 拆两半（K1 数据 + K2 tweak）。
//! 数据须为非空 16 字节倍数。

use crate::utils::{alloc_copy, ERR_NULL};
use aes::cipher::{BlockEncrypt, BlockDecrypt, KeyInit, generic_array::GenericArray};
use aes::Aes128;
use std::os::raw::c_int;

/// GF(2^128) 乘以 α（x = 2），little-endian bit ordering（XTS 标准）。
fn gf_mul_alpha(tweak: &mut [u8; 16]) {
    let mut carry = 0u8;
    for i in 0..16 {
        let next_carry = tweak[i] >> 7;
        tweak[i] = (tweak[i] << 1) | carry;
        carry = next_carry;
    }
    if carry != 0 {
        tweak[0] ^= 0x87; // x^128 + x^7 + x^2 + x + 1 的低字节
    }
}

fn make_xts_keys(key: &[u8]) -> Option<(Aes128, Aes128)> {
    let half = key.len() / 2;
    if half != 16 && half != 32 { return None; }
    let k1 = aes::Aes128::new(GenericArray::from_slice(&key[..half])); // AES-128 only for now
    let k2 = aes::Aes128::new(GenericArray::from_slice(&key[half..]));
    Some((k1, k2))
}

#[no_mangle]
pub extern "C" fn AesXtsEncrypt(pt: *const u8, pt_len: c_int, key: *const u8, key_len: c_int, sector_num: i64, out_len: *mut c_int) -> *mut u8 {
    if pt.is_null() || pt_len <= 0 || key.is_null() || pt_len % 16 != 0 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let pt = unsafe { std::slice::from_raw_parts(pt, pt_len as usize) };
    let key = unsafe { std::slice::from_raw_parts(key, key_len as usize) };
    let (k1, k2) = match make_xts_keys(key) { Some(k) => k, None => unsafe { *out_len = 0; return ERR_NULL; } };

    // Tweak = E_K2(sector_num as 16-byte little-endian)
    let mut tweak = [0u8; 16];
    tweak[..8].copy_from_slice(&sector_num.to_le_bytes());
    k2.encrypt_block(GenericArray::from_mut_slice(&mut tweak));

    let mut ct = pt.to_vec();
    for block in ct.chunks_mut(16) {
        // XOR with tweak
        for i in 0..16 { block[i] ^= tweak[i]; }
        // Encrypt with K1
        k1.encrypt_block(GenericArray::from_mut_slice(block));
        // XOR with tweak again
        for i in 0..16 { block[i] ^= tweak[i]; }
        // Advance tweak
        gf_mul_alpha(&mut tweak);
    }
    unsafe { alloc_copy(&ct, &mut *out_len) }
}

#[no_mangle]
pub extern "C" fn AesXtsDecrypt(ct: *const u8, ct_len: c_int, key: *const u8, key_len: c_int, sector_num: i64, out_len: *mut c_int) -> *mut u8 {
    if ct.is_null() || ct_len <= 0 || key.is_null() || ct_len % 16 != 0 {
        unsafe { *out_len = 0 }; return ERR_NULL;
    }
    let ct = unsafe { std::slice::from_raw_parts(ct, ct_len as usize) };
    let key = unsafe { std::slice::from_raw_parts(key, key_len as usize) };
    let (k1, k2) = match make_xts_keys(key) { Some(k) => k, None => unsafe { *out_len = 0; return ERR_NULL; } };

    let mut tweak = [0u8; 16];
    tweak[..8].copy_from_slice(&sector_num.to_le_bytes());
    k2.encrypt_block(GenericArray::from_mut_slice(&mut tweak));

    let mut pt = ct.to_vec();
    for block in pt.chunks_mut(16) {
        for i in 0..16 { block[i] ^= tweak[i]; }
        k1.decrypt_block(GenericArray::from_mut_slice(block));
        for i in 0..16 { block[i] ^= tweak[i]; }
        gf_mul_alpha(&mut tweak);
    }
    unsafe { alloc_copy(&pt, &mut *out_len) }
}
