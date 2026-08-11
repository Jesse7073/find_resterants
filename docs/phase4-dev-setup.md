# Dev Setup — 找餐廳網站

> **SDLC Phase 4 — 開發前準備**（步驟 18-20）
> 承接 `docs/phase3-design.md` 的任務清單。

## 1. 可運行的專案骨架

### 前端（`frontend/`）
- Vue 3 + TypeScript + Vite，已安裝 Vue Router、Pinia、axios（對應 `docs/phase3-architecture.md` 技術選型）
- 目錄結構對應 `docs/phase3-components.md` / `docs/phase3-modules.md`：`src/views`、`src/components`、`src/stores`、`src/api`、`src/router`
- 已建立所有頁面的路由 stub（含 `meta.requiresAuth` + `router.beforeEach` guard）與對應的 API client 封裝
- **驗證**：`npm run build` 執行成功（含 TypeScript 型別檢查），2026-08-11 已實測通過

### 後端（`backend/`）
- Spring Boot 4.1.0（Java 21）+ Web、Data JPA、PostgreSQL Driver、Security、Validation、Flyway，透過 Spring Initializr 產生
- 套件結構對應 `docs/phase3-modules.md`：`auth`、`restaurant`、`favorite`、`tag`、`note`、`common`（目前為空模組，等 Phase 5 依票號填入實作）
- Flyway migration 已依 `docs/phase3-db-schema.md` 寫成 `V1__init_schema.sql`（建表）與 `V2__seed_public_tags.sql`（公開標籤種子資料）
- `application.properties` 已設定資料庫連線與 JWT 密鑰**透過環境變數帶入**（`DB_URL`/`DB_USERNAME`/`DB_PASSWORD`/`JWT_SECRET`），本機開發有預設值但正式環境必須覆蓋，機敏資訊不寫死在檔案或提交進版控
- **驗證**：`mvn compile` 執行成功（使用 IntelliJ 已下載的 `ms-21.0.8` JDK），2026-08-11 已實測通過

### 已知未驗證項目（留待 Phase 5 開始前處理）
- **尚未啟動過完整應用程式**：`mvn compile` 只驗證原始碼可編譯，還沒有實際跑 `spring-boot:run` 連線到真正的 PostgreSQL 資料庫做端到端驗證，因為本機目前沒有跑起來的 PostgreSQL 服務。建議在 Phase 5 動工前，先用 Docker 或本機安裝的方式啟動一個 PostgreSQL 實例，跑一次 Flyway migration 確認 schema 正確套用
- 這台機器沒有系統層級安裝 JDK/Maven 在 PATH 上，目前是借用 IntelliJ 內建下載的 `C:\Users\jessemeng\.jdks\ms-21.0.8`。若要用命令列（非 IntelliJ）開發，建議之後設定 `JAVA_HOME` 環境變數指向此路徑，或請 IntelliJ 直接開啟 `backend/pom.xml` 讓 IDE 自行管理

## 2. 版控清單（僅列清單，不自動執行）

現況：Git repo 已存在，目前在 `dev` 分支，`master` 為主分支；`docs/` 各階段文件與本次骨架皆由你自己陸續 commit 到 `dev`。

### 建議分支策略（Phase 5 起）
- 每張 `docs/phase3-design.md` 的票號對應一個 feature branch
- **命名規則**：`feature/T-XX-簡短描述`（例如 `feature/T-02-auth-jwt`、`feature/T-13-add-restaurant-page`）
- **目標 MR**：feature branch 完工後開 PR 合併回 `dev`；`dev` 穩定、Phase 6 測試通過後再合併回 `master` 才視為正式版本
- 文件類變更（`docs/` 下的 SDLC 產出物）可維持目前直接 commit 到 `dev` 的作法，不強制開 branch

### 待你自行執行的動作（Lead 不自動化）
- [ ] 建立 `.gitignore`（專案根目錄，目前只有 `frontend/`、`backend/` 各自的 `.gitignore`，可視需要在根目錄加一份彙整或維持現狀）
- [ ] 將本次新增的 `frontend/`、`backend/`、`docs/phase4-dev-setup.md` 等檔案 `git add` + `git commit`
- [ ] 之後每張票開工前，自行 `git checkout -b feature/T-XX-描述`

## 3. 開發排程（依 `docs/phase3-design.md` 任務清單分組排序，不設定時間尺度）

| 順序 | 里程碑 | 涵蓋票號 | 說明 |
|---|---|---|---|
| M1 | 專案初始化 | T-01, T-11 | 已於本階段完成（骨架建立+驗證） |
| M2 | 帳號系統 | T-02, T-12 | 註冊/登入打通，是後續所有個人化功能的前提 |
| M3 | 餐廳核心 | T-03, T-04, T-13 | 餐廳 CRUD + 文字解析 + 新增餐廳頁，讓資料庫開始有真實資料可測 |
| M4 | 收藏與標籤 | T-05, T-06, T-07, T-17, T-18 | 口袋名單 + 私人/公開標籤，前後端一起做（後端 API 先，前端頁面接續） |
| M5 | 詳情與筆記 | T-08, T-14, T-19 | 餐廳詳情頁整合標籤/收藏/筆記，屬跨模組整合票 |
| M6 | 搜尋與地圖 | T-09, T-15, T-16 | 全站搜尋 API + 搜尋頁 + 地圖元件，是「查詢端」情境的完整體驗 |
| M7 | 收尾檢查 | T-10 | 統一錯誤處理格式，需等前面票大致完成、有實際錯誤情境可核對後再收斂 |

此順序依 `docs/phase3-design.md` 的票號依賴關係排列（後端先於對應前端票、基礎模組先於依賴它的模組），未設定實際時程，待你評估投入節奏後自行填入時間表。
