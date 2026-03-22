package main

import (
	"crypto/ecdsa"
	"crypto/x509"
)

func x509MarshalECPrivateKey(key *ecdsa.PrivateKey) ([]byte, error) {
	return x509.MarshalECPrivateKey(key)
}

func x509ParseECPrivateKey(der []byte) (*ecdsa.PrivateKey, error) {
	return x509.ParseECPrivateKey(der)
}
