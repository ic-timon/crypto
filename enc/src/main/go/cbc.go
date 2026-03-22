package main

import "C"
import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"unsafe"
)

//export AesCbcEncrypt
func AesCbcEncrypt(plaintext *C.char, plaintextLen C.int, key *C.char, keyLen C.int, outLen *C.int) *C.char {
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

	blockSize := block.BlockSize()
	paddedData := pkcs7Pad(plaintextBytes, blockSize)

	iv := make([]byte, blockSize)
	if _, err := rand.Read(iv); err != nil {
		*outLen = 0
		return nil
	}

	ciphertext := make([]byte, len(paddedData))
	mode := cipher.NewCBCEncrypter(block, iv)
	mode.CryptBlocks(ciphertext, paddedData)

	result := append(iv, ciphertext...)
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

//export AesCbcDecrypt
func AesCbcDecrypt(ciphertext *C.char, ciphertextLen C.int, key *C.char, keyLen C.int, outLen *C.int) *C.char {
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
	if len(ciphertextBytes) < blockSize {
		*outLen = 0
		return nil
	}

	iv := ciphertextBytes[:blockSize]
	ct := ciphertextBytes[blockSize:]

	plaintext := make([]byte, len(ct))
	mode := cipher.NewCBCDecrypter(block, iv)
	mode.CryptBlocks(plaintext, ct)

	unpadded, ok := pkcs7Unpad(plaintext)
	if !ok {
		*outLen = 0
		return nil
	}

	*outLen = C.int(len(unpadded))

	outPtr := C.malloc(C.size_t(len(unpadded)))
	if outPtr == nil {
		*outLen = 0
		return nil
	}

	for i, b := range unpadded {
		*(*byte)(unsafe.Pointer(uintptr(outPtr) + uintptr(i))) = b
	}

	return (*C.char)(outPtr)
}
