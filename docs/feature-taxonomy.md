# AutoPilot — 全量原子功能分类体系

> **项目定位**：类 MacroDroid 的 Android 自动化引擎，搭配 PC/Web 端宏编辑器，通过 Google 账号同步数据。
>
> **目标**：定义所有原子级功能，作为后续架构设计和分期实现的基础。
>
> **标记说明**：
> - 🟢 可直接实现（标准 Android API）
> - 🟡 需特殊权限/适配（Accessibility / Device Admin / Root / Shizuku）
> - 🔴 需本地化替代方案（国内不可用或需替换）
> - 🌐 PC/Web 端可视化编辑需同步支持
> - ⚙️ 引擎核心能力（非 UI 可见功能）

---

## 一、核心引擎（Engine Core）⚙️

### 1.1 宏（Macro）模型

| ID | 原子功能 | 描述 |
|---|---|---|
| E-001 | 宏定义 | 一个宏 = N 个触发器 + N 个动作 + N 个约束条件 |
| E-002 | 宏启用/禁用 | 单个宏的开关状态 |
| E-003 | 宏分类（Category） | 将宏分组管理，支持分类级启用/禁用 |
| E-004 | 宏导入/导出 | JSON 格式序列化，支持文件分享 |
| E-005 | 宏克隆 | 复制现有宏 |
| E-006 | 宏日志 | 记录每次宏触发的时间、触发器、执行结果 |
| E-007 | 宏模板 | 预置常用宏模板，用户可一键导入 |

### 1.2 变量系统（Variable System）

| ID | 原子功能 | 描述 |
|---|---|---|
| V-001 | Boolean 变量 | true/false |
| V-002 | Integer 变量 | 整数 |
| V-003 | Decimal 变量 | 浮点数 |
| V-004 | String 变量 | 字符串 |
| V-005 | Dictionary 变量 | 键值对映射 |
| V-006 | Array 变量 | 有序列表 |
| V-007 | 全局变量 | 跨宏共享 |
| V-008 | 局部变量 | 仅当前宏可见 |
| V-009 | 变量持久化 | 重启后变量值保留 |
| V-010 | 魔术变量 | 系统内置只读变量（当前时间、电量、设备名等） |
| V-011 | 变量表达式 | 支持数学运算、字符串拼接、条件表达式 |

### 1.3 控制流（Control Flow）

| ID | 原子功能 | 描述 |
|---|---|---|
| CF-001 | If / Else 条件分支 | 基于条件执行不同动作 |
| CF-002 | If Confirmed Then | 弹窗确认后执行 |
| CF-003 | Repeat 循环 | 重复执行 N 次或直到条件满足 |
| CF-004 | Iterate Dictionary/Array | 遍历字典/数组元素 |
| CF-005 | Break 跳出循环 | 终止当前循环 |
| CF-006 | Continue 继续循环 | 跳过当前迭代 |
| CF-007 | Wait Before Next | 延时等待指定时长 |
| CF-008 | Wait Until Trigger | 等待直到特定触发器触发 |
| CF-009 | Action Block | 可复用的动作块（函数） |
| CF-010 | Exit Action Block | 提前退出动作块 |
| CF-011 | Macro Run | 调用另一个宏执行 |
| CF-012 | Cancel Macro Actions | 取消当前宏剩余动作 |
| CF-013 | Confirm Next | 确认后继续下一步 |
| CF-014 | 同步/异步执行 | 动作可选阻塞或异步执行 |

### 1.4 表达式引擎（Expression Engine）

| ID | 原子功能 | 描述 |
|---|---|---|
| EX-001 | 数学运算 | +, -, *, /, %, 幂, 取整 |
| EX-002 | 字符串操作 | 拼接、截取、替换、正则匹配、大小写转换 |
| EX-003 | 日期/时间函数 | 格式化、差值计算、时区转换 |
| EX-004 | 逻辑运算 | AND, OR, NOT, 比较运算符 |
| EX-005 | JSON 解析 | 解析 JSON 字符串，提取字段 |
| EX-006 | 数组操作 | 排序、过滤、映射、查找、去重 |
| EX-007 | 文本操作 | 正则提取、模板替换、Base64 编解码 |
| EX-008 | OCR 文字识别 | 从图片/截图中提取文字 |

---

## 二、触发器（Triggers）

### 2.1 时间与日程

| ID | 原子触发器 | 描述 | 标记 |
|---|---|---|---|
| T-TIME-001 | Day/Time Trigger | 指定日期和时间触发 | 🟢🌐 |
| T-TIME-002 | Day of Week/Month | 按周几或每月第几天触发 | 🟢🌐 |
| T-TIME-003 | Regular Interval | 按固定间隔重复触发 | 🟢🌐 |
| T-TIME-004 | Stopwatch | 秒表到达指定时间触发 | 🟢🌐 |
| T-TIME-005 | Calendar Event | 日历事件开始/结束时触发 | 🟢 |
| T-TIME-006 | Sunset/Sunrise | 日出/日落时触发（基于地理位置计算） | 🟢🔴 |
| T-TIME-007 | Sleep | 定时休眠触发 | 🟢 |

### 2.2 设备状态

| ID | 原子触发器 | 描述 | 标记 |
|---|---|---|---|
| T-DEV-001 | Device Boot | 设备开机完成 | 🟢 |
| T-DEV-002 | Screen On/Off | 屏幕亮/灭 | 🟢 |
| T-DEV-003 | Screen Unlocked | 屏幕解锁 | 🟢 |
| T-DEV-004 | Screen Orientation | 屏幕方向变化（横/竖） | 🟢 |
| T-DEV-005 | Flip Device | 设备翻转（正面朝下/朝上） | 🟢 |
| T-DEV-006 | Shake Device | 摇晃设备 | 🟢 |
| T-DEV-007 | Fold State | 折叠屏展开/折叠 | 🟢 |
| T-DEV-008 | Proximity Sensor | 近距离传感器触发 | 🟢 |
| T-DEV-009 | Light Sensor | 光线传感器达到阈值 | 🟢 |
| T-DEV-010 | Device Docked/Undocked | 设备插入/拔出底座 | 🟢 |
| T-DEV-011 | Dark Theme Change | 深色模式切换 | 🟢 |
| T-DEV-012 | Auto Rotate Changed | 自动旋转开关变化 | 🟢 |
| T-DEV-013 | Daydream On/Off | 屏保开启/关闭 | 🟢 |

