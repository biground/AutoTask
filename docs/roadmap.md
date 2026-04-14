# AutoPilot — MVP → V1 → V2 实施路线图

> 基于 378 项原子功能分类体系，按「引擎核心 → 高频功能 → 完整覆盖」三阶段递进。  
> 每阶段保证交付物自身可用，不依赖后续阶段。

---

## 基线改进清单（AutoTask Fork）

### A. 可直接复用（保持主干不改）

- tasker-engine：Flow / Applet / Runtime / DTO 核心执行链
- 控制流：If / Else / Loop / Break / Continue / WaitUntil
- AppletOptionRegistry 注册机制与序列化能力
- 双模式自动化运行时：Shizuku + Accessibility 基础框架

### B. 需改造（在原有结构上扩展）

- 事件系统：从现有事件集合扩展到 83 类触发器
- 变量系统：从 Referent 引用模式扩展为强类型变量 + 持久化
- UI 层：在现有 Android 架构上持续引入 iOS 风格设计与一致交互
- 数据层：补齐宏分类、模板、版本字段，适配同步元数据

### C. 需新建（基线没有，需要独立实现）

- Web 端宏编辑器（React + TypeScript）
- Firebase 同步链路（Auth / Firestore / Conflict Resolver）
- 自定义场景运行时（Scene 渲染 + 事件绑定 + 数据绑定）
- 国内化模块（高德地理围栏、天气/OCR/翻译替代）

### 当前执行进度（2026-04-13）

| Wave | 状态 | 说明 |
|---|---|---|
| Wave 1 | ✅ 已完成 | T01+T02+T03+T04 基础设施 |
| Wave 2 | ✅ 已完成 | T05+T06+T07 核心引擎 |
| Wave 3 | ✅ 已完成 | T08+T09 Applet 实现 |
| Wave 4 | ✅ 已完成 | T10-T16 注册与 UI |
| Wave 5 | ✅ 已完成 | T17+T18 安全审查 + 集成测试（305 tests, 0 failures） |

已完成里程碑（已提交到 main）：
- T01：ML Kit 中文 OCR 依赖与 ProGuard 规则
- T05：MlKitOcrProvider 离线 OCR 引擎
- T06：OcrManager OCR 管线编排器
- T07：CloudOcrProvider 抽象层
- T08：OcrTextConstraint 三种匹配模式约束
- T09：OcrRecognizeAction 识别动作
- T10-T13：注册表系统 + 全局注册 + 字符串资源
- T14-T15：OCR 设置 UI + 区域选择器 UI
- T16：设置页入口
- T17：安全审查修复（ReDoS/API Key/线程安全）
- T18：全量编译 + 305 测试通过
- 地理围栏基础：GeofenceConstraint / GeofenceReferent / EventFilter 分支接入
- 配置入口：高德 API Key 设置对话框

---

## 阶段一：MVP（最小可行产品）

### 目标

交付一个功能完整的 Android 自动化引擎：宏的创建/编辑/执行/导入导出全链路可用，覆盖 15+ 高频触发器和 25+ 常用动作，满足日常自动化场景（定时任务、WiFi/蓝牙联动、来电/短信响应、通知联动、电量管理等）。

### 包含功能

#### 1. 引擎核心（31 项）

**宏模型（6 项）**
| ID | 功能 |
|---|---|
| E-001 | 宏定义 |
| E-002 | 宏启用/禁用 |
| E-003 | 宏分类 |
| E-004 | 宏导入/导出 |
| E-005 | 宏克隆 |
| E-006 | 宏日志 |

**变量系统（9 项）**
| ID | 功能 |
|---|---|
| V-001 | Boolean 变量 |
| V-002 | Integer 变量 |
| V-003 | Decimal 变量 |
| V-004 | String 变量 |
| V-007 | 全局变量 |
| V-008 | 局部变量 |
| V-009 | 变量持久化 |
| V-010 | 魔术变量 |
| V-011 | 变量表达式 |

