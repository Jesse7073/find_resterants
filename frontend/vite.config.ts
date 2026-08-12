import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    // 後端目前未設定 CORS（見 docs/phase3-design.md，CORS 屬於 release-handoff 階段範圍），
    // 開發期間透過 proxy 讓瀏覽器端請求維持同源，避免被瀏覽器擋下。
    // 8099 為本機驗證 T-02 時使用的埠（本機 8080 被其他服務佔用）。
    proxy: {
      '/api': {
        target: 'http://localhost:8099',
        changeOrigin: true,
      },
    },
  },
})
