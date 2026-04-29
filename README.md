# Koku / 观子

<p align="center">
  <img src="src/main/resources/icons/koku.png" alt="Koku Logo" width="140" />
</p>

<p align="center">
  A minimalist desktop board game collection built with JavaFX.
</p>

<p align="center">
  <a href="#english">English</a> ·
  <a href="#中文">中文</a> ·
  <a href="https://github.com/yiyangbear/koku/releases">Download</a>
</p>

---

## English

### Overview

**Koku** is a JavaFX desktop board game collection featuring multiple classic grid-based strategy games.

The project started as a Gomoku application and has been refactored into a more extensible game platform. It now supports multiple games through a shared game session, rule engine, settings system, timer service, and JavaFX user interface.

Koku is designed as both a playable desktop application and a learning project for Java, JavaFX, game logic design, and software architecture.

---

### Supported Games

| Game | Description |
| --- | --- |
| Tic-Tac-Toe | A classic 3×3 line-making game. |
| Connect Four | A gravity-based four-in-a-row game. |
| Gomoku | A five-in-a-row strategy game with optional rule settings. |
| Six-in-a-Row | A larger-board line-making variant. |

---

### Features

- Multiple board games in one desktop application
- Local two-player gameplay
- New game / reset support
- Undo support
- Win, draw, and timeout detection
- Game timer
- Last-move marker
- Coordinate display toggle
- Theme switching
- Language switching
- Configurable game settings
- JavaFX desktop interface
- macOS and Windows desktop builds

---

### Gomoku Features

Koku includes additional rule support for Gomoku:

- Configurable board size
- Five-in-a-row win detection
- Optional forbidden-move rule support
- Support for rule checks such as overline, double-four, and double-three patterns

---

### Download