**控制流（11 项）**
| ID | 功能 |
|---|---|
| CF-001 | If / Else 条件分支 |
| CF-002 | If Confirmed Then |
| CF-003 | Repeat 循环 |
| CF-005 | Break 跳出循环 |
| CF-006 | Continue 继续循环 |
| CF-007 | Wait Before Next |
| CF-008 | Wait Until Trigger |
| CF-011 | Macro Run |
| CF-012 | Cancel Macro Actions |
| CF-013 | Confirm Next |
| CF-014 | 同步/异步执行 |

**表达式引擎（5 项）**
| ID | 功能 |
|---|---|
| EX-001 | 数学运算 |
| EX-002 | 字符串操作 |
| EX-003 | 日期/时间函数 |
| EX-004 | 逻辑运算 |
| EX-005 | JSON 解析 |

#### 2. 触发器（19 项）

| ID | 功能 | 选取理由 |
|---|---|---|
| **时间（3）** | | |
| T-TIME-001 | Day/Time Trigger | 最基础的定时触发 |
| T-TIME-002 | Day of Week/Month | 周期性任务 |
| T-TIME-003 | Regular Interval | 固定间隔重复 |
| **设备状态（3）** | | |
| T-DEV-001 | Device Boot | 开机自启动宏 |
| T-DEV-002 | Screen On/Off | 亮灭屏联动 |
| T-DEV-003 | Screen Unlocked | 解锁联动 |
| **连接与网络（2）** | | |
| T-NET-001 | WiFi State Change | WiFi 自动化核心 |
| T-NET-003 | Bluetooth Event | 蓝牙设备联动 |
| **电话与短信（2）** | | |
| T-PHONE-001 | Call Incoming | 来电响应 |
| T-PHONE-006 | SMS Received | 短信响应 |
| **电量（2）** | | |
| T-PWR-001 | Power Connected/Disconnected | 充电状态联动 |
| T-PWR-002 | Battery Level | 低电量自动化 |
| **通知（1）** | | |
| T-NOTIF-001 | Notification | 通知联动核心 |
| **应用（2）** | | |
| T-APP-001 | Application Launched/Closed | 应用启动联动 |
| T-APP-006 | Clipboard Change | 剪贴板监听 |
| **硬件（1）** | | |
| T-HW-001 | Headphones Insert/Removed | 耳机插拔 |
| **内部触发器（3）** | | |
| T-INT-001 | Variable Change | 变量驱动的宏间联动 |
| T-INT-009 | Intent Received | 外部 Intent 触发 |
| T-INT-017 | Empty Trigger | 手动触发 |

#### 3. 动作（28 项）

| ID | 功能 | 选取理由 |
|---|---|---|
| **设备设置（7）** | | |
| A-SET-002 | Auto Rotate On/Off | 常用设置开关 |
| A-SET-003 | Bluetooth Configure | 蓝牙控制 |
| A-SET-004 | Brightness | 亮度控制 |
| A-SET-005 | Dark Theme | 深色模式切换 |
| A-SET-012 | Screen On/Off | 屏幕控制 |
| A-SET-016 | WiFi Configure | WiFi 控制 |
| A-SET-033 | Torch On/Off | 手电筒 |
| **音量与声音（6）** | | |
| A-SND-001 | Volume Change | 音量调节核心 |
| A-SND-005 | Play/Stop Sound | 播放音频 |
| A-SND-006 | Silent - Vibrate Off | 静音模式 |
| A-SND-007 | Speak Text | TTS 语音 |
| A-SND-010 | Vibrate | 振动反馈 |
| A-SND-014 | Do Not Disturb | 免打扰控制 |
| **通知与 UI（3）** | | |
| A-UI-001 | Display Notification | 自定义通知 |
| A-UI-002 | Display Dialog | 对话框提示 |
| A-UI-007 | Popup Message | Toast 消息 |
| **应用控制（2）** | | |
| A-APP-001 | Launch Application | 启动应用 |
| A-APP-002 | Launch Home Screen | 回到桌面 |
| **文件与数据（6）** | | |
| A-DATA-001 | File Operation | 文件操作 |
| A-DATA-002 | Write to File | 写入文件 |
| A-DATA-004 | Fill Clipboard | 设置剪贴板 |
| A-DATA-006 | Set Variable | 设置变量 |
| A-DATA-007 | Delete Variable | 删除变量 |
| A-DATA-013 | Log Event | 记录日志 |
| **网络（1）** | | |
| A-NET-001 | HTTP Request | HTTP 请求 |
| **媒体（1）** | | |
| A-MEDIA-001 | Control Media | 媒体播放控制 |
| **内部动作（2）** | | |
| A-INT-001 | Enable/Disable Macro | 宏开关控制 |
| A-INT-005 | Export Macros | 导出宏 |

