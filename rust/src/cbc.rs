//! CBC — AES-CBC + DES-CBC，PKCS7 填充。wire：iv(16/8) ‖ ct。
//! 手写 CBC 链（XOR + block encrypt），适配可变 AES key 长度 + DES。

use crate::utils::{alloc_copy, ERR_NULL};
use aes::cipher::{BlockEncrypt, BlockDecrypt, KeyInit, generic_array::GenericArray};
use rand::RngCore;
use std::os::raw::c_int;

fn pkcs7_pad(data: &[u8], bs: usize) -> Vec<u8> {
    let pad = bs - (data.len() % bs);
    let mut out = data.to_vec();
    out.extend(std::iter::repeat(pad as u8).take(pad));
    out
}

fn pkcs7_unpad(data: &[u8]) -> Option<Vec<u8>> {
    if data.is_empty() { return None; }
    let pad = *data.last()? as usize;
    if pad == 0 || pad > data.len() || pad > 16 { return None; }
    if data[data.len() - pad..].iter().any(|&b| b as usize != pad) { return None; }
    Some(data[..data.len() - pad].to_vec())
}

trait BlockCipher {
    fn encrypt_block(&self, block: &mut [u8]);
    fn decrypt_block(&self, block: &mut [u8]);
}

struct AesCipher(aes::Aes128);
struct Aes192Cipher(aes::Aes192);
struct Aes256Cipher(aes::Aes256);
struct DesCipher(des::Des);

impl BlockCipher for AesCipher {
    fn encrypt_block(&self, block: &mut [u8]) { self.0.encrypt_block(GenericArray::from_mut_slice(block)); }
    fn decrypt_block(&self, block: &mut [u8]) { self.0.decrypt_block(GenericArray::from_mut_slice(block)); }
}
impl BlockCipher for Aes192Cipher {
    fn encrypt_block(&self, block: &mut [u8]) { self.0.encrypt_block(GenericArray::from_mut_slice(block)); }
    fn decrypt_block(&self, block: &mut [u8]) { self.0.decrypt_block(GenericArray::from_mut_slice(block)); }
}
impl BlockCipher for Aes256Cipher {
    fn encrypt_block(&self, block: &mut [u8]) { self.0.encrypt_block(GenericArray::from_mut_slice(block)); }
    fn decrypt_block(&self, block: &mut [u8]) { self.0.decrypt_block(GenericArray::from_mut_slice(block)); }
}
impl BlockCipher for DesCipher {
    fn encrypt_block(&self, block: &mut [u8]) { self.0.encrypt_block(GenericArray::from_mut_slice(block)); }
    fn decrypt_block(&self, block: &mut [u8]) { self.0.decrypt_block(GenericArray::from_mut_slice(block)); }
}

fn cbc_encrypt(cipher: &dyn BlockCipher, pt: &[u8], iv: &[u8], bs: usize, out_len: &mut c_int) -> *mut u8 {
    let padded = pkcs7_pad(pt, bs);
    let mut prev = iv.to_vec();
    let mut ct = Vec::with_capacity(padded.len());
    for chunk in padded.chunks(bs) {
        let mut block: Vec<u8> = chunk.iter().zip(&prev).map(|(a, b)| a ^ b).collect();
        cipher.encrypt_block(&mut block);
        ct.extend_from_slice(&block);
        prev = block;
    }
    let mut out = Vec::with_capacity(iv.len() + ct.len());
    out.extend_from_slice(iv);
    out.extend_from_slice(&ct);
    alloc_copy(&out, out_len)
}

