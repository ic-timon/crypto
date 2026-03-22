package main

import "C"
import (
	"golang.org/x/crypto/argon2"
	"unsafe"
)

//export Argon2idHash
func Argon2idHash(password *C.char, passwordLen C.int, salt *C.char, saltLen C.int, timeCost C.int, memoryCost C.int, parallelism C.int, keyLen C.int, outLen *C.int) *C.char {
	if password == nil || passwordLen <= 0 || salt == nil || saltLen < 8 || timeCost < 1 || memoryCost < 8 || parallelism < 1 || keyLen <= 0 {
		*outLen = 0
		return nil
	}

	passwordBytes := C.GoBytes(unsafe.Pointer(password), passwordLen)
	saltBytes := C.GoBytes(unsafe.Pointer(salt), saltLen)

	key := argon2.IDKey(passwordBytes, saltBytes, uint32(timeCost), uint32(memoryCost), uint8(parallelism), uint32(keyLen))

	*outLen = C.int(len(key))

	outPtr := C.malloc(C.size_t(len(key)))
	if outPtr == nil {
		*outLen = 0
		return nil
	}

	for i, b := range key {
		*(*byte)(unsafe.Pointer(uintptr(outPtr) + uintptr(i))) = b
	}

	return (*C.char)(outPtr)
}
