# WanAndroid

Kotlin Multiplatform 玩 Android 客户端。Android 与桌面端共用 Compose Multiplatform / Material 3 界面；iOS 使用 SwiftUI，Web 使用 React 和原生 DOM。

## 模块边界

- `sharedLogic`：不依赖 Compose 的共享业务逻辑，包含 Ktor 请求、接口模型、首页/广场分页、项目分类与分页、HTML 文本清理、错误重试和并发取消。目标为 Android、JVM、iOS 和 Kotlin/JS。
- `shared`：Android 与桌面共用的 Compose UI、导航、主题偏好和文章 WebView，以及 Paging 3 `PagingSource → Pager → cachedIn`、ViewModel 与数据服务。
- `androidApp`：Android 应用入口。
- `iosApp`：SwiftUI 原生页面，通过 `SharedLogic.framework` 的状态订阅桥接业务逻辑，文章在 Safari 阅读器中打开。
- `webApp`：React + TypeScript + Vite，消费 Kotlin/JS 导出的 `WanAndroidWebClient`，不包含 Compose/Wasm。
- `desktopApp`：Nucleus Tao 基础窗口、应用生命周期与打包入口；macOS、Windows、Linux 均直接运行 `shared.App()` 的 Material 3 界面，不再包含三套平台样式适配器。

`WanAndroidClient` 为 SwiftUI/React 提供 `start`、`refresh`、`loadMore`、`selectProject`、`watch` 和 `close`，两端订阅同一状态的 JSON 快照。Android/桌面端由 Paging 3 管理加载状态、预取、缓存与重试。四端复用共享 HTTP 服务、模型、分页起点/末页规则和文本清理；界面层不重复实现接口。释放页面宿主时取消订阅并关闭 client。

## 页面

四端均提供首页（含置顶）、广场、项目分类列表和主题设置。列表支持加载状态、空态、刷新、分页、错误重试与文章跳转。共享层使用服务端的 `over/pageCount` 判断末页，避免广场过滤文章导致少于 page_size 时错误结束分页。项目切换取消旧请求，防止过期响应覆盖当前分类。

列表顶部不再显示重复的页面标题和刷新按钮。Android 使用 Material 3 `PullToRefreshBox` 下拉刷新；Android/桌面使用 Paging 3 `LazyPagingItems` 自动分页。桌面通过 Compose 右键菜单“刷新”或列表内 `⌘R` / `Ctrl+R` / `F5` 刷新，避免鼠标滚轮缺少触摸释放事件导致刷新箭头挂起。iOS 使用 SwiftUI `List.refreshable` 与列表页尾 `onAppear`，Web 使用浏览器原生滚动和 `IntersectionObserver` 触底加载。Web 下拉刷新由浏览器支持情况决定，支持时重新加载页面；桌面浏览器可使用系统刷新快捷键。没有自行模拟拖拽手势。

首次加载、空文章、空项目分类、请求失败与全部加载完成均有缺省状态。分页失败后停止自动请求，只重试失败页；下拉刷新和追加失败保留已显示内容。接口返回空页但仍有后续页时继续推进。

