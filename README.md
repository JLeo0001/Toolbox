# 🛠️ 安卓工具箱 (Android Tools)

![Platform](https://img.shields.io/badge/Android-7.0%2B-brightgreen)
![API](https://img.shields.io/badge/API-24--34-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple)
![Compose](https://img.shields.io/badge/Compose-BOM_2024.04-ff69b4)
![License](https://img.shields.io/badge/License-MIT-yellow)
![Build](https://github.com/JLeo0001/Toolbox/actions/workflows/build.yml/badge.svg)

一个功能强大的安卓工具类应用，采用 Material Design 3 设计，支持动态取色和深色模式。

## 📥 下载

[![GitHub Release](https://img.shields.io/github/v/release/JLeo0001/Toolbox?label=最新版本)](https://github.com/JLeo0001/Toolbox/releases)

前往 [Releases](https://github.com/JLeo0001/Toolbox/releases) 下载最新 APK。

## ✨ 功能特性

### 🌐 TCP 端口扫描器

- 支持输入任意网址或 IP 地址
- 可配置端口范围（1–65535）
- 自定义超时时间（默认 3000ms）和并发线程数（默认 100，最大 1000）
- 实时显示扫描进度与开放端口列表
- 扫描历史记录（本地 Room 持久化）
- 常用主机历史记录，快速复用

### 📁 文件合并拆分器

- 兼容 **MERGEDv3** 格式的文件合并与拆分
- 支持视频文件与附件文件的**隐藏合并**
- 提供两种处理模式：
  - **内存模式**：小文件快速处理
  - **流式模式**：大文件稳定处理（推荐 1GB 以上）
- 最大支持 **8GB** 文件
- 操作历史记录追踪

### ⚙️ 系统设置

- **深色模式**：手动开关 / 跟随系统
- **动态取色**：支持 Material You 动态配色
- **历史记录管理**：自动清理、数量限制、一键清空
- **缓存管理**：缓存大小限制、一键清理
- **通知控制**：扫描完成通知开关
- **恢复默认**：一键重置所有设置

## 📋 权限说明

| 权限 | 用途 |
|---|---|
| `INTERNET` | 端口扫描网络请求 |
| `ACCESS_NETWORK_STATE` | 检测网络连接状态 |
| `READ_EXTERNAL_STORAGE` | 读取文件（合并/拆分） |
| `WRITE_EXTERNAL_STORAGE` | 写入文件（仅 Android 7–9） |
| `READ_MEDIA_VIDEO` | 读取视频文件（Android 13+） |
| `READ_MEDIA_IMAGES` | 读取图片文件（Android 13+） |
| `READ_MEDIA_AUDIO` | 读取音频文件（Android 13+） |

## 📱 系统要求

- **最低版本**：Android 7.0 (API 24)
- **目标版本**：Android 14 (API 34)
- **存储空间**：至少 50MB（安装）+ 文件处理所需额外空间

## 🏗️ 技术栈

| 类别 | 技术 |
|---|---|
| 语言 | Kotlin 1.9.22 |
| UI | Jetpack Compose + Material Design 3 |
| 架构 | MVVM (ViewModel + Repository) |
| 数据库 | Room (SQLite) |
| 异步 | Kotlin Coroutines |
| 导航 | Navigation Compose |
| 主题 | Material You 动态取色 + 深色模式 |
| 构建 | Gradle 8.7 + AGP 8.6.0 |

## 📁 项目结构

```
app/src/main/java/com/ccdyz/tools/
├── MainActivity.kt                 # 主入口 Activity
├── ui/
│   ├── theme/                      # 主题配置（颜色、图标、排版）
│   │   ├── Color.kt               # 颜色定义
│   │   ├── Theme.kt               # ToolsAppTheme 主题
│   │   ├── ThemeManager.kt        # 深色/动态取色管理
│   │   ├── Icons.kt               # 图标定义
│   │   └── Type.kt                # 排版样式
│   ├── home/                      # 首页
│   │   └── HomeScreen.kt          # 主界面（功能卡片入口）
│   ├── navigation/                # 导航配置
│   │   └── ToolsAppNavigation.kt  # 页面路由
│   ├── portscanner/               # 端口扫描功能
│   │   ├── PortScannerScreen.kt   # 扫描界面
│   │   └── PortScannerViewModel.kt# 扫描逻辑
│   ├── filemerger/                # 文件合并拆分
│   │   ├── FileMergerScreen.kt    # 合并/拆分界面
│   │   ├── FileMergerViewModel.kt # 文件处理逻辑
│   │   └── ResultCardWithFolderButton.kt  # 结果展示组件
│   └── settings/                  # 系统设置
│       ├── SettingsScreen.kt      # 设置界面
│       └── SettingsViewModel.kt   # 设置逻辑
├── data/
│   ├── database/                  # Room 数据库
│   │   ├── dao/
│   │   │   └── ScanHistoryDao.kt  # 扫描历史 DAO
│   │   └── entities/
│   │       ├── ScanHistory.kt     # 扫描记录
│   │       ├── HostHistory.kt     # 主机记录
│   │       └── FileHistory.kt     # 文件操作记录
│   └── repository/
│       └── ScanRepository.kt      # 扫描数据仓库
└── utils/
    ├── AppContext.kt              # 应用上下文持有者
    ├── AppPreferencesManager.kt   # 偏好设置管理
    ├── AppSettingsManager.kt      # 设置管理器
    ├── Constants.kt               # 全局常量
    ├── FileUtils.kt               # 文件工具类
    ├── MergedFileProcessor.kt     # MERGEDv3 处理器
    ├── NetworkUtils.kt            # 网络工具类
    └── PreferencesManager.kt      # 偏好设置基类
```

## 🚀 使用指南

### 端口扫描

1. 打开应用，点击「TCP 端口扫描器」
2. 输入目标主机名或 IP 地址
3. 设置端口范围（起始端口 - 结束端口）
4. 调整超时时间（毫秒）和并发线程数
5. 点击「开始扫描」
6. 实时查看开放端口列表及扫描进度
7. 扫描记录自动保存至历史

### 文件合并

1. 点击「文件合并拆分器」→ 切换到「合并」标签
2. 选择视频文件（载体文件）
3. 选择要隐藏的附件文件
4. 选择处理模式：
   - **内存模式**：文件读入内存处理，速度更快
   - **流式模式**：逐块流式处理，适合大文件
5. 点击「开始合并」等待完成

### 文件拆分

1. 切换到「拆分」标签
2. 选择 MERGEDv3 格式的合并文件
3. 点击「开始拆分」
4. 等待处理完成，提取原始文件

### 开发者模式

在文件合并拆分器中可启用开发者模式，查看：
- 详细的处理流程日志
- 错误详情与堆栈追踪
- 性能统计数据

## 🔧 本地构建

```bash
# 克隆仓库
git clone https://github.com/JLeo0001/Toolbox.git
cd Toolbox

# Debug 构建
./gradlew assembleDebug

# Release 构建（需配置签名）
# 1. 将 keystore 放入项目根目录
# 2. 创建 key.properties：
#    storePassword=xxx
#    keyPassword=xxx
#    keyAlias=xxx
#    storeFile=./publish.keystore
# 3. 运行
./gradlew assembleRelease
```

构建产物位于 `app/build/outputs/apk/`。

## 🤖 CI/CD

项目使用 GitHub Actions 自动构建：
- **Push / PR 到 master**：自动构建 Debug + Release APK
- **打 v* 标签**：自动构建并发布 GitHub Release
- 签名密钥通过 GitHub Secrets 注入（`KEYSTORE_BASE64` / `STORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD`）

## ⚠️ 注意事项

1. 端口扫描功能请遵守相关法律法规，仅用于**合法的网络诊断**
2. 大文件处理（>1GB）建议使用**流式模式**以避免内存溢出
3. 确保设备有足够的存储空间进行文件操作
4. 某些网络环境下的防火墙可能会阻止端口扫描活动
5. 此为个人项目，内部测试阶段，可能含有未修复的 Bug

## 📄 许可证

本项目仅供学习和研究使用。
