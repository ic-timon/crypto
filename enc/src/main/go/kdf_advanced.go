package main

import "C"
import (
	"crypto/sha256"
	"unsafe"

	"golang.org/x/crypto/pbkdf2"
	"golang.org/x/crypto/scrypt"
)

//export Scrypt
func Scrypt(password *C.char, passwordLen C.int, salt *C.char, saltLen C.int, keyLen C.int, outLen *C.int) *C.char {
	if password == nil || passwordLen <= 0 || salt == nil || saltLen < 8 || keyLen <= 0 {
		*outLen = 0
		return nil
	}

	passwordBytes := C.GoBytes(unsafe.Pointer(password), passwordLen)
	saltBytes := C.GoBytes(unsafe.Pointer(salt), saltLen)

	key, err := scrypt.Key(passwordBytes, saltBytes, 32768, 8, 1, int(keyLen))
	if err != nil {
		*outLen = 0
		return nil
	}

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

//export Pbkdf2
func Pbkdf2(password *C.char, passwordLen C.int, salt *C.char, saltLen C.int, iterations C.int, keyLen C.int, outLen *C.int) *C.char {
	if password == nil || passwordLen <= 0 || salt == nil || saltLen < 8 || iterations < 1 || keyLen <= 0 {
		*outLen = 0
		return nil
	}

	passwordBytes := C.GoBytes(unsafe.Pointer(password), passwordLen)
	saltBytes := C.GoBytes(unsafe.Pointer(salt), saltLen)

	key := pbkdf2.Key(passwordBytes, saltBytes, int(iterations), int(keyLen), sha256.New)

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

//export Hkdf
func Hkdf(ikm *C.char, ikmLen C.int, salt *C.char, saltLen C.int, info *C.char, infoLen C.int, keyLen C.int, outLen *C.int) *C.char {
	if ikm == nil || ikmLen <= 0 || keyLen <= 0 {
		*outLen = 0
		return nil
	}

	ikmBytes := C.GoBytes(unsafe.Pointer(ikm), ikmLen)
	var saltBytes []byte
	if salt != nil && saltLen > 0 {
		saltBytes = C.GoBytes(unsafe.Pointer(salt), saltLen)
	}
	var infoBytes []byte
	if info != nil && infoLen > 0 {
		infoBytes = C.GoBytes(unsafe.Pointer(info), infoLen)
	}

	key := hkdfDerive(ikmBytes, saltBytes, infoBytes, int(keyLen))
	if key == nil {
		*outLen = 0
		return nil
	}

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

func hkdfDerive(ikm, salt, info []byte, keyLen int) []byte {
	if len(ikm) == 0 || keyLen <= 0 {
		return nil
	}

	if len(salt) == 0 {
		salt = make([]byte, sha256.Size)
	}

	prk := hmacSha256Raw(salt, ikm)
	if prk == nil {
		return nil
	}

	okm := make([]byte, 0, keyLen)
	t := []byte{}
	for len(okm) < keyLen {
		t = hmacSha256Raw(prk, append(t, info...))
		if t == nil {
			return nil
		}
		okm = append(okm, t...)
	}

	return okm[:keyLen]
}

func hmacSha256Raw(key, data []byte) []byte {
	blockSize := sha256.New().BlockSize()
	if len(key) > blockSize {
		hash := sha256.Sum256(key)
		key = hash[:]
	}
	if len(key) < blockSize {
		padded := make([]byte, blockSize)
		copy(padded, key)
		key = padded
	}

	oKeyPad := make([]byte, blockSize)
	iKeyPad := make([]byte, blockSize)
	for i := 0; i < blockSize; i++ {
		oKeyPad[i] = key[i] ^ 0x5c
		iKeyPad[i] = key[i] ^ 0x36
	}

	inner := sha256.Sum256(append(iKeyPad, data...))
	outer := sha256.Sum256(append(oKeyPad, inner[:]...))
	return outer[:]
}
