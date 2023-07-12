import { createRouter, createWebHistory } from 'vue-router'
import { toRoute } from '@/qn/ui/UI'
import entityTypeDefinitions from '@/domain'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: entityTypeDefinitions.map(toRoute)
})

export default router