#### 4. 约束条件（17 项）

| ID | 功能 | 选取理由 |
|---|---|---|
| **设备（2）** | | |
| C-DEV-001 | Screen On/Off | 屏幕状态 |
| C-DEV-002 | Device Locked/Unlocked | 锁定状态 |
| **网络（3）** | | |
| C-NET-001 | WiFi State | WiFi 连接状态 |
| C-NET-003 | Bluetooth State | 蓝牙连接状态 |
| C-NET-006 | Airplane Mode | 飞行模式状态 |
| **电量（2）** | | |
| C-PWR-001 | Battery Level | 电量范围 |
| C-PWR-004 | Power Connected | 充电状态 |
| **时间（2）** | | |
| C-TIME-001 | Time of Day | 时间段约束 |
| C-TIME-002 | Day of the Week | 星期约束 |
| **应用（2）** | | |
| C-APP-001 | Application Running | 应用前台状态 |
| C-APP-003 | Headphone Connection | 耳机连接状态 |
| **电话（1）** | | |
| C-PHONE-001 | Call State | 通话状态 |
| **系统（2）** | | |
| C-SYS-004 | Dark Mode | 深色模式状态 |
| C-SYS-006 | Compare Values | 变量/表达式比较 |
| **内部（3）** | | |
| C-INT-001 | Variable | 变量值条件 |
| C-INT-002 | Macro Running | 宏运行状态 |
| C-INT-013 | AND / OR / XOR / NOT | 逻辑组合 |

#### 5. Android 端基础设施（7 项）

| ID | 功能 |
|---|---|
| AND-001 | 常驻通知服务（前台保活） |
| AND-003 | Notification Listener |
| AND-005 | 事件广播接收 |
| PERM-001 | 运行时权限请求 |
| PERM-004 | 特殊权限引导 |
| PERM-005 | 电池优化白名单 |
| WIDGET-003 | App Shortcut |

#### 6. 设计系统与基础 UI（16 项）

**设计语言（6 项）**
| ID | 功能 |
|---|---|
| DES-001 | iOS 风格视觉体系 |
| DES-002 | 色彩系统 |
| DES-003 | 字体排版 |
| DES-004 | 图标系统 |
| DES-005 | 间距系统 |
| DES-006 | 动画系统 |

**应用原生 UI（10 项）**
| ID | 功能 |
|---|---|
| NUI-001 | 首页/宏列表 |
| NUI-002 | 宏编辑器 |
| NUI-003 | 触发器/动作选择器 |
| NUI-004 | 参数配置面板 |
| NUI-005 | 变量管理界面 |
| NUI-006 | 日志查看器 |
| NUI-007 | 设置界面 |
| NUI-008 | 引导流程（权限申请 + 功能介绍） |
| NUI-009 | 空状态设计 |
| NUI-010 | 手势交互 |

### MVP 功能统计

| 分类 | 数量 |
|---|---|
| 引擎核心 | 31 |
| 触发器 | 19 |
| 动作 | 28 |
| 约束条件 | 17 |
| Android 基础设施 | 7 |
| 设计系统与 UI | 16 |
| **MVP 合计** | **118** |

### MVP 架构工作量

1. **引擎内核**：需从零搭建宏调度器（事件总线 + 触发器监听 + 动作执行管线 + 约束评估器），这是整个项目的技术核心，工作量最大
2. **变量/表达式系统**：自定义表达式解析器（词法分析 + AST + 求值），支持数学/字符串/日期/逻辑运算
3. **前台服务 + 广播接收**：Android 后台保活方案，适配 Android 16 限制
4. **数据持久化层**：宏/变量/日志的本地存储（Room Database），JSON 序列化导入导出
5. **Jetpack Compose UI 架构**：Material 3 主题定制为 iOS 简约风格，导航架构

