package main

import "C"
import (
	"crypto/ecdsa"
	"crypto/elliptic"
	"crypto/rand"
	"crypto/sha256"
	"math/big"
	"unsafe"
)

//export EcdsaGenerateKey
func EcdsaGenerateKey(curve C.int, outLen *C.int) *C.char {
	var c elliptic.Curve
	switch curve {
	case 224:
		c = elliptic.P224()
	case 256:
		c = elliptic.P256()
	case 384:
		c = elliptic.P384()
	case 521:
		c = elliptic.P521()
	default:
		*outLen = 0
		return nil
	}

	key, err := ecdsa.GenerateKey(c, rand.Reader)
	if err != nil {
		*outLen = 0
		return nil
	}

	keyBytes, err := x509MarshalECPrivateKey(key)
	if err != nil {
		*outLen = 0
		return nil
	}

	*outLen = C.int(len(keyBytes))

	outPtr := C.malloc(C.size_t(len(keyBytes)))
	if outPtr == nil {
		*outLen = 0
		return nil
	}

	for i, b := range keyBytes {
		*(*byte)(unsafe.Pointer(uintptr(outPtr) + uintptr(i))) = b
	}

	return (*C.char)(outPtr)
}

//export EcdsaSign
func EcdsaSign(message *C.char, messageLen C.int, privateKey *C.char, privateKeyLen C.int, outLen *C.int) *C.char {
	if message == nil || messageLen <= 0 || privateKey == nil || privateKeyLen <= 0 {
		*outLen = 0
		return nil
	}

	messageBytes := C.GoBytes(unsafe.Pointer(message), messageLen)
	privateKeyBytes := C.GoBytes(unsafe.Pointer(privateKey), privateKeyLen)

	privKey, err := x509ParseECPrivateKey(privateKeyBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	hashed := sha256.Sum256(messageBytes)
	r, s, err := ecdsa.Sign(rand.Reader, privKey, hashed[:])
	if err != nil {
		*outLen = 0
		return nil
	}

	rBytes := r.Bytes()
	sBytes := s.Bytes()

	result := make([]byte, 4+len(rBytes)+len(sBytes))
	result[0] = byte(len(rBytes) >> 24)
	result[1] = byte(len(rBytes) >> 16)
	result[2] = byte(len(rBytes) >> 8)
	result[3] = byte(len(rBytes))
	copy(result[4:], rBytes)
	copy(result[4+len(rBytes):], sBytes)

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

//export EcdsaVerify
func EcdsaVerify(message *C.char, messageLen C.int, signature *C.char, signatureLen C.int, publicKey *C.char, publicKeyLen C.int, outLen *C.int) *C.char {
	if message == nil || messageLen <= 0 || signature == nil || signatureLen <= 8 || publicKey == nil || publicKeyLen <= 0 {
		*outLen = 0
		return nil
	}

	messageBytes := C.GoBytes(unsafe.Pointer(message), messageLen)
	signatureBytes := C.GoBytes(unsafe.Pointer(signature), signatureLen)
	publicKeyBytes := C.GoBytes(unsafe.Pointer(publicKey), publicKeyLen)

	pubKey, err := x509ParsePKIXPublicKey(publicKeyBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	ecdsaPubKey, ok := pubKey.(*ecdsa.PublicKey)
	if !ok {
		*outLen = 0
		return nil
	}

	rLen := int(signatureBytes[0])<<24 | int(signatureBytes[1])<<16 | int(signatureBytes[2])<<8 | int(signatureBytes[3])
	if rLen <= 0 || 4+rLen >= len(signatureBytes) {
		*outLen = 0
		return nil
	}

	r := new(big.Int).SetBytes(signatureBytes[4 : 4+rLen])
	s := new(big.Int).SetBytes(signatureBytes[4+rLen:])

	hashed := sha256.Sum256(messageBytes)
	valid := ecdsa.Verify(ecdsaPubKey, hashed[:], r, s)

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

//export EcdsaPrivateKeyToPublicKey
func EcdsaPrivateKeyToPublicKey(privateKey *C.char, privateKeyLen C.int, outLen *C.int) *C.char {
	if privateKey == nil || privateKeyLen <= 0 {
		*outLen = 0
		return nil
	}

	privateKeyBytes := C.GoBytes(unsafe.Pointer(privateKey), privateKeyLen)

	privKey, err := x509ParseECPrivateKey(privateKeyBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	publicKeyBytes, err := x509MarshalPKIXPublicKey(&privKey.PublicKey)
	if err != nil {
		*outLen = 0
		return nil
	}

	*outLen = C.int(len(publicKeyBytes))

	outPtr := C.malloc(C.size_t(len(publicKeyBytes)))
	if outPtr == nil {
		*outLen = 0
		return nil
	}

	for i, b := range publicKeyBytes {
		*(*byte)(unsafe.Pointer(uintptr(outPtr) + uintptr(i))) = b
	}

	return (*C.char)(outPtr)
}
