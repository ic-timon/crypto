#!/bin/bash
# cargo build Apple targets → libencrust.a（cinterop 用，见 cryptoNative.def）
# 用法: bash build-apple.sh（由 Gradle buildRustApple task 调用）
set -euo pipefail
cd "$(dirname "$0")/../rust"
export IPHONEOS_DEPLOYMENT_TARGET=13.0
export RUSTFLAGS="${RUSTFLAGS:--A warnings}"

for target in aarch64-apple-ios aarch64-apple-ios-sim aarch64-apple-darwin x86_64-apple-darwin; do
  echo "=== cargo build $target ==="
  cargo build --release --target "$target"
done
echo "[build-apple] done → 4 Apple libencrust.a"
