package main

import "C"
import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"unsafe"
)

//export AesGcmEncrypt
func AesGcmEncrypt(plaintext *C.char, plaintextLen C.int, key *C.char, keyLen C.int, outLen *C.int) *C.char {
	if plaintext == nil || plaintextLen < 0 || key == nil || keyLen <= 0 {
		*outLen = 0
		return nil
	}

	plaintextBytes := C.GoBytes(unsafe.Pointer(plaintext), plaintextLen)
	keyBytes := C.GoBytes(unsafe.Pointer(key), keyLen)

	block, err := aes.NewCipher(keyBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	aesgcm, err := cipher.NewGCM(block)
	if err != nil {
		*outLen = 0
		return nil
	}

	nonce := make([]byte, aesgcm.NonceSize())
	if _, err := rand.Read(nonce); err != nil {
		*outLen = 0
		return nil
	}

	ciphertext := aesgcm.Seal(nil, nonce, plaintextBytes, nil)

	result := append(nonce, ciphertext...)
	*outLen = C.int(len(result))

	outPtr := C.malloc(C.size_t(len(result)))
	if outPtr == nil {
		*outLen = 0
		return nil
	}

	for i, b := range result {
		*(*byte)(unsafe.Pointer(uintptr(outPtr) + uintptr(i))) = b
	}

	return (*C.char)(outPtr)
}

//export AesGcmDecrypt
func AesGcmDecrypt(ciphertext *C.char, ciphertextLen C.int, key *C.char, keyLen C.int, outLen *C.int) *C.char {
	if ciphertext == nil || ciphertextLen <= 12 || key == nil || keyLen <= 0 {
		*outLen = 0
		return nil
	}

	ciphertextBytes := C.GoBytes(unsafe.Pointer(ciphertext), ciphertextLen)
	keyBytes := C.GoBytes(unsafe.Pointer(key), keyLen)

	block, err := aes.NewCipher(keyBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	aesgcm, err := cipher.NewGCM(block)
	if err != nil {
		*outLen = 0
		return nil
	}

	nonceSize := aesgcm.NonceSize()
	if len(ciphertextBytes) < nonceSize {
		*outLen = 0
		return nil
	}

	nonce := ciphertextBytes[:nonceSize]
	ct := ciphertextBytes[nonceSize:]

	plaintext, err := aesgcm.Open(nil, nonce, ct, nil)
	if err != nil {
		*outLen = 0
		return nil
	}

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
