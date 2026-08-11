import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi } from '../api/authApi'

interface AuthState {
  token: string | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: null,
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
  },
  actions: {
    // token 僅存於記憶體（Pinia state），重新整理頁面需重新登入，取捨理由見 docs/phase3-architecture.md
    async login(email: string, password: string) {
      const { data } = await loginApi(email, password)
      this.token = data.token
    },
    async register(email: string, password: string) {
      await registerApi(email, password)
    },
    logout() {
      this.token = null
    },
  },
})
