# Handoff Log — 找餐廳網站

> 由 Phase 5-7 共用（implementation-handoff / qa-handoff / release-handoff）。每個 Phase 用固定標頭分節，分節第一行為狀態行。此檔本身不加 phase 前綴。

## Phase 5 — Implementation
狀態：進行中

### 開工前置驗證（2026-08-11）
- 本機 Docker 起了開發用 PostgreSQL 容器 `find-restaurants-postgres`
- 對外埠幾經調整：5432（本機既有原生 PostgreSQL 服務占用）→ 5433（後來發現這個埠使用者自己也另有用途）→ **最終定為 5434**，`application.properties` 的 `DB_URL` 預設值已對應更新
- `spring-boot:run` 成功連線資料庫，Flyway migration 成功套用（`V1__init_schema.sql`、`V2__seed_public_tags.sql`），9 張資料表 + 8 筆公開標籤種子資料皆確認建立正確
- 已知問題：本機 8080 埠被其他服務占用，`spring-boot:run` 的網頁伺服器啟動步驟會失敗，跟資料庫/Flyway 驗證無關，開發時改用其他埠（例如 `--server.port=8099`）繞開即可
- 對應 `docs/phase3-design.md` 任務清單，步驟 21「資料庫建置」的 Output（可用的資料庫）已達成

### 票號進度

| 票號 | 狀態 | 完成日期 | 品質門禁結果 | 備註 |
|---|---|---|---|---|
| T-01 | 完成 | 2026-08-11 | `mvn compile` 通過 | Phase 4 完成的專案初始化 |
| T-11 | 完成 | 2026-08-11 | `npm run build` 通過 | Phase 4 完成的專案初始化 |
| T-02 | 完成，已合併進 `dev`（commit `72a740f`） | 2026-08-12 | `mvn test` 19/19 通過；實際以 curl 打 `/auth/register`、`/auth/login` 驗證 201/409/200/401 四種情境皆正確，password_hash 確認為 bcrypt、`created_at` 正確填值 | 對應 `docs/phase3-design.md` 票號 T-02。分支 `feature/T-02-auth-jwt`（原為 worktree 隔離機制自動命名的 `worktree-agent-aa94d86fed97dd00c`，收尾時改名並 rebase 到 `dev` 後 fast-forward 合併）。驗收時抓到並修正兩個 bug：① `JwtService` 用密鑰原始位元組長度（248 bits）建 HMAC key，本機開發預設密鑰不足 256 bits 導致啟動即噴 `WeakKeyException`，改為 SHA-256 正規化成固定 256-bit key；② `User` entity 的 `created_at`（NOT NULL）沒有 `@PrePersist` 賦值，Hibernate INSERT 帶 NULL 違反資料庫限制，被 `AuthService` 的 catch 區塊誤判成 `DUPLICATE_EMAIL`（導致所有註冊，包括全新 email，都回 409），已補上 `@PrePersist` 修正 |
| T-12 | 完成，已合併進 `dev`（commit `1d5ebe6`） | 2026-08-12 | `npm run build`（含 vue-tsc 型別檢查）通過；透過 vite dev proxy 實際打 `/auth/register`、`/auth/login` 驗證 201/409/200 三種情境正確，`/login`、`/register` 路由回 200 | 對應 `docs/phase3-design.md` 票號 T-12。分支 `feature/T-12-login-register`（原為 worktree 隔離機制自動命名，收尾時改名，因分支已直接接在 `dev` 最新 commit 上不需 rebase，直接 fast-forward 合併）。實作：LoginView/RegisterView 表單 + `vite.config.ts` 加 dev proxy（`/api` → `localhost:8099`，繞開後端未設 CORS 的問題，8099 是本機驗證慣用埠，非正式規格）。未做瀏覽器層級點擊測試（僅 API + 路由層驗證 + 程式碼審查），留意此為證據缺口 |
| T-03 | 待開始 | | | 對應 `docs/phase3-design.md` 票號 T-03（餐廳模組 CRUD），依賴 T-02（已完成） |
