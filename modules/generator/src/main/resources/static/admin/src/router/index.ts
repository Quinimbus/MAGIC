import { createRouter, createWebHistory } from 'vue-router'
import { toRoute } from '@quinimbus/admin-ui'
import entityTypeDefinitions from '@/domain'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: entityTypeDefinitions.map(toRoute)
})

export default router
