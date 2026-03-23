# Contributing to crypto

Thank you for your interest in contributing to crypto!

crypto is an Android native cryptography library with a Kotlin API, internally using a Kotlin → JNI (C) → Go pipeline. We welcome contributions of all kinds: bug fixes, new cryptographic algorithms, tests, documentation, and more.

All contributors are expected to follow our [Code of Conduct](#code-of-conduct).

## Table of Contents

- [Getting Started](#getting-started)
- [Git Workflow](#git-workflow)
  - [Fork the Repository](#1-fork-the-repository)
  - [Clone Your Fork](#2-clone-your-fork)
  - [Add Upstream Remote](#3-add-upstream-remote)
  - [Keep Your Fork Synced](#4-keep-your-fork-synced)
  - [Create a Branch](#5-create-a-branch)
  - [Make Changes](#6-make-changes)
  - [Commit Your Changes](#7-commit-your-changes)
  - [Push to Your Fork](#8-push-to-your-fork)
  - [Open a Pull Request](#9-open-a-pull-request)
- [Pull Request Guidelines](#pull-request-guidelines)
  - [PR Title Format](#pr-title-format)
  - [PR Description](#pr-description)
  - [PR Lifecycle](#pr-lifecycle)
- [Code Style](#code-style)
- [Three-Layer Architecture](#three-layer-architecture)
- [Testing](#testing)
- [Building](#building)
- [Security](#security)
- [Release Process](#release-process)
- [Code of Conduct](#code-of-conduct)

---

## Getting Started

### Prerequisites

- Go 1.26 or later
- Android SDK (API 33+)
- Android NDK
- JDK 17

### Setup

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/crypto.git
cd crypto

# Add upstream as remote
git remote add upstream https://github.com/ic-timon/crypto.git

# Verify
git remote -v
# origin    https://github.com/YOUR_USERNAME/crypto.git (fetch)
# origin    https://github.com/YOUR_USERNAME/crypto.git (push)
# upstream  https://github.com/ic-timon/crypto.git (fetch)
# upstream  https://github.com/ic-timon/crypto.git (push)
```

---

## Git Workflow

### 1. Fork the Repository

Click the **Fork** button on the [main repository](https://github.com/ic-timon/crypto) page.

### 2. Clone Your Fork

```bash
git clone https://github.com/YOUR_USERNAME/crypto.git
cd crypto
```

### 3. Add Upstream Remote

```bash
git remote add upstream https://github.com/ic-timon/crypto.git
```

### 4. Keep Your Fork Synced

Before starting new work, always sync your fork with the latest upstream:

```bash
# Fetch latest from upstream
git fetch upstream

# Switch to main branch
git checkout main

# Merge upstream/main into your main
git merge upstream/main

# Push updates to your fork
git push origin main
```

### 5. Create a Branch

Create a new branch for your work. Never develop on `main`.

```bash
git checkout main
git fetch upstream
git merge upstream/main  # or git rebase upstream/main

# Create a new branch
git checkout -b feature/add-bls-signature
# or for bug fixes
git checkout -b fix/aead-gcm-memory-leak
# or for documentation
git checkout -b docs/update-readme
```

**Branch naming conventions:**

| Prefix | Purpose |
|--------|---------|
| `feature/` | New features |
| `fix/` | Bug fixes |
| `docs/` | Documentation updates |
| `test/` | Test-related changes |
| `refactor/` | Code refactoring |
| `crypto/` | Cryptographic implementation changes |

### 6. Make Changes

Implement your changes following the [Code Style](#code-style) guidelines.

### 7. Commit Your Changes

Follow the commit message format:

```
<type>: <short description>

Types:
- feat:     New feature
- fix:      Bug fix
- docs:     Documentation changes
- style:    Code style (formatting, no logic change)
- refactor: Code refactoring
- test:     Adding or updating tests
- chore:    Build or tooling changes
- crypto:   Cryptographic implementation changes

Examples:
feat: add BLS12-381 signature aggregation support
fix: resolve AES-GCM memory leak in decryption path
crypto: optimize SHA-256 performance on ARM64
```

Keep commits **atomic** - each commit should represent a single logical change. If your change naturally splits into multiple independent parts, use multiple commits.

### 8. Push to Your Fork

```bash
# Push the branch to your fork
git push origin feature/add-bls-signature
```

### 9. Open a Pull Request

1. Go to your fork on GitHub
2. Click **Compare & pull request**
3. Fill in the PR description (see [PR Description](#pr-description))
4. Submit the pull request

---

## Pull Request Guidelines

### PR Title Format

```
<type>: <short description>
```

Use the same type prefixes as commit messages (see [Commit Your Changes](#7-commit-your-changes)).

**Good titles:**
```
feat: add BLS12-381 signature aggregation
fix: resolve AES-GCM memory leak
crypto: optimize SHA-256 performance on ARM64
```

**Bad titles:**
```
Fixed something
Updates
WIP
My changes
```

### PR Description

Use this template:

```markdown
## Description
Briefly describe what this PR does.

## Motivation
Why is this change needed? What problem does it solve?

## Changes
- List the main changes made
- Note any new APIs or modified behaviors
- Mention breaking changes if applicable

## Testing
- [ ] Local tests pass
- [ ] New test cases added (if applicable)
- [ ] Documentation updated (if applicable)

## Related Issues
Fixes #123
```

### PR Lifecycle

1. **Open** - Submit your PR for review
2. **Review** - Maintainer reviews your code; respond to feedback
3. **Amend** - Push fixes using `git commit --amend` or new commits
4. **Merge** - Once approved, maintainer merges your PR

---

## Code Style

This project follows the official Kotlin code style. See [AGENTS.md](AGENTS.md) for detailed guidelines:

- **Indentation**: 4 spaces (no tabs)
- **Max line length**: 100 characters
- **Imports order**: Android/Compose → Third-party → Project (alphabetically sorted)
- **Naming**:
  - Classes/Objects/Functions: `PascalCase` / `camelCase`
  - Constants: `PascalCase` or `SCREAMING_SNAKE_CASE`
  - Test methods: `snake_case` (e.g., `addition_isCorrect`)
- **Prefer `val` over `var`**
- **Use `object` for singletons**

```kotlin
object Hash {
    external fun sha256(data: ByteArray): ByteArray
}
```

---

## Three-Layer Architecture

This library uses a three-layer architecture: **Kotlin → JNI (C) → Go**.

When modifying cryptographic functions, you **must** update all three layers:

1. **Kotlin API** - `crypto/src/main/java/mobi/timon/crypto/*.kt`
   - `external fun` declarations

2. **JNI Bridge** - `crypto/src/main/cpp/enc_jni.c`
   - `Java_mobi_timon_crypto_*` JNI bindings

3. **Go Implementation** - `crypto/src/main/go/*.go`
   - `//export` Go functions

**Memory management**: Go returns C-allocated buffers via `FreeBytes` in JNI. Boolean verify functions use `verifyBoolResult`.

**Example**: To add a new hash function `blake2b256`:
1. Add `external fun blake2b256(data: ByteArray): ByteArray` to `Hash.kt`
2. Add `Java_mobi_timon_crypto_Hash_blake2b256` to `enc_jni.c`
3. Add `//export blake2b256` function to `hash.go`

---

## Testing

**Unit tests** (app module):
```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:test --tests "mobi.timon.android.ExampleUnitTest"
```

**Instrumented tests** (requires device/emulator):
```bash
./gradlew :app:connectedDebugAndroidTest
./gradlew :crypto:connectedDebugAndroidTest

# Run single test class
./gradlew :crypto:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=mobi.timon.crypto.HashHmacRandomInstrumentedTest

# Run single test method
./gradlew :crypto:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=mobi.timon.crypto.HashHmacRandomInstrumentedTest#sha256_knownVector
```

**Lint**:
```bash
./gradlew :app:lintDebug
./gradlew :crypto:lintDebug
```

Reports are generated at:
- `app/build/reports/lint-results-debug.html`
- `crypto/build/reports/lint-results-debug.html`

---

## Building

```bash
# Build crypto module (debug)
./gradlew :crypto:assembleDebug

# Build crypto module (release)
./gradlew :crypto:assembleRelease

# Compile Go only (generates libencgo.so)
./gradlew :crypto:compileGoDebug

# Full clean build
./gradlew clean assembleDebug
```

---

## Security

> **⚠️ This is a cryptography library. Security is paramount.**

- **Do not** introduce backdoors or weaken cryptographic implementations
- **Do not** use insecure algorithms unless required for legacy interoperability (MD5, SHA-1, DES are documented as weak)
- **Report security vulnerabilities** through [GitHub Issues](https://github.com/ic-timon/crypto/issues) with the label `security`

For sensitive security issues, please check our [Security Policy](#security-policy) and report appropriately.

---

## Release Process

Releases are automated via GitHub Actions when a tag is pushed:

```bash
# Create and push a tag
git tag v1.2.0
git push origin v1.2.0
```

The release workflow:
1. Builds the crypto module
2. Publishes to GitHub Packages
3. Creates a GitHub Release

Version numbers follow [Semantic Versioning (semver)](https://semver.org/).

---

## Code of Conduct

This project adheres to the [Contributor Covenant](https://www.contributor-covenant.org/) code of conduct. By participating, you are expected to uphold this code.

---

## Questions?

- Open an issue on [GitHub](https://github.com/ic-timon/crypto/issues)
- Check the [README.md](README.md) for project overview
- Review [AGENTS.md](AGENTS.md) for development guidelines
