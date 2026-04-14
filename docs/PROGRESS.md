# AutoPilot 开发进度与上下文

> 最近更新：2026-04-14（MVP 收尾批次 A 完成）

---

## 构建环境

- **JDK**: OpenJDK 17（macOS Homebrew: `/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home`）
- **Gradle**: 8.0 Wrapper, AGP 8.1.2
- **Android SDK**: platforms 34-36, build-tools 35.x

### 构建命令速查

```bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home

# 打 debug APK
./gradlew :app:assembleDebug
# APK 输出: app/build/outputs/apk/debug/app-debug.apk

# 仅编译检查
./gradlew :app:compileDebugKotlin

# 跑单测
./gradlew :app:testDebugUnitTest --tests "*.SomeTest"
```

### 已知踩坑

- **高德 SDK 依赖冲突**：`3dmap` + `location` + `search` 分开引入会因 `AMapUtilCoreApi`/`NetProxy` 重复类导致构建失败。必须使用官方合并包 `com.amap.api:3dmap-location-search:latest.integration`
- **signingConfig**：`app/build.gradle` 中 signingConfig 增加了空值保护，debug 构建使用默认签名
- **Java 版本**：从 18 降至 17 才能兼容当前 AGP

---

## 已完成功能模块

### 1. 变量/模式系统 ✅（全部完成）

- **计划文件**: `docs/plan/variable-mode-system/plan.yaml`
- **核心组件**:
  - `VariableRepository` — 变量仓库（全局/局部/持久化）
  - `ModeRepository` — 模式仓库（激活/停用/监听）
  - `ExpressionEvaluator` — AST 表达式求值器
- **动作**: `SetVariableAction`, `SetModeAction`
- **约束**: `ModeConstraint`, `VariableConstraint`
- **触发器**: `ModeChangeTrigger` + `ModeChangeEventDispatcher`
- **注册**: `ModeActionRegistry`, `ModeCriterionRegistry`, `BootstrapOptionRegistry`
- **适配器**: `VariableReferent` — 桥接变量系统到 Referent/Reference 系统
- **测试**: 端到端集成测试 9 场景全部通过

### 2. 地理围栏系统 ✅（全部完成）

- **计划文件**: `docs/plan/geofence-system/plan.yaml`
- **核心组件**:
  - `GeofenceConfig` / `GeofenceTransition` — 围栏数据模型 + 枚举
  - `GeofenceConfigRepository` — DataStore + JSON 持久化
  - `GeofenceConstraint` — 围栏约束 Applet（支持取反，依赖注入可测）
  - `GeofenceReferent` — 围栏引用（name/lat/lng/transition 5 索引）
- **事件分发**: `LocationEventDispatcher` → `EventFilter` 地理围栏分支
- **UI**:
  - `GeofencePickerDialog` — 地图选点 + POI 搜索 + 围栏可视化（圆形覆盖物/半径滑块）
  - `AmapApiKeyDialog` — 高德 API Key 设置对话框
  - 围栏列表管理对话框
- **注册**: `LocationCriterionRegistry`, `EventCriterionRegistry` 接入
- **依赖**: `com.amap.api:3dmap-location-search` 合并包
- **测试**: 端到端集成测试通过

### 3. OCR 屏幕识别 ✅（全部完成）

- **计划文件**: `docs/plan/ocr-screen-recognition/plan.yaml`
- **基础设施 (T01-T07)**:
  - T01: ML Kit `text-recognition-chinese` 依赖 + ProGuard 规则
  - T02: `OcrResult` / `TextBlock` / `OcrTextMatchMode` 数据模型
  - T03: `OcrProvider` 统一接口 + 单测
  - T04: `ScreenshotProvider` 截图接口 + `DefaultScreenshotProvider`
  - T05: `MlKitOcrProvider` ML Kit 离线 OCR 引擎（7 个单测通过）
  - T06: `OcrManager` 管线编排器
  - T07: `CloudOcrProvider` 云端 OCR 抽象基类
- **Applet 实现 (T08-T09)**:
  - T08: `OcrTextConstraint` — 三种匹配模式（含/精确/正则），ReDoS 防护（嵌套量词预检测+线程超时）
  - T09: `OcrRecognizeAction` — OCR 识别动作，输出文本到变量引用
- **注册与 UI (T10-T16)**:
  - T10: `ScreenOcrCriterionRegistry` — 三种匹配模式注册
  - T11: `ScreenOcrActionRegistry` — OCR 识别动作注册
  - T12: `BootstrapOptionRegistry` + `AppletOptionFactory` 全局注册（ID=0x1B/0x5B）
  - T13: 字符串资源（含 not_ 反转字符串）
  - T14: `OcrSettingsDialog` — 引擎选择 + 云端配置 + 测试识别 + 结果预览
  - T15: `RegionSelectorView` + `OcrRegionSelectorDialog` — 区域选择器 UI（prototype）
  - T16: 设置页 OCR 入口
- **安全审查 (T17)**:
  - ReDoS 防护：嵌套量词预检测 + ExecutorService 线程超时
  - API Key：inputType=textPassword（正式版需改用 EncryptedSharedPreferences）
  - OcrManager：@Volatile 并发安全
