//! 哈希函数 — SHA-1/256/384/512/512_256/BLAKE2b256/MD5/RIPEMD160/Keccak256/Keccak512。

use crate::utils::alloc_copy;
use blake2::Blake2b;
use digest::generic_array::typenum::U32;
use md5::Md5;
use ripemd::Ripemd160;
use sha1::Sha1;
use sha2::{Sha256, Sha384, Sha512};
use sha3::{Keccak256, Keccak512};
use std::os::raw::c_int;
use digest::Digest;

const ERR_NULL: *mut u8 = std::ptr::null_mut();

#[no_mangle]
pub extern "C" fn Sha1(data: *const u8, data_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if data.is_null() || data_len < 0 {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let input = unsafe { std::slice::from_raw_parts(data, data_len as usize) };
    let result = <Sha1>::digest(input);
    unsafe { alloc_copy(&result, &mut *out_len) }
}
#[no_mangle]
pub extern "C" fn Sha256(data: *const u8, data_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if data.is_null() || data_len < 0 {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let input = unsafe { std::slice::from_raw_parts(data, data_len as usize) };
    let result = <Sha256>::digest(input);
    unsafe { alloc_copy(&result, &mut *out_len) }
}
#[no_mangle]
pub extern "C" fn Sha384(data: *const u8, data_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if data.is_null() || data_len < 0 {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let input = unsafe { std::slice::from_raw_parts(data, data_len as usize) };
    let result = <Sha384>::digest(input);
    unsafe { alloc_copy(&result, &mut *out_len) }
}
#[no_mangle]
pub extern "C" fn Sha512(data: *const u8, data_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if data.is_null() || data_len < 0 {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let input = unsafe { std::slice::from_raw_parts(data, data_len as usize) };
    let result = <Sha512>::digest(input);
    unsafe { alloc_copy(&result, &mut *out_len) }
}
#[no_mangle]
pub extern "C" fn Blake2b256(data: *const u8, data_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if data.is_null() || data_len < 0 {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let input = unsafe { std::slice::from_raw_parts(data, data_len as usize) };
    let result = <Blake2b<U32>>::digest(input);
    unsafe { alloc_copy(&result, &mut *out_len) }
}
#[no_mangle]
pub extern "C" fn Md5(data: *const u8, data_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if data.is_null() || data_len < 0 {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let input = unsafe { std::slice::from_raw_parts(data, data_len as usize) };
    let result = <Md5>::digest(input);
    unsafe { alloc_copy(&result, &mut *out_len) }
}
#[no_mangle]
pub extern "C" fn Ripemd160(data: *const u8, data_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if data.is_null() || data_len < 0 {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let input = unsafe { std::slice::from_raw_parts(data, data_len as usize) };
    let result = <Ripemd160>::digest(input);
    unsafe { alloc_copy(&result, &mut *out_len) }
}
#[no_mangle]
pub extern "C" fn Keccak256(data: *const u8, data_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if data.is_null() || data_len < 0 {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let input = unsafe { std::slice::from_raw_parts(data, data_len as usize) };
    let result = <Keccak256>::digest(input);
    unsafe { alloc_copy(&result, &mut *out_len) }
}
#[no_mangle]
pub extern "C" fn Keccak512(data: *const u8, data_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if data.is_null() || data_len < 0 {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let input = unsafe { std::slice::from_raw_parts(data, data_len as usize) };
    let result = <Keccak512>::digest(input);
    unsafe { alloc_copy(&result, &mut *out_len) }
}

/// SHA-512/256（SHA-512 with different IV，截断 256 位）。
#[no_mangle]
pub extern "C" fn Sha512_256(data: *const u8, data_len: c_int, out_len: *mut c_int) -> *mut u8 {
    if data.is_null() || data_len < 0 {
        unsafe { *out_len = 0 };
        return ERR_NULL;
    }
    let input = unsafe { std::slice::from_raw_parts(data, data_len as usize) };
    use sha2::Sha512_256;
    let result = Sha512_256::digest(input);
    unsafe { alloc_copy(&result, &mut *out_len) }
}
