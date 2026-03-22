package main

import "C"
import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"golang.org/x/crypto/chacha20"
	"unsafe"
)

//export AesCtrEncrypt
func AesCtrEncrypt(plaintext *C.char, plaintextLen C.int, key *C.char, keyLen C.int, outLen *C.int) *C.char {
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

	nonce := make([]byte, block.BlockSize())
	if _, err := rand.Read(nonce); err != nil {
		*outLen = 0
		return nil
	}

	ciphertext := make([]byte, plaintextLen)
	stream := cipher.NewCTR(block, nonce)
	stream.XORKeyStream(ciphertext, plaintextBytes)

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

//export AesCtrDecrypt
func AesCtrDecrypt(ciphertext *C.char, ciphertextLen C.int, key *C.char, keyLen C.int, outLen *C.int) *C.char {
	if ciphertext == nil || ciphertextLen <= 16 || key == nil || keyLen <= 0 {
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

	blockSize := block.BlockSize()
	nonce := ciphertextBytes[:blockSize]
	ct := ciphertextBytes[blockSize:]

	plaintext := make([]byte, len(ct))
	stream := cipher.NewCTR(block, nonce)
	stream.XORKeyStream(plaintext, ct)

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

//export ChaCha20Encrypt
func ChaCha20Encrypt(plaintext *C.char, plaintextLen C.int, key *C.char, keyLen C.int, outLen *C.int) *C.char {
	if plaintext == nil || plaintextLen < 0 || key == nil || keyLen != 32 {
		*outLen = 0
		return nil
	}

	plaintextBytes := C.GoBytes(unsafe.Pointer(plaintext), plaintextLen)
	keyBytes := C.GoBytes(unsafe.Pointer(key), keyLen)

	nonce := make([]byte, 12)
	if _, err := rand.Read(nonce); err != nil {
		*outLen = 0
		return nil
	}

	ciphertext := make([]byte, plaintextLen)
	c, err := chacha20.NewUnauthenticatedCipher(keyBytes, nonce)
	if err != nil {
		*outLen = 0
		return nil
	}
	c.XORKeyStream(ciphertext, plaintextBytes)

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

//export ChaCha20Decrypt
func ChaCha20Decrypt(ciphertext *C.char, ciphertextLen C.int, key *C.char, keyLen C.int, outLen *C.int) *C.char {
	if ciphertext == nil || ciphertextLen <= 12 || key == nil || keyLen != 32 {
		*outLen = 0
		return nil
	}

	ciphertextBytes := C.GoBytes(unsafe.Pointer(ciphertext), ciphertextLen)
	keyBytes := C.GoBytes(unsafe.Pointer(key), keyLen)

	nonce := ciphertextBytes[:12]
	ct := ciphertextBytes[12:]

	plaintext := make([]byte, len(ct))
	c, err := chacha20.NewUnauthenticatedCipher(keyBytes, nonce)
	if err != nil {
		*outLen = 0
		return nil
	}
	c.XORKeyStream(plaintext, ct)

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
