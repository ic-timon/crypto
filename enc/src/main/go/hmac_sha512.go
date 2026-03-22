package main

import "C"
import (
	"crypto/hmac"
	"crypto/sha512"
	"unsafe"
)

//export HmacSha512
func HmacSha512(data *C.char, dataLen C.int, key *C.char, keyLen C.int, outLen *C.int) *C.char {
	if dataLen < 0 || keyLen < 0 || (dataLen > 0 && data == nil) || (keyLen > 0 && key == nil) {
		*outLen = 0
		return nil
	}

	dataBytes := C.GoBytes(unsafe.Pointer(data), dataLen)
	keyBytes := C.GoBytes(unsafe.Pointer(key), keyLen)

	mac := hmac.New(sha512.New, keyBytes)
	mac.Write(dataBytes)
	hash := mac.Sum(nil)

	*outLen = C.int(len(hash))

	result := C.malloc(C.size_t(len(hash)))
	if result == nil {
		*outLen = 0
		return nil
	}

	for i, b := range hash {
		*(*byte)(unsafe.Pointer(uintptr(result) + uintptr(i))) = b
	}

	return (*C.char)(result)
}
