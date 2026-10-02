# 📱 简切图 (QuickSlice) - 智能九宫格与自由创意切图神器

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20%2B%20Material%203-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Compose M3" />
  <img src="https://img.shields.io/badge/Min%20SDK-26%2B-blue?style=flat-square" alt="Min SDK" />
  <img src="https://img.shields.io/badge/Build-Gradle%20Kotlin%20DSL-02303A?style=flat-square&logo=gradle&logoColor=white" alt="Gradle" />
  <img src="https://img.shields.io/badge/License-MIT-green?style=flat-square" alt="License" />
</p>

---

## 📖 项目简介 (Overview)

**「简切图」(QuickSlice)** 是一款基于 **Kotlin + Jetpack Compose + Material 3** 原生打造的现代化、全功能、极简美观的移动端智能图片切割与社交拼贴应用。

专为 **微信朋友圈、小红书轮播图、微博九宫格、Instagram 拼图** 等各大社交平台打造，支持将一张照片或多张图片智能裁剪为高质感切片、艺术杂志排版、胶片时间戳拼图与长图网格。所有图像处理均在**本地设备端（On-Device）极速完成，100% 离线隐私保护**。

---

## ✨ 核心特性 (Key Features)

### 1. 🧩 丰富多样的切图版式 (Versatile Slice Layouts)
- **经典网格**：标准 3×3 九宫格、2×2 四宫格、1×3/3×1 三连屏、2×3、3×4、以及支持自由调节行列数（1~10）的自定义网格。
- **自由切割线（Free Split Lines）**：支持任意添加多条横纵切割线，支持拖拽磁吸（自动吸附至 **50% 中心对齐、0.618 黄金分割点、三分线与四等分线**）。
- **创意形状与拼贴**：中心 C 位留白、心形透视九宫格、自由局部多选框特写。
- **单张切割线效果大图**：支持直接导出带白/黑分割线、外围圆角衬底与阴影的艺术海报大图。

### 2. 🔍 手势操控与微调交互 (Gestures & Precision Editing)
- **双指手势无级缩放（Pinch-to-Zoom）**：支持 **1.0x ~ 5.0x** 自由放大查看画面细节与切割对齐。
- **精准拖拽放大镜（Loupe Lens）**：手指拖动角点或切割线时，自动在手指上方浮现 2x 圆形准星放大镜，彻底解决手指遮挡画面的痛点。
- **双击快速缩放/复位**：双击即可快速在 1:1 全貌和 200% 精细放大之间平滑切换。
- **触觉震动反馈（Haptic Feedback）**：切割线磁吸命中、把手拖拽与切图完成均具备细腻触感反馈。

### 3. 🧠 智能分析与人脸避让 (Smart Avoidance & AI Presets)
- **人脸/主体自动避让**：实时分析画面视觉重心与人脸区域；当切割线切过人脸时，弹出 **`💡 一键智能避让`** 提示，自动微调线条与画幅。
- **社交平台比例智能匹配**：自动检测照片长宽比并推荐小红书 3:4、朋友圈 1:1、抖音 9:16、微博横版 16:9 最佳适配版式。

### 4. ⚡ 批量多图极速切片 (Batch Processing)
- 支持一次性导入数十张图片；
- 统一批量应用版式、统一调色滤镜、统一圆角间距与水印，一键批量秒级切片并直接导出至系统相册专属分类目录。

### 5. 📋 朋友圈「防乱序」指引与一键顺次分享 (Posting Guide & Multi-Share)
- **防乱序发图对照卡**：导出时可一键生成 1~9 标号缩略图及上传步骤卡，发朋友圈再也不怕选错顺序。
- **系统多图顺次分享**：通过 `ACTION_SEND_MULTIPLE` 一键将切片按顺序分享至微信、小红书、微博或相册。

### 6. 🪄 杂志级排印与独立单格微调 (Editorial & Per-Slice Tuning)
- **杂志级版式排印**：自定义标题文本、副标题、品牌标识与条形码水印。
- **复古胶片时间戳**：支持橙红/荧光黄胶片日期印记与拍摄相机型号标签。
- **单格微调**：点击任意单独切片，可独立设置留白文字、局部滤镜或个性化贴纸。

### 7. 🛡️ 自动草稿箱与历史工程 (Auto-Draft & Resume)
- 中途退出或误触返回时，系统自动在本地保存编辑工程；
- 首页顶部常驻 **「发现未完成草稿」** 快捷卡片，随时一键恢复所有图层与线条参数。

### 8. 🌈 Material 3 灵动调色板与暗色模式 (M3 Theming)
- 内置 **8 套精选 M3 Expressive 主题色**（极光靛蓝、薄荷翠绿、落日暖橙、深海蔚蓝、紫罗兰等）；
- 完美适配系统深色/浅色模式与全面屏手势沉浸式边到边（Edge-to-Edge）。

---

## 🛠️ 技术架构与选型 (Tech Stack)

