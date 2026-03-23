# 参与贡献

感谢您对参与 crypto 项目的贡献感兴趣！

crypto 是一个 Android 原生密码学库，对外提供 Kotlin API，内部采用 Kotlin → JNI (C) → Go 技术栈。我们欢迎各种形式的贡献：Bug 修复、新密码学算法、测试、文档等。

所有贡献者都需要遵守我们的[行为准则](#行为准则)。

## 目录

- [快速开始](#快速开始)
- [Git 工作流程](#git-工作流程)
  - [Fork 仓库](#1-fork-仓库)
  - [克隆你的 Fork](#2-克隆你的-fork)
  - [添加上游远程仓库](#3-添加上游远程仓库)
  - [保持 Fork 同步](#4-保持-fork-同步)
  - [创建分支](#5-创建分支)
  - [进行修改](#6-进行修改)
  - [提交更改](#7-提交更改)
  - [推送到你的 Fork](#8-推送到你的-fork)
  - [创建 Pull Request](#9-创建-pull-request)
- [Pull Request 指南](#pull-request-指南)
  - [PR 标题格式](#pr-标题格式)
  - [PR 描述](#pr-描述)
  - [PR 流程](#pr-流程)
- [代码风格](#代码风格)
- [三层架构](#三层架构)
- [测试](#测试)
- [构建](#构建)
- [安全](#安全)
- [发布流程](#发布流程)
- [行为准则](#行为准则)

---

## 快速开始

### 环境要求

- Go 1.26 或更高版本
- Android SDK (API 33+)
- Android NDK
- JDK 17

### 环境配置

```bash
# 克隆你的 Fork
git clone https://github.com/YOUR_USERNAME/crypto.git
cd crypto

# 添加上游仓库作为远程仓库
git remote add upstream https://github.com/ic-timon/crypto.git

# 验证配置
git remote -v
# origin    https://github.com/YOUR_USERNAME/crypto.git (fetch)
# origin    https://github.com/YOUR_USERNAME/crypto.git (push)
# upstream  https://github.com/ic-timon/crypto.git (fetch)
# upstream  https://github.com/ic-timon/crypto.git (push)
```

---

## Git 工作流程

### 1. Fork 仓库

在[主仓库](https://github.com/ic-timon/crypto)页面点击 **Fork** 按钮。

### 2. 克隆你的 Fork

```bash
git clone https://github.com/YOUR_USERNAME/crypto.git
cd crypto
```

### 3. 添加上游远程仓库

```bash
git remote add upstream https://github.com/ic-timon/crypto.git
```

### 4. 保持 Fork 同步

在进行新工作之前，始终将你的 Fork 与上游最新代码同步：

```bash
# 获取上游最新代码
git fetch upstream

# 切换到 main 分支
git checkout main

# 将 upstream/main 合并到你的 main
git merge upstream/main

# 推送更新到你的 Fork
git push origin main
```

### 5. 创建分支

为你的工作创建新分支。**不要在 `main` 上进行开发**。

```bash
git checkout main
git fetch upstream
git merge upstream/main  # 或 git rebase upstream/main

# 创建新分支
git checkout -b feature/add-bls-signature
# 或者修复 Bug
git checkout -b fix/aead-gcm-memory-leak
# 或者更新文档
git checkout -b docs/update-readme
```

**分支命名规范：**

| 前缀 | 用途 |
|------|------|
| `feature/` | 新功能 |
| `fix/` | Bug 修复 |
| `docs/` | 文档更新 |
| `test/` | 测试相关变更 |
| `refactor/` | 代码重构 |
| `crypto/` | 密码学实现变更 |

### 6. 进行修改

按照[代码风格](#代码风格)指南实现你的更改。

### 7. 提交更改

遵循以下提交信息格式：

```
<类型>: <简短描述>

类型:
- feat:     新功能
- fix:      Bug 修复
- docs:     文档变更
- style:    代码风格（格式调整，不影响逻辑）
- refactor: 代码重构
- test:     添加或更新测试
- chore:    构建或工具变更
- crypto:   密码学实现变更

示例:
feat: add BLS12-381 signature aggregation support
fix: resolve AES-GCM memory leak in decryption path
crypto: optimize SHA-256 performance on ARM64
```

保持提交**原子性** - 每个提交应代表一个独立的逻辑变更。如果你的变更可以自然拆分为多个独立部分，请使用多个提交。

### 8. 推送到你的 Fork

```bash
# 将分支推送到你的 Fork
git push origin feature/add-bls-signature
```

### 9. 创建 Pull Request

1. 访问你在 GitHub 上的 Fork
2. 点击 **Compare & pull request**
3. 填写 PR 描述（参见 [PR 描述](#pr-描述)）
4. 提交 Pull Request

---

## Pull Request 指南

### PR 标题格式

```
<类型>: <简短描述>
```

使用与提交信息相同的类型前缀（参见[提交更改](#7-提交更改)）。

**好的标题：**
```
feat: add BLS12-381 signature aggregation
fix: resolve AES-GCM memory leak
crypto: optimize SHA-256 performance on ARM64
```

**不好的标题：**
```
Fixed something
Updates
WIP
My changes
```

### PR 描述

使用以下模板：

```markdown
## 描述
简要说明这个 PR 做了什么。

## 动机
为什么需要这个变更？解决了什么问题？

## 变更内容
- 列出主要变更
- 说明新增或修改的 API（如适用）
- 说明是否有破坏性变更

## 测试
- [ ] 本地测试通过
- [ ] 新增了测试用例（如适用）
- [ ] 文档已更新（如适用）

## 相关 Issue
Fixes #123
```

### PR 流程

1. **Open** - 提交 PR 进行 Review
2. **Review** - 维护者 Review 你的代码；根据反馈进行修改
3. **Amend** - 使用 `git commit --amend` 或新提交推送修复
4. **Merge** - 获得批准后，维护者合并你的 PR

---

## 代码风格

本项目遵循官方 Kotlin 代码风格。详见 [AGENTS.md](AGENTS.md)：

- **缩进**：4 空格（不使用 Tab）
- **最大行长**：100 字符
- **导入顺序**：Android/Compose → 第三方 → 项目（按字母顺序排序）
- **命名**：
  - 类/对象/函数：`PascalCase` / `camelCase`
  - 常量：`PascalCase` 或 `SCREAMING_SNAKE_CASE`
  - 测试方法：`snake_case`（如 `addition_isCorrect`）
- **优先使用 `val`**
- **使用 `object` 实现单例**

```kotlin
object Hash {
    external fun sha256(data: ByteArray): ByteArray
}
```

---

## 三层架构

本库采用三层架构：**Kotlin → JNI (C) → Go**。

修改密码学函数时，必须同步更新全部三层：

1. **Kotlin API** - `crypto/src/main/java/mobi/timon/crypto/*.kt`
   - `external fun` 声明

2. **JNI 桥接层** - `crypto/src/main/cpp/enc_jni.c`
   - `Java_mobi_timon_crypto_*` JNI 绑定

3. **Go 实现** - `crypto/src/main/go/*.go`
   - `//export` Go 函数

**内存管理**：Go 通过 JNI 中的 `FreeBytes` 释放 C 分配的缓冲区。布尔验证函数使用 `verifyBoolResult`。

**示例**：添加新的哈希函数 `blake2b256`：
1. 在 `Hash.kt` 中添加 `external fun blake2b256(data: ByteArray): ByteArray`
2. 在 `enc_jni.c` 中添加 `Java_mobi_timon_crypto_Hash_blake2b256`
3. 在 `hash.go` 中添加 `//export blake2b256` 函数

---

## 测试

**单元测试**（app 模块）：
```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:test --tests "mobi.timon.android.ExampleUnitTest"
```

**Instrumented 测试**（需要设备/模拟器）：
```bash
./gradlew :app:connectedDebugAndroidTest
./gradlew :crypto:connectedDebugAndroidTest

# 运行单个测试类
./gradlew :crypto:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=mobi.timon.crypto.HashHmacRandomInstrumentedTest

# 运行单个测试方法
./gradlew :crypto:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=mobi.timon.crypto.HashHmacRandomInstrumentedTest#sha256_knownVector
```

**Lint**：
```bash
./gradlew :app:lintDebug
./gradlew :crypto:lintDebug
```

报告生成位置：
- `app/build/reports/lint-results-debug.html`
- `crypto/build/reports/lint-results-debug.html`

---

## 构建

```bash
# 构建 crypto 模块（Debug）
./gradlew :crypto:assembleDebug

# 构建 crypto 模块（Release）
./gradlew :crypto:assembleRelease

# 仅编译 Go（生成 libencgo.so）
./gradlew :crypto:compileGoDebug

# 完全清空重新构建
./gradlew clean assembleDebug
```

---

## 安全

> **⚠️ 这是一个密码学库。安全性至关重要。**

- **不要**引入后门或削弱密码学实现
- **不要**使用不安全的算法，除非是为了兼容旧系统（MD5、SHA-1、DES 被标记为弱算法）
- **通过 [GitHub Issues](https://github.com/ic-timon/crypto/issues) 报告安全漏洞**，请添加 `security` 标签

---

## 发布流程

通过推送 tag 触发 GitHub Actions 自动发布：

```bash
# 创建并推送 tag
git tag v1.2.0
git push origin v1.2.0
```

发布流程：
1. 构建 crypto 模块
2. 发布到 GitHub Packages
3. 创建 GitHub Release

版本号遵循[语义化版本 (semver)](https://semver.org/)。

---

## 行为准则

本项目遵守 [Contributor Covenant](https://www.contributor-covenant.org/) 行为准则。参与本项目即表示你同意遵守该准则。

---

## 问题？

- 在 [GitHub Issues](https://github.com/ic-timon/crypto/issues) 上提交问题
- 查看 [README.md](README.md) 了解项目概况
- 阅读 [AGENTS.md](AGENTS.md) 了解开发指南