### 2.3 连接与网络

| ID | 原子触发器 | 描述 | 标记 |
|---|---|---|---|
| T-NET-001 | WiFi State Change | WiFi 开启/关闭/连接/断开 | 🟢 |
| T-NET-002 | WiFi SSID Transition | 连接到特定 SSID / 离开特定 SSID | 🟢 |
| T-NET-003 | Bluetooth Event | 蓝牙设备连接/断开/状态变化 | 🟢 |
| T-NET-004 | Data Connectivity Change | 移动数据连接状态变化 | 🟢 |
| T-NET-005 | IP Address Change | IP 地址变化 | 🟢 |
| T-NET-006 | NFC Tag | 扫描到 NFC 标签 | 🟢 |
| T-NET-007 | NFC Enabled State | NFC 开关状态变化 | 🟢 |
| T-NET-008 | Airplane Mode Changed | 飞行模式切换 | 🟢 |
| T-NET-009 | Hotspot Enabled/Disabled | 热点开关变化 | 🟢 |
| T-NET-010 | USB Tethering | USB 网络共享状态变化 | 🟢 |
| T-NET-011 | VPN State | VPN 连接/断开 | 🟢 |
| T-NET-012 | Roaming Started/Stopped | 漫游状态变化 | 🟢 |
| T-NET-013 | Cell Tower Change | 基站切换 | 🟢 |
| T-NET-014 | Bluetooth Beacon | 蓝牙信标检测 | 🟢 |
| T-NET-015 | Mobile Service Status | 移动服务状态变化 | 🟢 |
| T-NET-016 | SIM Card Change | SIM 卡变更 | 🟢 |

### 2.4 电话与短信

| ID | 原子触发器 | 描述 | 标记 |
|---|---|---|---|
| T-PHONE-001 | Call Incoming | 来电 | 🟢 |
| T-PHONE-002 | Call Outgoing | 去电 | 🟢 |
| T-PHONE-003 | Call Active | 通话中 | 🟢 |
| T-PHONE-004 | Call Ended | 通话结束 | 🟢 |
| T-PHONE-005 | Call Missed | 未接来电 | 🟢 |
| T-PHONE-006 | SMS Received | 收到短信 | 🟢 |
| T-PHONE-007 | SMS Sent | 发送短信 | 🟢 |
| T-PHONE-008 | Dial Phone Number | 拨号特定号码 | 🟢 |

### 2.5 电量与电源

| ID | 原子触发器 | 描述 | 标记 |
|---|---|---|---|
| T-PWR-001 | Power Connected/Disconnected | 充电器插入/拔出 | 🟢 |
| T-PWR-002 | Battery Level | 电量达到指定阈值 | 🟢 |
| T-PWR-003 | Battery Temperature | 电池温度达到阈值 | 🟢 |
| T-PWR-004 | Battery Saver State | 省电模式开关 | 🟢 |

### 2.6 通知

| ID | 原子触发器 | 描述 | 标记 |
|---|---|---|---|
| T-NOTIF-001 | Notification | 收到来自指定应用的通知（可过滤文本） | 🟡 |
| T-NOTIF-002 | Notification Bar Button | 点击通知栏中的自定义按钮 | 🟢 |

### 2.7 应用与界面

| ID | 原子触发器 | 描述 | 标记 |
|---|---|---|---|
| T-APP-001 | Application Launched/Closed | 指定应用启动/关闭 | 🟡 |
| T-APP-002 | App Install/Remove/Update | 应用安装/卸载/更新 | 🟢 |
| T-APP-003 | Screen Content | 屏幕上出现指定文本 | 🟡 |
| T-APP-004 | Screenshot Content | 截屏内容匹配 | 🟡 |
| T-APP-005 | UI Click | 点击屏幕特定 UI 元素 | 🟡 |
| T-APP-006 | Clipboard Change | 剪贴板内容变化 | 🟢 |
| T-APP-007 | Recent Apps Opened | 最近任务列表打开 | 🟡 |
| T-APP-008 | Swipe Screen | 特定方向滑动屏幕 | 🟡 |

### 2.8 传感器与活动

| ID | 原子触发器 | 描述 | 标记 |
|---|---|---|---|
| T-SENSOR-001 | Activity Recognition | 检测用户活动（步行、跑步、驾驶、静止） | 🟢🔴 |
| T-SENSOR-002 | Weather | 天气条件变化触发 | 🟢🔴 |

### 2.9 位置

| ID | 原子触发器 | 描述 | 标记 |
|---|---|---|---|
| T-LOC-001 | Geofence Trigger | 进入/离开地理围栏区域 | 🔴 |
| T-LOC-002 | Location Trigger | 到达指定位置 | 🔴 |

### 2.10 硬件按键

| ID | 原子触发器 | 描述 | 标记 |
|---|---|---|---|
| T-HW-001 | Headphones Insert/Removed | 耳机插入/拔出 | 🟢 |
| T-HW-002 | Power Button Toggle | 电源键连按 | 🟡 |
| T-HW-003 | Power Button Long Press | 电源键长按 | 🟡 |
| T-HW-004 | Volume Button Press | 音量键按下 | 🟡 |
| T-HW-005 | Volume Button Long Press | 音量键长按 | 🟡 |
| T-HW-006 | Home Button Long Press | Home 键长按 | 🟡 |
| T-HW-007 | Fingerprint Gesture | 指纹手势 | 🟡 |
| T-HW-008 | USB Device Connect/Disconnect | USB 设备连接/断开 | 🟢 |
| T-HW-009 | Media Button Pressed | 媒体按钮按下 | 🟢 |

### 2.11 媒体

| ID | 原子触发器 | 描述 | 标记 |
|---|---|---|---|
| T-MEDIA-001 | Media Track Changed | 正在播放的曲目变化 | 🟢 |
| T-MEDIA-002 | Music/Sound Playing | 检测到音频播放/停止 | 🟢 |
| T-MEDIA-003 | Spotify | Spotify 播放状态变化 | 🟢 |
| T-MEDIA-004 | Photo Taken | 拍照完成 | 🟢 |

### 2.12 系统设置