接口依据 [玩 Android 开放 API](https://www.wanandroid.com/blog/show/2)：

| 内容 | 接口 | 起始页 |
| --- | --- | --- |
| 首页 | `article/list/{page}/json`、`article/top/json` | 0 |
| 广场 | `user_article/list/{page}/json` | 0 |
| 项目分类 | `project/tree/json` | — |
| 项目列表 | `project/list/{page}/json?cid={id}` | 1 |

所有分页请求固定 `page_size=20`，重试失败页不会跳页。

## 运行

需要 JDK 21、Android SDK 37；iOS 需要 Xcode；Web 需要 Node.js 22.12+。

```sh
# Android
./gradlew :androidApp:assembleDebug

# Nucleus 桌面
./gradlew :desktopApp:run

# Web：首次生成共享 JS 包并安装依赖
npm run setup:web
npm start
# 生产构建（会重新生成共享业务包）
npm run build:web
npm run preview

# iOS framework（也可直接在 Xcode 内构建，由 Build Phase 自动执行）
./gradlew :sharedLogic:linkDebugFrameworkIosSimulatorArm64
open iosApp/iosApp.xcodeproj
```

Web 的开发/预览服务器代理 `/api` 到玩 Android。生产环境也必须配置同源代理，不能仅将静态目录部署后期待接口跨域可用，参见 [Web 说明](webApp/README.md) 与 `webApp/deploy/nginx.conf`。

桌面三端共用一套 Compose UI。Nucleus 不支持跨操作系统打包，Windows / Linux 安装包需在对应系统生成；当前仅验收 macOS。桌面打包配置位于 `desktopApp/build.gradle.kts`，GraalVM 工具链仅在 Native Image 任务中自动准备；常规 JVM 构建使用 JDK 21。

## 验证

```sh
./gradlew :shared:jvmTest :sharedLogic:jvmTest :sharedLogic:jsNodeTest
./gradlew :sharedLogic:iosSimulatorArm64Test
./gradlew :androidApp:assembleDebug :desktopApp:compileKotlin
npm run build:web
```

测试通过可控的服务替身和 Ktor MockEngine 验证分页起点、固定参数、失败重试、分类切换竞态、HTML 清理及接口异常，不依赖线上数据。

## 最近验证（2026-09-28，macOS arm64）

- Paging 3 的 18 项分页/分类回归测试在 JVM 上通过；共享业务 18 项回归测试分别在 JVM、Node.js 上通过。
- Android Debug APK 构建、连接设备安装成功；设备处于锁屏状态，未完成真机页面交互验收。
- Xcode 模拟器目标构建成功，包括 `OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED=YES` 时自动调用 SharedLogic Gradle framework 任务；本机缺少有效模拟器运行时，`iosSimulatorArm64Test` 和模拟器界面运行尚未验证。
- Web TypeScript/生产构建通过；真实接口触底加载使首页从 21 篇增至 40 篇、广场从 15 篇增至 24 篇，项目分类切换及 390px 移动布局已检查，生产预览无控制台错误。手机浏览器原生下拉刷新尚未真机验收。生产 JS 主包约 502 KB gzip，仍有 Vite 大包提示。
- 桌面历史版本的 macOS `.app` 与 DMG 已验证；当前统一 Compose UI 的验证见下文。Windows/Linux 运行与安装包、GraalVM native image、签名和公证尚未在对应环境验证。

## 移动端界面优化（2026-09-30）

Android 与 iOS 使用绿色强调色、分级标题与宽松行距，分别保留 Material 3 与 SwiftUI 原生交互。首页增加广场和项目快捷入口，置顶文章使用强调卡片；广场突出作者与发布时间；项目页补充分区说明、真实分类数量、分类选中状态和项目简介。列表展示的数量为已载入条数，并非站点总量。iOS 项目保留远程封面及加载失败占位。

Android 保留原有悬浮 Tab 栏和选中动画，列表底部预留空间；移除未接入功能的搜索按钮，文章页顶部显示真实标题。设置页由占位内容补齐为可保存的系统／浅色／深色主题、阅读说明与社区入口。iOS 设置页同步调整层级，并支持长按文章调用系统分享菜单。

本轮 Android Debug APK、iOS 模拟器 Debug 构建与既有 18 项 Paging 回归测试通过。已在 Pixel 8 安装改版并检查真实首页、项目分类切换与设置页；iOS 模拟器已检查首页、深色与辅助功能大字体截图，完整页面交互仍需进一步验收。

## 桌面端统一 Compose UI（2026-09-30）

桌面端恢复使用共享 `App()`：三端共用 Material 3 页面、绿色主题、悬浮导航、文章卡片、项目分类和外观设置，宽屏阅读由 Material 3 Adaptive 处理。已删除 macOS UI、Fluent、Yaru 的代码、依赖、版本别名及系统选择测试；保留 Nucleus 基础窗口、WebView 和打包能力。

桌面编译、18 项共享 Paging 3 回归测试和 macOS arm64 DMG 构建通过。新 `.app` 已实测悬浮导航、项目分类、主题切换、设置返回与文章双栏阅读；包内不再包含三套 UI 库及旧适配器类。DMG 从 112.58 MiB 降至 105.85 MiB，减少约 6.74 MiB。Windows / Linux 尚未实机验收。