- **测试**: 305 个单测全部通过，0 回归

### 4. 触发器批量实现 ✅（全部完成）

- **计划文件**: `docs/plan/trigger-batch-implementation/plan.yaml`
- **基础设施 (T01-T03)**:
  - T01: 16 个事件常量 `Event.EVENT_ON_ALARM_FIRED`(20) ~ `EVENT_ON_MANUAL_TRIGGER`(35)
  - T02: 26 个触发器字符串资源（事件名 + Referent 标签）
  - T03: 7 个权限声明（SCHEDULE_EXACT_ALARM / BLUETOOTH_CONNECT / READ_PHONE_STATE / READ_CALL_LOG / RECEIVE_SMS / READ_SMS / USE_EXACT_ALARM）
- **已有分发器启用 (T04)**:
  - 启用 ClipboardEventDispatcher / VariableChangeEventDispatcher / ModeChangeEventDispatcher
  - EventCriterionRegistry 新增 3 条目（primaryClipChanged / manualTrigger / deviceBooted）
  - EventFilter 新增 DEVICE_BOOTED / MANUAL_TRIGGER 分支
- **简单广播分发器 (T05-T07)**:
  - T05: `ScreenEventDispatcher` — SCREEN_ON / SCREEN_OFF / USER_PRESENT
  - T06: `PowerEventDispatcher` — POWER_CONNECTED / POWER_DISCONNECTED
  - T07: `HeadsetEventDispatcher` — HEADSET_PLUG (state=0/1)
- **复杂分发器 + Referent (T08-T12)**:
  - T08: `BluetoothEventDispatcher` + `BluetoothReferent` — BT 状态/连接/断开，权限检查
  - T09: `AlarmEventDispatcher` + `AlarmReferent` — 闹钟调度 + RepeatRule (Daily/Weekly/Monthly) + computeNextTriggerTime
  - T10: `PhoneCallEventDispatcher` + `PhoneCallReferent` — TelephonyCallback API 31+，来电状态+号码
  - T11: `SmsEventDispatcher` + `SmsReferent` — SMS_RECEIVED 广播，PDU 解析多段合并
  - T12: `IntentEventDispatcher` + `IntentReferent` — 动态 IntentFilter，用户可配置 action
- **集成注册 (T13)**:
  - AutomatorService 注册 8 个新分发器
  - EventFilter 新增 12 个 when 分支（简单→EMPTY_SUCCESS，复杂→Referent）
  - EventCriterionRegistry 新增 14 个 @AppletOrdinal 条目 (0x001B ~ 0x0028)
- **验证 (T14-T15)**:
  - T14: 轮询触发器验证 — PollEventDispatcher + TimeCriterionRegistry + GlobalCriterionRegistry MVP 可行
  - T15: 全量编译+测试 — 364 tests, 0 failures
- **安全审查 (T16)**:
  - AlarmEventDispatcher: require 非空集 + 循环迭代上限（防 ANR）
  - 3 个简单 Dispatcher + PhoneCallEventDispatcher: destroy() 异常保护
  - IntentEventDispatcher: ConcurrentHashMap 线程安全
  - PhoneCallReferent / SmsReferent / BluetoothReferent: toString() PII 脱敏
- **测试**: 364 个单测全部通过（基线 305 → +59 新测试），0 回归

### 5. 动作+约束批量实现 ✅（全部完成）

- **计划文件**: `docs/plan/action-constraint-batch/plan.yaml`
- **3 个新 Registry**:
  - `SoundActionRegistry` (0x5C) — 5 个动作: volumeChange, silentVibrateMode, doNotDisturb, speakText, playSound
  - `UiActionRegistry` (0x5D) — 2 个动作: displayNotification, displayDialog
  - `NetworkActionRegistry` (0x5E) — 1 个动作: httpRequest
- **4 个新 Bridge**:
  - `AudioManagerBridge` — 音频管理 + 媒体控制
  - `TtsBridge` — TTS 语音合成
  - `NotificationManagerBridge` — 系统通知
  - `OverlayDialogBridge` — Overlay 对话框
- **18 个新动作**:
  - 设备设置 (6): setAutoRotate, toggleBluetooth, setBrightness, toggleDarkTheme, toggleWifi, toggleTorch
  - 声音 (5): volumeChange, silentVibrateMode, doNotDisturb, speakText, playSound
  - UI (3): displayNotification, displayDialog, (Toast 验证已有)
  - 网络 (1): httpRequest
  - 内部控制 (3): controlMedia, toggleMacro, exportMacros
- **6 个新约束**:
  - 全局 (5): isDeviceLocked, isHeadphoneConnected, isDarkMode, callState, isMacroRunning
  - 网络 (1): isAirplaneMode
- **修复**:
  - ControlActionRegistry ordinal 冲突 (pauseFor 0x0020→0x0021)
  - toggleTorch 与 waitForIdle 的 appletId hash 碰撞
  - 4 个安全修复: SSRF 私有地址过滤, 路径遍历防护增强, 音量范围校验
- **测试**: 379 个单测全部通过（基线 364 → +15 新测试），0 回归

