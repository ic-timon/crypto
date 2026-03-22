package main

import "C"
import (
	"crypto/rand"
	"golang.org/x/crypto/chacha20poly1305"
	"unsafe"
)

//export ChaCha20Poly1305Encrypt
func ChaCha20Poly1305Encrypt(plaintext *C.char, plaintextLen C.int, key *C.char, keyLen C.int, outLen *C.int) *C.char {
	if plaintext == nil || plaintextLen < 0 || key == nil || keyLen != 32 {
		*outLen = 0
		return nil
	}

	plaintextBytes := C.GoBytes(unsafe.Pointer(plaintext), plaintextLen)
	keyBytes := C.GoBytes(unsafe.Pointer(key), keyLen)

	if len(keyBytes) != 32 {
		*outLen = 0
		return nil
	}

	aead, err := chacha20poly1305.New(keyBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	nonce := make([]byte, aead.NonceSize())
	if _, err := rand.Read(nonce); err != nil {
		*outLen = 0
		return nil
	}

	ciphertext := aead.Seal(nil, nonce, plaintextBytes, nil)

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

//export ChaCha20Poly1305Decrypt
func ChaCha20Poly1305Decrypt(ciphertext *C.char, ciphertextLen C.int, key *C.char, keyLen C.int, outLen *C.int) *C.char {
	if ciphertext == nil || ciphertextLen <= 12 || key == nil || keyLen != 32 {
		*outLen = 0
		return nil
	}

	ciphertextBytes := C.GoBytes(unsafe.Pointer(ciphertext), ciphertextLen)
	keyBytes := C.GoBytes(unsafe.Pointer(key), keyLen)

	aead, err := chacha20poly1305.New(keyBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	nonceSize := aead.NonceSize()
	if len(ciphertextBytes) <= nonceSize {
		*outLen = 0
		return nil
	}

	nonce := ciphertextBytes[:nonceSize]
	ct := ciphertextBytes[nonceSize:]

	plaintext, err := aead.Open(nil, nonce, ct, nil)
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
