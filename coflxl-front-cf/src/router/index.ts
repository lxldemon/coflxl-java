import { createRouter, createWebHistory } from 'vue-router'
import MarketingPage from '../views/MarketingPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'MarketingPage',
      component: MarketingPage
    }
  ]
})

export default router