fn cbc_decrypt(cipher: &dyn BlockCipher, data: &[u8], iv_len: usize, bs: usize, out_len: &mut c_int) -> *mut u8 {
    if data.len() <= iv_len || (data.len() - iv_len) % bs != 0 { *out_len = 0; return std::ptr::null_mut(); }
    let iv = &data[..iv_len];
    let ct = &data[iv_len..];
    let mut prev = iv.to_vec();
    let mut pt = Vec::with_capacity(ct.len());
    for chunk in ct.chunks(bs) {
        let mut block = chunk.to_vec();
        cipher.decrypt_block(&mut block);
        let plain: Vec<u8> = block.iter().zip(&prev).map(|(a, b)| a ^ b).collect();
        pt.extend_from_slice(&plain);
        prev = chunk.to_vec();
    }
    match pkcs7_unpad(&pt) {
        Some(unpadded) => alloc_copy(&unpadded, out_len),
        None => { *out_len = 0; std::ptr::null_mut() }
    }
}

fn make_aes(key: &[u8]) -> Option<Box<dyn BlockCipher>> {
    match key.len() {
        16 => Some(Box::new(AesCipher(aes::Aes128::new_from_slice(key).unwrap()))),
        24 => Some(Box::new(Aes192Cipher(aes::Aes192::new_from_slice(key).unwrap()))),
        32 => Some(Box::new(Aes256Cipher(aes::Aes256::new_from_slice(key).unwrap()))),
        _ => None,
    }
}

#[no_mangle]
pub extern "C" fn AesCbcEncrypt(pt: *const u8, pt_len: c_int, key: *const u8, key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if pt.is_null() || pt_len < 0 || key.is_null() { unsafe { *out_len = 0 }; return ERR_NULL; }
    let pt = unsafe { std::slice::from_raw_parts(pt, pt_len as usize) };
    let key = unsafe { std::slice::from_raw_parts(key, key_len as usize) };
    let cipher = match make_aes(key) { Some(c) => c, None => unsafe { *out_len = 0; return ERR_NULL; } };
    let mut iv = [0u8; 16];
    rand::thread_rng().fill_bytes(&mut iv);
    cbc_encrypt(&*cipher, pt, &iv, 16, unsafe { &mut *out_len })
}

#[no_mangle]
pub extern "C" fn AesCbcDecrypt(ct: *const u8, ct_len: c_int, key: *const u8, key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if ct.is_null() || ct_len < 0 || key.is_null() { unsafe { *out_len = 0 }; return ERR_NULL; }
    let ct = unsafe { std::slice::from_raw_parts(ct, ct_len as usize) };
    let key = unsafe { std::slice::from_raw_parts(key, key_len as usize) };
    let cipher = match make_aes(key) { Some(c) => c, None => unsafe { *out_len = 0; return ERR_NULL; } };
    cbc_decrypt(&*cipher, ct, 16, 16, unsafe { &mut *out_len })
}

#[no_mangle]
pub extern "C" fn DesCbcEncrypt(pt: *const u8, pt_len: c_int, key: *const u8, key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if pt.is_null() || pt_len < 0 || key.is_null() || key_len != 8 { unsafe { *out_len = 0 }; return ERR_NULL; }
    let pt = unsafe { std::slice::from_raw_parts(pt, pt_len as usize) };
    let key = unsafe { std::slice::from_raw_parts(key, 8) };
    let cipher = DesCipher(des::Des::new_from_slice(key).unwrap());
    let mut iv = [0u8; 8];
    rand::thread_rng().fill_bytes(&mut iv);
    cbc_encrypt(&cipher, pt, &iv, 8, unsafe { &mut *out_len })
}

#[no_mangle]
pub extern "C" fn DesCbcDecrypt(ct: *const u8, ct_len: c_int, key: *const u8, key_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if ct.is_null() || ct_len < 0 || key.is_null() || key_len != 8 { unsafe { *out_len = 0 }; return ERR_NULL; }
    let ct = unsafe { std::slice::from_raw_parts(ct, ct_len as usize) };
    let key = unsafe { std::slice::from_raw_parts(key, 8) };
    let cipher = DesCipher(des::Des::new_from_slice(key).unwrap());
    cbc_decrypt(&cipher, ct, 8, 8, unsafe { &mut *out_len })
}
