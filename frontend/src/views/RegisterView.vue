<script setup lang="ts">
// T-12：註冊表單，串接 authStore.register()（見 docs/phase3-design.md）
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import axios from 'axios'

const email = ref('')
const password = ref('')
const errorMessage = ref('')
const isSubmitting = ref(false)

const router = useRouter()
const authStore = useAuthStore()

async function handleSubmit() {
  errorMessage.value = ''
  isSubmitting.value = true
  try {
    await authStore.register(email.value, password.value)
    router.push({ path: '/login', query: { registered: '1' } })
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.data?.message) {
      errorMessage.value = error.response.data.message
    } else {
      errorMessage.value = '註冊失敗，請稍後再試'
    }
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <section class="auth-page">
    <h1>註冊</h1>
    <form @submit.prevent="handleSubmit">
      <label for="register-email">Email</label>
      <input id="register-email" v-model="email" type="email" required autocomplete="email" />

      <label for="register-password">密碼</label>
      <input id="register-password" v-model="password" type="password" required autocomplete="new-password" />

      <p v-if="errorMessage" class="error-message" role="alert">{{ errorMessage }}</p>

      <button type="submit" :disabled="isSubmitting">{{ isSubmitting ? '註冊中...' : '註冊' }}</button>
    </form>
    <p class="auth-switch">
      已經有帳號？<router-link to="/login">前往登入</router-link>
    </p>
  </section>
</template>

<style scoped>
.auth-page {
  max-width: 360px;
  margin: 0 auto;
  padding: 24px 20px;
  text-align: left;
}

form {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

label {
  font-size: 14px;
  color: var(--text-h);
}

input {
  padding: 8px 10px;
  border: 1px solid var(--border);
  border-radius: 4px;
  font: inherit;
  margin-bottom: 12px;
}

button {
  margin-top: 8px;
  padding: 10px;
  border: none;
  border-radius: 4px;
  background: var(--accent);
  color: #fff;
  font: inherit;
  cursor: pointer;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-message {
  color: #d33;
  margin: 0 0 12px;
}

.auth-switch {
  margin-top: 16px;
}
</style>
