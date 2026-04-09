# AutoTask → AutoPilot 差距分析

> 基于对 [AutoTask](https://github.com/biground/AutoTask) (原作者 xjunz) 源码的深度分析，
> 对比 AutoPilot 378 项原子功能分类体系，识别可复用资产与需新增/改造的功能。

---

## 一、AutoTask 项目概况

| 维度 | 详情 |
|---|---|
| 语言 | Kotlin + Java (JNI/C++) |
| UI 框架 | Android XML Views (Material 3) — **非 Compose** |
| 引擎核心 | `tasker-engine` 模块（纯引擎，无 Android 依赖） |
| 自动化模式 | ① Shizuku (UiAutomation) ② Accessibility Service |
| 数据格式 | kotlinx.serialization JSON |
| 目标 SDK | 24-34（主要适配 Android 14） |
| 许可证 | Apache-2.0 |

### 模块架构

```
autotask/
├── app/                     # 主应用（UI + 服务 + Applet 注册）
├── tasker-engine/           # 纯引擎核心（Flow/Applet/Runtime/DTO）
├── ui-automator/            # UiAutomation 封装
├── coroutine-ui-automator/  # 协程版 UI 自动化桥接
├── hidden-apis/             # Android 隐藏 API 反射桥
├── shared-library/          # 共享工具库
└── ssl/                     # APK 签名保护（JNI）
```

---

## 二、AutoTask 已有能力盘点

### 2.1 引擎核心 ✅ 非常成熟

| 能力 | AutoTask 实现 | 对应 AutoPilot ID |
|---|---|---|
| 任务定义 (Macro) | `XTask` (常驻/一次性两种类型) | E-001 |
| 任务启用/禁用 | ✅ | E-002 |
| 任务导入/导出 | `AppletDTO` + JSON 序列化 | E-004 |
| 任务日志 | Snapshot 系统 (保留最近 10 个) | E-006 |
| If / Else 分支 | `If`, `Do`, `Else`, `ElseIf`, `Then` | CF-001 |
| 循环 | `Loop`, `Break`, `Continue` | CF-003, CF-005, CF-006 |
| 等待 | `WaitFor`, `WaitUntil` | CF-007, CF-008 |
| 延迟 | `PauseFor`, `PauseUntilTomorrow` | CF-007 |
| 同步/异步 | Applet 的 `relation` (AND/OR/ANYWAY) | CF-014 |
| 变量引用 | `Referent` 系统 (输出→输入引用链) | V-007, V-008 |
| 表达式 | 文本提取 (正则)、文本操作 | EX-002 (部分) |

### 2.2 事件/触发器系统

AutoTask 使用事件驱动模型，支持 **14 种事件类型**：

| AutoTask 事件 | 对应 AutoPilot ID | 状态 |
|---|---|---|
| 应用进入 (EVENT_ON_PACKAGE_ENTERED) | T-APP-001 | ✅ |
| 应用退出 (EVENT_ON_PACKAGE_EXITED) | T-APP-001 | ✅ |
| 内容变化 (EVENT_ON_CONTENT_CHANGED) | T-APP-003 | ✅ |
| 通知接收 (EVENT_ON_NOTIFICATION_RECEIVED) | T-NOTIF-001 | ✅ |
| 新窗口弹出 (EVENT_ON_NEW_WINDOW) | — (无直接对应) | ✅ |
| 剪贴板变化 (EVENT_ON_PRIMARY_CLIP_CHANGED) | T-APP-006 | ✅ |
| 定时轮询 (EVENT_ON_TICK) | T-TIME-003 | ✅ |
| Toast 接收 (EVENT_ON_TOAST_RECEIVED) | T-INT-016 | ✅ |
| 文件创建 (EVENT_ON_FILE_CREATED) | — (无直接对应) | ✅ |
| 文件删除 (EVENT_ON_FILE_DELETED) | — (无直接对应) | ✅ |
| WiFi 连接 (EVENT_ON_WIFI_CONNECTED) | T-NET-001 | ✅ |
| WiFi 断开 (EVENT_ON_WIFI_DISCONNECTED) | T-NET-001 | ✅ |
| 网络可用 (EVENT_ON_NETWORK_AVAILABLE) | T-NET-004 | ✅ |
| 网络不可用 (EVENT_ON_NETWORK_UNAVAILABLE) | T-NET-004 | ✅ |

### 2.3 动作/操作系统

| AutoTask 动作 | 对应 AutoPilot ID | 状态 |
|---|---|---|
| 文件操作 (FileAction: 删除/复制/重命名/写入) | A-DATA-001, A-DATA-002 | ✅ |
| 手势 (GestureAction: 点击/长按/滑动/自定义) | A-SCR-001 | ✅ |
| Shell 命令 (ShellCmdAction) | A-SCRIPT-001 | ✅ |
| Shell 脚本文件 | A-SCRIPT-001 | ✅ |
| 振动 (Vibrate) | A-SND-010 | ✅ |
| 延迟 (PauseFor) | CF-007 | ✅ |
| 强制停止应用 (forceStopApp) | A-APP-003 | ✅ |
| 启动应用 (launchApp) | A-APP-001 | ✅ |
| 返回键 (pressBack) | A-SCR-003 | ✅ |
| 最近任务 (pressRecents) | — | ✅ |
| 日志输出 (logcatText) | A-DATA-013 | ✅ |
| 文本提取 (extractText) | EX-002 (部分) | ✅ |
| 复制文本 (copyText) | A-DATA-004 | ✅ |

### 2.4 条件/约束系统

| AutoTask 条件 | 对应 AutoPilot ID | 状态 |
|---|---|---|
| 是特定应用 (isCertainApp) | C-APP-001 | ✅ |
| 屏幕方向 (isScreenPortrait) | C-DEV-004 | ✅ |
| 充电状态 (isBatteryCharging) | C-PWR-004 | ✅ |
| 电量范围 (batteryCapacityRange) | C-PWR-001 | ✅ |
| 网络可用 (isNetworkActive) | C-NET-001 (部分) | ✅ |
| WiFi 网络 (isWifiNetwork) | C-NET-001 | ✅ |
| 蜂窝网络 (isCellularNetwork) | C-NET-004 | ✅ |
| WiFi SSID 匹配 (currentWifiSsidIs) | C-NET-001 | ✅ |
| 时间范围 (timeRange) | C-TIME-001 | ✅ |
| 月份 (month) | C-TIME-004 | ✅ |
| 星期 (dayOfWeek) | C-TIME-002 | ✅ |
| 文本匹配 (equals/startsWith/endsWith/contains) | C-SYS-006 (部分) | ✅ |
| UI 对象匹配 (uiObjectMatches) | — (UI 自动化特有) | ✅ |
| UI 对象包含 (containsUiObject) | — | ✅ |

### 2.5 UI 自动化能力 ✅ 非常强大

| 能力 | 实现方式 |
|---|---|
| 坐标点击/长按/滑动 | GestureAction + GestureGenerator |
| 自定义手势录制 | SerializableInputEvent 序列 |
| UI 元素查找 (文本/ID/属性) | UiObjectTarget + UiObjectCriterionRegistry |
| UI 元素点击 | ClickUiObjectWithText, ClickButtonWithText |
| 滚动查找 | ScrollIntoUiObject, ForEachUiScrollable |
| 文本输入 | InputTextToFirstTextField |
| 屏幕内容读取 | Accessibility 节点树遍历 |
| 布局审查 | FloatingInspector (覆盖层布局树) |

### 2.6 控制流 ✅ 完善

| 能力 | 实现 |
|---|---|
| If / Else / ElseIf | ControlFlow 体系 |
| Loop + Break + Continue | Loop + Break + Continue |
| WaitFor (等待事件+超时) | WaitFor extends If |
| WaitUntil (条件轮询) | WaitUntil extends If |
| 顺序/或/无条件执行 | REL_AND / REL_OR / REL_ANYWAY |
| ScopeFlow (作用域容器) | ScopeFlow<T> |
| 嵌套深度限制 | 最大 9 层嵌套 |

---

## 三、差距分析 — AutoTask 缺少的功能

### 3.1 触发器差距（缺少 ~69 项 / 83 项 = 83%）

AutoTask 仅有 14 种事件类型，主要集中在**应用/通知/网络/定时**四个领域。

**完全缺失的触发器类别：**

| 类别 | 缺少项 | 优先级 |
|---|---|---|
| ⏰ 时间触发器 | T-TIME-001 (精确时间), T-TIME-002 (周/月日), T-TIME-004~007 | 高 |
| 📱 设备状态 | T-DEV-001~013 (全部 13 项：开机/屏幕/翻转/摇晃/折叠/传感器等) | 高 |
| 📞 电话/短信 | T-PHONE-001~008 (全部 8 项) | 高 |
| 🔋 电量/电源 | T-PWR-001~004 (全部 4 项) | 高 |
| 🔌 更多网络事件 | T-NET-002~016 (蓝牙/NFC/飞行模式/VPN/热点/漫游/基站等 14 项) | 中 |
| 🎮 硬件按键 | T-HW-001~009 (全部 9 项：耳机/电源键/音量键等) | 中 |
| 🎵 媒体 | T-MEDIA-001~004 (全部 4 项) | 低 |
| 📍 位置 | T-LOC-001~002 (地理围栏，需国内适配) | 低 |
| 🌡 传感器 | T-SENSOR-001~002 (活动识别/天气) | 低 |
| ⚙️ 系统设置 | T-SYS-001~010 (大部分缺失) | 中 |
| 🔧 内部触发器 | T-INT-001~018 (大部分缺失：变量变化/Webhook/Widget 等) | 中 |

### 3.2 动作差距（缺少 ~115 项 / 128 项 = 90%）

AutoTask 的动作系统高度专注于 **UI 自动化和文件操作**，缺少大量设备控制和系统设置能力。

**完全缺失的动作类别：**

| 类别 | 缺少项 | 优先级 |
|---|---|---|
| ⚙️ 设备设置 | A-SET-001~033 (全部 33 项：WiFi/蓝牙/亮度/GPS/热点等) | 高 |
| 🔊 音量与声音 | A-SND-001~014 (全部 14 项) | 高 |
| 📞 通讯 | A-COMM-001~009 (全部 9 项) | 中 |
| 🔔 通知与 UI | A-UI-001~021 (全部 21 项) | 高 |
| 🌐 网络 | A-NET-001~010 (大部分缺失：HTTP 请求/Server/UDP 等) | 高 |
| 📊 数据操作 | A-DATA-006~017 (变量设置/JSON 解析/文本处理) | 高 |
| 📱 应用控制 | A-APP-002/007~010 (部分缺失) | 中 |
| 🔧 系统/电源 | A-SYS-001~008 (全部 8 项) | 中 |
| 🎵 媒体控制 | A-MEDIA-001~002 | 低 |
| 🏠 内部动作 | A-INT-001~016 (大部分缺失) | 中 |

### 3.3 约束差距（缺少 ~45 项 / 59 项 = 76%）

| 类别 | 已有 | 缺失 |
|---|---|---|
| 设备状态 | 屏幕方向 | 屏幕亮灭/锁定/朝向/亮度/手电筒等 |
| 网络 | WiFi/蜂窝/SSID | 蓝牙/NFC/飞行/VPN/IP/漫游等 |
| 电量 | 充电/电量范围 | 温度/省电模式 |
| 时间 | 时间范围/月份/星期 | 日期/开机时长/日出日落 |
| 位置 | — | 地理围栏/基站 (全缺) |
| 应用 | 特定应用 | 音乐/耳机/日历 |
| 电话通知 | — | 通话状态/铃响/音量/免打扰 |
| 系统 | — | 自动旋转/同步/GPS/深色/剪贴板等 |
| 内部 | — | 变量/宏状态/模式/逻辑组合等 |

### 3.4 变量系统差距

| 能力 | AutoTask | AutoPilot 目标 | 差距 |
|---|---|---|---|
| 变量类型 | 引用系统 (Referent) | 6 种明确类型 (Bool/Int/Decimal/String/Dict/Array) | ⚠️ 需要类型化重构 |
| 全局变量 | 任务级/事件级 scope | 跨宏全局 + 宏内局部 | ⚠️ 需要全局 scope |
| 变量持久化 | — | 重启后保留 | ❌ 缺失 |
| 魔术变量 | — | 系统内置只读变量 | ❌ 缺失 |
| 变量表达式 | 文本正则 | 完整表达式引擎 | ❌ 基本缺失 |
| Dictionary/Array | — | 完整集合操作 | ❌ 缺失 |

### 3.5 完全缺失的系统

| 系统 | 说明 |
|---|---|
| 🌐 PC/Web 端编辑器 | AutoTask 无任何 Web 端 |
| ☁️ 云同步 | 无账号系统、无同步能力 |
| 🎨 自定义 UI 场景 | 无 Custom Scene 系统 |
| 📁 宏分类 (Category) | 无分类管理 |
| 📋 宏模板 | 无模板库 |
| 🧩 Action Block | 无可复用动作块 |
| 📡 Webhook/HTTP Server | 无内置 HTTP 服务 |
| 🔽 桌面小部件 | 无 Widget 支持 |
| ⚡ Quick Settings Tile | 无快速设置磁贴 |
| 🫧 悬浮按钮 | 有 FloatingInspector（审查用），但无触发用悬浮按钮 |

---

## 四、可复用资产评估

### 4.1 ⭐ 高价值可复用（直接使用）

| 资产 | 描述 | 价值 |
|---|---|---|
| **tasker-engine 模块** | 纯引擎核心：Flow/Applet/Runtime/DTO/Scheduler | ⭐⭐⭐⭐⭐ |
| **控制流体系** | If/Else/ElseIf/Loop/Break/Continue/WaitFor/WaitUntil | ⭐⭐⭐⭐⭐ |
| **Applet 插件架构** | AppletOption + AppletOptionRegistry 注册表模式 | ⭐⭐⭐⭐⭐ |
| **序列化/DTO** | AppletDTO + XTaskDTO + JSON 序列化 | ⭐⭐⭐⭐ |
| **双模式服务** | Shizuku + A11y 两种运行模式 | ⭐⭐⭐⭐ |
| **UI 自动化能力** | 手势/UI 元素操作/布局审查 | ⭐⭐⭐⭐ |
| **协程执行模型** | 事件驱动 + 协程调度，省电高效 | ⭐⭐⭐⭐ |
| **隐藏 API 桥接** | hidden-apis 模块 | ⭐⭐⭐ |

### 4.2 ⚠️ 需改造（可复用但需重构）

| 资产 | 改造原因 |
|---|---|
| **变量系统** | 从 Referent 模式 → 类型化全局变量系统 |
| **事件系统** | 从 14 种 → 83 种，需扩展 Event 类型和 Dispatcher |
| **UI 层** | 从 XML Views → Jetpack Compose + iOS 风格设计 |
| **数据持久化** | 从简单 JSON 文件 → Room DB + 云同步适配 |
| **宏模型** | 增加 Category、Template、版本号、同步元数据 |

### 4.3 ❌ 需全新开发

| 系统 | 说明 |
|---|---|
| PC/Web 端 | React + TypeScript 全新开发 |
| Firebase 后端 | Auth + Firestore + Cloud Functions |
| 云同步协议 | 增量同步 + 冲突解决 |
| 自定义 UI 场景引擎 | Compose 运行时 UI 渲染器 |
| 表达式引擎 | 完整的 AST 表达式解析/求值 |
| 大部分触发器 | ~69 个新触发器需实现 |
| 大部分动作 | ~115 个新动作需实现 |
| 设计系统 | iOS 风格 Compose 组件库 |

---

## 五、改进策略建议

### 方案：Fork + 渐进式重构

1. **Fork AutoTask** 作为起点
2. **保留引擎核心** (`tasker-engine`)，这是最有价值的部分
3. **重构变量系统**：在现有 Referent 基础上增加类型化全局变量
4. **扩展事件系统**：新增 EventDispatcher 类别（Phone/Power/Hardware/Location 等）
5. **新增 Applet Registry**：按 AutoPilot 分类注册新的 Trigger/Action/Constraint
6. **UI 层迁移**：XML Views → Jetpack Compose（可渐进替换）
7. **新增 Web 端**：完全独立的 React 项目
8. **新增 Firebase 层**：Auth + Realtime Sync

### 核心优势

- ✅ **跳过 3-6 个月的引擎开发**：FlowEngine/调度器/控制流/DTO 已经成熟
- ✅ **UI 自动化免费获得**：Shizuku + A11y 双模式已全面实现
- ✅ **插件架构可直接扩展**：AppletOptionRegistry 注册新功能非常自然
- ✅ **Apache-2.0 许可证**：允许商业化和修改
