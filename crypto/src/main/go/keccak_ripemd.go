package main

import "C"
import (
	"unsafe"

	"golang.org/x/crypto/ripemd160"
	"golang.org/x/crypto/sha3"
)

//export Ripemd160
func Ripemd160(data *C.char, dataLen C.int, outLen *C.int) *C.char {
	if dataLen < 0 || (dataLen > 0 && data == nil) {
		*outLen = 0
		return nil
	}

	bytes := C.GoBytes(unsafe.Pointer(data), dataLen)
	hasher := ripemd160.New()
	hasher.Write(bytes)
	hash := hasher.Sum(nil)

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

//export Keccak256
func Keccak256(data *C.char, dataLen C.int, outLen *C.int) *C.char {
	if dataLen < 0 || (dataLen > 0 && data == nil) {
		*outLen = 0
		return nil
	}

	bytes := C.GoBytes(unsafe.Pointer(data), dataLen)
	hash := sha3.NewLegacyKeccak256()
	hash.Write(bytes)
	hashBytes := hash.Sum(nil)

	*outLen = C.int(len(hashBytes))

	result := C.malloc(C.size_t(len(hashBytes)))
	if result == nil {
		*outLen = 0
		return nil
	}

	for i, b := range hashBytes {
		*(*byte)(unsafe.Pointer(uintptr(result) + uintptr(i))) = b
	}

	return (*C.char)(result)
}

//export Keccak512
func Keccak512(data *C.char, dataLen C.int, outLen *C.int) *C.char {
	if dataLen < 0 || (dataLen > 0 && data == nil) {
		*outLen = 0
		return nil
	}

	bytes := C.GoBytes(unsafe.Pointer(data), dataLen)
	hash := sha3.NewLegacyKeccak512()
	hash.Write(bytes)
	hashBytes := hash.Sum(nil)

	*outLen = C.int(len(hashBytes))

	result := C.malloc(C.size_t(len(hashBytes)))
	if result == nil {
		*outLen = 0
		return nil
	}

	for i, b := range hashBytes {
		*(*byte)(unsafe.Pointer(uintptr(result) + uintptr(i))) = b
	}

	return (*C.char)(result)
}
