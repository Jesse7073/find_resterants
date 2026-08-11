import { defineStore } from 'pinia'
import { getPrivateTags, getPublicTags } from '../api/tagApi'

export const useTagsStore = defineStore('tags', {
  state: () => ({
    privateTags: [] as unknown[],
    publicTags: [] as unknown[],
  }),
  actions: {
    async fetchPrivateTags() {
      const { data } = await getPrivateTags()
      this.privateTags = data
    },
    async fetchPublicTags() {
      const { data } = await getPublicTags()
      this.publicTags = data
    },
  },
})
