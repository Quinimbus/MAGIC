import { createRouter, createWebHistory, type RouteComponent } from 'vue-router'
import { AppLayout, guardRoutesByRoles, OIDCCallbackView, toRoute } from '@quinimbus/admin-ui'
import entityTypeDefinitions from '@/domain'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: AppLayout as RouteComponent,
      props: { entityTypeDefinitions: entityTypeDefinitions },
      children: entityTypeDefinitions.map(toRoute)
    },
    {
      path: "/oidc-callback",
      name: 'oidc-callback',
      component: OIDCCallbackView,
    }
  ]
   
})

router.beforeEach(guardRoutesByRoles)

export default router
