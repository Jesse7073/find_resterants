import apiClient from './client'

// 私人標籤
export function getPrivateTags() {
  return apiClient.get('/private-tags')
}

export function createPrivateTag(name: string) {
  return apiClient.post('/private-tags', { name })
}

export function deletePrivateTag(id: number) {
  return apiClient.delete(`/private-tags/${id}`)
}

export function applyPrivateTag(favoriteId: number, privateTagId: number) {
  return apiClient.post(`/favorites/${favoriteId}/private-tags`, { privateTagId })
}

export function removePrivateTag(favoriteId: number, privateTagId: number) {
  return apiClient.delete(`/favorites/${favoriteId}/private-tags/${privateTagId}`)
}

// 公開標籤（唯讀清單，僅能套用/移除，不可新增）
export function getPublicTags() {
  return apiClient.get('/public-tags')
}

export function applyPublicTag(restaurantId: number, publicTagId: number) {
  return apiClient.post(`/restaurants/${restaurantId}/public-tags`, { publicTagId })
}

export function removePublicTag(restaurantId: number, publicTagId: number) {
  return apiClient.delete(`/restaurants/${restaurantId}/public-tags/${publicTagId}`)
}