| ID | 原子触发器 | 描述 | 标记 |
|---|---|---|---|
| T-SYS-001 | System Setting Change | 系统设置项变化 | 🟢 |
| T-SYS-002 | System Log Entry | 系统日志出现特定条目 | 🟡 |
| T-SYS-003 | Logcat Message | Logcat 出现特定消息 | 🟡 |
| T-SYS-004 | GPS Enabled/Disabled | GPS 开关变化 | 🟢 |
| T-SYS-005 | Silent Mode Enabled/Disabled | 静音模式变化 | 🟢 |
| T-SYS-006 | Priority Mode/Do Not Disturb | 免打扰模式变化 | 🟢 |
| T-SYS-007 | Torch On/Off | 手电筒开关 | 🟢 |
| T-SYS-008 | Autosync Changed | 自动同步开关变化 | 🟢 |
| T-SYS-009 | Failed Login Attempt | 解锁失败 | 🟢 |
| T-SYS-010 | Accessibility Service State | 无障碍服务状态变化 | 🟡 |

### 2.13 AutoPilot 内部触发器

| ID | 原子触发器 | 描述 | 标记 |
|---|---|---|---|
| T-INT-001 | Variable Change | 内部变量值变化 | 🟢🌐 |
| T-INT-002 | Macro Enabled | 宏被启用 | 🟢🌐 |
| T-INT-003 | Macro Finished | 宏执行完成 | 🟢🌐 |
| T-INT-004 | Mode Change | 应用模式切换 | 🟢🌐 |
| T-INT-005 | App Initialized | AutoPilot 启动完成 | 🟢 |
| T-INT-006 | Drawer Open/Close | 侧边栏打开/关闭 | 🟢 |
| T-INT-007 | Webhook (URL) | 接收到 HTTP Webhook 请求 | 🟢🌐 |
| T-INT-008 | HTTP Server Request | 内置 HTTP Server 收到请求 | 🟢 |
| T-INT-009 | Intent Received | 收到 Android Intent | 🟢 |
| T-INT-010 | Floating Button | 悬浮按钮点击 | 🟢 |
| T-INT-011 | Widget Button | 桌面小部件按钮点击 | 🟢 |
| T-INT-012 | Shortcut Launched | 快捷方式启动 | 🟢 |
| T-INT-013 | Quick Settings Tile | 快速设置磁贴点击 | 🟢 |
| T-INT-014 | Text Shared to App | 文本分享到 AutoPilot | 🟢 |
| T-INT-015 | File Shared to App | 文件分享到 AutoPilot | 🟢 |
| T-INT-016 | Popup Message | 弹窗消息触发 | 🟢 |
| T-INT-017 | Empty Trigger | 仅手动触发的空触发器 | 🟢🌐 |
| T-INT-018 | Tasker/Locale Plugin | 第三方插件触发 | 🟢 |

---

## 三、动作（Actions）

### 3.1 设备设置

| ID | 原子动作 | 描述 | 标记 |
|---|---|---|---|
| A-SET-001 | Airplane Mode On/Off | 开关飞行模式 | 🟡 |
| A-SET-002 | Auto Rotate On/Off | 开关自动旋转 | 🟢 |
| A-SET-003 | Bluetooth Configure | 蓝牙开关/连接/断开指定设备 | 🟢 |
| A-SET-004 | Brightness | 设置屏幕亮度 | 🟢 |
| A-SET-005 | Dark Theme | 切换深色/浅色模式 | 🟢 |
| A-SET-006 | GPS Enable/Disable | 开关 GPS | 🟡 |
| A-SET-007 | HotSpot On/Off | 开关热点 | 🟡 |
| A-SET-008 | Immersive Mode | 开关沉浸模式 | 🟡 |
| A-SET-009 | Location Mode | 设置定位模式 | 🟡 |
| A-SET-010 | Mobile Data On/Off | 开关移动数据 | 🟡 |
| A-SET-011 | NFC Enable/Disable | 开关 NFC | 🟡 |
| A-SET-012 | Screen On/Off | 控制屏幕亮灭 | 🟡 |
| A-SET-013 | Set Screen Timeout | 设置屏幕超时 | 🟢 |
| A-SET-014 | Set Screen Lock | 设置锁屏方式 | 🟡 |
| A-SET-015 | USB Tethering | 开关 USB 网络共享 | 🟡 |
| A-SET-016 | WiFi Configure | WiFi 开关/连接/断开/忘记网络 | 🟢 |
| A-SET-017 | Battery Saver | 开关省电模式 | 🟡 |
| A-SET-018 | Font Scale | 设置字体缩放 | 🟡 |
| A-SET-019 | Force Screen Rotation | 强制屏幕旋转方向 | 🟡 |
| A-SET-020 | Ambient Display | 开关环境显示 | 🟡 |
| A-SET-021 | Camera Enable/Disable | 开关摄像头 | 🟡 |
| A-SET-022 | Autosync On/Off | 开关自动同步 | 🟢 |
| A-SET-023 | System Setting | 修改任意系统设置项 | 🟡 |
| A-SET-024 | Secure Settings | 修改安全设置 | 🟡 |
| A-SET-025 | Car Mode | 开关驾驶模式 | 🟢 |
| A-SET-026 | Demo Mode | 开关演示模式 | 🟡 |
| A-SET-027 | Invert Colours | 反色显示 | 🟡 |
| A-SET-028 | Heads-up Enable/Disable | 开关悬浮通知 | 🟡 |
| A-SET-029 | Notification LED Enable/Disable | 开关通知 LED | 🟡 |
| A-SET-030 | Set Wallpaper | 设置壁纸 | 🟢 |
| A-SET-031 | Set Digital Assistant | 设置默认数字助手 | 🟡 |
| A-SET-032 | Keyboard - Set Default | 设置默认输入法 | 🟡 |
| A-SET-033 | Torch On/Off | 开关手电筒 | 🟢 |

### 3.2 音量与声音