You can download the latest desktop version from the [Releases](https://github.com/yiyangbear/koku/releases) page.

Available builds:

- macOS `.dmg`
- Windows `.exe`

> Note: The app is not code-signed yet.  
> macOS or Windows may show a security warning when opening the app for the first time.

#### macOS

If macOS blocks the app, right-click `Koku.app` and choose **Open**.

#### Windows

If Windows SmartScreen appears, click **More info**, then choose **Run anyway**.

---

### Run from Source

#### Requirements

- JDK 25
- Maven
- Git

#### Clone the Repository

```bash
git clone https://github.com/yiyangbear/koku.git
cd koku
```

#### Run the Application

```bash
mvn clean javafx:run
```

#### Build the JAR

```bash
mvn clean package
```

---

### Packaging

Koku can be packaged as a native desktop application using `jpackage`.

#### macOS

```bash
jpackage \
  --type dmg \
  --name Koku \
  --app-version 1.0.0 \
  --input build/package-input \
  --main-jar koku-1.0-SNAPSHOT.jar \
  --main-class com.example.koku.app.KokuLauncher \
  --dest build/dist \
  --icon build/koku.icns \
  --mac-package-name Koku \
  --mac-package-identifier com.yiyangbear.koku
```

#### Windows

The Windows installer is built automatically through GitHub Actions.

You can also build it manually on Windows with:

```powershell
jpackage `
  --type exe `
  --name Koku `
  --app-version 1.0.0 `
  --input build/package-input `
  --main-jar koku-1.0-SNAPSHOT.jar `
  --main-class com.example.koku.app.KokuLauncher `
  --dest build/dist `
  --win-menu `
  --win-shortcut
```

---

### Project Structure

```text
src/main/java/com/example/koku
├── app        # Application launcher
├── config     # App settings, rule options, themes, language, and timer options
├── domain     # Core board models, players, moves, results, and game engines
├── game       # Game definitions, registry, and engine/view factories
├── service    # Game session, timer, settings, i18n, and theme services
└── ui         # JavaFX views, board rendering, top bar, status view, and settings panel

src/main/resources
├── fonts      # Font resources
├── i18n       # Language resource files
└── icons      # Application icons
```

---

### Architecture

Koku is organized around a simple separation of responsibilities:

| Layer | Responsibility |
| --- | --- |
| App | Application entry point and launch logic |
| Config | User settings, rule options, themes, and language options |
| Domain | Core game models and rule engines |
| Game | Game registration and game-specific factories |
| Service | Session management, timer, theme, language, and settings services |
| UI | JavaFX views and user interaction |

This structure makes it easier to add new games, adjust rules, and improve the interface without rewriting the entire application.

---

### Development Goals

Koku is built as a learning-oriented desktop project focused on:

- Java application design
- JavaFX UI development
- Game rule modeling
- Clean project structure
- Refactoring toward extensibility
- Cross-platform desktop packaging

---

### Future Improvements

Possible future improvements include:

- AI opponent
- Move replay
- Save and load game records
- More visual themes
- More rule presets
- Better release automation
- Code signing for macOS and Windows
- Online multiplayer support

---

### License

No license has been specified yet.

---

## 中文

### 项目简介

**Koku / 观子** 是一个使用 JavaFX 开发的桌面棋类游戏合集。

这个项目最初是一个五子棋应用，后来被重构成一个更容易扩展的棋类游戏平台。现在它通过统一的游戏会话、规则引擎、设置系统、计时器服务和 JavaFX 界面支持多种棋类游戏。

Koku 不只是一个可以玩的桌面应用，也是一个用于练习 Java、JavaFX、游戏规则设计和软件架构的学习项目。

---

### 支持的游戏

| 游戏 | 说明 |
| --- | --- |
| Tic-Tac-Toe / 井字棋 | 经典 3×3 连线游戏。 |
| Connect Four / 四子棋 | 带重力落子的四子连线游戏。 |
| Gomoku / 五子棋 | 经典五子连线策略游戏，支持部分规则配置。 |
| Six-in-a-Row / 六子棋 | 更大棋盘上的六子连线变体。 |

---

### 功能特性

- 一个桌面应用中支持多种棋类游戏
- 本地双人对战
- 新游戏 / 重置游戏
- 悔棋功能
- 胜负、平局、超时检测
- 游戏计时器
- 最近一步标记
- 坐标显示开关
- 主题切换
- 语言切换
- 游戏规则配置
- JavaFX 桌面界面
- 支持 macOS 和 Windows 桌面打包

---

### 五子棋功能

Koku 对五子棋提供了额外的规则支持：

- 可配置棋盘大小
- 五子连线胜负判断
- 可选禁手规则
- 支持长连、双四、双三等规则检测

---

### 下载

你可以在 [Releases](https://github.com/yiyangbear/koku/releases) 页面下载最新版本。

目前提供：

- macOS `.dmg`
- Windows `.exe`

> 注意：目前应用还没有进行代码签名。  
> 第一次打开时，macOS 或 Windows 可能会显示安全提醒。

#### macOS

如果 macOS 阻止打开应用，请右键点击 `Koku.app`，然后选择 **打开 / Open**。

#### Windows

如果 Windows 出现 SmartScreen 提示，请点击 **More info / 更多信息**，然后选择 **Run anyway / 仍要运行**。

---

### 从源码运行

#### 环境要求

- JDK 25
- Maven
- Git

#### 克隆项目

```bash
git clone https://github.com/yiyangbear/koku.git
cd koku
```

#### 运行项目

```bash
mvn clean javafx:run
```

#### 打包 JAR

```bash
mvn clean package
```

---

### 应用打包

Koku 可以通过 `jpackage` 打包成原生桌面应用。

#### macOS

```bash
jpackage \
  --type dmg \
  --name Koku \
  --app-version 1.0.0 \
  --input build/package-input \
  --main-jar koku-1.0-SNAPSHOT.jar \
  --main-class com.example.koku.app.KokuLauncher \
  --dest build/dist \
  --icon build/koku.icns \
  --mac-package-name Koku \
  --mac-package-identifier com.yiyangbear.koku
```

#### Windows

Windows 安装包目前通过 GitHub Actions 自动构建。

如果需要在 Windows 上手动构建，也可以使用：

```powershell
jpackage `
  --type exe `
  --name Koku `
  --app-version 1.0.0 `
  --input build/package-input `
  --main-jar koku-1.0-SNAPSHOT.jar `
  --main-class com.example.koku.app.KokuLauncher `
  --dest build/dist `
  --win-menu `
  --win-shortcut
```

---

### 项目结构

```text
src/main/java/com/example/koku
├── app        # 应用启动入口
├── config     # 应用设置、规则选项、主题、语言和计时器配置
├── domain     # 核心棋盘模型、玩家、落子、结果和游戏引擎
├── game       # 游戏定义、游戏注册表和引擎 / 视图工厂
├── service    # 游戏会话、计时器、设置、国际化和主题服务
└── ui         # JavaFX 视图、棋盘渲染、顶部栏、状态栏和设置面板

src/main/resources
├── fonts      # 字体资源
├── i18n       # 多语言资源文件
└── icons      # 应用图标
```

---

### 架构设计

Koku 采用简单清晰的分层设计：

| 层级 | 职责 |
| --- | --- |
| App | 应用入口和启动逻辑 |
| Config | 用户设置、规则选项、主题和语言配置 |
| Domain | 核心游戏模型和规则引擎 |
| Game | 游戏注册和游戏相关工厂 |
| Service | 游戏会话、计时器、主题、语言和设置服务 |
| UI | JavaFX 界面和用户交互 |

这种结构使项目更容易添加新游戏、调整规则和改进界面，而不需要重写整个应用。

---

### 开发目标

Koku 是一个学习导向的桌面项目，主要用于练习：

- Java 应用设计
- JavaFX 界面开发
- 棋类规则建模
- 清晰的项目结构
- 面向扩展的重构
- 跨平台桌面应用打包

---

### 未来改进方向

未来可以继续加入：

- AI 对手
- 棋局回放
- 保存和加载棋局
- 更多视觉主题
- 更多规则预设
- 更完善的自动发布流程
- macOS 和 Windows 代码签名
- 在线多人对战

---

### License

目前尚未指定开源许可证。