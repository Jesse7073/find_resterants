<script setup lang="ts">
// T-12：登入表單，串接 authStore.login()（見 docs/phase3-design.md）
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import axios from 'axios'

const email = ref('')
const password = ref('')
const errorMessage = ref('')
const isSubmitting = ref(false)

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const showRegisteredNotice = route.query.registered === '1'

async function handleSubmit() {
  errorMessage.value = ''
  isSubmitting.value = true
  try {
    await authStore.login(email.value, password.value)
    const redirect = route.query.redirect
    const target = typeof redirect === 'string' && redirect ? redirect : '/'
    router.push(target)
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.data?.message) {
      errorMessage.value = error.response.data.message
    } else {
      errorMessage.value = '登入失敗，請稍後再試'
    }
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <section class="auth-page">
    <h1>登入</h1>
    <p v-if="showRegisteredNotice" class="success-message">註冊成功，請登入</p>
    <form @submit.prevent="handleSubmit">
      <label for="login-email">Email</label>
      <input id="login-email" v-model="email" type="email" required autocomplete="email" />

      <label for="login-password">密碼</label>
      <input id="login-password" v-model="password" type="password" required autocomplete="current-password" />

      <p v-if="errorMessage" class="error-message" role="alert">{{ errorMessage }}</p>

      <button type="submit" :disabled="isSubmitting">{{ isSubmitting ? '登入中...' : '登入' }}</button>
    </form>
    <p class="auth-switch">
      還沒有帳號？<router-link to="/register">前往註冊</router-link>
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

.success-message {
  color: #2a8f4d;
  margin: 0 0 12px;
}

.auth-switch {
  margin-top: 16px;
}
</style>
