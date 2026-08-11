# Handoff Log — 找餐廳網站

> 由 Phase 5-7 共用（implementation-handoff / qa-handoff / release-handoff）。每個 Phase 用固定標頭分節，分節第一行為狀態行。此檔本身不加 phase 前綴。

## Phase 5 — Implementation
狀態：進行中

### 開工前置驗證（2026-08-11）
- 本機 Docker 起了開發用 PostgreSQL 容器 `find-restaurants-postgres`，對外埠 **5433**（本機 5432 已被既有原生 PostgreSQL 服務占用，避開衝突）
- `spring-boot:run` 成功連線資料庫，Flyway migration 成功套用（`V1__init_schema.sql`、`V2__seed_public_tags.sql`），9 張資料表 + 8 筆公開標籤種子資料皆確認建立正確
- 已知問題：本機 8080 埠被其他服務占用，`spring-boot:run` 的網頁伺服器啟動步驟會失敗，跟資料庫/Flyway 驗證無關，開發時再處理
- 對應 `docs/phase3-design.md` 任務清單，步驟 21「資料庫建置」的 Output（可用的資料庫）已達成

### 票號進度

| 票號 | 狀態 | 完成日期 | 品質門禁結果 | 備註 |
|---|---|---|---|---|
| T-01 | 完成 | 2026-08-11 | `mvn compile` 通過 | Phase 4 完成的專案初始化 |
| T-11 | 完成 | 2026-08-11 | `npm run build` 通過 | Phase 4 完成的專案初始化 |
| T-02 | 待開始 | | | 對應 `docs/phase3-design.md` 票號 T-02 |
