# Frontend Components — 找餐廳網站

> SDLC Phase 3 步驟 14 產出。Vue 3 + TypeScript + Vite + Pinia + Vue Router。

## 頁面清單（Views）

| 頁面 | 路由 | 需登入 | 說明 |
|---|---|---|---|
| LoginView | `/login` | 否 | 登入表單 |
| RegisterView | `/register` | 否 | 註冊表單 |
| SearchView | `/`（首頁） | 否（部分功能需登入） | 關鍵字搜尋 + 標籤/地區篩選 + 列表/地圖切換檢視 |
| RestaurantDetailView | `/restaurants/:id` | 否（收藏/標籤/筆記操作需登入） | 顯示店名、地址、公開標籤；已登入且已收藏時顯示私人標籤與筆記入口 |
| AddRestaurantView | `/restaurants/new` | 是 | 貼上文字 → 呼叫解析 API 預填 → 確認送出 |
| FavoritesView | `/favorites` | 是 | 口袋名單列表，依私人/公開標籤、地區篩選排序 |
| TagManagementView | `/tags` | 是 | 私人標籤 CRUD |

## 可重用元件（Components）

| 元件 | 可重用範圍 | 說明 |
|---|---|---|
| `RestaurantCard` | SearchView, FavoritesView | 餐廳列表項目卡片，顯示店名/地址縮寫/標籤 |
| `TagChip` | RestaurantDetailView, RestaurantCard, TagManagementView | 單一標籤顯示，依 props 區分私人/公開樣式 |
| `TagSelector` | AddRestaurantView（套用標籤）、RestaurantDetailView | 標籤選擇下拉元件；依 props 切換資料源——私人標籤模式支援「新增」，公開標籤模式僅能從既有清單「套用」，不提供新增入口 |
| `MapView` | SearchView, FavoritesView | Leaflet 地圖元件，接受餐廳陣列 props 顯示圖釘，點擊圖釘觸發導航至詳情頁 |
| `FilterPanel` | SearchView, FavoritesView | 標籤/地區/關鍵字篩選面板 |
| `NoteEditor` | RestaurantDetailView | 個人筆記/評分編輯元件 |
| `AuthGuardNotice` | 任一需登入但未登入頁面 | 提示「請先登入」並導向 LoginView |

## 狀態管理（Pinia Stores）

- `authStore`：JWT token、目前登入使用者資訊、登入/登出 action
- `favoritesStore`：目前使用者口袋名單快取
- `tagsStore`：私人標籤清單、公開標籤清單快取

## 路由與權限

- Vue Router 設定 `meta: { requiresAuth: true }` 於 AddRestaurantView、FavoritesView、TagManagementView
- Global route guard（`router.beforeEach`）檢查 `authStore` 是否有有效 token，無則導向 `/login` 並保留原目標路徑（登入後導回）

## 頁面組成藍圖 — 涵蓋所有使用情境

**情境一（收藏）**：使用者在 IG/Google Maps 看到餐廳 → 複製文字 → 開啟 `AddRestaurantView`（未登入則先導向 `LoginView`）→ 貼上文字 → 呼叫 `/restaurants/parse` 預填 → 確認/修改欄位 → 送出 `/restaurants` → 若有 `duplicateCandidates` 顯示提示 → 導向 `RestaurantDetailView` 並可直接加入口袋名單（呼叫 `/favorites`）

**情境二（查詢）**：使用者開啟 `SearchView` → 輸入關鍵字或透過 `FilterPanel` 選標籤/地區 → 切換 `列表` 或 `地圖`（`MapView`）檢視 → 點擊 `RestaurantCard` 或地圖圖釘 → 進入 `RestaurantDetailView` → 可加入口袋名單、（若已收藏）套用私人標籤、撰寫筆記

**標籤整理情境**：使用者於 `TagManagementView` 建立/刪除私人標籤，或於 `RestaurantDetailView`/`FavoritesView` 直接透過 `TagSelector` 套用既有標籤
