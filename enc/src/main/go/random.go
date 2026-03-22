package main

import "C"
import (
	"crypto/rand"
	"unsafe"
)

//export RandomBytes
func RandomBytes(length C.int, outLen *C.int) *C.char {
	if length <= 0 {
		*outLen = 0
		return nil
	}

	bytes := make([]byte, length)
	if _, err := rand.Read(bytes); err != nil {
		*outLen = 0
		return nil
	}

	*outLen = length

	result := C.malloc(C.size_t(length))
	if result == nil {
		*outLen = 0
		return nil
	}

	for i, b := range bytes {
		*(*byte)(unsafe.Pointer(uintptr(result) + uintptr(i))) = b
	}

	return (*C.char)(result)
}
