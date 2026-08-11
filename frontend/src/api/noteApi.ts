import apiClient from './client'

export function getNote(favoriteId: number) {
  return apiClient.get(`/favorites/${favoriteId}/note`)
}

export function upsertNote(favoriteId: number, payload: { content?: string; rating?: number }) {
  return apiClient.put(`/favorites/${favoriteId}/note`, payload)
}
