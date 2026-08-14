//! HMAC — SHA-256/512/SHA1。注意 Go 版参数顺序：data 在前，key 在后。

use crate::utils::alloc_copy;
use hmac::{Hmac, Mac};
use sha1::Sha1;
use sha2::{Sha256, Sha512};
use std::os::raw::c_int;

const ERR_NULL: *mut u8 = std::ptr::null_mut();

macro_rules! hmac_fn {
    ($name:ident, $hasher:ty) => {
        #[no_mangle]
        pub extern "C" fn $name(
            data: *const u8, data_len: c_int,
            key: *const u8, key_len: c_int,
            out_len: *mut c_int,
        ) -> *mut u8 {
            if data.is_null() || data_len < 0 || key.is_null() || key_len < 0 {
                unsafe { *out_len = 0 };
                return ERR_NULL;
            }
            let data_slice = unsafe { std::slice::from_raw_parts(data, data_len as usize) };
            let key_slice = unsafe { std::slice::from_raw_parts(key, key_len as usize) };
            let mut mac = match <Hmac<$hasher> as Mac>::new_from_slice(key_slice) {
                Ok(m) => m,
                Err(_) => unsafe { *out_len = 0; return ERR_NULL; },
            };
            mac.update(data_slice);
            let result = mac.finalize().into_bytes();
            unsafe { alloc_copy(&result, &mut *out_len) }
        }
    };
}

hmac_fn!(HmacSha256, Sha256);
hmac_fn!(HmacSha512, Sha512);
hmac_fn!(HmacSha1, Sha1);