### MVP 关键技术风险

| 风险 | 影响 | 缓解措施 |
|---|---|---|
| Android 16 后台运行限制收紧 | 前台服务可能被系统杀死，触发器漏触发 | 使用 Foreground Service Type 声明 + 电池优化白名单引导 |
| 通知监听权限用户拒绝 | T-NOTIF-001 完全不可用 | 分步引导 + 功能降级提示 |
| 表达式引擎复杂度 | 解析器 bug 导致宏执行异常 | 充分的单元测试 + 沙箱执行 |
| Accessibility Service 审核（MVP 暂不需要，但 T-APP-001 可能依赖） | 可能需要用 UsageStatsManager 替代前台应用检测 | 提供双路径实现 |

---

## 阶段二：V1（功能完善版）

### 目标

补全所有中高频触发器/动作/约束，上线 Web 端宏编辑器与 Google 账号云同步，交付自定义 UI 场景系统（Custom Scene）和 Action Block 可复用动作块，使 AutoPilot 成为功能齐全的自动化平台。

### 新增功能

#### 1. 引擎核心补全（7 项）

| ID | 功能 | 说明 |
|---|---|---|
| E-007 | 宏模板 | 预置常用宏模板 |
| V-005 | Dictionary 变量 | 高级数据结构 |
| V-006 | Array 变量 | 高级数据结构 |
| CF-004 | Iterate Dictionary/Array | 遍历集合元素 |
| CF-009 | Action Block | 可复用动作块（函数） |
| CF-010 | Exit Action Block | 提前退出动作块 |
| EX-006 | 数组操作 | 排序/过滤/映射/查找 |

#### 2. 新增触发器（38 项）

**时间（2 项）**：T-TIME-004（Stopwatch）、T-TIME-005（Calendar Event）

**设备状态（4 项）**：T-DEV-004（Screen Orientation）、T-DEV-005（Flip Device）、T-DEV-006（Shake Device）、T-DEV-011（Dark Theme Change）

**连接与网络（5 项）**：T-NET-002（WiFi SSID Transition）、T-NET-004（Data Connectivity）、T-NET-008（Airplane Mode）、T-NET-009（Hotspot）、T-NET-011（VPN State）

**电话与短信（5 项）**：T-PHONE-002（Call Outgoing）、T-PHONE-003（Call Active）、T-PHONE-004（Call Ended）、T-PHONE-005（Call Missed）、T-PHONE-007（SMS Sent）

**电量（2 项）**：T-PWR-003（Battery Temperature）、T-PWR-004（Battery Saver State）

**通知（1 项）**：T-NOTIF-002（Notification Bar Button）

**应用（1 项）**：T-APP-002（App Install/Remove/Update）

**硬件（2 项）**：T-HW-008（USB Device）、T-HW-009（Media Button）

**媒体（2 项）**：T-MEDIA-001（Media Track Changed）、T-MEDIA-002（Music/Sound Playing）

**系统（6 项）**：T-SYS-001（System Setting Change）、T-SYS-004（GPS）、T-SYS-005（Silent Mode）、T-SYS-006（DND）、T-SYS-007（Torch）、T-SYS-008（Autosync）

**内部触发器（8 项）**：T-INT-002（Macro Enabled）、T-INT-003（Macro Finished）、T-INT-004（Mode Change）、T-INT-007（Webhook URL）、T-INT-010（Floating Button）、T-INT-011（Widget Button）、T-INT-012（Shortcut Launched）、T-INT-013（Quick Settings Tile）

#### 3. 新增动作（55 项）

**设备设置（10 项）**：A-SET-001（Airplane Mode）、A-SET-006（GPS）、A-SET-007（HotSpot）、A-SET-008（Immersive Mode）、A-SET-010（Mobile Data）、A-SET-013（Screen Timeout）、A-SET-017（Battery Saver）、A-SET-022（Autosync）、A-SET-025（Car Mode）、A-SET-030（Set Wallpaper）