| ID | 原子动作 | 描述 | 标记 |
|---|---|---|---|
| A-SND-001 | Volume Change | 设置/调整音量（铃声/媒体/通知/系统/通话） | 🟢 |
| A-SND-002 | Volume Up/Down | 音量增减 | 🟢 |
| A-SND-003 | Ringtone Configure | 设置铃声 | 🟢 |
| A-SND-004 | Set Notification Sound | 设置通知音 | 🟢 |
| A-SND-005 | Play/Stop Sound | 播放/停止音频文件 | 🟢 |
| A-SND-006 | Silent - Vibrate Off | 静音模式 | 🟢 |
| A-SND-007 | Speak Text | TTS 语音朗读文本 | 🟢 |
| A-SND-008 | Say Current Time | 语音报时 | 🟢 |
| A-SND-009 | Speakerphone On/Off | 开关扬声器 | 🟢 |
| A-SND-010 | Vibrate | 触发振动（自定义模式） | 🟢 |
| A-SND-011 | Vibrate Enable/Disable | 开关振动 | 🟢 |
| A-SND-012 | Show Volume Popup | 显示音量调节弹窗 | 🟢 |
| A-SND-013 | Record Microphone | 录音 | 🟢 |
| A-SND-014 | Priority Mode / Do Not Disturb | 设置免打扰模式 | 🟢 |

### 3.3 通讯

| ID | 原子动作 | 描述 | 标记 |
|---|---|---|---|
| A-COMM-001 | Make Call | 拨打电话 | 🟢 |
| A-COMM-002 | Answer Call | 接听来电 | 🟡 |
| A-COMM-003 | Call Reject | 拒接来电 | 🟡 |
| A-COMM-004 | Send SMS | 发送短信 | 🟢 |
| A-COMM-005 | Send Email | 发送邮件（通过 Intent） | 🟢 |
| A-COMM-006 | WhatsApp Send | 通过 WhatsApp 发送消息 | 🟢 |
| A-COMM-007 | Clear Call Log | 清除通话记录 | 🟢 |
| A-COMM-008 | Contact Via App | 通过指定应用联系人 | 🟢 |
| A-COMM-009 | Notification Reply | 在通知中直接回复 | 🟡 |

### 3.4 通知与 UI 显示

| ID | 原子动作 | 描述 | 标记 |
|---|---|---|---|
| A-UI-001 | Display Notification | 发送自定义通知 | 🟢 |
| A-UI-002 | Display Dialog | 显示对话框 | 🟢 |
| A-UI-003 | Display Bubble Notification | 显示气泡通知 | 🟢 |
| A-UI-004 | Display Custom Scene | 显示自定义 UI 场景（全屏/悬浮） | 🟢 |
| A-UI-005 | Option Dialog | 显示选项对话框 | 🟢 |
| A-UI-006 | Selection Dialog | 显示选择列表对话框 | 🟢 |
| A-UI-007 | Popup Message | 显示 Toast 消息 | 🟢 |
| A-UI-008 | Floating Text | 显示悬浮文本 | 🟢 |
| A-UI-009 | Floating Button Configure | 配置悬浮按钮 | 🟢 |
| A-UI-010 | Overlay Bar | 显示覆盖工具栏 | 🟢 |
| A-UI-011 | Animation Overlay | 显示动画覆盖层 | 🟢 |
| A-UI-012 | Expand/Collapse Status Bar | 展开/收起状态栏 | 🟡 |
| A-UI-013 | Clear Notifications | 清除通知 | 🟡 |
| A-UI-014 | Clear App Dialog | 清除 AutoPilot 弹窗 | 🟢 |
| A-UI-015 | Configure App Notifications | 配置应用通知设置 | 🟡 |
| A-UI-016 | Notification Interaction | 与通知交互（点击/展开/操作） | 🟡 |
| A-UI-017 | Restore Hidden Notifications | 恢复隐藏的通知 | 🟡 |
| A-UI-018 | Keyboard Prompt | 弹出键盘输入提示 | 🟢 |
| A-UI-019 | Voice Input | 弹出语音输入 | 🟢 |
| A-UI-020 | Dim Screen | 降低屏幕亮度覆盖层 | 🟢 |
| A-UI-021 | Daydream/Screensaver On | 启动屏保 | 🟢 |

### 3.5 应用控制

| ID | 原子动作 | 描述 | 标记 |
|---|---|---|---|
| A-APP-001 | Launch Application | 启动指定应用 | 🟢 |
| A-APP-002 | Launch Home Screen | 回到桌面 | 🟢 |
| A-APP-003 | Kill Application | 强制停止应用 | 🟡 |
| A-APP-004 | Kill Background Processes | 清除后台进程 | 🟡 |
| A-APP-005 | App Enable/Disable | 启用/禁用应用 | 🟡 |
| A-APP-006 | Clear App Data | 清除应用数据 | 🟡 |
| A-APP-007 | Launch Shortcut | 启动快捷方式 | 🟢 |
| A-APP-008 | Android Shortcut | 执行 Android 快捷方式 | 🟢 |
| A-APP-009 | Launch and Press | 启动应用并模拟按键 | 🟡 |
| A-APP-010 | Alarm Clock | 设置/修改闹钟 | 🟢 |

### 3.6 文件与数据

| ID | 原子动作 | 描述 | 标记 |
|---|---|---|---|
| A-DATA-001 | File Operation | 文件操作（复制/移动/删除/重命名/创建目录） | 🟢 |
| A-DATA-002 | Write to File | 写入文件（覆盖/追加） | 🟢 |
| A-DATA-003 | Open File | 打开文件（通过 Intent） | 🟢 |
| A-DATA-004 | Fill Clipboard | 设置剪贴板内容 | 🟢 |
| A-DATA-005 | Clipboard Refresh | 刷新剪贴板 | 🟢 |
| A-DATA-006 | Set Variable | 设置变量值 | 🟢🌐 |
| A-DATA-007 | Delete Variable | 删除变量 | 🟢🌐 |
| A-DATA-008 | Clear Variables | 清除所有变量 | 🟢🌐 |
| A-DATA-009 | Array Manipulation | 数组操作（添加/删除/排序/查找） | 🟢🌐 |
| A-DATA-010 | Clear Dictionary/Array Entry | 清除字典/数组条目 | 🟢🌐 |
| A-DATA-011 | JSON Parse | 解析 JSON 字符串 | 🟢🌐 |
| A-DATA-012 | Text Manipulation | 文本处理（正则/替换/截取/编码） | 🟢🌐 |
| A-DATA-013 | Log Event | 记录日志条目 | 🟢 |
| A-DATA-014 | Clear Log | 清除日志 | 🟢 |
| A-DATA-015 | Export Log | 导出日志 | 🟢 |
| A-DATA-016 | Calendar - Add Event | 添加日历事件 | 🟢 |
| A-DATA-017 | Translate Text | 翻译文本 | 🟢🔴 |

