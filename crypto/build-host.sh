#!/bin/bash
# cargo build host target（JVM 单测 JNI 用）→ rust/target/release/libencrust.dylib|.so
set -euo pipefail
cd "$(dirname "$0")/../rust"
export RUSTFLAGS="${RUSTFLAGS:--A warnings}"
echo "=== cargo build (host: $(rustc -vV | grep host | cut -d' ' -f2)) ==="
cargo build --release --features jni-bridge
