package main

import "C"
import (
	"crypto/sha512"
	"unsafe"
)

//export Sha512
func Sha512(data *C.char, dataLen C.int, outLen *C.int) *C.char {
	if dataLen < 0 || (dataLen > 0 && data == nil) {
		*outLen = 0
		return nil
	}

	bytes := C.GoBytes(unsafe.Pointer(data), dataLen)
	hash := sha512.Sum512(bytes)

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

//export Sha384
func Sha384(data *C.char, dataLen C.int, outLen *C.int) *C.char {
	if dataLen < 0 || (dataLen > 0 && data == nil) {
		*outLen = 0
		return nil
	}

	bytes := C.GoBytes(unsafe.Pointer(data), dataLen)
	hash := sha512.Sum384(bytes)

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

//export Sha512_256
func Sha512_256(data *C.char, dataLen C.int, outLen *C.int) *C.char {
	if dataLen < 0 || (dataLen > 0 && data == nil) {
		*outLen = 0
		return nil
	}

	bytes := C.GoBytes(unsafe.Pointer(data), dataLen)
	hash := sha512.Sum512_256(bytes)

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
