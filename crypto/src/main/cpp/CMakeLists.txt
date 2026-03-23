cmake_minimum_required(VERSION 3.22.1)
project("encjni")

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O2 -ffast-math")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O2 -ffast-math")

set(GO_LIB_DIR ${CMAKE_SOURCE_DIR}/../go/lib/${ANDROID_ABI})

add_library(encgo SHARED IMPORTED GLOBAL)
set_target_properties(encgo PROPERTIES
    IMPORTED_LOCATION ${GO_LIB_DIR}/libencgo.so
    IMPORTED_NO_SONAME TRUE
)

add_library(encjni SHARED enc_jni.c)

target_include_directories(encjni PRIVATE
    ${GO_LIB_DIR}
)

target_link_libraries(encjni
    encgo
    log
)