### 6. MVP 收尾批次 A ✅（全部完成）

- **计划文件**: `docs/plan/mvp-infra-completion/plan.yaml`
- **引擎扩展 (T01-T02)**:
  - T01: `BatteryEventDispatcher` + `BatteryReferent` — 电量变化触发器，debounce 机制（≥1% 变化才分发），EventCriterionRegistry 新增 @AppletOrdinal(0x0029) batteryLevelChanged
  - T02: FileAction `ACTION_WRITE` 实现 — 覆盖/追加模式，canonicalPath + 白名单路径安全校验
- **Android 基础设施 (T03-T05)**:
  - T03: `ForegroundNotificationManager` — 前台服务保活通知（暂停/恢复 action button），A11yAutomatorService.onServiceConnected() 集成 startForeground，AndroidManifest 新增 FOREGROUND_SERVICE + FOREGROUND_SERVICE_SPECIAL_USE 权限
  - T04: `BatteryOptimizationGuideDialog` — 电池优化白名单引导（PowerManager 检测 + 系统设置跳转）
  - T05: `PermissionGuideDialog` — 5 种特殊权限逐步引导（A11y / 电池优化 / DND / Overlay / Settings.Write），onResume 自动刷新状态
- **UI 功能 (T06-T08)**:
  - T06: `OnboardingFragment` — 首次启动引导（ViewPager2 4 页：欢迎→功能→权限→完成），Preferences.isOnboardingCompleted 控制
  - T07: `VariableManagerDialog` — 变量管理 UI（搜索/类型过滤/编辑/删除/添加），接入 VariableRepository
  - T08: SnapshotLogDialog 扩展 — 文本搜索 + 时间排序（最新在前）+ 匹配计数
- **集成 (T09)**: BatteryEventDispatcher 注册到 AutomatorService.initEventDispatcher()
- **验证**: 397 个单测全部通过（基线 379 → +18 新测试），0 回归
- **新增文件**: 7 个 Kotlin 文件 + 7 个布局文件 + 2 个测试文件（共 2829 行新增）

---

## MVP 完成度评估

| 分类 | MVP 目标 | 已实现 | 完成度 | 说明 |
|---|---|---|---|---|
| 引擎核心 | 31 | ~31 | ~100% | 基线 + 变量/模式系统覆盖 |
| 触发器 | 19 | ~18 | ~95% | Battery Level 已实现，WiFiState 待接入 |
| 动作 | 28 | ~25 | ~89% | +Write to File，剩余 Screen On/Off 等 |
| 约束条件 | 17 | ~13 | ~75% | BatteryLevel/PowerConnected/TimeOfDay/DayOfWeek |
| Android 基础设施 | 7 | ~5 | ~71% | +前台通知/电池优化/权限引导，剩余通知频道管理等 |
| 设计系统与 UI | 16 | ~8 | ~50% | +引导流程/变量管理/日志扩展，设计系统待建设 |
| **MVP 合计** | **118** | **~100** | **~85%** | 批次 A +8 功能 |

---

## 关键架构模式

### Applet 注册链路

```
*Registry (定义 Applet 选项)
    ↓
BootstrapOptionRegistry (汇总注册)
    ↓
AppletOptionFactory (运行时解析)
```

### 事件分发链路

```
EventDispatcher (监听系统事件，调度 Event)
    ↓
EventFilter (按 event.type 路由)
    ↓
*Referent (提取事件参数，暴露给变量引用)
```

### 数据持久化

- `DataStore Preferences` + JSON 序列化
- 配置类通常有一个 `*Repository` 封装 DataStore 读写

### 包结构

- 主包名: `top.xjunz.tasker`
- 源码: `app/src/main/java/top/xjunz/tasker/`
- 单测: `app/src/test/kotlin/top/xjunz/tasker/`

---

## 执行计划清单

| 计划 | 路径 | 状态 |
|---|---|---|
| 变量/模式系统 | `docs/plan/variable-mode-system/plan.yaml` | ✅ 全部完成 |
| 地理围栏系统 | `docs/plan/geofence-system/plan.yaml` | ✅ 全部完成 |
| OCR 屏幕识别 | `docs/plan/ocr-screen-recognition/plan.yaml` | ✅ 全部完成 |
| 触发器批量实现 | `docs/plan/trigger-batch-implementation/plan.yaml` | ✅ 全部完成 |
| 动作+约束批量实现 | `docs/plan/action-constraint-batch/plan.yaml` | ✅ 全部完成 |
| AutoTask 逆向研究 | `docs/plan/autotask-reverse-research/` | 参考资料 |

---

## 下一步

1. 剩余触发器接入: BatteryLevel / WiFi State Change 事件分发器
2. 剩余约束补齐: BatteryLevel / PowerConnected / TimeOfDay / DayOfWeek
3. 基线动作验证: Screen On/Off 等 AutoTask 已有动作的兼容性确认
4. Android 基础设施: 前台服务常驻通知 / Notification Listener / 权限引导
5. 设计系统与 UI: iOS 风格视觉体系 / 宏编辑器 / 设置界面
6. 手机端集成测试与 bug 修复
