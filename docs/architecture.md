# System Architecture — 找餐廳網站

> SDLC Phase 3 步驟 11 產出。

## 架構型態：前後端分離

```
┌─────────────────────┐        REST API (JSON, JWT)        ┌──────────────────────────┐
│   Vue 3 + TS SPA     │ ──────────────────────────────────▶│  Spring Boot (Java 21)   │
│   (瀏覽器)            │◀──────────────────────────────────  │  REST API Server          │
└─────────┬────────────┘                                     └────────────┬─────────────┘
          │ 直接請求（前端瀏覽器 → 第三方服務，不經後端）                        │
          ▼                                                               ▼
┌─────────────────────┐                                     ┌──────────────────────────┐
│ OpenStreetMap Tile   │                                     │      PostgreSQL          │
│ Server（地圖底圖）     │                                     │      (單一資料庫)          │
└─────────────────────┘                                     └──────────────────────────┘
```

## 技術選型與理由

| 項目 | 選型 | 理由 |
|---|---|---|
| 前端 | Vue 3 + TypeScript（Vite 建置） | 使用者本身熟悉的技術，選用理由是開發效率與可維護性，非陌生技術試驗 |
| 前端狀態管理 | Pinia | Vue 3 官方推薦，管理登入狀態、標籤/收藏快取 |
| 前端路由 | Vue Router | 搭配 route guard 保護需登入頁面 |
| 後端 | Spring Boot（Java 21） | 使用者本身熟悉的技術；生態成熟，Spring Security 對 JWT 有成熟整合方案 |
| 資料庫 | PostgreSQL | 關聯式資料庫，適合本專案多對多關聯（標籤、收藏）的資料模型；未來若要做地理位置查詢有 PostGIS 擴充可選用 |
| ORM | Spring Data JPA / Hibernate | Spring Boot 標準搭配 |
| 資料庫版本控管 | Flyway | Spring Boot 生態常見搭配，版本化 migration script |
| 認證機制 | JWT（Stateless） | 前後端分離架構下的合理選擇；使用者已選定此方案 |
| 地圖底圖 | Leaflet + OpenStreetMap | 完全免費、開源生態成熟，符合「不使用付費地圖 API」的限制（見 `docs/dependencies-risks.md`） |

## 後端分層

- **Controller 層**：REST API 端點，處理請求驗證、序列化
- **Service 層**：商業邏輯（新增餐廳文字解析、標籤套用權限規則、收藏權限判斷等）
- **Repository 層**：Spring Data JPA repository
- **Entity 層**：對應資料庫表

## 外部資源/服務

- **OpenStreetMap Tile Server**：前端瀏覽器直接串接的免費服務（不經後端代理），需遵守其 usage policy（tile 使用量限制、需標示版權）
- 無其他外部付費服務

## 認證流程

1. 使用者於 `/api/v1/auth/login` 送出帳密，後端驗證後簽發 JWT
2. 前端將 JWT 存放於記憶體（Pinia store），並在每次 API 請求的 `Authorization: Bearer <token>` header 帶上
3. **已知風險**：JWT 存放於前端記憶體/localStorage 皆有 XSS 竊取風險；本版採記憶體存放（重新整理頁面需重新登入或搭配 refresh token），不做 httpOnly cookie 方案，原因是維持前後端完全分離、避免 CORS + cookie 的額外複雜度。此取捨列入 `docs/dependencies-risks.md`
4. Token 過期時間、refresh token 機制：本版先用短效 token（如 1 小時）+ 過期後要求重新登入，不做 refresh token（降低複雜度，留待下一輪迭代視使用情況決定是否要加）

## 影響既有模組

無（全新專案，無既有系統）

## 部署/環境

不在本階段決定，留待 `dev-setup` / `release-handoff` 階段處理；架構設計未綁定特定雲端服務或 serverless 限制，維持一般化的容器化可行性（前端靜態檔案 + 後端 JAR + PostgreSQL 三個獨立部署單元）。

## 相關文件

- 資料庫設計：`docs/db-schema.md`
- API 規格：`docs/api-spec.md`
- 前端元件規劃：`docs/components.md`
- 模組劃分：`docs/modules.md`
- 任務清單/開票：`docs/design.md`
