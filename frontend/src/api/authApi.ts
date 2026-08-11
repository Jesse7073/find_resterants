import apiClient from './client'

export interface LoginResponse {
  token: string
  expiresIn: number
}

export function register(email: string, password: string) {
  return apiClient.post('/auth/register', { email, password })
}

export function login(email: string, password: string) {
  return apiClient.post<LoginResponse>('/auth/login', { email, password })
}
