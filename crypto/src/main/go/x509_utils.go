package main

import (
	"crypto"
	"crypto/rsa"
	"crypto/x509"
)

var cryptoSHA256 = crypto.SHA256

func x509MarshalPKCS8PrivateKey(key *rsa.PrivateKey) []byte {
	bytes, err := x509.MarshalPKCS8PrivateKey(key)
	if err != nil {
		return nil
	}
	return bytes
}

func x509ParsePKCS8PrivateKey(der []byte) (crypto.PrivateKey, error) {
	return x509.ParsePKCS8PrivateKey(der)
}

func x509MarshalPKIXPublicKey(pub crypto.PublicKey) ([]byte, error) {
	return x509.MarshalPKIXPublicKey(pub)
}

func x509ParsePKIXPublicKey(der []byte) (crypto.PublicKey, error) {
	return x509.ParsePKIXPublicKey(der)
}
