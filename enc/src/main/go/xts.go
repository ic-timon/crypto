package main

import "C"
import (
	"crypto/aes"
	"golang.org/x/crypto/xts"
	"unsafe"
)

//export AesXtsEncrypt
func AesXtsEncrypt(plaintext *C.char, plaintextLen C.int, key *C.char, keyLen C.int, sectorNum C.longlong, outLen *C.int) *C.char {
	if plaintext == nil || plaintextLen < 0 || key == nil || (keyLen != 32 && keyLen != 64) {
		*outLen = 0
		return nil
	}

	plaintextBytes := C.GoBytes(unsafe.Pointer(plaintext), plaintextLen)
	keyBytes := C.GoBytes(unsafe.Pointer(key), keyLen)

	c, err := xts.NewCipher(aes.NewCipher, keyBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	ciphertext := make([]byte, len(plaintextBytes))
	c.Encrypt(ciphertext, plaintextBytes, uint64(sectorNum))

	*outLen = C.int(len(ciphertext))

	outPtr := C.malloc(C.size_t(len(ciphertext)))
	if outPtr == nil {
		*outLen = 0
		return nil
	}

	for i, b := range ciphertext {
		*(*byte)(unsafe.Pointer(uintptr(outPtr) + uintptr(i))) = b
	}

	return (*C.char)(outPtr)
}

//export AesXtsDecrypt
func AesXtsDecrypt(ciphertext *C.char, ciphertextLen C.int, key *C.char, keyLen C.int, sectorNum C.longlong, outLen *C.int) *C.char {
	if ciphertext == nil || ciphertextLen < 0 || key == nil || (keyLen != 32 && keyLen != 64) {
		*outLen = 0
		return nil
	}

	ciphertextBytes := C.GoBytes(unsafe.Pointer(ciphertext), ciphertextLen)
	keyBytes := C.GoBytes(unsafe.Pointer(key), keyLen)

	c, err := xts.NewCipher(aes.NewCipher, keyBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	plaintext := make([]byte, len(ciphertextBytes))
	c.Decrypt(plaintext, ciphertextBytes, uint64(sectorNum))

	*outLen = C.int(len(plaintext))

	outPtr := C.malloc(C.size_t(len(plaintext)))
	if outPtr == nil {
		*outLen = 0
		return nil
	}

	for i, b := range plaintext {
		*(*byte)(unsafe.Pointer(uintptr(outPtr) + uintptr(i))) = b
	}

	return (*C.char)(outPtr)
}
