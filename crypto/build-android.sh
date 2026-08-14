#!/bin/bash
# cargo-ndk 编 4 个 Android ABI 的 libencrust.so → src/androidMain/jniLibs/<abi>/
# 用法: bash build-android.sh（由 Gradle buildRustAndroid task 调用）
set -euo pipefail
cd "$(dirname "$0")/../rust"

# NDK 解析：ANDROID_NDK_HOME 优先，否则取 macOS SDK 目录最高版本
NDK="${ANDROID_NDK_HOME:-}"
if [ -z "$NDK" ]; then
  NDK=$(ls -d "$HOME/Library/Android/sdk/ndk/"* 2>/dev/null | sort -V | tail -1)
fi
if [ -z "$NDK" ] || [ ! -d "$NDK" ]; then
  echo "ERROR: ANDROID_NDK_HOME not set and no NDK found under ~/Library/Android/sdk/ndk/" >&2
  exit 1
fi
export ANDROID_NDK_HOME="$NDK"
# release profile 默认 -D warnings；关掉防 cargo-ndk 内部 warning 误报
export RUSTFLAGS="${RUSTFLAGS:--A warnings}"
echo "[build-android] ANDROID_NDK_HOME=$NDK"

for pair in "arm64-v8a:aarch64-linux-android" "armeabi-v7a:armv7-linux-androideabi" "x86:i686-linux-android" "x86_64:x86_64-linux-android"; do
  abi="${pair%%:*}"
  target="${pair##*:}"
  echo "=== cargo ndk $abi ($target) ==="
  cargo ndk -t "$target" -P 33 build --release --features jni-bridge
  cp -f "target/$target/release/libencrust.so" "../crypto/src/androidMain/jniLibs/$abi/"
done
echo "[build-android] done → 4 ABI libencrust.so in jniLibs/"