### 3.7 屏幕操作与自动化

| ID | 原子动作 | 描述 | 标记 |
|---|---|---|---|
| A-SCR-001 | Touch Screen | 模拟触屏点击/滑动 | 🟡 |
| A-SCR-002 | UI Interaction | 通过 Accessibility 操作 UI 元素 | 🟡 |
| A-SCR-003 | Press Back Button | 模拟返回键 | 🟡 |
| A-SCR-004 | Read Screen Contents | 读取屏幕上的文字内容 | 🟡 |
| A-SCR-005 | Read Screenshot Contents | 读取截图中的文字内容 | 🟡 |
| A-SCR-006 | Take Screenshot | 截屏 | 🟡 |
| A-SCR-007 | Take Picture | 拍照 | 🟢 |
| A-SCR-008 | Check Pixel Colour | 检查屏幕特定位置像素颜色 | 🟡 |
| A-SCR-009 | Check Text In Screenshot | 检查截图中是否包含指定文本 | 🟡 |
| A-SCR-010 | Check Text On Screen | 检查屏幕上是否显示指定文本 | 🟡 |
| A-SCR-011 | Get Text From View Id | 通过 View ID 获取文本 | 🟡 |
| A-SCR-012 | Text from Image (OCR) | 图片文字识别 | 🟢🔴 |
| A-SCR-013 | Block Screen Touch | 禁用触摸 | 🟡 |

### 3.8 网络与通信

| ID | 原子动作 | 描述 | 标记 |
|---|---|---|---|
| A-NET-001 | HTTP Request | 发送 HTTP 请求（GET/POST/PUT/DELETE/PATCH） | 🟢🌐 |
| A-NET-002 | HTTP Server Response | 作为 HTTP Server 返回响应 | 🟢 |
| A-NET-003 | Open Website / HTTP GET | 打开网页 | 🟢 |
| A-NET-004 | UDP Command | 发送 UDP 命令 | 🟢 |
| A-NET-005 | Send Intent | 发送 Android Intent | 🟢 |
| A-NET-006 | Connectivity Check | 检测网络连通性 | 🟢 |
| A-NET-007 | Share Text | 分享文本 | 🟢 |
| A-NET-008 | Share Location | 分享位置 | 🟢🔴 |
| A-NET-009 | Share Last Photo | 分享最近照片 | 🟢 |
| A-NET-010 | Open Last Photo | 打开最近照片 | 🟢 |

### 3.9 脚本执行

| ID | 原子动作 | 描述 | 标记 |
|---|---|---|---|
| A-SCRIPT-001 | Shell Script | 执行 Shell 命令 | 🟡 |
| A-SCRIPT-002 | JavaScript Code | 执行 JavaScript 代码 | 🟢🌐 |

### 3.10 系统与电源

| ID | 原子动作 | 描述 | 标记 |
|---|---|---|---|
| A-SYS-001 | Reboot/Power Off | 重启/关机 | 🟡 |
| A-SYS-002 | Keep Device Awake | 保持设备唤醒 | 🟢 |
| A-SYS-003 | Force Location Update | 强制刷新位置 | 🟢🔴 |
| A-SYS-004 | Set Location Update Rate | 设置位置更新频率 | 🟢🔴 |
| A-SYS-005 | Authenticate User | 生物识别/密码验证 | 🟢 |
| A-SYS-006 | Sync Account | 同步账户数据 | 🟢 |
| A-SYS-007 | Locale/Tasker Plugin | 调用第三方插件 | 🟢 |
| A-SYS-008 | Voice Search | 启动语音搜索 | 🟢 |

### 3.11 媒体控制

| ID | 原子动作 | 描述 | 标记 |
|---|---|---|---|
| A-MEDIA-001 | Control Media | 播放/暂停/上一首/下一首/停止 | 🟢 |
| A-MEDIA-002 | Stopwatch | 秒表操作（开始/停止/重置/记录） | 🟢🌐 |

### 3.12 AutoPilot 内部动作

| ID | 原子动作 | 描述 | 标记 |
|---|---|---|---|
| A-INT-001 | Enable/Disable Macro | 启用/禁用指定宏 | 🟢🌐 |
| A-INT-002 | Enable/Disable Category | 启用/禁用指定分类 | 🟢🌐 |
| A-INT-003 | Enable/Disable Trigger | 启用/禁用指定触发器 | 🟢🌐 |
| A-INT-004 | Delete Macro | 删除宏 | 🟢🌐 |
| A-INT-005 | Export Macros | 导出宏 | 🟢🌐 |
| A-INT-006 | Set App Icon | 设置应用图标 | 🟢 |
| A-INT-007 | Set App Mode | 设置应用模式 | 🟢🌐 |
| A-INT-008 | Set Notification Text | 设置常驻通知文本 | 🟢 |
| A-INT-009 | App Settings | 打开 AutoPilot 设置 | 🟢 |
| A-INT-010 | Configure Quick Tile | 配置快速设置磁贴 | 🟢 |
| A-INT-011 | Widget Button Modify | 修改桌面小部件按钮 | 🟢 |
| A-INT-012 | Open Log | 打开运行日志 | 🟢 |
| A-INT-013 | Disable AutoPilot | 禁用引擎 | 🟢 |
| A-INT-014 | Set Button Bar Image | 设置按钮栏图片 | 🟢 |
| A-INT-015 | App Drawer | 打开应用抽屉 | 🟢 |
| A-INT-016 | Empty Action | 空动作（占位） | 🟢🌐 |

---

## 四、约束条件（Constraints）

### 4.1 设备状态

| ID | 原子约束 | 描述 | 标记 |
|---|---|---|---|
| C-DEV-001 | Screen On/Off | 屏幕是否亮 | 🟢🌐 |
| C-DEV-002 | Device Locked/Unlocked | 设备是否锁定 | 🟢🌐 |
| C-DEV-003 | Device Facing | 设备朝向（正面朝上/朝下） | 🟢🌐 |
| C-DEV-004 | Device Orientation | 设备旋转方向 | 🟢🌐 |
| C-DEV-005 | Proximity Sensor | 近距离传感器状态 | 🟢 |
| C-DEV-006 | Light Sensor | 光线传感器值范围 | 🟢 |
| C-DEV-007 | Brightness | 当前亮度值范围 | 🟢🌐 |
| C-DEV-008 | Torch On/Off | 手电筒是否开启 | 🟢🌐 |

