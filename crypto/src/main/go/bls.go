package main

import "C"
import (
	"crypto/rand"
	"unsafe"

	kilic "github.com/kilic/bls12-381"
)

// BLS_SIG_BLS12381G2_XMD:SHA-256_SSWU_RO_POP_ — IETF / 常见 BLS 实现使用的 DST，签名与验签须一致。
var blsG2HashDst = []byte("BLS_SIG_BLS12381G2_XMD:SHA-256_SSWU_RO_POP_")

var blsG1 = kilic.NewG1()
var blsG2 = kilic.NewG2()

//export BlsGenerateKey
func BlsGenerateKey(outLen *C.int) *C.char {
	privKey, err := kilic.NewFr().Rand(rand.Reader)
	if err != nil {
		*outLen = 0
		return nil
	}

	keyBytes := privKey.ToBytes()

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

//export BlsPrivateKeyToPublicKey
func BlsPrivateKeyToPublicKey(privateKey *C.char, privateKeyLen C.int, outLen *C.int) *C.char {
	if privateKey == nil || privateKeyLen != 32 {
		*outLen = 0
		return nil
	}

	privateKeyBytes := C.GoBytes(unsafe.Pointer(privateKey), privateKeyLen)

	privKey := kilic.NewFr().FromBytes(privateKeyBytes)

	pubKey := blsG1.New()
	blsG1.MulScalar(pubKey, blsG1.One(), privKey)

	pubKeyBytes := blsG1.ToCompressed(pubKey)

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

//export BlsSign
func BlsSign(message *C.char, messageLen C.int, privateKey *C.char, privateKeyLen C.int, outLen *C.int) *C.char {
	if message == nil || messageLen < 0 || privateKey == nil || privateKeyLen != 32 {
		*outLen = 0
		return nil
	}

	messageBytes := C.GoBytes(unsafe.Pointer(message), messageLen)
	privateKeyBytes := C.GoBytes(unsafe.Pointer(privateKey), privateKeyLen)

	privKey := kilic.NewFr().FromBytes(privateKeyBytes)

	h, err := blsG2.HashToCurve(messageBytes, blsG2HashDst)
	if err != nil {
		*outLen = 0
		return nil
	}

	sig := blsG2.New()
	blsG2.MulScalar(sig, h, privKey)

	sigBytes := blsG2.ToCompressed(sig)

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

//export BlsVerify
func BlsVerify(message *C.char, messageLen C.int, signature *C.char, signatureLen C.int, publicKey *C.char, publicKeyLen C.int, outLen *C.int) *C.char {
	if message == nil || messageLen < 0 || signature == nil || signatureLen != 96 || publicKey == nil || publicKeyLen != 48 {
		*outLen = 0
		return nil
	}

	messageBytes := C.GoBytes(unsafe.Pointer(message), messageLen)
	signatureBytes := C.GoBytes(unsafe.Pointer(signature), signatureLen)
	publicKeyBytes := C.GoBytes(unsafe.Pointer(publicKey), publicKeyLen)

	sig, err := blsG2.FromCompressed(signatureBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	pubKey, err := blsG1.FromCompressed(publicKeyBytes)
	if err != nil {
		*outLen = 0
		return nil
	}

	// BLS 验签：e(PK, H(m)) == e(G1, σ)  ⇔  e(PK, H(m)) · e(-G1, σ) = 1（GT 中乘为单位元）
	h, err := blsG2.HashToCurve(messageBytes, blsG2HashDst)
	if err != nil {
		*outLen = 0
		return nil
	}

	pairing := kilic.NewEngine()
	pairing.AddPair(pubKey, h)
	pairing.AddPairInv(blsG1.One(), sig)

	valid := pairing.Check()

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

//export BlsAggregateSignatures
func BlsAggregateSignatures(signatures *C.char, signaturesLen C.int, count C.int, outLen *C.int) *C.char {
	if signatures == nil || signaturesLen <= 0 || count <= 0 {
		*outLen = 0
		return nil
	}

	signaturesBytes := C.GoBytes(unsafe.Pointer(signatures), signaturesLen)

	sigSize := 96
	if len(signaturesBytes) != int(count)*sigSize {
		*outLen = 0
		return nil
	}

	aggSig := blsG2.Zero()
	for i := 0; i < int(count); i++ {
		sigBytes := signaturesBytes[i*sigSize : (i+1)*sigSize]
		sig, err := blsG2.FromCompressed(sigBytes)
		if err != nil {
			*outLen = 0
			return nil
		}
		blsG2.Add(aggSig, aggSig, sig)
	}

	sigBytes := blsG2.ToCompressed(aggSig)

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

//export BlsAggregatePublicKeys
func BlsAggregatePublicKeys(publicKeys *C.char, publicKeysLen C.int, count C.int, outLen *C.int) *C.char {
	if publicKeys == nil || publicKeysLen <= 0 || count <= 0 {
		*outLen = 0
		return nil
	}

	publicKeysBytes := C.GoBytes(unsafe.Pointer(publicKeys), publicKeysLen)

	pubKeySize := 48
	if len(publicKeysBytes) != int(count)*pubKeySize {
		*outLen = 0
		return nil
	}

	aggPubKey := blsG1.Zero()
	for i := 0; i < int(count); i++ {
		pkBytes := publicKeysBytes[i*pubKeySize : (i+1)*pubKeySize]
		pk, err := blsG1.FromCompressed(pkBytes)
		if err != nil {
			*outLen = 0
			return nil
		}
		blsG1.Add(aggPubKey, aggPubKey, pk)
	}

	pkBytes := blsG1.ToCompressed(aggPubKey)

	*outLen = C.int(len(pkBytes))

	result := C.malloc(C.size_t(len(pkBytes)))
	if result == nil {
		*outLen = 0
		return nil
	}

	for i, b := range pkBytes {
		*(*byte)(unsafe.Pointer(uintptr(result) + uintptr(i))) = b
	}

	return (*C.char)(result)
}
