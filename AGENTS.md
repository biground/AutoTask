# AGENTS.md — AutoPilot 项目规范

## 项目名称
AutoPilot — Android 自动化引擎 + PC/Web 宏编辑器

## 技术栈
- **Android**: Kotlin + Jetpack Compose（仅适配 Android 16）
- **PC/Web**: React + TypeScript
- **后端**: Firebase（Auth + Firestore + Cloud Functions）
- **宏数据格式**: JSON

## 基线项目
- 基于开源项目 [AutoTask](https://github.com/biground/AutoTask) (Apache-2.0) Fork 改进
- AutoTask 提供成熟的引擎核心（tasker-engine、控制流、Applet 插件架构、DTO 序列化）
- AutoTask 提供 UI 自动化能力（Shizuku + Accessibility 双模式）
- 差距分析见 `docs/gap-analysis.md`

## 架构决策
- 引擎核心在 Android 端原生运行，PC/Web 端仅做编辑器
- 地理围栏/定位使用高德地图 SDK（国内适配），必须用合并包 `3dmap-location-search`
- 天气 API 使用和风天气或彩云天气
- OCR 使用 ML Kit（离线优先）+ CloudOcrProvider（云端扩展）
- 翻译使用百度翻译或有道翻译 API
- Applet 注册链路: `*Registry` → `BootstrapOptionRegistry` → `AppletOptionFactory`
- 事件分发链路: `EventDispatcher` → `EventFilter` → `*Referent`
- 数据持久化: `DataStore Preferences` + JSON 序列化

## 命名规范
- 触发器 ID: `T-{CATEGORY}-{NNN}` (如 T-TIME-001)
- 动作 ID: `A-{CATEGORY}-{NNN}` (如 A-SET-001)
- 约束 ID: `C-{CATEGORY}-{NNN}` (如 C-DEV-001)
- 功能文档见 `docs/feature-taxonomy.md`
- PRD 见 `docs/PRD.yaml`

## 关键文档
- `docs/feature-taxonomy.md` — 全量 378 项原子功能分类体系
- `docs/PRD.yaml` — 产品需求文档
- `docs/gap-analysis.md` — AutoTask → AutoPilot 差距分析
- `docs/roadmap.md` — MVP → V1 → V2 实施路线图
- `docs/PROGRESS.md` — 开发进度、构建知识、已完成模块汇总
