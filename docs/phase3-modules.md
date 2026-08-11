# Module Breakdown — 找餐廳網站

> **SDLC Phase 3 — 設計**（步驟 15 產出）

## 後端模組（Spring Boot package 劃分）

| 模組 | 職責 | 依賴 |
|---|---|---|
| `auth` | 註冊、登入、JWT 簽發/驗證 | 無（基礎模組） |
| `restaurant` | 餐廳 CRUD、複製文字解析、重複偵測、全站搜尋 | `auth`（取得目前使用者） |
| `favorite` | 口袋名單、收藏關聯 CRUD | `auth`, `restaurant` |
| `tag` | 私人標籤（依附 `favorite`）與公開標籤（依附 `restaurant`），內部依 domain 拆 `PrivateTagService` / `PublicTagService` | `auth`, `restaurant`, `favorite` |
| `note` | 個人筆記/評分 | `auth`, `favorite` |
| `common` | 統一錯誤處理、分頁工具、DTO 基礎類別 | 無（被所有模組依賴） |

**模組邊界規則**：`restaurant` 是基礎資料模組，不依賴 `favorite`/`tag`/`note`，避免循環依賴；`common` 為橫向共用模組，不依賴任何業務模組。

## 前端模組（依 Vue 專案慣例）

| 目錄 | 內容 |
|---|---|
| `views/` | 頁面元件（見 `docs/phase3-components.md`） |
| `components/` | 可重用元件 |
| `stores/` | Pinia store：`authStore`, `favoritesStore`, `tagsStore` |
| `api/` | API client 封裝，對應後端模組：`authApi`, `restaurantApi`, `favoriteApi`, `tagApi`, `noteApi` |
| `router/` | 路由設定與 auth guard |

## 依賴關係圖（後端）

```
common ◀── auth ◀── restaurant ◀── favorite ◀── tag
                                        ▲          ▲
                                        └── note ───┘
```

無循環依賴。
