# AutoPilot 开发进度与上下文

> 最近更新：2026-04-10

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

### 3. OCR 屏幕识别 🔄（Wave 2 完成，Wave 3 待开始）

- **计划文件**: `docs/plan/ocr-screen-recognition/plan.yaml`
- **已完成 (T01-T07)**:
  - T01: ML Kit `text-recognition-chinese` 依赖 + ProGuard 规则
  - T02: `OcrResult` / `OcrTextBlock` 数据模型
  - T03: `OcrProvider` 统一接口 + 单测
  - T04: `ScreenshotProvider` 截图接口 + `DefaultScreenshotProvider`
  - T05: `MlKitOcrProvider` ML Kit 离线 OCR 引擎（7 个单测通过）
  - T06: `OcrManager` 管线编排器
  - T07: `CloudOcrProvider` 云端 OCR 抽象基类
- **待实施 (T08+)**:
  - T08-T09: 待查看 plan.yaml 确认具体内容
  - T10-T18: Applet 约束/动作、注册、UI 等

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
| OCR 屏幕识别 | `docs/plan/ocr-screen-recognition/plan.yaml` | 🔄 Wave 2 完成 |
| AutoTask 逆向研究 | `docs/plan/autotask-reverse-research/` | 参考资料 |

---

## 下一步

1. 继续 OCR 计划 Wave 3+ (查看 `docs/plan/ocr-screen-recognition/plan.yaml`)
2. MVP 路线图中待实施功能（参见 `docs/roadmap.md`）
3. 手机测试反馈后的 bug 修复
