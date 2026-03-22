package main

import "C"
import (
	"crypto/sha1"
	"crypto/sha256"
	"unsafe"
)

//export Sha1
func Sha1(data *C.char, dataLen C.int, outLen *C.int) *C.char {
	if dataLen < 0 || (dataLen > 0 && data == nil) {
		*outLen = 0
		return nil
	}

	bytes := C.GoBytes(unsafe.Pointer(data), dataLen)
	hash := sha1.Sum(bytes)

	*outLen = C.int(len(hash))

	result := C.malloc(C.size_t(len(hash)))
	if result == nil {
		*outLen = 0
		return nil
	}

	outBytes := (*[20]byte)(result)
	for i, b := range hash {
		outBytes[i] = b
	}

	return (*C.char)(result)
}

//export Sha256
func Sha256(data *C.char, dataLen C.int, outLen *C.int) *C.char {
	if dataLen < 0 || (dataLen > 0 && data == nil) {
		*outLen = 0
		return nil
	}

	bytes := C.GoBytes(unsafe.Pointer(data), dataLen)
	hash := sha256.Sum256(bytes)

	*outLen = C.int(len(hash))

	result := C.malloc(C.size_t(len(hash)))
	if result == nil {
		*outLen = 0
		return nil
	}

	outBytes := (*[32]byte)(result)
	for i, b := range hash {
		outBytes[i] = b
	}

	return (*C.char)(result)
}