**音量与声音（7 项）**：A-SND-002（Volume Up/Down）、A-SND-003（Ringtone Configure）、A-SND-004（Notification Sound）、A-SND-008（Say Current Time）、A-SND-009（Speakerphone）、A-SND-011（Vibrate Enable/Disable）、A-SND-012（Volume Popup）

**通讯（6 项）**：A-COMM-001（Make Call）、A-COMM-002（Answer Call）、A-COMM-003（Call Reject）、A-COMM-004（Send SMS）、A-COMM-005（Send Email）、A-COMM-009（Notification Reply）

**通知与 UI（10 项）**：A-UI-003（Bubble Notification）、A-UI-004（Custom Scene）、A-UI-005（Option Dialog）、A-UI-006（Selection Dialog）、A-UI-008（Floating Text）、A-UI-009（Floating Button Config）、A-UI-012（Expand/Collapse Status Bar）、A-UI-013（Clear Notifications）、A-UI-018（Keyboard Prompt）、A-UI-020（Dim Screen）

**应用控制（3 项）**：A-APP-003（Kill Application）、A-APP-007（Launch Shortcut）、A-APP-010（Alarm Clock）

**文件与数据（9 项）**：A-DATA-003（Open File）、A-DATA-008（Clear Variables）、A-DATA-009（Array Manipulation）、A-DATA-010（Clear Dictionary/Array Entry）、A-DATA-011（JSON Parse Action）、A-DATA-012（Text Manipulation）、A-DATA-014（Clear Log）、A-DATA-015（Export Log）、A-DATA-016（Calendar Add Event）

**网络（4 项）**：A-NET-002（HTTP Server Response）、A-NET-003（Open Website）、A-NET-005（Send Intent）、A-NET-007（Share Text）

**脚本（1 项）**：A-SCRIPT-002（JavaScript Code）

**系统（2 项）**：A-SYS-002（Keep Device Awake）、A-SYS-005（Authenticate User）

**媒体（1 项）**：A-MEDIA-002（Stopwatch）

**内部动作（2 项）**：A-INT-002（Enable/Disable Category）、A-INT-003（Enable/Disable Trigger）

#### 4. 新增约束条件（28 项）

**设备（6 项）**：C-DEV-003（Device Facing）、C-DEV-004（Orientation）、C-DEV-005（Proximity）、C-DEV-006（Light Sensor）、C-DEV-007（Brightness）、C-DEV-008（Torch）

**网络（5 项）**：C-NET-002（WiFi HotSpot）、C-NET-004（Mobile Data）、C-NET-005（NFC）、C-NET-007（VPN）、C-NET-008（IP Address）

**电量（2 项）**：C-PWR-002（Battery Temperature）、C-PWR-003（Battery Saver）

**时间（3 项）**：C-TIME-003（Day of Month）、C-TIME-004（Month of Year）、C-TIME-006（Time Since Boot）

**应用（2 项）**：C-APP-002（Music Active）、C-APP-004（Calendar Entry）

**电话与通知（5 项）**：C-PHONE-002（Phone Ringing）、C-PHONE-004（Notification Present）、C-PHONE-005（Notification Volume）、C-PHONE-006（Ringer Volume）、C-PHONE-008（DND）

**系统（5 项）**：C-SYS-001（Auto Rotate）、C-SYS-002（Auto Sync）、C-SYS-003（GPS）、C-SYS-005（System Settings）、C-SYS-007（Clipboard Content）

#### 5. 自定义 UI 场景系统（全部 15 项）

UI-001 ~ UI-015（Scene 画布、按钮、文本标签、图片、输入框、开关、滑块、列表/网格、下拉选择、进度条、布局容器、样式系统、事件绑定、数据绑定、场景导航）

#### 6. 用户自定义场景设计能力（6 项）

DES-SCENE-001 ~ DES-SCENE-006（预置主题、组件样式预设、动画预设、深色模式自适应、模糊背景效果、自适应布局）

#### 7. PC/Web 端（全部 21 项）

