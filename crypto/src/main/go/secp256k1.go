package main

import "C"
import (
	"crypto/sha256"
	"unsafe"

	"github.com/btcsuite/btcd/btcec/v2"
	"github.com/btcsuite/btcd/btcec/v2/ecdsa"
	"github.com/btcsuite/btcd/btcec/v2/schnorr"
)

// schnorrMessageHash BIP-340 要求对 32 字节摘要签名；对任意长度消息先 SHA256 再签/验。
func schnorrMessageHash(message []byte) [32]byte {
	return sha256.Sum256(message)
}

//export Secp256k1GenerateKey
func Secp256k1GenerateKey(outLen *C.int) *C.char {
	privateKey, err := btcec.NewPrivateKey()
	if err != nil {
		*outLen = 0
		return nil
	}

	keyBytes := privateKey.Serialize()

	*outLen = C.int(len(keyBytes))

	result := C.malloc(C.size_t(len(keyBytes)))
	if result == nil {
		*outLen = 0
		return nil
	}

	for i, b := range keyBytes {
		*(*byte)(unsafe.Pointer(uintptr(result) + uintptr(i))) = b
	}

	return (*C.char)(result)
}

//export Secp256k1PrivateKeyToPublicKey
func Secp256k1PrivateKeyToPublicKey(privateKey *C.char, privateKeyLen C.int, compressed C.int, outLen *C.int) *C.char {
	if privateKey == nil || privateKeyLen != 32 {
		*outLen = 0
		return nil
	}

	privateKeyBytes := C.GoBytes(unsafe.Pointer(privateKey), privateKeyLen)

	privKey, _ := btcec.PrivKeyFromBytes(privateKeyBytes)

	var pubKeyBytes []byte
	if compressed == 1 {
		pubKeyBytes = privKey.PubKey().SerializeCompressed()
	} else {
		pubKeyBytes = privKey.PubKey().SerializeUncompressed()
	}

	*outLen = C.int(len(pubKeyBytes))

	result := C.malloc(C.size_t(len(pubKeyBytes)))
	if result == nil {
		*outLen = 0
		return nil
	}

	for i, b := range pubKeyBytes {
		*(*byte)(unsafe.Pointer(uintptr(result) + uintptr(i))) = b
	}

	return (*C.char)(result)
}

//export Secp256k1Sign
func Secp256k1Sign(message *C.char, messageLen C.int, privateKey *C.char, privateKeyLen C.int, outLen *C.int) *C.char {
	if message == nil || messageLen < 0 || privateKey == nil || privateKeyLen != 32 {
		*outLen = 0
		return nil
	}

	messageBytes := C.GoBytes(unsafe.Pointer(message), messageLen)
	privateKeyBytes := C.GoBytes(unsafe.Pointer(privateKey), privateKeyLen)

	privKey, _ := btcec.PrivKeyFromBytes(privateKeyBytes)

	signature := ecdsa.Sign(privKey, messageBytes)

	compactSig := signature.Serialize()

	*outLen = C.int(len(compactSig))

	outPtr := C.malloc(C.size_t(len(compactSig)))
	if outPtr == nil {
		*outLen = 0
		return nil
	}

	for i, b := range compactSig {
		*(*byte)(unsafe.Pointer(uintptr(outPtr) + uintptr(i))) = b
	}

	return (*C.char)(outPtr)
}

