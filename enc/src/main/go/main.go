package main

// main is required for -buildmode=c-shared; the .so is loaded by JNI, not run as a process.
func main() {}