**宏编辑器（12 项）**：WEB-001 ~ WEB-012（可视化编辑器、触发器/动作/约束配置面板、控制流可视化、变量管理器、表达式编辑器、宏测试/模拟、宏列表管理、分类管理、自定义场景编辑器、宏模板浏览）

**账户与同步（9 项）**：SYNC-001 ~ SYNC-009（Google 登录、宏同步、变量同步、场景同步、设置同步、冲突解决、离线编辑、增量同步、同步状态指示）

#### 8. Android 端补充（7 项）

| ID | 功能 |
|---|---|
| AND-002 | Accessibility Service |
| AND-006 | 内置 HTTP Server |
| AND-008 | 传感器服务 |
| PERM-002 | Shizuku/ADB 权限 |
| WIDGET-001 | 桌面小部件（按钮） |
| WIDGET-002 | Quick Settings Tile |
| WIDGET-004 | 悬浮按钮 |

### V1 功能统计

| 分类 | 新增数量 |
|---|---|
| 引擎核心补全 | 7 |
| 触发器 | 38 |
| 动作 | 55 |
| 约束条件 | 28 |
| 自定义 UI 场景 | 15 |
| 场景设计能力 | 6 |
| PC/Web 端 | 21 |
| Android 端补充 | 7 |
| **V1 新增合计** | **177** |
| **累计（MVP + V1）** | **295** |

### V1 架构工作量

1. **Action Block 系统**：类似函数调用的可复用动作块，需要实现调用栈、参数传递、返回值
2. **自定义 UI 场景引擎**：基于 Compose 的运行时 UI 渲染器，解析场景定义 JSON → 动态渲染组件 → 事件/数据绑定
3. **React Web 端宏编辑器**：拖拽式可视化编辑器，需实现复杂的控制流可视化和参数配置 UI
4. **Firebase 云同步**：Auth + Firestore 数据模型设计、双向增量同步、冲突解决策略
5. **Accessibility Service**：无障碍服务框架，支持通知交互、UI 元素操作
6. **内置 HTTP Server**：轻量级 HTTP Server（Ktor/NanoHTTPD），Webhook 接收和响应

### V1 关键技术风险

| 风险 | 影响 | 缓解措施 |
|---|---|---|
| Firestore 双向同步冲突 | 多端同时编辑导致数据丢失 | 乐观锁 + 版本号 + Last-Write-Wins + 冲突 UI |
| Custom Scene 渲染性能 | 复杂场景导致卡顿 | 组件池化 + 懒加载 + Compose 重组优化 |
| Web 编辑器数据模型与 Android 端一致性 | 两端宏格式不一致导致同步异常 | 统一 JSON Schema + 严格版本化 |
| Accessibility Service 在国产 ROM 上的兼容性 | 服务被系统自动关闭 | 引导用户加入自启动白名单 + 定期检测 |
| Shizuku 用户安装门槛 | 部分用户无法使用高级功能 | 作为可选增强，核心功能不依赖 Shizuku |

---

## 阶段三：V2（完整版）

### 目标

覆盖全部剩余功能，包括地理围栏（国内适配）、Shell Script / Root 特权操作、高级 UI 自动化（OCR/屏幕内容分析）、全量传感器和冷门硬件触发器，以及宏模板库和性能优化，实现产品功能的完整覆盖。

### 新增功能

#### 1. 引擎核心补全（2 项）

| ID | 功能 |
|---|---|
| EX-007 | 文本操作（正则提取、模板替换、Base64） |
| EX-008 | OCR 文字识别 |

#### 2. 剩余触发器（26 项）

**时间（2 项）**：T-TIME-006（Sunset/Sunrise 🔴 需天文算法本地计算）、T-TIME-007（Sleep）

**设备状态（6 项）**：T-DEV-007（Fold State）、T-DEV-008（Proximity Sensor）、T-DEV-009（Light Sensor）、T-DEV-010（Device Docked）、T-DEV-012（Auto Rotate Changed）、T-DEV-013（Daydream）