### 4.2 连接与网络

| ID | 原子约束 | 描述 | 标记 |
|---|---|---|---|
| C-NET-001 | WiFi State | WiFi 是否连接/指定 SSID | 🟢🌐 |
| C-NET-002 | WiFi HotSpot State | 热点是否开启 | 🟢🌐 |
| C-NET-003 | Bluetooth State | 蓝牙是否开启/连接指定设备 | 🟢🌐 |
| C-NET-004 | Mobile Data On/Off | 移动数据是否开启 | 🟢🌐 |
| C-NET-005 | NFC State | NFC 是否开启 | 🟢🌐 |
| C-NET-006 | Airplane Mode | 飞行模式是否开启 | 🟢🌐 |
| C-NET-007 | VPN State | VPN 是否连接 | 🟢🌐 |
| C-NET-008 | IP Address | IP 地址匹配 | 🟢🌐 |
| C-NET-009 | Is Roaming | 是否漫游中 | 🟢🌐 |
| C-NET-010 | Mobile Service Status | 移动网络服务状态 | 🟢🌐 |
| C-NET-011 | Roaming Enabled | 漫游是否启用 | 🟢🌐 |

### 4.3 电量

| ID | 原子约束 | 描述 | 标记 |
|---|---|---|---|
| C-PWR-001 | Battery Level | 电量在指定范围内 | 🟢🌐 |
| C-PWR-002 | Battery Temperature | 电池温度在指定范围内 | 🟢🌐 |
| C-PWR-003 | Battery Saver State | 省电模式是否开启 | 🟢🌐 |
| C-PWR-004 | Power Connected | 是否正在充电 | 🟢🌐 |

### 4.4 时间

| ID | 原子约束 | 描述 | 标记 |
|---|---|---|---|
| C-TIME-001 | Time of Day | 当前时间在指定范围内 | 🟢🌐 |
| C-TIME-002 | Day of the Week | 当前是指定星期几 | 🟢🌐 |
| C-TIME-003 | Day of the Month | 当前是每月第几天 | 🟢🌐 |
| C-TIME-004 | Month of the Year | 当前是几月 | 🟢🌐 |
| C-TIME-005 | Sunrise/Sunset | 当前在日出/日落前后 | 🟢🔴🌐 |
| C-TIME-006 | Time Since Boot | 设备开机后经过时间 | 🟢🌐 |

### 4.5 位置

| ID | 原子约束 | 描述 | 标记 |
|---|---|---|---|
| C-LOC-001 | Geofence (Location) | 是否在指定地理围栏内 | 🔴🌐 |
| C-LOC-002 | Cell Towers | 当前连接的基站匹配 | 🟢 |

### 4.6 应用与音频

| ID | 原子约束 | 描述 | 标记 |
|---|---|---|---|
| C-APP-001 | Application Running | 指定应用是否在前台运行 | 🟡🌐 |
| C-APP-002 | Music Active | 是否有音频播放中 | 🟢🌐 |
| C-APP-003 | Headphone Connection | 耳机是否连接 | 🟢🌐 |
| C-APP-004 | Calendar Entry | 当前是否在日历事件时间内 | 🟢🌐 |

### 4.7 电话与通知

| ID | 原子约束 | 描述 | 标记 |
|---|---|---|---|
| C-PHONE-001 | Call State | 当前通话状态（空闲/响铃/通话中） | 🟢🌐 |
| C-PHONE-002 | Phone Ringing | 电话是否正在响铃 | 🟢🌐 |
| C-PHONE-003 | Speakerphone On/Off | 扬声器是否开启 | 🟢🌐 |
| C-PHONE-004 | Notification Present | 指定通知是否存在 | 🟡🌐 |
| C-PHONE-005 | Notification Volume | 通知音量级别 | 🟢🌐 |
| C-PHONE-006 | Ringer Volume | 铃声音量级别 | 🟢🌐 |
| C-PHONE-007 | Volume Level | 指定音量通道级别 | 🟢🌐 |
| C-PHONE-008 | Priority Mode / Do Not Disturb | 免打扰模式状态 | 🟢🌐 |

### 4.8 系统

| ID | 原子约束 | 描述 | 标记 |
|---|---|---|---|
| C-SYS-001 | Auto Rotate | 自动旋转是否开启 | 🟢🌐 |
| C-SYS-002 | Auto Sync | 自动同步是否开启 | 🟢🌐 |
| C-SYS-003 | GPS State | GPS 是否开启 | 🟢🌐 |
| C-SYS-004 | Dark Mode | 深色模式是否开启 | 🟢🌐 |
| C-SYS-005 | System Settings | 指定系统设置值匹配 | 🟢🌐 |
| C-SYS-006 | Compare Values | 比较两个变量/表达式 | 🟢🌐 |
| C-SYS-007 | Clipboard Content | 剪贴板内容匹配 | 🟢🌐 |
| C-SYS-008 | ADB Hacked | 是否通过 ADB 授权 | 🟢🌐 |
| C-SYS-009 | Rooted Device | 设备是否已 Root | 🟢🌐 |

### 4.9 AutoPilot 内部约束

| ID | 原子约束 | 描述 | 标记 |
|---|---|---|---|
| C-INT-001 | Variable | 内部变量值匹配条件 | 🟢🌐 |
| C-INT-002 | Macro Running | 指定宏是否正在运行 | 🟢🌐 |
| C-INT-003 | Macro(s) Enabled/Disabled | 指定宏是否启用/禁用 | 🟢🌐 |
| C-INT-004 | Macro(s) Invoked Recently | 指定宏是否最近被调用过 | 🟢🌐 |
| C-INT-005 | App Mode | 当前应用模式 | 🟢🌐 |
| C-INT-006 | Category Enabled/Disabled | 分类是否启用/禁用 | 🟢🌐 |
| C-INT-007 | Trigger Fired | 当前触发的触发器类型 | 🟢🌐 |
| C-INT-008 | Invocation Method | 宏的调用方式 | 🟢🌐 |
| C-INT-009 | Floating Button | 悬浮按钮是否可见 | 🟢🌐 |
| C-INT-010 | Floating Text | 悬浮文本是否可见 | 🟢🌐 |
| C-INT-011 | Quick Tile State | 快速设置磁贴状态 | 🟢🌐 |
| C-INT-012 | Stopwatch | 秒表运行状态/时间 | 🟢🌐 |
| C-INT-013 | AND / OR / XOR / NOT | 逻辑组合约束 | 🟢🌐 |

