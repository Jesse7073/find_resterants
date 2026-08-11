# API Spec — 找餐廳網站

> **SDLC Phase 3 — 設計**（步驟 13 產出）
> Base path：`/api/v1`。全新專案，無既有 client，無向下相容疑慮。

## 錯誤處理格式（統一）

```json
{
  "timestamp": "2026-08-11T10:00:00Z",
  "status": 403,
  "error": "FORBIDDEN",
  "code": "FORBIDDEN_NOT_OWNER",
  "message": "僅原始新增者可編輯此餐廳",
  "path": "/api/v1/restaurants/42"
}
```

業務錯誤碼（例）：`DUPLICATE_EMAIL`、`INVALID_CREDENTIALS`、`FORBIDDEN_NOT_OWNER`、`ALREADY_FAVORITED`、`RESTAURANT_NOT_FOUND`、`PUBLIC_TAG_NOT_FOUND`。

## Auth

| Method | Path | 需登入 | 說明 |
|---|---|---|---|
| POST | `/auth/register` | 否 | Body `{email, password}` → 201 `{id, email}`；email 重複回 409 `DUPLICATE_EMAIL` |
| POST | `/auth/login` | 否 | Body `{email, password}` → 200 `{token, expiresIn}`；帳密錯誤回 401 `INVALID_CREDENTIALS` |

## Restaurants（全站共享）

| Method | Path | 需登入 | 說明 |
|---|---|---|---|
| GET | `/restaurants` | 否 | Query: `keyword, city, publicTagIds[], page, size` → 200 分頁結果；**預設排除已下架（`deletedAt` 不為 null）餐廳**；已登入時每筆標示 `isFavorited` |
| GET | `/restaurants/{id}` | 否 | → 200 `{id, name, address, category, note, publicTags[], createdBy, isFavorited, isDeleted}`；不存在回 404（已下架仍可查詢到，供已收藏使用者查看，`isDeleted:true`） |
| POST | `/restaurants/parse` | 是 | Body `{rawText}` → 200 `{name, address}`（僅解析預填，不寫入資料庫） |
| POST | `/restaurants` | 是 | Body `{name, address, category?, note?}` → 201 `{id, ...}` + `duplicateCandidates[]`（店名+地址高度相似的既有餐廳清單，供前端提示；**這是本版防止重複資料的唯一機制**，使用者可忽略提示強行新增） |
| PUT | `/restaurants/{id}` | 是 | 僅 `createdBy` 本人可操作，否則 403 `FORBIDDEN_NOT_OWNER` |
| DELETE | `/restaurants/{id}` | 是 | 僅 `createdBy` 本人可操作；**軟刪除**（更新 `deletedAt`，不做實體刪除）→ 204；不影響其他使用者的收藏關聯，該餐廳會在他們的口袋名單中標示「已下架」 |

## Favorites（口袋名單）

| Method | Path | 需登入 | 說明 |
|---|---|---|---|
| GET | `/favorites` | 是 | Query: `privateTagIds[], publicTagIds[], city, sort` → 200 目前使用者的口袋名單 |
| POST | `/favorites` | 是 | Body `{restaurantId}` → 201；已收藏過回 409 `ALREADY_FAVORITED` |
| DELETE | `/favorites/{restaurantId}` | 是 | 從口袋名單移除（僅刪除收藏關聯，不影響餐廳本身）→ 204 |

## Private Tags

| Method | Path | 需登入 | 說明 |
|---|---|---|---|
| GET | `/private-tags` | 是 | → 200 自己的私人標籤清單 |
| POST | `/private-tags` | 是 | Body `{name}` → 201；同名回 409 |
| DELETE | `/private-tags/{id}` | 是 | → 204，CASCADE 移除所有套用關聯 |
| POST | `/favorites/{favoriteId}/private-tags` | 是 | Body `{privateTagId}` → 201 套用標籤到該筆收藏 |
| DELETE | `/favorites/{favoriteId}/private-tags/{privateTagId}` | 是 | → 204 移除套用 |

## Public Tags

公開標籤清單由開發者透過資料庫 migration/種子資料維護，**不開放任何新增/編輯/刪除標籤本身的 API**。使用者僅能瀏覽既有清單、套用到餐廳、或移除自己套用的關聯。

| Method | Path | 需登入 | 說明 |
|---|---|---|---|
| GET | `/public-tags` | 否 | → 200 所有公開標籤清單（唯讀） |
| POST | `/restaurants/{restaurantId}/public-tags` | 是 | Body `{publicTagId}` → 201 套用既有標籤（記錄 `appliedBy` = 目前使用者）；`publicTagId` 不存在回 404 `PUBLIC_TAG_NOT_FOUND` |
| DELETE | `/restaurants/{restaurantId}/public-tags/{publicTagId}` | 是 | 僅能移除 `appliedBy` = 自己的套用關聯，否則 403 |

## Notes

| Method | Path | 需登入 | 說明 |
|---|---|---|---|
| GET | `/favorites/{favoriteId}/note` | 是 | → 200 `{content, rating}`；尚未建立回 200 空物件（非 404，因筆記為選填附屬資料） |
| PUT | `/favorites/{favoriteId}/note` | 是 | Body `{content?, rating?}` → 200（新增或更新，upsert 語意） |

## 認證授權共通規則

- 所有標註「需登入」的端點，未帶有效 JWT 一律回 401
- 涉及擁有權判斷（編輯/刪除餐廳、移除自己套用的公開標籤等）由 Service 層依 token 中的 user id 與資料庫紀錄比對，不信任前端傳入的 user id
