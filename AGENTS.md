# AGENTS.md — AutoPilot 项目规范

## 项目名称
AutoPilot — Android 自动化引擎 + PC/Web 宏编辑器

## 技术栈
- **Android**: Kotlin + Jetpack Compose（仅适配 Android 16）
- **PC/Web**: React + TypeScript
- **后端**: Firebase（Auth + Firestore + Cloud Functions）
- **宏数据格式**: JSON

## 架构决策
- 引擎核心在 Android 端原生运行，PC/Web 端仅做编辑器
- 地理围栏/定位使用高德地图 SDK（国内适配）
- 天气 API 使用和风天气或彩云天气
- OCR 使用 ML Kit 或百度 OCR
- 翻译使用百度翻译或有道翻译 API

## 命名规范
- 触发器 ID: `T-{CATEGORY}-{NNN}` (如 T-TIME-001)
- 动作 ID: `A-{CATEGORY}-{NNN}` (如 A-SET-001)
- 约束 ID: `C-{CATEGORY}-{NNN}` (如 C-DEV-001)
- 功能文档见 `docs/feature-taxonomy.md`
- PRD 见 `docs/PRD.yaml`

## 关键文档
- `docs/feature-taxonomy.md` — 全量 356 项原子功能分类体系
- `docs/PRD.yaml` — 产品需求文档
