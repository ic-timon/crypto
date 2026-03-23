#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include "libencgo.h"

static jbyteArray processResult(JNIEnv *env, void *result, int outLen) {
    if (result == NULL || outLen == 0) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "operation failed");
        return NULL;
    }
    
    jbyteArray outArray = (*env)->NewByteArray(env, outLen);
    if (outArray == NULL) {
        FreeBytes(result);
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "failed to allocate output");
        return NULL;
    }
    
    (*env)->SetByteArrayRegion(env, outArray, 0, outLen, (jbyte *)result);
    FreeBytes(result);
    return outArray;
}

static jboolean verifyBoolResult(JNIEnv *env, char *result, int outLen) {
    (void)env;
    if (result == NULL || outLen != 1) {
        FreeBytes(result);
        return JNI_FALSE;
    }
    jboolean ok = (result[0] == 1) ? JNI_TRUE : JNI_FALSE;
    FreeBytes(result);
    return ok;
}

// === Hash ===
JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Hash_sha1(JNIEnv *env, jobject obj, jbyteArray data) {
    if (data == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "sha1: input data is null");
        return NULL;
    }
    jsize dataLen = (*env)->GetArrayLength(env, data);
    jbyte *dataPtr = (*env)->GetByteArrayElements(env, data, NULL);
    int outLen = 0;
    char *result = Sha1((char *)dataPtr, dataLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, data, dataPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Hash_sha256(JNIEnv *env, jobject obj, jbyteArray data) {
    if (data == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "sha256: input data is null");
        return NULL;
    }
    jsize dataLen = (*env)->GetArrayLength(env, data);
    jbyte *dataPtr = (*env)->GetByteArrayElements(env, data, NULL);
    int outLen = 0;
    char *result = Sha256((char *)dataPtr, dataLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, data, dataPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Hash_sha512(JNIEnv *env, jobject obj, jbyteArray data) {
    if (data == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "sha512: input data is null");
        return NULL;
    }
    jsize dataLen = (*env)->GetArrayLength(env, data);
    jbyte *dataPtr = (*env)->GetByteArrayElements(env, data, NULL);
    int outLen = 0;
    char *result = Sha512((char *)dataPtr, dataLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, data, dataPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Hash_sha384(JNIEnv *env, jobject obj, jbyteArray data) {
    if (data == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "sha384: input data is null");
        return NULL;
    }
    jsize dataLen = (*env)->GetArrayLength(env, data);
    jbyte *dataPtr = (*env)->GetByteArrayElements(env, data, NULL);
    int outLen = 0;
    char *result = Sha384((char *)dataPtr, dataLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, data, dataPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Hash_sha512_1256(JNIEnv *env, jobject obj, jbyteArray data) {
    if (data == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "sha512_256: input data is null");
        return NULL;
    }
    jsize dataLen = (*env)->GetArrayLength(env, data);
    jbyte *dataPtr = (*env)->GetByteArrayElements(env, data, NULL);
    int outLen = 0;
    char *result = Sha512_256((char *)dataPtr, dataLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, data, dataPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Hash_blake2b256(JNIEnv *env, jobject obj, jbyteArray data) {
    if (data == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "blake2b256: input data is null");
        return NULL;
    }
    jsize dataLen = (*env)->GetArrayLength(env, data);
    jbyte *dataPtr = (*env)->GetByteArrayElements(env, data, NULL);
    int outLen = 0;
    char *result = Blake2b256((char *)dataPtr, dataLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, data, dataPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Hash_md5(JNIEnv *env, jobject obj, jbyteArray data) {
    if (data == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "md5: input data is null");
        return NULL;
    }
    jsize dataLen = (*env)->GetArrayLength(env, data);
    jbyte *dataPtr = (*env)->GetByteArrayElements(env, data, NULL);
    int outLen = 0;
    char *result = Md5((char *)dataPtr, dataLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, data, dataPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

// === Hmac ===
JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Hmac_hmacSha256(JNIEnv *env, jobject obj, jbyteArray data, jbyteArray key) {
    if (data == NULL || key == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "hmacSha256: input is null");
        return NULL;
    }
    jsize dataLen = (*env)->GetArrayLength(env, data);
    jsize keyLen = (*env)->GetArrayLength(env, key);
    jbyte *dataPtr = (*env)->GetByteArrayElements(env, data, NULL);
    jbyte *keyPtr = (*env)->GetByteArrayElements(env, key, NULL);
    int outLen = 0;
    char *result = HmacSha256((char *)dataPtr, dataLen, (char *)keyPtr, keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, data, dataPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, key, keyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Hmac_hmacSha1(JNIEnv *env, jobject obj, jbyteArray data, jbyteArray key) {
    if (data == NULL || key == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "hmacSha1: input is null");
        return NULL;
    }
    jsize dataLen = (*env)->GetArrayLength(env, data);
    jsize keyLen = (*env)->GetArrayLength(env, key);
    jbyte *dataPtr = (*env)->GetByteArrayElements(env, data, NULL);
    jbyte *keyPtr = (*env)->GetByteArrayElements(env, key, NULL);
    int outLen = 0;
    char *result = HmacSha1((char *)dataPtr, dataLen, (char *)keyPtr, keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, data, dataPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, key, keyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Hmac_hmacSha512(JNIEnv *env, jobject obj, jbyteArray data, jbyteArray key) {
    if (data == NULL || key == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "hmacSha512: input is null");
        return NULL;
    }
    jsize dataLen = (*env)->GetArrayLength(env, data);
    jsize keyLen = (*env)->GetArrayLength(env, key);
    jbyte *dataPtr = (*env)->GetByteArrayElements(env, data, NULL);
    jbyte *keyPtr = (*env)->GetByteArrayElements(env, key, NULL);
    int outLen = 0;
    char *result = HmacSha512((char *)dataPtr, dataLen, (char *)keyPtr, keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, data, dataPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, key, keyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

// === Random ===
JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Random_bytes(JNIEnv *env, jobject obj, jint length) {
    if (length <= 0) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "randomBytes: length must be positive");
        return NULL;
    }
    int outLen = 0;
    char *result = RandomBytes(length, &outLen);
    return processResult(env, result, outLen);
}

// === Aead ===
JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Aead_aesGcmEncrypt(JNIEnv *env, jobject obj, jbyteArray plaintext, jbyteArray key) {
    if (plaintext == NULL || key == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "aesGcmEncrypt: input is null");
        return NULL;
    }
    jsize plaintextLen = (*env)->GetArrayLength(env, plaintext);
    jsize keyLen = (*env)->GetArrayLength(env, key);
    jbyte *plaintextPtr = (*env)->GetByteArrayElements(env, plaintext, NULL);
    jbyte *keyPtr = (*env)->GetByteArrayElements(env, key, NULL);
    int outLen = 0;
    char *result = AesGcmEncrypt((char *)plaintextPtr, plaintextLen, (char *)keyPtr, keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, plaintext, plaintextPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, key, keyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Aead_aesGcmDecrypt(JNIEnv *env, jobject obj, jbyteArray ciphertext, jbyteArray key) {
    if (ciphertext == NULL || key == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "aesGcmDecrypt: input is null");
        return NULL;
    }
    jsize ciphertextLen = (*env)->GetArrayLength(env, ciphertext);
    jsize keyLen = (*env)->GetArrayLength(env, key);
    jbyte *ciphertextPtr = (*env)->GetByteArrayElements(env, ciphertext, NULL);
    jbyte *keyPtr = (*env)->GetByteArrayElements(env, key, NULL);
    int outLen = 0;
    char *result = AesGcmDecrypt((char *)ciphertextPtr, ciphertextLen, (char *)keyPtr, keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, ciphertext, ciphertextPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, key, keyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Aead_chacha20Poly1305Encrypt(JNIEnv *env, jobject obj, jbyteArray plaintext, jbyteArray key) {
    if (plaintext == NULL || key == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "chacha20Poly1305Encrypt: input is null");
        return NULL;
    }
    jsize plaintextLen = (*env)->GetArrayLength(env, plaintext);
    jsize keyLen = (*env)->GetArrayLength(env, key);
    jbyte *plaintextPtr = (*env)->GetByteArrayElements(env, plaintext, NULL);
    jbyte *keyPtr = (*env)->GetByteArrayElements(env, key, NULL);
    int outLen = 0;
    char *result = ChaCha20Poly1305Encrypt((char *)plaintextPtr, plaintextLen, (char *)keyPtr, keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, plaintext, plaintextPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, key, keyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

// === Cbc ===
JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Cbc_aesCbcEncrypt(JNIEnv *env, jobject obj, jbyteArray plaintext, jbyteArray key) {
    if (plaintext == NULL || key == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "aesCbcEncrypt: input is null");
        return NULL;
    }
    jsize plaintextLen = (*env)->GetArrayLength(env, plaintext);
    jsize keyLen = (*env)->GetArrayLength(env, key);
    jbyte *plaintextPtr = (*env)->GetByteArrayElements(env, plaintext, NULL);
    jbyte *keyPtr = (*env)->GetByteArrayElements(env, key, NULL);
    int outLen = 0;
    char *result = AesCbcEncrypt((char *)plaintextPtr, plaintextLen, (char *)keyPtr, keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, plaintext, plaintextPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, key, keyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Cbc_aesCbcDecrypt(JNIEnv *env, jobject obj, jbyteArray ciphertext, jbyteArray key) {
    if (ciphertext == NULL || key == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "aesCbcDecrypt: input is null");
        return NULL;
    }
    jsize ciphertextLen = (*env)->GetArrayLength(env, ciphertext);
    jsize keyLen = (*env)->GetArrayLength(env, key);
    jbyte *ciphertextPtr = (*env)->GetByteArrayElements(env, ciphertext, NULL);
    jbyte *keyPtr = (*env)->GetByteArrayElements(env, key, NULL);
    int outLen = 0;
    char *result = AesCbcDecrypt((char *)ciphertextPtr, ciphertextLen, (char *)keyPtr, keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, ciphertext, ciphertextPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, key, keyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Cbc_desCbcEncrypt(JNIEnv *env, jobject obj, jbyteArray plaintext, jbyteArray key) {
    if (plaintext == NULL || key == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "desCbcEncrypt: input is null");
        return NULL;
    }
    jsize plaintextLen = (*env)->GetArrayLength(env, plaintext);
    jsize keyLen = (*env)->GetArrayLength(env, key);
    jbyte *plaintextPtr = (*env)->GetByteArrayElements(env, plaintext, NULL);
    jbyte *keyPtr = (*env)->GetByteArrayElements(env, key, NULL);
    int outLen = 0;
    char *result = DesCbcEncrypt((char *)plaintextPtr, plaintextLen, (char *)keyPtr, keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, plaintext, plaintextPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, key, keyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Cbc_desCbcDecrypt(JNIEnv *env, jobject obj, jbyteArray ciphertext, jbyteArray key) {
    if (ciphertext == NULL || key == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "desCbcDecrypt: input is null");
        return NULL;
    }
    jsize ciphertextLen = (*env)->GetArrayLength(env, ciphertext);
    jsize keyLen = (*env)->GetArrayLength(env, key);
    jbyte *ciphertextPtr = (*env)->GetByteArrayElements(env, ciphertext, NULL);
    jbyte *keyPtr = (*env)->GetByteArrayElements(env, key, NULL);
    int outLen = 0;
    char *result = DesCbcDecrypt((char *)ciphertextPtr, ciphertextLen, (char *)keyPtr, keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, ciphertext, ciphertextPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, key, keyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

// === Kdf ===
JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Kdf_bcryptHash(JNIEnv *env, jobject obj, jbyteArray password, jint cost) {
    if (password == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "bcryptHash: password is null");
        return NULL;
    }
    jsize passwordLen = (*env)->GetArrayLength(env, password);
    jbyte *passwordPtr = (*env)->GetByteArrayElements(env, password, NULL);
    int outLen = 0;
    char *result = BcryptHash((char *)passwordPtr, passwordLen, cost, &outLen);
    (*env)->ReleaseByteArrayElements(env, password, passwordPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jboolean JNICALL
Java_mobi_timon_crypto_Kdf_bcryptVerify(JNIEnv *env, jobject obj, jbyteArray password, jbyteArray hash) {
    if (password == NULL || hash == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "bcryptVerify: input is null");
        return JNI_FALSE;
    }
    jsize passwordLen = (*env)->GetArrayLength(env, password);
    jsize hashLen = (*env)->GetArrayLength(env, hash);
    jbyte *passwordPtr = (*env)->GetByteArrayElements(env, password, NULL);
    jbyte *hashPtr = (*env)->GetByteArrayElements(env, hash, NULL);
    int outLen = 0;
    char *result = BcryptVerify((char *)passwordPtr, passwordLen, (char *)hashPtr, hashLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, password, passwordPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, hash, hashPtr, JNI_ABORT);

    return verifyBoolResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Aead_chacha20Poly1305Decrypt(JNIEnv *env, jobject obj, jbyteArray ciphertext, jbyteArray key) {
    if (ciphertext == NULL || key == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "chacha20Poly1305Decrypt: input is null");
        return NULL;
    }
    jsize ciphertextLen = (*env)->GetArrayLength(env, ciphertext);
    jsize keyLen = (*env)->GetArrayLength(env, key);
    jbyte *ciphertextPtr = (*env)->GetByteArrayElements(env, ciphertext, NULL);
    jbyte *keyPtr = (*env)->GetByteArrayElements(env, key, NULL);
    int outLen = 0;
    char *result = ChaCha20Poly1305Decrypt((char *)ciphertextPtr, ciphertextLen, (char *)keyPtr, keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, ciphertext, ciphertextPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, key, keyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Stream_aesCtrEncrypt(JNIEnv *env, jobject obj, jbyteArray plaintext, jbyteArray key) {
    if (plaintext == NULL || key == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "aesCtrEncrypt: input is null");
        return NULL;
    }
    jsize plaintextLen = (*env)->GetArrayLength(env, plaintext);
    jsize keyLen = (*env)->GetArrayLength(env, key);
    jbyte *plaintextPtr = (*env)->GetByteArrayElements(env, plaintext, NULL);
    jbyte *keyPtr = (*env)->GetByteArrayElements(env, key, NULL);
    int outLen = 0;
    char *result = AesCtrEncrypt((char *)plaintextPtr, plaintextLen, (char *)keyPtr, keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, plaintext, plaintextPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, key, keyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Stream_aesCtrDecrypt(JNIEnv *env, jobject obj, jbyteArray ciphertext, jbyteArray key) {
    if (ciphertext == NULL || key == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "aesCtrDecrypt: input is null");
        return NULL;
    }
    jsize ciphertextLen = (*env)->GetArrayLength(env, ciphertext);
    jsize keyLen = (*env)->GetArrayLength(env, key);
    jbyte *ciphertextPtr = (*env)->GetByteArrayElements(env, ciphertext, NULL);
    jbyte *keyPtr = (*env)->GetByteArrayElements(env, key, NULL);
    int outLen = 0;
    char *result = AesCtrDecrypt((char *)ciphertextPtr, ciphertextLen, (char *)keyPtr, keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, ciphertext, ciphertextPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, key, keyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Stream_chacha20Encrypt(JNIEnv *env, jobject obj, jbyteArray plaintext, jbyteArray key) {
    if (plaintext == NULL || key == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "chacha20Encrypt: input is null");
        return NULL;
    }
    jsize plaintextLen = (*env)->GetArrayLength(env, plaintext);
    jsize keyLen = (*env)->GetArrayLength(env, key);
    jbyte *plaintextPtr = (*env)->GetByteArrayElements(env, plaintext, NULL);
    jbyte *keyPtr = (*env)->GetByteArrayElements(env, key, NULL);
    int outLen = 0;
    char *result = ChaCha20Encrypt((char *)plaintextPtr, plaintextLen, (char *)keyPtr, keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, plaintext, plaintextPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, key, keyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Stream_chacha20Decrypt(JNIEnv *env, jobject obj, jbyteArray ciphertext, jbyteArray key) {
    if (ciphertext == NULL || key == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "chacha20Decrypt: input is null");
        return NULL;
    }
    jsize ciphertextLen = (*env)->GetArrayLength(env, ciphertext);
    jsize keyLen = (*env)->GetArrayLength(env, key);
    jbyte *ciphertextPtr = (*env)->GetByteArrayElements(env, ciphertext, NULL);
    jbyte *keyPtr = (*env)->GetByteArrayElements(env, key, NULL);
    int outLen = 0;
    char *result = ChaCha20Decrypt((char *)ciphertextPtr, ciphertextLen, (char *)keyPtr, keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, ciphertext, ciphertextPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, key, keyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Xts_aesXtsEncrypt(JNIEnv *env, jobject obj, jbyteArray plaintext, jbyteArray key, jlong sectorNum) {
    if (plaintext == NULL || key == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "aesXtsEncrypt: input is null");
        return NULL;
    }
    jsize plaintextLen = (*env)->GetArrayLength(env, plaintext);
    jsize keyLen = (*env)->GetArrayLength(env, key);
    jbyte *plaintextPtr = (*env)->GetByteArrayElements(env, plaintext, NULL);
    jbyte *keyPtr = (*env)->GetByteArrayElements(env, key, NULL);
    int outLen = 0;
    char *result = AesXtsEncrypt((char *)plaintextPtr, plaintextLen, (char *)keyPtr, keyLen, sectorNum, &outLen);
    (*env)->ReleaseByteArrayElements(env, plaintext, plaintextPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, key, keyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Xts_aesXtsDecrypt(JNIEnv *env, jobject obj, jbyteArray ciphertext, jbyteArray key, jlong sectorNum) {
    if (ciphertext == NULL || key == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "aesXtsDecrypt: input is null");
        return NULL;
    }
    jsize ciphertextLen = (*env)->GetArrayLength(env, ciphertext);
    jsize keyLen = (*env)->GetArrayLength(env, key);
    jbyte *ciphertextPtr = (*env)->GetByteArrayElements(env, ciphertext, NULL);
    jbyte *keyPtr = (*env)->GetByteArrayElements(env, key, NULL);
    int outLen = 0;
    char *result = AesXtsDecrypt((char *)ciphertextPtr, ciphertextLen, (char *)keyPtr, keyLen, sectorNum, &outLen);
    (*env)->ReleaseByteArrayElements(env, ciphertext, ciphertextPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, key, keyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

// === Kdf (advanced) ===
JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Kdf_argon2idHash(JNIEnv *env, jobject obj, jbyteArray password, jbyteArray salt,
                                     jint timeCost, jint memoryCost, jint parallelism, jint keyLen) {
    if (password == NULL || salt == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "argon2idHash: input is null");
        return NULL;
    }
    jsize passwordLen = (*env)->GetArrayLength(env, password);
    jsize saltLen = (*env)->GetArrayLength(env, salt);
    jbyte *passwordPtr = (*env)->GetByteArrayElements(env, password, NULL);
    jbyte *saltPtr = (*env)->GetByteArrayElements(env, salt, NULL);
    int outLen = 0;
    char *result = Argon2idHash((char *)passwordPtr, passwordLen, (char *)saltPtr, saltLen,
                                timeCost, memoryCost, parallelism, keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, password, passwordPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, salt, saltPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Kdf_scrypt(JNIEnv *env, jobject obj, jbyteArray password, jbyteArray salt, jint keyLen) {
    if (password == NULL || salt == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "scrypt: input is null");
        return NULL;
    }
    jsize passwordLen = (*env)->GetArrayLength(env, password);
    jsize saltLen = (*env)->GetArrayLength(env, salt);
    jbyte *passwordPtr = (*env)->GetByteArrayElements(env, password, NULL);
    jbyte *saltPtr = (*env)->GetByteArrayElements(env, salt, NULL);
    int outLen = 0;
    char *result = Scrypt((char *)passwordPtr, passwordLen, (char *)saltPtr, saltLen, keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, password, passwordPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, salt, saltPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Kdf_pbkdf2(JNIEnv *env, jobject obj, jbyteArray password, jbyteArray salt,
                               jint iterations, jint keyLen) {
    if (password == NULL || salt == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "pbkdf2: input is null");
        return NULL;
    }
    jsize passwordLen = (*env)->GetArrayLength(env, password);
    jsize saltLen = (*env)->GetArrayLength(env, salt);
    jbyte *passwordPtr = (*env)->GetByteArrayElements(env, password, NULL);
    jbyte *saltPtr = (*env)->GetByteArrayElements(env, salt, NULL);
    int outLen = 0;
    char *result = Pbkdf2((char *)passwordPtr, passwordLen, (char *)saltPtr, saltLen, iterations, keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, password, passwordPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, salt, saltPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Kdf_hkdf(JNIEnv *env, jobject obj, jbyteArray ikm, jbyteArray salt, jbyteArray info, jint keyLen) {
    if (ikm == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "hkdf: ikm is null");
        return NULL;
    }
    jsize ikmLen = (*env)->GetArrayLength(env, ikm);
    jsize saltLen = 0;
    jbyte *saltPtr = NULL;
    if (salt != NULL) {
        saltLen = (*env)->GetArrayLength(env, salt);
        saltPtr = (*env)->GetByteArrayElements(env, salt, NULL);
    }
    jsize infoLen = 0;
    jbyte *infoPtr = NULL;
    if (info != NULL) {
        infoLen = (*env)->GetArrayLength(env, info);
        infoPtr = (*env)->GetByteArrayElements(env, info, NULL);
    }
    jbyte *ikmPtr = (*env)->GetByteArrayElements(env, ikm, NULL);
    int outLen = 0;
    char *result = Hkdf((char *)ikmPtr, (int)ikmLen,
                          salt != NULL ? (char *)saltPtr : NULL, (int)saltLen,
                          info != NULL ? (char *)infoPtr : NULL, (int)infoLen,
                          keyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, ikm, ikmPtr, JNI_ABORT);
    if (salt != NULL) {
        (*env)->ReleaseByteArrayElements(env, salt, saltPtr, JNI_ABORT);
    }
    if (info != NULL) {
        (*env)->ReleaseByteArrayElements(env, info, infoPtr, JNI_ABORT);
    }
    return processResult(env, result, outLen);
}

// === Rsa ===
JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Rsa_generateKey(JNIEnv *env, jobject obj, jint bits) {
    int outLen = 0;
    char *result = RsaGenerateKey(bits, &outLen);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Rsa_encrypt(JNIEnv *env, jobject obj, jbyteArray plaintext, jbyteArray publicKey) {
    if (plaintext == NULL || publicKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "rsaEncrypt: input is null");
        return NULL;
    }
    jsize plaintextLen = (*env)->GetArrayLength(env, plaintext);
    jsize publicKeyLen = (*env)->GetArrayLength(env, publicKey);
    jbyte *plaintextPtr = (*env)->GetByteArrayElements(env, plaintext, NULL);
    jbyte *publicKeyPtr = (*env)->GetByteArrayElements(env, publicKey, NULL);
    int outLen = 0;
    char *result = RsaEncrypt((char *)plaintextPtr, plaintextLen, (char *)publicKeyPtr, publicKeyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, plaintext, plaintextPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, publicKey, publicKeyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Rsa_decrypt(JNIEnv *env, jobject obj, jbyteArray ciphertext, jbyteArray privateKey) {
    if (ciphertext == NULL || privateKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "rsaDecrypt: input is null");
        return NULL;
    }
    jsize ciphertextLen = (*env)->GetArrayLength(env, ciphertext);
    jsize privateKeyLen = (*env)->GetArrayLength(env, privateKey);
    jbyte *ciphertextPtr = (*env)->GetByteArrayElements(env, ciphertext, NULL);
    jbyte *privateKeyPtr = (*env)->GetByteArrayElements(env, privateKey, NULL);
    int outLen = 0;
    char *result = RsaDecrypt((char *)ciphertextPtr, ciphertextLen, (char *)privateKeyPtr, privateKeyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, ciphertext, ciphertextPtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, privateKey, privateKeyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Rsa_sign(JNIEnv *env, jobject obj, jbyteArray message, jbyteArray privateKey) {
    if (message == NULL || privateKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "rsaSign: input is null");
        return NULL;
    }
    jsize messageLen = (*env)->GetArrayLength(env, message);
    jsize privateKeyLen = (*env)->GetArrayLength(env, privateKey);
    jbyte *messagePtr = (*env)->GetByteArrayElements(env, message, NULL);
    jbyte *privateKeyPtr = (*env)->GetByteArrayElements(env, privateKey, NULL);
    int outLen = 0;
    char *result = RsaSign((char *)messagePtr, messageLen, (char *)privateKeyPtr, privateKeyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, message, messagePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, privateKey, privateKeyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jboolean JNICALL
Java_mobi_timon_crypto_Rsa_verify(JNIEnv *env, jobject obj, jbyteArray message, jbyteArray signature, jbyteArray publicKey) {
    if (message == NULL || signature == NULL || publicKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "rsaVerify: input is null");
        return JNI_FALSE;
    }
    jsize messageLen = (*env)->GetArrayLength(env, message);
    jsize signatureLen = (*env)->GetArrayLength(env, signature);
    jsize publicKeyLen = (*env)->GetArrayLength(env, publicKey);
    jbyte *messagePtr = (*env)->GetByteArrayElements(env, message, NULL);
    jbyte *signaturePtr = (*env)->GetByteArrayElements(env, signature, NULL);
    jbyte *publicKeyPtr = (*env)->GetByteArrayElements(env, publicKey, NULL);
    int outLen = 0;
    char *result = RsaVerify((char *)messagePtr, messageLen, (char *)signaturePtr, signatureLen,
                             (char *)publicKeyPtr, publicKeyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, message, messagePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, signature, signaturePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, publicKey, publicKeyPtr, JNI_ABORT);
    return verifyBoolResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Rsa_privateKeyToPublicKey(JNIEnv *env, jobject obj, jbyteArray privateKey) {
    if (privateKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "rsaPrivateKeyToPublicKey: input is null");
        return NULL;
    }
    jsize privateKeyLen = (*env)->GetArrayLength(env, privateKey);
    jbyte *privateKeyPtr = (*env)->GetByteArrayElements(env, privateKey, NULL);
    int outLen = 0;
    char *result = RsaPrivateKeyToPublicKey((char *)privateKeyPtr, privateKeyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, privateKey, privateKeyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

// === Ecdsa ===
JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Ecdsa_generateKey(JNIEnv *env, jobject obj, jint curveBits) {
    int outLen = 0;
    char *result = EcdsaGenerateKey(curveBits, &outLen);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Ecdsa_sign(JNIEnv *env, jobject obj, jbyteArray message, jbyteArray privateKey) {
    if (message == NULL || privateKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "ecdsaSign: input is null");
        return NULL;
    }
    jsize messageLen = (*env)->GetArrayLength(env, message);
    jsize privateKeyLen = (*env)->GetArrayLength(env, privateKey);
    jbyte *messagePtr = (*env)->GetByteArrayElements(env, message, NULL);
    jbyte *privateKeyPtr = (*env)->GetByteArrayElements(env, privateKey, NULL);
    int outLen = 0;
    char *result = EcdsaSign((char *)messagePtr, messageLen, (char *)privateKeyPtr, privateKeyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, message, messagePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, privateKey, privateKeyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jboolean JNICALL
Java_mobi_timon_crypto_Ecdsa_verify(JNIEnv *env, jobject obj, jbyteArray message, jbyteArray signature, jbyteArray publicKey) {
    if (message == NULL || signature == NULL || publicKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "ecdsaVerify: input is null");
        return JNI_FALSE;
    }
    jsize messageLen = (*env)->GetArrayLength(env, message);
    jsize signatureLen = (*env)->GetArrayLength(env, signature);
    jsize publicKeyLen = (*env)->GetArrayLength(env, publicKey);
    jbyte *messagePtr = (*env)->GetByteArrayElements(env, message, NULL);
    jbyte *signaturePtr = (*env)->GetByteArrayElements(env, signature, NULL);
    jbyte *publicKeyPtr = (*env)->GetByteArrayElements(env, publicKey, NULL);
    int outLen = 0;
    char *result = EcdsaVerify((char *)messagePtr, messageLen, (char *)signaturePtr, signatureLen,
                               (char *)publicKeyPtr, publicKeyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, message, messagePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, signature, signaturePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, publicKey, publicKeyPtr, JNI_ABORT);
    return verifyBoolResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Ecdsa_privateKeyToPublicKey(JNIEnv *env, jobject obj, jbyteArray privateKey) {
    if (privateKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "ecdsaPrivateKeyToPublicKey: input is null");
        return NULL;
    }
    jsize privateKeyLen = (*env)->GetArrayLength(env, privateKey);
    jbyte *privateKeyPtr = (*env)->GetByteArrayElements(env, privateKey, NULL);
    int outLen = 0;
    char *result = EcdsaPrivateKeyToPublicKey((char *)privateKeyPtr, privateKeyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, privateKey, privateKeyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

// === Ed25519 ===
JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Ed25519_generateKey(JNIEnv *env, jobject obj) {
    int outLen = 0;
    char *result = Ed25519GenerateKey(&outLen);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Ed25519_sign(JNIEnv *env, jobject obj, jbyteArray message, jbyteArray privateKey) {
    if (message == NULL || privateKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "ed25519Sign: input is null");
        return NULL;
    }
    jsize messageLen = (*env)->GetArrayLength(env, message);
    jsize privateKeyLen = (*env)->GetArrayLength(env, privateKey);
    jbyte *messagePtr = (*env)->GetByteArrayElements(env, message, NULL);
    jbyte *privateKeyPtr = (*env)->GetByteArrayElements(env, privateKey, NULL);
    int outLen = 0;
    char *result = Ed25519Sign((char *)messagePtr, messageLen, (char *)privateKeyPtr, privateKeyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, message, messagePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, privateKey, privateKeyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jboolean JNICALL
Java_mobi_timon_crypto_Ed25519_verify(JNIEnv *env, jobject obj, jbyteArray message, jbyteArray signature, jbyteArray publicKey) {
    if (message == NULL || signature == NULL || publicKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "ed25519Verify: input is null");
        return JNI_FALSE;
    }
    jsize messageLen = (*env)->GetArrayLength(env, message);
    jsize signatureLen = (*env)->GetArrayLength(env, signature);
    jsize publicKeyLen = (*env)->GetArrayLength(env, publicKey);
    jbyte *messagePtr = (*env)->GetByteArrayElements(env, message, NULL);
    jbyte *signaturePtr = (*env)->GetByteArrayElements(env, signature, NULL);
    jbyte *publicKeyPtr = (*env)->GetByteArrayElements(env, publicKey, NULL);
    int outLen = 0;
    char *result = Ed25519Verify((char *)messagePtr, messageLen, (char *)signaturePtr, signatureLen,
                                 (char *)publicKeyPtr, publicKeyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, message, messagePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, signature, signaturePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, publicKey, publicKeyPtr, JNI_ABORT);
    return verifyBoolResult(env, result, outLen);
}

// === Hash Extended ===
JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Hash_ripemd160(JNIEnv *env, jobject obj, jbyteArray data) {
    if (data == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "ripemd160: input data is null");
        return NULL;
    }
    jsize dataLen = (*env)->GetArrayLength(env, data);
    jbyte *dataPtr = (*env)->GetByteArrayElements(env, data, NULL);
    int outLen = 0;
    char *result = Ripemd160((char *)dataPtr, dataLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, data, dataPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Hash_keccak256(JNIEnv *env, jobject obj, jbyteArray data) {
    if (data == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "keccak256: input data is null");
        return NULL;
    }
    jsize dataLen = (*env)->GetArrayLength(env, data);
    jbyte *dataPtr = (*env)->GetByteArrayElements(env, data, NULL);
    int outLen = 0;
    char *result = Keccak256((char *)dataPtr, dataLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, data, dataPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Hash_keccak512(JNIEnv *env, jobject obj, jbyteArray data) {
    if (data == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "keccak512: input data is null");
        return NULL;
    }
    jsize dataLen = (*env)->GetArrayLength(env, data);
    jbyte *dataPtr = (*env)->GetByteArrayElements(env, data, NULL);
    int outLen = 0;
    char *result = Keccak512((char *)dataPtr, dataLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, data, dataPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

// === Secp256k1 ===
JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Secp256k1_generateKey(JNIEnv *env, jobject obj) {
    int outLen = 0;
    char *result = Secp256k1GenerateKey(&outLen);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Secp256k1_privateKeyToPublicKey(JNIEnv *env, jobject obj, jbyteArray privateKey, jboolean compressed) {
    if (privateKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "secp256k1PrivateKeyToPublicKey: privateKey is null");
        return NULL;
    }
    jsize privateKeyLen = (*env)->GetArrayLength(env, privateKey);
    jbyte *privateKeyPtr = (*env)->GetByteArrayElements(env, privateKey, NULL);
    int outLen = 0;
    char *result = Secp256k1PrivateKeyToPublicKey((char *)privateKeyPtr, privateKeyLen, compressed ? 1 : 0, &outLen);
    (*env)->ReleaseByteArrayElements(env, privateKey, privateKeyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Secp256k1_sign(JNIEnv *env, jobject obj, jbyteArray message, jbyteArray privateKey) {
    if (message == NULL || privateKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "secp256k1Sign: input is null");
        return NULL;
    }
    jsize messageLen = (*env)->GetArrayLength(env, message);
    jsize privateKeyLen = (*env)->GetArrayLength(env, privateKey);
    jbyte *messagePtr = (*env)->GetByteArrayElements(env, message, NULL);
    jbyte *privateKeyPtr = (*env)->GetByteArrayElements(env, privateKey, NULL);
    int outLen = 0;
    char *result = Secp256k1Sign((char *)messagePtr, messageLen, (char *)privateKeyPtr, privateKeyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, message, messagePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, privateKey, privateKeyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jboolean JNICALL
Java_mobi_timon_crypto_Secp256k1_verify(JNIEnv *env, jobject obj, jbyteArray message, jbyteArray signature, jbyteArray publicKey) {
    if (message == NULL || signature == NULL || publicKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "secp256k1Verify: input is null");
        return JNI_FALSE;
    }
    jsize messageLen = (*env)->GetArrayLength(env, message);
    jsize signatureLen = (*env)->GetArrayLength(env, signature);
    jsize publicKeyLen = (*env)->GetArrayLength(env, publicKey);
    jbyte *messagePtr = (*env)->GetByteArrayElements(env, message, NULL);
    jbyte *signaturePtr = (*env)->GetByteArrayElements(env, signature, NULL);
    jbyte *publicKeyPtr = (*env)->GetByteArrayElements(env, publicKey, NULL);
    int outLen = 0;
    char *result = Secp256k1Verify((char *)messagePtr, messageLen, (char *)signaturePtr, signatureLen,
                                   (char *)publicKeyPtr, publicKeyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, message, messagePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, signature, signaturePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, publicKey, publicKeyPtr, JNI_ABORT);
    return verifyBoolResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Secp256k1_recoverPublicKey(JNIEnv *env, jobject obj, jbyteArray message, jbyteArray signature, jboolean compressed) {
    if (message == NULL || signature == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "secp256k1RecoverPublicKey: input is null");
        return NULL;
    }
    jsize messageLen = (*env)->GetArrayLength(env, message);
    jsize signatureLen = (*env)->GetArrayLength(env, signature);
    jbyte *messagePtr = (*env)->GetByteArrayElements(env, message, NULL);
    jbyte *signaturePtr = (*env)->GetByteArrayElements(env, signature, NULL);
    int outLen = 0;
    char *result = Secp256k1RecoverPublicKey((char *)messagePtr, messageLen, (char *)signaturePtr, signatureLen,
                                              compressed ? 1 : 0, &outLen);
    (*env)->ReleaseByteArrayElements(env, message, messagePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, signature, signaturePtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

// === Schnorr ===
JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Secp256k1_schnorrSign(JNIEnv *env, jobject obj, jbyteArray message, jbyteArray privateKey) {
    if (message == NULL || privateKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "schnorrSign: input is null");
        return NULL;
    }
    jsize messageLen = (*env)->GetArrayLength(env, message);
    jsize privateKeyLen = (*env)->GetArrayLength(env, privateKey);
    jbyte *messagePtr = (*env)->GetByteArrayElements(env, message, NULL);
    jbyte *privateKeyPtr = (*env)->GetByteArrayElements(env, privateKey, NULL);
    int outLen = 0;
    char *result = SchnorrSign((char *)messagePtr, messageLen, (char *)privateKeyPtr, privateKeyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, message, messagePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, privateKey, privateKeyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jboolean JNICALL
Java_mobi_timon_crypto_Secp256k1_schnorrVerify(JNIEnv *env, jobject obj, jbyteArray message, jbyteArray signature, jbyteArray publicKey) {
    if (message == NULL || signature == NULL || publicKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "schnorrVerify: input is null");
        return JNI_FALSE;
    }
    jsize messageLen = (*env)->GetArrayLength(env, message);
    jsize signatureLen = (*env)->GetArrayLength(env, signature);
    jsize publicKeyLen = (*env)->GetArrayLength(env, publicKey);
    jbyte *messagePtr = (*env)->GetByteArrayElements(env, message, NULL);
    jbyte *signaturePtr = (*env)->GetByteArrayElements(env, signature, NULL);
    jbyte *publicKeyPtr = (*env)->GetByteArrayElements(env, publicKey, NULL);
    int outLen = 0;
    char *result = SchnorrVerify((char *)messagePtr, messageLen, (char *)signaturePtr, signatureLen,
                                 (char *)publicKeyPtr, publicKeyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, message, messagePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, signature, signaturePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, publicKey, publicKeyPtr, JNI_ABORT);
    return verifyBoolResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Secp256k1_schnorrPrivateKeyToPublicKey(JNIEnv *env, jobject obj, jbyteArray privateKey) {
    if (privateKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "schnorrPrivateKeyToPublicKey: privateKey is null");
        return NULL;
    }
    jsize privateKeyLen = (*env)->GetArrayLength(env, privateKey);
    jbyte *privateKeyPtr = (*env)->GetByteArrayElements(env, privateKey, NULL);
    int outLen = 0;
    char *result = SchnorrPrivateKeyToPublicKey((char *)privateKeyPtr, privateKeyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, privateKey, privateKeyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

// === BLS ===
JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Bls_generateKey(JNIEnv *env, jobject obj) {
    int outLen = 0;
    char *result = BlsGenerateKey(&outLen);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Bls_privateKeyToPublicKey(JNIEnv *env, jobject obj, jbyteArray privateKey) {
    if (privateKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "blsPrivateKeyToPublicKey: privateKey is null");
        return NULL;
    }
    jsize privateKeyLen = (*env)->GetArrayLength(env, privateKey);
    jbyte *privateKeyPtr = (*env)->GetByteArrayElements(env, privateKey, NULL);
    int outLen = 0;
    char *result = BlsPrivateKeyToPublicKey((char *)privateKeyPtr, privateKeyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, privateKey, privateKeyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Bls_sign(JNIEnv *env, jobject obj, jbyteArray message, jbyteArray privateKey) {
    if (message == NULL || privateKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "blsSign: input is null");
        return NULL;
    }
    jsize messageLen = (*env)->GetArrayLength(env, message);
    jsize privateKeyLen = (*env)->GetArrayLength(env, privateKey);
    jbyte *messagePtr = (*env)->GetByteArrayElements(env, message, NULL);
    jbyte *privateKeyPtr = (*env)->GetByteArrayElements(env, privateKey, NULL);
    int outLen = 0;
    char *result = BlsSign((char *)messagePtr, messageLen, (char *)privateKeyPtr, privateKeyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, message, messagePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, privateKey, privateKeyPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jboolean JNICALL
Java_mobi_timon_crypto_Bls_verify(JNIEnv *env, jobject obj, jbyteArray message, jbyteArray signature, jbyteArray publicKey) {
    if (message == NULL || signature == NULL || publicKey == NULL) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "blsVerify: input is null");
        return JNI_FALSE;
    }
    jsize messageLen = (*env)->GetArrayLength(env, message);
    jsize signatureLen = (*env)->GetArrayLength(env, signature);
    jsize publicKeyLen = (*env)->GetArrayLength(env, publicKey);
    jbyte *messagePtr = (*env)->GetByteArrayElements(env, message, NULL);
    jbyte *signaturePtr = (*env)->GetByteArrayElements(env, signature, NULL);
    jbyte *publicKeyPtr = (*env)->GetByteArrayElements(env, publicKey, NULL);
    int outLen = 0;
    char *result = BlsVerify((char *)messagePtr, messageLen, (char *)signaturePtr, signatureLen,
                             (char *)publicKeyPtr, publicKeyLen, &outLen);
    (*env)->ReleaseByteArrayElements(env, message, messagePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, signature, signaturePtr, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, publicKey, publicKeyPtr, JNI_ABORT);
    return verifyBoolResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Bls_aggregateSignatures(JNIEnv *env, jobject obj, jbyteArray signatures, jint count) {
    if (signatures == NULL || count <= 0) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "blsAggregateSignatures: input is null or count is invalid");
        return NULL;
    }
    jsize signaturesLen = (*env)->GetArrayLength(env, signatures);
    jbyte *signaturesPtr = (*env)->GetByteArrayElements(env, signatures, NULL);
    int outLen = 0;
    char *result = BlsAggregateSignatures((char *)signaturesPtr, signaturesLen, count, &outLen);
    (*env)->ReleaseByteArrayElements(env, signatures, signaturesPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}

JNIEXPORT jbyteArray JNICALL
Java_mobi_timon_crypto_Bls_aggregatePublicKeys(JNIEnv *env, jobject obj, jbyteArray publicKeys, jint count) {
    if (publicKeys == NULL || count <= 0) {
        jclass excClass = (*env)->FindClass(env, "mobi/timon/crypto/EncException");
        (*env)->ThrowNew(env, excClass, "blsAggregatePublicKeys: input is null or count is invalid");
        return NULL;
    }
    jsize publicKeysLen = (*env)->GetArrayLength(env, publicKeys);
    jbyte *publicKeysPtr = (*env)->GetByteArrayElements(env, publicKeys, NULL);
    int outLen = 0;
    char *result = BlsAggregatePublicKeys((char *)publicKeysPtr, publicKeysLen, count, &outLen);
    (*env)->ReleaseByteArrayElements(env, publicKeys, publicKeysPtr, JNI_ABORT);
    return processResult(env, result, outLen);
}