**连接与网络（7 项）**：T-NET-005（IP Address Change）、T-NET-006（NFC Tag）、T-NET-007（NFC Enabled）、T-NET-010（USB Tethering）、T-NET-012（Roaming）、T-NET-013（Cell Tower）、T-NET-014（Bluetooth Beacon）、T-NET-015（Mobile Service）、T-NET-016（SIM Card Change）

**电话（1 项）**：T-PHONE-008（Dial Phone Number）

**应用与界面（4 项）**：T-APP-003（Screen Content 🟡 UI 自动化）、T-APP-004（Screenshot Content 🟡）、T-APP-005（UI Click 🟡）、T-APP-008（Swipe Screen 🟡）

**传感器（2 项）**：T-SENSOR-001（Activity Recognition 🔴）、T-SENSOR-002（Weather 🔴）

**位置（2 项）**：T-LOC-001（Geofence 🔴 高德 GeoFence API）、T-LOC-002（Location 🔴）

**硬件按键（6 项）**：T-HW-002 ~ T-HW-007（Power Button Toggle/Long Press、Volume Button Press/Long Press、Home Button Long Press、Fingerprint Gesture）

**媒体（2 项）**：T-MEDIA-003（Spotify）、T-MEDIA-004（Photo Taken）

**系统（4 项）**：T-SYS-002（System Log Entry 🟡）、T-SYS-003（Logcat Message 🟡）、T-SYS-009（Failed Login）、T-SYS-010（Accessibility Service State 🟡）

**内部触发器（5 项）**：T-INT-005（App Initialized — 如 MVP 未含则此处补入）、T-INT-006（Drawer Open/Close）、T-INT-008（HTTP Server Request）、T-INT-015（File Shared）、T-INT-016（已在 V1）、T-INT-018（Tasker/Locale Plugin）、T-INT-014（Text Shared）

> 注：部分内部触发器可能已在 V1 中包含，此处列出剩余项。

#### 3. 剩余动作（45 项）

**设备设置（13 项）**：A-SET-009（Location Mode）、A-SET-011（NFC）、A-SET-014（Set Screen Lock）、A-SET-015（USB Tethering）、A-SET-018（Font Scale）、A-SET-019（Force Screen Rotation）、A-SET-020（Ambient Display）、A-SET-021（Camera）、A-SET-023（System Setting）、A-SET-024（Secure Settings）、A-SET-026（Demo Mode）、A-SET-027（Invert Colours）、A-SET-028（Heads-up）、A-SET-029（Notification LED）、A-SET-031（Digital Assistant）、A-SET-032（Default Keyboard）

**音量（1 项）**：A-SND-013（Record Microphone）

**通讯（3 项）**：A-COMM-006（WhatsApp Send）、A-COMM-007（Clear Call Log）、A-COMM-008（Contact Via App）

**通知与 UI（7 项）**：A-UI-010（Overlay Bar）、A-UI-011（Animation Overlay）、A-UI-014（Clear App Dialog）、A-UI-015（Configure App Notifications）、A-UI-016（Notification Interaction）、A-UI-017（Restore Hidden Notifications）、A-UI-019（Voice Input）、A-UI-021（Daydream/Screensaver）

**应用控制（5 项）**：A-APP-004（Kill Background Processes）、A-APP-005（App Enable/Disable）、A-APP-006（Clear App Data）、A-APP-008（Android Shortcut）、A-APP-009（Launch and Press）

**文件与数据（1 项）**：A-DATA-005（Clipboard Refresh）、A-DATA-017（Translate Text 🔴 百度翻译/有道翻译）

**屏幕操作（全部 13 项）**：A-SCR-001 ~ A-SCR-013（Touch Screen、UI Interaction、Press Back、Read Screen/Screenshot Contents、Take Screenshot/Picture、Pixel Colour、Text In Screenshot/On Screen、View ID 文本、OCR、Block Touch）

**网络（3 项）**：A-NET-004（UDP Command）、A-NET-006（Connectivity Check — 如 V1 未含）、A-NET-008（Share Location 🔴）、A-NET-009（Share Last Photo）、A-NET-010（Open Last Photo）

**脚本（1 项）**：A-SCRIPT-001（Shell Script 🟡）