//export Secp256k1Verify
func Secp256k1Verify(message *C.char, messageLen C.int, signature *C.char, signatureLen C.int, publicKey *C.char, publicKeyLen C.int, outLen *C.int) *C.char {
	if message == nil || messageLen < 0 || signature == nil || signatureLen < 64 || publicKey == nil || publicKeyLen == 0 {
		*outLen = 0
		return nil
	}

	messageBytes := C.GoBytes(unsafe.Pointer(message), messageLen)
	signatureBytes := C.GoBytes(unsafe.Pointer(signature), signatureLen)
	publicKeyBytes := C.GoBytes(unsafe.Pointer(publicKey), publicKeyLen)

	pubKey, err := btcec.ParsePubKey(publicKeyBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	sig, err := ecdsa.ParseDERSignature(signatureBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	valid := sig.Verify(messageBytes, pubKey)

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

//export Secp256k1RecoverPublicKey
func Secp256k1RecoverPublicKey(message *C.char, messageLen C.int, signature *C.char, signatureLen C.int, compressed C.int, outLen *C.int) *C.char {
	if message == nil || messageLen < 0 || signature == nil || signatureLen < 64 {
		*outLen = 0
		return nil
	}

	messageBytes := C.GoBytes(unsafe.Pointer(message), messageLen)
	signatureBytes := C.GoBytes(unsafe.Pointer(signature), signatureLen)

	pubKey, _, err := ecdsa.RecoverCompact(signatureBytes, messageBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	var pubKeyBytes []byte
	if compressed == 1 {
		pubKeyBytes = pubKey.SerializeCompressed()
	} else {
		pubKeyBytes = pubKey.SerializeUncompressed()
	}

	*outLen = C.int(len(pubKeyBytes))

	result := C.malloc(C.size_t(len(pubKeyBytes)))
	if result == nil {
		*outLen = 0
		return nil
	}

	for i, b := range pubKeyBytes {
		*(*byte)(unsafe.Pointer(uintptr(result) + uintptr(i))) = b
	}

	return (*C.char)(result)
}

//export SchnorrSign
func SchnorrSign(message *C.char, messageLen C.int, privateKey *C.char, privateKeyLen C.int, outLen *C.int) *C.char {
	if message == nil || messageLen < 0 || privateKey == nil || privateKeyLen != 32 {
		*outLen = 0
		return nil
	}

	messageBytes := C.GoBytes(unsafe.Pointer(message), messageLen)
	privateKeyBytes := C.GoBytes(unsafe.Pointer(privateKey), privateKeyLen)

	privKey, _ := btcec.PrivKeyFromBytes(privateKeyBytes)

	msgHash := schnorrMessageHash(messageBytes)
	signature, err := schnorr.Sign(privKey, msgHash[:])
	if err != nil {
		*outLen = 0
		return nil
	}

	sigBytes := signature.Serialize()

	*outLen = C.int(len(sigBytes))

	result := C.malloc(C.size_t(len(sigBytes)))
	if result == nil {
		*outLen = 0
		return nil
	}

	for i, b := range sigBytes {
		*(*byte)(unsafe.Pointer(uintptr(result) + uintptr(i))) = b
	}

	return (*C.char)(result)
}

//export SchnorrVerify
func SchnorrVerify(message *C.char, messageLen C.int, signature *C.char, signatureLen C.int, publicKey *C.char, publicKeyLen C.int, outLen *C.int) *C.char {
	if message == nil || messageLen < 0 || signature == nil || signatureLen != 64 || publicKey == nil || publicKeyLen != 32 {
		*outLen = 0
		return nil
	}

	messageBytes := C.GoBytes(unsafe.Pointer(message), messageLen)
	signatureBytes := C.GoBytes(unsafe.Pointer(signature), signatureLen)
	publicKeyBytes := C.GoBytes(unsafe.Pointer(publicKey), publicKeyLen)

	pubKey, err := schnorr.ParsePubKey(publicKeyBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	sig, err := schnorr.ParseSignature(signatureBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	msgHash := schnorrMessageHash(messageBytes)
	valid := sig.Verify(msgHash[:], pubKey)

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

//export SchnorrPrivateKeyToPublicKey
func SchnorrPrivateKeyToPublicKey(privateKey *C.char, privateKeyLen C.int, outLen *C.int) *C.char {
	if privateKey == nil || privateKeyLen != 32 {
		*outLen = 0
		return nil
	}

	privateKeyBytes := C.GoBytes(unsafe.Pointer(privateKey), privateKeyLen)

	privKey, _ := btcec.PrivKeyFromBytes(privateKeyBytes)

	pubKeyBytes := privKey.PubKey().SerializeCompressed()[1:]

	*outLen = C.int(len(pubKeyBytes))

	result := C.malloc(C.size_t(len(pubKeyBytes)))
	if result == nil {
		*outLen = 0
		return nil
	}

	for i, b := range pubKeyBytes {
		*(*byte)(unsafe.Pointer(uintptr(result) + uintptr(i))) = b
	}

	return (*C.char)(result)
}
