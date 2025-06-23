import { createRouter, createWebHistory, type RouteComponent } from 'vue-router'
import { AppLayout, dashboardRoute, guardRoutesByRoles, oidcCallbackRoute, toRoute } from '@quinimbus/admin-ui'
import entityTypeDefinitions, { contentGroups } from '@/domain'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: AppLayout as RouteComponent,
      props: { entityTypeDefinitions: entityTypeDefinitions, groups: contentGroups },
      children: [
        dashboardRoute(entityTypeDefinitions, contentGroups),
        ...entityTypeDefinitions.map(toRoute)
      ]
    },
    oidcCallbackRoute()
  ]
   
})

router.beforeEach(guardRoutesByRoles)

export default router
