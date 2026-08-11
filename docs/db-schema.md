# Database Schema — 找餐廳網站

> SDLC Phase 3 步驟 12 產出。資料庫：PostgreSQL（單一 datasource，無跨資料庫一致性需求）。

## ER 圖

```
users ──1───N── restaurants (created_by)
users ──1───N── favorites
restaurants ──1───N── favorites
favorites ──1───1── notes
users ──1───N── private_tags
favorites ──N───M── private_tags   (透過 favorite_tags)
public_tags（由開發者透過 migration/種子資料建立，非使用者關聯）
restaurants ──N───M── public_tags  (透過 restaurant_public_tags，記錄 applied_by)
```

## 資料表定義

### users
| 欄位 | 型別 | 規則 |
|---|---|---|
| id | BIGSERIAL PK | |
| email | VARCHAR(255) | UNIQUE, NOT NULL |
| password_hash | VARCHAR(255) | NOT NULL（bcrypt） |
| created_at | TIMESTAMP | NOT NULL, default now() |

### restaurants（全站共享）
| 欄位 | 型別 | 規則 |
|---|---|---|
| id | BIGSERIAL PK | |
| name | VARCHAR(255) | NOT NULL |
| address | VARCHAR(500) | NOT NULL |
| category | VARCHAR(100) | NULLABLE |
| note | TEXT | NULLABLE（新增時的公開備註，非個人筆記） |
| created_by | BIGINT FK → users.id | NOT NULL |
| created_at | TIMESTAMP | NOT NULL, default now() |
| updated_at | TIMESTAMP | NOT NULL, default now() |
| deleted_at | TIMESTAMP | NULLABLE（軟刪除標記，非 NULL 代表已下架） |

索引：`name`, `address` 建立 index 供關鍵字搜尋與相似度比對（新增時的重複提示）使用。查重比對是本版**唯一**防止重複資料的機制（不串接 Google Maps 等付費地點 API），新增餐廳時 Service 層需依此索引比對既有餐廳的店名+地址相似度。

### favorites（口袋名單 / 使用者—餐廳收藏關聯）
| 欄位 | 型別 | 規則 |
|---|---|---|
| id | BIGSERIAL PK | |
| user_id | BIGINT FK → users.id | NOT NULL |
| restaurant_id | BIGINT FK → restaurants.id | NOT NULL |
| created_at | TIMESTAMP | NOT NULL, default now() |

約束：`UNIQUE(user_id, restaurant_id)` — 避免同一使用者重複收藏同一餐廳。

### private_tags（私人標籤定義）
| 欄位 | 型別 | 規則 |
|---|---|---|
| id | BIGSERIAL PK | |
| user_id | BIGINT FK → users.id | NOT NULL（標籤擁有者） |
| name | VARCHAR(50) | NOT NULL |
| created_at | TIMESTAMP | NOT NULL, default now() |

約束：`UNIQUE(user_id, name)` — 同一使用者標籤名稱不可重複。

### favorite_tags（私人標籤套用在收藏關聯上）
| 欄位 | 型別 | 規則 |
|---|---|---|
| id | BIGSERIAL PK | |
| favorite_id | BIGINT FK → favorites.id | NOT NULL, ON DELETE CASCADE |
| private_tag_id | BIGINT FK → private_tags.id | NOT NULL, ON DELETE CASCADE |

約束：`UNIQUE(favorite_id, private_tag_id)`。刪除 `private_tags` 或 `favorites` 時，對應關聯記錄一併刪除（CASCADE）。

### public_tags（公開標籤定義，全站共用字典）
| 欄位 | 型別 | 規則 |
|---|---|---|
| id | BIGSERIAL PK | |
| name | VARCHAR(50) | UNIQUE, NOT NULL |
| created_at | TIMESTAMP | NOT NULL, default now() |

**管理方式**：本版不開放任何使用者（含一般使用者）透過 API 新增公開標籤，清單完全由開發者透過 Flyway migration 的 seed script（如 `V2__seed_public_tags.sql`）維護。使用者只能從既有清單「套用」標籤到餐廳，不能建立新的公開標籤。因此不需要 `created_by` 欄位，也不需要 API 層的標籤名稱過濾規則（開發者自行把關內容品質）。

### restaurant_public_tags（公開標籤套用在餐廳上）
| 欄位 | 型別 | 規則 |
|---|---|---|
| id | BIGSERIAL PK | |
| restaurant_id | BIGINT FK → restaurants.id | NOT NULL, ON DELETE CASCADE |
| public_tag_id | BIGINT FK → public_tags.id | NOT NULL, ON DELETE CASCADE |
| applied_by | BIGINT FK → users.id | NOT NULL（用於權限判斷：僅能移除自己套用的關聯，以及未來可能的濫用追蹤） |
| created_at | TIMESTAMP | NOT NULL, default now() |

約束：`UNIQUE(restaurant_id, public_tag_id)` — 同一標籤不會在同一餐廳重複套用。

### notes（個人筆記/評分）
| 欄位 | 型別 | 規則 |
|---|---|---|
| id | BIGSERIAL PK | |
| favorite_id | BIGINT FK → favorites.id | NOT NULL, UNIQUE, ON DELETE CASCADE（一筆收藏對應一筆筆記） |
| content | TEXT | NULLABLE |
| rating | SMALLINT | NULLABLE, CHECK (rating BETWEEN 1 AND 5) |
| updated_at | TIMESTAMP | NOT NULL, default now() |

## 刪除規則（對應 `requirements-spec.md` 4.10）

- **`restaurants` 採軟刪除**：刪除餐廳時只更新 `deleted_at`，不做實體刪除，不 CASCADE 移除 `favorites`。其他使用者已收藏的餐廳不受影響、仍會出現在其口袋名單中，但需標示「已下架」狀態（前端依 `deleted_at` 是否為 NULL 判斷）。全站搜尋/瀏覽（`GET /restaurants`）預設排除已下架餐廳。
- 刪除 `favorites` → CASCADE 刪除 `favorite_tags`、`notes`（此為使用者主動「移除收藏」的正常行為，非餐廳被下架導致，兩者是獨立事件）
- 刪除 `private_tags` → CASCADE 刪除 `favorite_tags` 中的對應關聯
- `public_tags` 由開發者透過 migration 管理，不開放 API 刪除，不涉及一般使用者操作的 CASCADE 情境

## Migration 計畫

- 全新專案，無既有資料需要遷移，第一版直接以 Flyway migration script（`V1__init_schema.sql`）建立上述所有表
- `V2__seed_public_tags.sql` 建立初始公開標籤清單（開發者維護的種子資料）
- 後續版本異動一律新增 Flyway migration script（`V3__...`），不得修改已套用的舊 script；新增/調整公開標籤走同一套 migration 流程
- 資料量評估：MVP 初期資料量小（個位數~數百筆），schema migration 可在部署時直接執行，無需 downtime 規劃
- 回滾方案：部署前備份資料庫快照；Flyway 本身不自動產生 down script，若需回滾以還原快照為主要手段（規模小、風險可控）
