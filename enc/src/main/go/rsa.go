package main

import "C"
import (
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"unsafe"
)

//export RsaGenerateKey
func RsaGenerateKey(bits C.int, outLen *C.int) *C.char {
	if bits != 2048 && bits != 3072 && bits != 4096 {
		*outLen = 0
		return nil
	}

	key, err := rsa.GenerateKey(rand.Reader, int(bits))
	if err != nil {
		*outLen = 0
		return nil
	}

	keyBytes := x509MarshalPKCS8PrivateKey(key)

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

//export RsaEncrypt
func RsaEncrypt(plaintext *C.char, plaintextLen C.int, publicKey *C.char, publicKeyLen C.int, outLen *C.int) *C.char {
	if plaintext == nil || plaintextLen <= 0 || publicKey == nil || publicKeyLen <= 0 {
		*outLen = 0
		return nil
	}

	plaintextBytes := C.GoBytes(unsafe.Pointer(plaintext), plaintextLen)
	publicKeyBytes := C.GoBytes(unsafe.Pointer(publicKey), publicKeyLen)

	pubKey, err := x509ParsePKIXPublicKey(publicKeyBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	rsaPubKey, ok := pubKey.(*rsa.PublicKey)
	if !ok {
		*outLen = 0
		return nil
	}

	ciphertext, err := rsa.EncryptOAEP(sha256.New(), rand.Reader, rsaPubKey, plaintextBytes, nil)
	if err != nil {
		*outLen = 0
		return nil
	}

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

//export RsaDecrypt
func RsaDecrypt(ciphertext *C.char, ciphertextLen C.int, privateKey *C.char, privateKeyLen C.int, outLen *C.int) *C.char {
	if ciphertext == nil || ciphertextLen <= 0 || privateKey == nil || privateKeyLen <= 0 {
		*outLen = 0
		return nil
	}

	ciphertextBytes := C.GoBytes(unsafe.Pointer(ciphertext), ciphertextLen)
	privateKeyBytes := C.GoBytes(unsafe.Pointer(privateKey), privateKeyLen)

	privKey, err := x509ParsePKCS8PrivateKey(privateKeyBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	rsaPrivKey, ok := privKey.(*rsa.PrivateKey)
	if !ok {
		*outLen = 0
		return nil
	}

	plaintext, err := rsa.DecryptOAEP(sha256.New(), rand.Reader, rsaPrivKey, ciphertextBytes, nil)
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

//export RsaSign
func RsaSign(message *C.char, messageLen C.int, privateKey *C.char, privateKeyLen C.int, outLen *C.int) *C.char {
	if message == nil || messageLen <= 0 || privateKey == nil || privateKeyLen <= 0 {
		*outLen = 0
		return nil
	}

	messageBytes := C.GoBytes(unsafe.Pointer(message), messageLen)
	privateKeyBytes := C.GoBytes(unsafe.Pointer(privateKey), privateKeyLen)

	privKey, err := x509ParsePKCS8PrivateKey(privateKeyBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	rsaPrivKey, ok := privKey.(*rsa.PrivateKey)
	if !ok {
		*outLen = 0
		return nil
	}

	hashed := sha256.Sum256(messageBytes)
	signature, err := rsa.SignPKCS1v15(rand.Reader, rsaPrivKey, cryptoSHA256, hashed[:])
	if err != nil {
		*outLen = 0
		return nil
	}

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

//export RsaVerify
func RsaVerify(message *C.char, messageLen C.int, signature *C.char, signatureLen C.int, publicKey *C.char, publicKeyLen C.int, outLen *C.int) *C.char {
	if message == nil || messageLen <= 0 || signature == nil || signatureLen <= 0 || publicKey == nil || publicKeyLen <= 0 {
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

	rsaPubKey, ok := pubKey.(*rsa.PublicKey)
	if !ok {
		*outLen = 0
		return nil
	}

	hashed := sha256.Sum256(messageBytes)
	err = rsa.VerifyPKCS1v15(rsaPubKey, cryptoSHA256, hashed[:], signatureBytes)

	result := make([]byte, 1)
	if err != nil {
		result[0] = 0
	} else {
		result[0] = 1
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

//export RsaPrivateKeyToPublicKey
func RsaPrivateKeyToPublicKey(privateKey *C.char, privateKeyLen C.int, outLen *C.int) *C.char {
	if privateKey == nil || privateKeyLen <= 0 {
		*outLen = 0
		return nil
	}

	privateKeyBytes := C.GoBytes(unsafe.Pointer(privateKey), privateKeyLen)

	privKey, err := x509ParsePKCS8PrivateKey(privateKeyBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	rsaPrivKey, ok := privKey.(*rsa.PrivateKey)
	if !ok {
		*outLen = 0
		return nil
	}

	publicKeyBytes, err := x509MarshalPKIXPublicKey(&rsaPrivKey.PublicKey)
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
