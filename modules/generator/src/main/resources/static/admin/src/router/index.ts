import { createRouter, createWebHistory } from 'vue-router'
import { AppLayout,  toRoute } from '@quinimbus/admin-ui'
import entityTypeDefinitions from '@/domain'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: AppLayout,
      props: { entityTypeDefinitions: entityTypeDefinitions },
      children: entityTypeDefinitions.map(toRoute)
    }
  ]
   
})

export default router
