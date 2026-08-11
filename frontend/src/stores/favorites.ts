import { defineStore } from 'pinia'
import { getFavorites, type FavoriteQueryParams } from '../api/favoriteApi'

export const useFavoritesStore = defineStore('favorites', {
  state: () => ({
    items: [] as unknown[],
  }),
  actions: {
    async fetch(params: FavoriteQueryParams = {}) {
      const { data } = await getFavorites(params)
      this.items = data
    },
  },
})
