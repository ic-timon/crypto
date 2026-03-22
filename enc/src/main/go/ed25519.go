package main

import "C"
import (
	"crypto/ed25519"
	"crypto/rand"
	"unsafe"
)

//export Ed25519GenerateKey
func Ed25519GenerateKey(outLen *C.int) *C.char {
	publicKey, privateKey, err := ed25519.GenerateKey(rand.Reader)
	if err != nil {
		*outLen = 0
		return nil
	}

	result := make([]byte, 32+64)
	copy(result[:32], publicKey)
	copy(result[32:], privateKey)

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

//export Ed25519Sign
func Ed25519Sign(message *C.char, messageLen C.int, privateKey *C.char, privateKeyLen C.int, outLen *C.int) *C.char {
	if message == nil || messageLen < 0 || privateKey == nil || privateKeyLen != 64 {
		*outLen = 0
		return nil
	}

	messageBytes := C.GoBytes(unsafe.Pointer(message), messageLen)
	privateKeyBytes := C.GoBytes(unsafe.Pointer(privateKey), privateKeyLen)

	signature := ed25519.Sign(ed25519.PrivateKey(privateKeyBytes), messageBytes)

	*outLen = C.int(len(signature))

	outPtr := C.malloc(C.size_t(len(signature)))
	if outPtr == nil {
		*outLen = 0
		return nil
	}

	for i, b := range signature {
		*(*byte)(unsafe.Pointer(uintptr(outPtr) + uintptr(i))) = b
	}

	return (*C.char)(outPtr)
}

//export Ed25519Verify
func Ed25519Verify(message *C.char, messageLen C.int, signature *C.char, signatureLen C.int, publicKey *C.char, publicKeyLen C.int, outLen *C.int) *C.char {
	if message == nil || messageLen < 0 || signature == nil || signatureLen != 64 || publicKey == nil || publicKeyLen != 32 {
		*outLen = 0
		return nil
	}

	messageBytes := C.GoBytes(unsafe.Pointer(message), messageLen)
	signatureBytes := C.GoBytes(unsafe.Pointer(signature), signatureLen)
	publicKeyBytes := C.GoBytes(unsafe.Pointer(publicKey), publicKeyLen)

	valid := ed25519.Verify(ed25519.PublicKey(publicKeyBytes), messageBytes, signatureBytes)

	result := make([]byte, 1)
	if valid {
		result[0] = 1
	} else {
		result[0] = 0
	}

	*outLen = 1

	outPtr := C.malloc(1)
	if outPtr == nil {
		*outLen = 0
		return nil
	}

	*(*byte)(outPtr) = result[0]

	return (*C.char)(outPtr)
}
