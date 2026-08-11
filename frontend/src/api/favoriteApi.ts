import apiClient from './client'

export interface FavoriteQueryParams {
  privateTagIds?: number[]
  publicTagIds?: number[]
  city?: string
  sort?: string
}

export function getFavorites(params: FavoriteQueryParams) {
  return apiClient.get('/favorites', { params })
}

export function addFavorite(restaurantId: number) {
  return apiClient.post('/favorites', { restaurantId })
}

export function removeFavorite(restaurantId: number) {
  return apiClient.delete(`/favorites/${restaurantId}`)
}