**系统（5 项）**：A-SYS-001（Reboot/Power Off 🟡）、A-SYS-003（Force Location Update 🔴）、A-SYS-004（Location Update Rate 🔴）、A-SYS-006（Sync Account）、A-SYS-007（Locale/Tasker Plugin）、A-SYS-008（Voice Search）

**内部动作（6 项）**：A-INT-004（Delete Macro）、A-INT-006（Set App Icon）、A-INT-007（Set App Mode）、A-INT-008（Set Notification Text）、A-INT-009 ~ A-INT-016 中 V1 未包含的剩余项

#### 4. 剩余约束条件（14 项）

**网络（3 项）**：C-NET-009（Is Roaming）、C-NET-010（Mobile Service）、C-NET-011（Roaming Enabled）

**时间（1 项）**：C-TIME-005（Sunrise/Sunset 🔴）

**位置（2 项）**：C-LOC-001（Geofence 🔴）、C-LOC-002（Cell Towers）

**电话（2 项）**：C-PHONE-003（Speakerphone）、C-PHONE-007（Volume Level）

**系统（3 项）**：C-SYS-008（ADB Hacked）、C-SYS-009（Rooted Device）

**内部（3 项）**：C-INT-003（Macros Enabled — 如 V1 未含）、C-INT-004 ~ C-INT-012 中 V1 未包含的剩余项

#### 5. Android 端补全（3 项）

| ID | 功能 |
|---|---|
| AND-004 | Device Admin |
| AND-007 | 位置服务（高德/百度后台定位 🔴） |
| PERM-003 | Root 权限 |

### V2 功能统计

| 分类 | 新增数量 |
|---|---|
| 引擎核心 | 2 |
| 触发器 | ~26 |
| 动作 | ~45 |
| 约束条件 | ~14 |
| Android 端补全 | 3 |
| **V2 新增合计** | **~90** |
| **累计（MVP + V1 + V2）** | **~378（全量覆盖）** |

> 注：V2 各分类精确数量取决于 V1 实际交付时的边界微调，但保证最终覆盖全部 378 项。

### V2 架构工作量

1. **地理围栏系统**：集成高德地图 SDK GeoFence API，后台位置持续监听，电量优化
2. **屏幕自动化引擎**：基于 Accessibility + OCR 的屏幕内容分析管线，支持坐标点击/文字匹配/View ID 查找
3. **Shell Script / Root 执行环境**：沙箱化 Shell 执行器，Root 命令安全代理
4. **OCR 管线**：ML Kit / 百度 OCR 集成，截图 → 文字提取 → 匹配的完整管线
5. **活动识别**：Android ActivityRecognitionClient 或国内 SDK，低功耗后台持续检测
6. **性能优化**：引擎调度器优化、内存控制、启动速度、数据库查询优化

### V2 关键技术风险

| 风险 | 影响 | 缓解措施 |
|---|---|---|
| 高德地图 SDK 后台位置耗电 | 地理围栏严重影响续航 | 自适应精度调节 + 用户可配置更新频率 |
| OCR 精度与性能 | 中文识别准确率不足或延迟过高 | ML Kit 本地模型 + 百度 OCR 云端降级 |
| Root/Shell 安全风险 | 用户误操作导致系统损坏 | 危险命令白名单 + 确认弹窗 + 沙箱隔离 |
| 屏幕自动化在不同 ROM 上表现差异 | Accessibility 节点树结构不一致 | 多 ROM 测试矩阵 + 容错处理 |
| 国内天气/翻译 API 可靠性 | 接口变更或限流 | 多供应商降级策略 + 缓存 |

---

## 三阶段全局统计

| 阶段 | 功能数 | 累计 | 覆盖率 |
|---|---|---|---|
| MVP | 118 | 118 | 31% |
| V1 | 177 | 295 | 78% |
| V2 | ~83 | 378 | 100% |

### 不包含/排除项清单

以下内容在路线图中**明确不做**：
- 非 Android 16 的版本适配
- iOS 端
- 多用户协作编辑
- 宏市场/社区分享平台
- 第三方开发者 SDK/插件市场
