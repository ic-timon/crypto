package main

import "C"
import (
	"golang.org/x/crypto/bcrypt"
	"unsafe"
)

//export BcryptHash
func BcryptHash(password *C.char, passwordLen C.int, cost C.int, outLen *C.int) *C.char {
	if password == nil || passwordLen <= 0 || cost < 4 || cost > 31 {
		*outLen = 0
		return nil
	}

	passwordBytes := C.GoBytes(unsafe.Pointer(password), passwordLen)

	hash, err := bcrypt.GenerateFromPassword(passwordBytes, int(cost))
	if err != nil {
		*outLen = 0
		return nil
	}

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

//export BcryptVerify
func BcryptVerify(password *C.char, passwordLen C.int, hash *C.char, hashLen C.int, outLen *C.int) *C.char {
	if password == nil || passwordLen <= 0 || hash == nil || hashLen <= 0 {
		*outLen = 0
		return nil
	}

	passwordBytes := C.GoBytes(unsafe.Pointer(password), passwordLen)
	hashBytes := C.GoBytes(unsafe.Pointer(hash), hashLen)

	err := bcrypt.CompareHashAndPassword(hashBytes, passwordBytes)

	// Return 1 for match, 0 for no match
	*outLen = 1
	if err != nil {
		// No match
		result := C.malloc(1)
		if result == nil {
			*outLen = 0
			return nil
		}
		*(*byte)(result) = 0
		return (*C.char)(result)
	}

	// Match
	result := C.malloc(1)
	if result == nil {
		*outLen = 0
		return nil
	}
	*(*byte)(result) = 1
	return (*C.char)(result)
}
