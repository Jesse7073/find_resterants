import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'search', component: () => import('../views/SearchView.vue') },
    { path: '/login', name: 'login', component: () => import('../views/LoginView.vue') },
    { path: '/register', name: 'register', component: () => import('../views/RegisterView.vue') },
    { path: '/restaurants/new', name: 'add-restaurant', component: () => import('../views/AddRestaurantView.vue'), meta: { requiresAuth: true } },
    { path: '/restaurants/:id', name: 'restaurant-detail', component: () => import('../views/RestaurantDetailView.vue') },
    { path: '/favorites', name: 'favorites', component: () => import('../views/FavoritesView.vue'), meta: { requiresAuth: true } },
    { path: '/tags', name: 'tags', component: () => import('../views/TagManagementView.vue'), meta: { requiresAuth: true } },
  ],
})

// 對應 docs/phase3-components.md「路由與權限」：需登入頁面由此 guard 統一攔截
router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
})

export default router