---

## 五、自定义 UI 系统（Custom Scene）

| ID | 原子功能 | 描述 | 标记 |
|---|---|---|---|
| UI-001 | Scene 画布 | 自定义 UI 设计画布（支持全屏和悬浮窗） | 🟢🌐 |
| UI-002 | 按钮组件 | 可配置文本、图标、点击动作 | 🟢🌐 |
| UI-003 | 文本标签 | 静态/动态文本显示 | 🟢🌐 |
| UI-004 | 图片组件 | 显示图片 | 🟢🌐 |
| UI-005 | 输入框组件 | 文本输入 | 🟢🌐 |
| UI-006 | 开关组件 | Toggle 开关 | 🟢🌐 |
| UI-007 | 滑块组件 | 数值滑块 | 🟢🌐 |
| UI-008 | 列表/网格组件 | 数据列表/网格布局 | 🟢🌐 |
| UI-009 | 下拉选择组件 | 下拉菜单 | 🟢🌐 |
| UI-010 | 进度条组件 | 进度显示 | 🟢🌐 |
| UI-011 | 布局容器 | 水平/垂直/层叠布局 | 🟢🌐 |
| UI-012 | 样式系统 | 颜色、字体、边距、圆角、透明度 | 🟢🌐 |
| UI-013 | 事件绑定 | 组件事件 → 触发宏动作 | 🟢🌐 |
| UI-014 | 数据绑定 | 变量值绑定到 UI 组件 | 🟢🌐 |
| UI-015 | 场景间导航 | 多场景切换 | 🟢🌐 |

---

## 六、PC/Web 端功能

### 6.1 宏编辑器

| ID | 原子功能 | 描述 |
|---|---|---|
| WEB-001 | 可视化宏编辑器 | 拖拽式编辑触发器/动作/约束条件 |
| WEB-002 | 触发器配置面板 | 配置每个触发器的参数 |
| WEB-003 | 动作配置面板 | 配置每个动作的参数 |
| WEB-004 | 约束条件配置面板 | 配置每个约束的参数 |
| WEB-005 | 控制流可视化 | If/Loop/Block 以流程图形式展示 |
| WEB-006 | 变量管理器 | 创建/编辑/删除变量 |
| WEB-007 | 表达式编辑器 | 支持自动补全的表达式输入 |
| WEB-008 | 宏测试/模拟 | 在 Web 端模拟宏执行（仅逻辑部分） |
| WEB-009 | 宏列表管理 | 查看/搜索/排序/过滤所有宏 |
| WEB-010 | 分类管理 | 创建/编辑/删除宏分类 |
| WEB-011 | 自定义场景编辑器 | 拖拽式 UI 场景设计器 |
| WEB-012 | 宏模板浏览 | 浏览和应用宏模板 |

### 6.2 账户与同步

| ID | 原子功能 | 描述 |
|---|---|---|
| SYNC-001 | Google 账号登录 | OAuth 2.0 认证 |
| SYNC-002 | 宏数据云同步 | Android ↔ Cloud ↔ Web 双向同步 |
| SYNC-003 | 变量数据同步 | 全局变量同步 |
| SYNC-004 | 自定义场景同步 | 场景定义同步 |
| SYNC-005 | 设置同步 | 应用设置同步 |
| SYNC-006 | 冲突解决 | 多端同时编辑时的冲突处理策略 |
| SYNC-007 | 离线编辑 | 离线编辑，联网后自动同步 |
| SYNC-008 | 增量同步 | 仅同步变更部分，节约带宽 |
| SYNC-009 | 同步状态指示 | 显示同步状态（已同步/同步中/冲突） |

---

## 七、Android 端特有功能

### 7.1 后台服务

| ID | 原子功能 | 描述 | 标记 |
|---|---|---|---|
| AND-001 | 常驻通知服务 | 前台服务保活 | 🟢 |
| AND-002 | Accessibility Service | 无障碍服务（UI 自动化核心） | 🟡 |
| AND-003 | Notification Listener | 通知监听服务 | 🟡 |
| AND-004 | Device Admin | 设备管理员（锁屏/擦除等） | 🟡 |
| AND-005 | 事件广播接收 | 监听系统广播 | 🟢 |
| AND-006 | 内置 HTTP Server | 接收外部 HTTP 请求 | 🟢 |
| AND-007 | 位置服务 | 前台/后台位置获取 | 🟢🔴 |
| AND-008 | 传感器服务 | 持续监听传感器数据 | 🟢 |

### 7.2 权限管理

| ID | 原子功能 | 描述 | 标记 |
|---|---|---|---|
| PERM-001 | 运行时权限请求 | 动态请求各种权限 | 🟢 |
| PERM-002 | Shizuku/ADB 权限 | 通过 Shizuku 获取系统级权限 | 🟡 |
| PERM-003 | Root 权限 | Root 权限操作 | 🟡 |
| PERM-004 | 特殊权限引导 | 引导用户开启无障碍、通知等权限 | 🟢 |
| PERM-005 | 电池优化白名单 | 引导用户关闭电池优化 | 🟢 |

### 7.3 桌面组件

| ID | 原子功能 | 描述 | 标记 |
|---|---|---|---|
| WIDGET-001 | 桌面小部件（按钮） | 可配置按钮小部件 | 🟢 |
| WIDGET-002 | Quick Settings Tile | 快速设置磁贴（最多 N 个） | 🟢 |
| WIDGET-003 | App Shortcut | 长按图标快捷方式 | 🟢 |
| WIDGET-004 | 悬浮按钮 | 全局悬浮触发按钮 | 🟢 |

---

## 八、设计系统与 UX 交互

> **设计方向**：iOS 风格简约设计（Clean, Minimal, Elegant），统一应用于软件原生界面和用户自定义场景。

### 8.1 设计语言

