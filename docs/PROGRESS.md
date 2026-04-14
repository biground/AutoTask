# AutoPilot 开发进度与上下文

> 最近更新：2026-04-14

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
| AutoTask 逆向研究 | `docs/plan/autotask-reverse-research/` | 参考资料 |

---

## 下一步

1. MVP 路线图中待实施功能（参见 `docs/roadmap.md`）
2. 动作 (Actions) 模块实现
3. 约束 (Constraints) 模块实现
4. Android 基础设施（前台服务、通知管理等）
5. 手机测试反馈后的 bug 修复
