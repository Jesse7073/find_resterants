import apiClient from './client'

export interface RestaurantSearchParams {
  keyword?: string
  city?: string
  publicTagIds?: number[]
  page?: number
  size?: number
}

export function searchRestaurants(params: RestaurantSearchParams) {
  return apiClient.get('/restaurants', { params })
}

export function getRestaurant(id: number) {
  return apiClient.get(`/restaurants/${id}`)
}

export function parseRestaurantText(rawText: string) {
  return apiClient.post<{ name: string; address: string }>('/restaurants/parse', { rawText })
}

export function createRestaurant(payload: { name: string; address: string; category?: string; note?: string }) {
  return apiClient.post('/restaurants', payload)
}

export function updateRestaurant(id: number, payload: { name?: string; address?: string; category?: string; note?: string }) {
  return apiClient.put(`/restaurants/${id}`, payload)
}

export function deleteRestaurant(id: number) {
  return apiClient.delete(`/restaurants/${id}`)
}