| 模块 | 技术选型 | 说明 |
| :--- | :--- | :--- |
| **编程语言** | Kotlin 1.9+ | 100% 现代 Kotlin 语言编写，协程与 Flow 驱动 |
| **UI 框架** | Jetpack Compose + Material 3 | 声明式响应式 UI，动态配色与平滑状态驱动 |
| **架构设计** | MVVM / Clean Architecture | `StateFlow` + `SliceViewModel` 单向数据流 |
| **图像处理** | Android Canvas + Native Graphics | 自研轻量级高性能位图矩阵变换与裁剪渲染引擎 |
| **手势系统** | Compose PointerInput | 变换手势（Transform）、点击（Tap）、拖拽（Drag）多维结合 |
| **触觉引擎** | Android LocalHapticFeedback | 交互震动反馈与磁吸感知 |
| **持久化与草稿** | Internal Storage + SharedPreferences | 本地沙盒极速无损保存草稿工程与预设配置 |
| **文件共享与导出**| AndroidX FileProvider + MediaStore API | 兼容 Android 10+ 分区存储（Scoped Storage） |

---

## 📂 项目工程结构 (Project Structure)

```text
app/src/main/java/com/example/
├── MainActivity.kt                  # 主入口与 M3 动态主题容器
├── model/                           # 数据实体与模型定义
│   ├── AspectRatioOption.kt         # 预设画幅比例 (1:1, 4:3, 16:9, 3:4等)
│   ├── LayoutType.kt                # 核心切图版式枚举 (九宫格, 自由切割等)
│   ├── ImageFilterPreset.kt         # 胶片、复古、黑白、冷暖等滤镜预设
│   ├── MagazineTypographyConfig.kt  # 杂志排印与胶片时间戳配置
│   ├── StylePresetItem.kt           # 预设模板数据结构
│   └── SlicePiece.kt                # 单个切片模型
├── ui/
│   ├── components/                  # 可复用 UI 与 Canvas 渲染组件
│   │   ├── SliceCanvasOverlay.kt    # 核心交互画布（切割线、磁吸、放大镜）
│   │   ├── SocialFeedSimulator.kt   # 微信朋友圈/小红书真实九宫格模拟器
│   │   ├── MagazineTypographyDialog # 杂志排版与排印编辑器
│   │   └── ExportSettingsDialog.kt  # 格式、分辨率与水印导出弹窗
│   ├── screens/                     # 一级路由页面
│   │   ├── HomeScreen.kt            # 首页大厅（快捷切图、草稿箱、预设美图）
│   │   ├── EditorScreen.kt          # 单图工作台（缩放平移、滤镜、微调）
│   │   ├── BatchEditorScreen.kt     # 多图批量切图工作台
│   │   └── ExportPreviewScreen.kt   # 导出预览（大图预览、切片列表、防乱序卡）
│   └── theme/                       # Material 3 调色板与 Typography
├── util/                            # 图像算法与辅助工具
│   ├── BitmapHelper.kt              # 高性能位图无损裁剪与网格渲染引擎
│   ├── ImageSaver.kt                # MediaStore 相册保存、多图顺序分享、防乱序卡生成
│   ├── SmartSubjectDetector.kt      # 人脸/视觉主体智能避让算法
│   ├── DraftManager.kt              # 编辑工程本地自动保存与恢复
│   ├── SmartAnalyzer.kt             # 色彩分布与画幅智能推荐器
│   └── PresetManager.kt             # 模板预设管理器
└── viewmodel/
    └── SliceViewModel.kt            # 核心业务 ViewModel 与 UI 状态机
```

---

## 🚀 快速开始与本地构建 (Getting Started)

### 环境要求 (Prerequisites)
- **Android Studio** Hedgehog (2023.1.1) 或更高版本
- **JDK** 17+
- **Android SDK** API 26 (Android 8.0) ~ API 34 (Android 14)
- **Gradle** 8.x + AGP 8.x

### 构建步骤 (Build & Run)
1. **克隆仓库至本地**：
   ```bash
   git clone https://github.com/your-username/QuickSlice.git
   cd QuickSlice
   ```

2. **使用 Android Studio 打开项目**，等待 Gradle 依赖同步完成。

3. **编译并生成 Debug APK**：
   ```bash
   ./gradlew assembleDebug
   ```

4. **安装至连接的真机或模拟器**：
   ```bash
   ./gradlew installDebug
   ```

---

## 🔒 隐私与安全性 (Privacy & Permissions)

- **完全离线运行**：所有图片解码、滤镜渲染、切割矩阵计算均在设备本地完成，不上传任何用户图片至外部服务器。
- **最小化权限设计**：
  - 采用 Android 现代照片选择器（Photo Picker），无需申请读取外部存储权限；
  - 仅使用系统 `MediaStore` API 将用户确认导出的切片图片写入相册目录 `Pictures/简切图`。

---

## 🤝 贡献与反馈 (Contributing)

欢迎提交 Issue 和 Pull Request！
1. Fork 本项目仓库
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的修改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送至分支 (`git push origin feature/AmazingFeature`)
5. 新建 Pull Request

---

## 📄 开源许可证 (License)

本项目采用 [MIT License](LICENSE) 开源许可证。
