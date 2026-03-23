# mobi.timon.crypto — JNI: Kotlin `external` must keep names matching libencjni
# (symbols like Java_mobi_timon_crypto_Hash_sha256). Merged into apps that depend on this AAR.
-keep class mobi.timon.crypto.** { *; }