| ID | 原子功能 | 描述 |
|---|---|---|
| DES-001 | iOS 风格视觉体系 | 大圆角卡片、毛玻璃/模糊背景、轻投影、留白充足 |
| DES-002 | 色彩系统 | 主色调 + 语义色（成功/警告/错误/信息），支持深色/浅色模式自适应 |
| DES-003 | 字体排版 | 层级分明的字号体系，SF Pro 风格无衬线字体 |
| DES-004 | 图标系统 | 线性图标为主，Rounded 风格，一致的线宽和视觉重量 |
| DES-005 | 间距系统 | 4px 基础网格，8/12/16/24/32/48 常用间距 |
| DES-006 | 动画系统 | 弹性曲线(Spring)动画、转场动画、列表动画，60fps 流畅 |

### 8.2 应用原生 UI

| ID | 原子功能 | 描述 |
|---|---|---|
| NUI-001 | 首页/宏列表 | 卡片式宏列表，左滑操作，分类 Tab/筛选 |
| NUI-002 | 宏编辑器 | 分步式编辑流程：触发器 → 动作 → 约束，清晰的层级结构 |
| NUI-003 | 触发器/动作选择器 | 分类浏览 + 搜索，带图标和简要描述 |
| NUI-004 | 参数配置面板 | Bottom Sheet 风格，表单元素统一设计 |
| NUI-005 | 变量管理界面 | 变量列表 + 类型标签 + 实时值预览 |
| NUI-006 | 日志查看器 | 时间线式日志，可过滤/搜索 |
| NUI-007 | 设置界面 | iOS Settings 风格分组列表 |
| NUI-008 | 引导流程 | 首次使用引导：权限申请 → 功能介绍 → 示例宏 |
| NUI-009 | 空状态设计 | 每个空列表都有插画 + 引导文案 |
| NUI-010 | 手势交互 | 左滑删除/右滑启用、长按拖拽排序、下拉刷新 |

### 8.3 用户自定义场景设计能力

| ID | 原子功能 | 描述 | 标记 |
|---|---|---|---|
| DES-SCENE-001 | 预置 iOS 风格主题 | 用户创建场景时可选简约白/深邃黑/毛玻璃等预置主题 | 🟢🌐 |
| DES-SCENE-002 | 组件样式预设 | 按钮/标签/输入框等提供 iOS 风格默认样式 | 🟢🌐 |
| DES-SCENE-003 | 动画预设 | 淡入淡出/弹性/滑动等预置动画效果 | 🟢🌐 |
| DES-SCENE-004 | 深色模式自适应 | 场景自动跟随系统深色/浅色模式 | 🟢🌐 |
| DES-SCENE-005 | 模糊背景效果 | 悬浮窗场景支持毛玻璃背景 | 🟢 |
| DES-SCENE-006 | 自适应布局 | 场景组件根据屏幕尺寸自适应调整 | 🟢🌐 |

---

## 九、数据模型定义

### 8.1 宏（Macro）数据结构

```
Macro {
  id: UUID
  name: String
  description: String
  category_id: UUID?
  enabled: Boolean
  triggers: [TriggerConfig]
  actions: [ActionConfig]
  constraints: [ConstraintConfig]
  local_variables: [Variable]
  created_at: Timestamp
  updated_at: Timestamp
  last_run_at: Timestamp?
  run_count: Int
  is_template: Boolean
  version: Int  // 用于同步冲突检测
}
```

### 8.2 触发器配置

```
TriggerConfig {
  id: UUID
  type: TriggerType  // 枚举，对应 T-xxx-xxx
  enabled: Boolean
  params: Map<String, Any>  // 每种触发器的具体参数
  constraints: [ConstraintConfig]  // 触发器级约束
}
```

### 8.3 动作配置

```
ActionConfig {
  id: UUID
  type: ActionType  // 枚举，对应 A-xxx-xxx
  params: Map<String, Any>
  constraints: [ConstraintConfig]  // 动作级约束
  block_next: Boolean  // 是否阻塞下一个动作
  // 控制流动作特有
  children: [ActionConfig]?  // If/Loop 的子动作
  else_children: [ActionConfig]?  // If 的 else 分支
}
```

### 8.4 约束配置

```
ConstraintConfig {
  id: UUID
  type: ConstraintType  // 枚举，对应 C-xxx-xxx
  inverted: Boolean  // 取反
  params: Map<String, Any>
  // 逻辑组合
  logic_type: AND | OR | XOR | NOT  // 仅用于 C-INT-013
  children: [ConstraintConfig]?
}
```

### 8.5 变量

```
Variable {
  id: UUID
  name: String
  type: BOOLEAN | INTEGER | DECIMAL | STRING | DICTIONARY | ARRAY
  value: Any
  scope: GLOBAL | LOCAL
  persisted: Boolean
}
```

---

## 十、统计总览

| 分类 | 数量 |
|---|---|
| 引擎核心功能 | 36 项 |
| 触发器（Triggers） | 83 项 |
| 动作（Actions） | 128 项 |
| 约束条件（Constraints） | 59 项 |
| 自定义 UI 组件 | 15 项 |
| 设计系统与 UX | 22 项 |
| PC/Web 端功能 | 21 项 |
| Android 端特有功能 | 14 项 |
| **总计** | **378 项** |

### 需本地化适配（🔴）汇总

| ID | 功能 | 本地化方案 |
|---|---|---|
| T-TIME-006 | 日出/日落 | 使用天文算法本地计算，不依赖 Google API |
| T-SENSOR-001 | Activity Recognition | 使用 Android 原生 ActivityRecognitionClient 或国内 SDK |
| T-SENSOR-002 | 天气 | 使用和风天气/彩云天气 API 替代 |
| T-LOC-001 | 地理围栏 | 使用高德/百度地图 SDK 的 GeoFence API |
| T-LOC-002 | 位置触发 | 同上 |
| C-TIME-005 | 日出/日落约束 | 同 T-TIME-006 |
| C-LOC-001 | 地理围栏约束 | 同 T-LOC-001 |
| A-DATA-017 | 翻译文本 | 使用百度翻译/有道翻译 API |
| A-SCR-012 | OCR | 使用 ML Kit 或百度 OCR |
| A-NET-008 | 分享位置 | 使用高德/百度坐标系 |
| A-SYS-003/004 | 位置相关 | 使用国内定位 SDK |
| AND-007 | 后台位置 | 使用高德/百度后台定位 |
