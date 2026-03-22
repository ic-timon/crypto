package main

import "C"
import (
	"crypto/md5"
	"unsafe"

	"golang.org/x/crypto/blake2b"
)

//export Blake2b256
func Blake2b256(data *C.char, dataLen C.int, outLen *C.int) *C.char {
	if dataLen < 0 || (dataLen > 0 && data == nil) {
		*outLen = 0
		return nil
	}

	dataBytes := C.GoBytes(unsafe.Pointer(data), dataLen)
	sum := blake2b.Sum256(dataBytes)

	*outLen = C.int(len(sum))

	result := C.malloc(C.size_t(len(sum)))
	if result == nil {
		*outLen = 0
		return nil
	}

	for i, b := range sum {
		*(*byte)(unsafe.Pointer(uintptr(result) + uintptr(i))) = b
	}

	return (*C.char)(result)
}

//export Md5
func Md5(data *C.char, dataLen C.int, outLen *C.int) *C.char {
	if dataLen < 0 || (dataLen > 0 && data == nil) {
		*outLen = 0
		return nil
	}

	dataBytes := C.GoBytes(unsafe.Pointer(data), dataLen)
	sum := md5.Sum(dataBytes)

	*outLen = C.int(len(sum))

	result := C.malloc(C.size_t(len(sum)))
	if result == nil {
		*outLen = 0
		return nil
	}

	for i, b := range sum {
		*(*byte)(unsafe.Pointer(uintptr(result) + uintptr(i))) = b
	}

	return (*C.char)(result)
}
