package main

// #include <stdlib.h>
import "C"
import "unsafe"

//export FreeBytes
func FreeBytes(ptr unsafe.Pointer) {
	if ptr != nil {
		C.free(ptr)
	}
}

func pkcs7Pad(data []byte, blockSize int) []byte {
	padding := blockSize - len(data)%blockSize
	padded := make([]byte, len(data)+padding)
	copy(padded, data)
	for i := len(data); i < len(padded); i++ {
		padded[i] = byte(padding)
	}
	return padded
}

func pkcs7Unpad(data []byte) ([]byte, bool) {
	if len(data) == 0 {
		return nil, false
	}
	padding := int(data[len(data)-1])
	if padding <= 0 || padding > len(data) {
		return nil, false
	}
	for i := len(data) - padding; i < len(data); i++ {
		if data[i] != byte(padding) {
			return nil, false
		}
	}
	return data[:len(data)-padding], true
}
