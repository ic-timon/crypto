//! CSPRNG — crypto-secure random bytes。

use crate::utils::alloc_copy;
use std::os::raw::c_int;

#[no_mangle]
pub extern "C" fn RandomBytes(length: c_int, out_len: *mut c_int) -> *mut u8 {
    if length <= 0 {
        unsafe { *out_len = 0 };
        return std::ptr::null_mut();
    }
    let mut buf = vec![0u8; length as usize];
    if getrandom::getrandom(&mut buf).is_err() {
        unsafe { *out_len = 0 };
        return std::ptr::null_mut();
    }
    unsafe { alloc_copy(&buf, &mut *out_len) }
}
