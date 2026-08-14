//! FFI 辅助：内存分配 / 释放。

use std::os::raw::c_int;

/// FFI null 返回值（错误信号）。
pub const ERR_NULL: *mut u8 = std::ptr::null_mut();

/// 分配 len 字节，拷贝 data 进去，设置 *out_len，返回指针。
/// 调用方（JNI / cinterop）用 [`enc_free`] 释放。
pub(crate) fn alloc_copy(data: &[u8], out_len: &mut c_int) -> *mut u8 {
    *out_len = data.len() as c_int;
    unsafe {
        let ptr = libc::malloc(data.len()) as *mut u8;
        if ptr.is_null() {
            *out_len = 0;
            return std::ptr::null_mut();
        }
        std::ptr::copy_nonoverlapping(data.as_ptr(), ptr, data.len());
        ptr
    }
}

/// 分配单字节（verify 类返回值），设置 *out_len = 1。
pub(crate) fn alloc_bool(val: bool, out_len: &mut c_int) -> *mut u8 {
    *out_len = 1;
    unsafe {
        let ptr = libc::malloc(1) as *mut u8;
        if ptr.is_null() {
            *out_len = 0;
            return std::ptr::null_mut();
        }
        *ptr = if val { 1 } else { 0 };
        ptr
    }
}

/// 释放 malloc 分配的缓冲区（对应 Go 版 FreeBytes）。
/// JNI 侧和 cinterop 侧都调这个。
#[no_mangle]
pub extern "C" fn enc_free(ptr: *mut u8, len: c_int) {
    if !ptr.is_null() && len > 0 {
        unsafe { libc::free(ptr as *mut libc::c_void) };
    }
}
