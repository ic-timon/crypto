#!/bin/bash
# cargo build Apple targets → libencrust.a（cinterop 用，见 cryptoNative.def）
# 用法: bash build-apple.sh（由 Gradle buildRustApple task 调用）
set -euo pipefail
cd "$(dirname "$0")/../rust"

# Linux CI（Android 构建）不需要真的编译 Apple 归档——它们只被 K/N cinterop 读取符号表，
# 从不进入 Android 产物。macOS 之外的宿主机生成空归档 stub 即可让 cinterop/任务图走通。
# （真 Apple 编译依赖 xcrun/Apple SDK，Linux 上不可行；依赖树里的 secp256k1-sys/blst 也编不了。）
if [ "$(uname)" != "Darwin" ]; then
  echo "[build-apple] 非 macOS 宿主（$(uname)）→ 生成 stub libencrust.a（Apple 归档不进 Android 产物）"
  for target in aarch64-apple-ios aarch64-apple-ios-sim aarch64-apple-darwin x86_64-apple-darwin; do
    mkdir -p "target/$target/release"
    ar rcs "target/$target/release/libencrust.a"
  done
  echo "[build-apple] done → 4 个空归档 stub"
  exit 0
fi

export IPHONEOS_DEPLOYMENT_TARGET=13.0
export RUSTFLAGS="${RUSTFLAGS:--A warnings}"

for target in aarch64-apple-ios aarch64-apple-ios-sim aarch64-apple-darwin x86_64-apple-darwin; do
  echo "=== cargo build $target ==="
  cargo build --release --target "$target"
done
echo "[build-apple] done → 4 Apple libencrust.a"
