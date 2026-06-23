import type { RouteRecordRaw } from 'vue-router'
import DashboardLayout from '@/pages/dashboard/index.vue'
import ZoneLayout from '@/pages/zone/index.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'index',
    component: DashboardLayout,
    redirect: '/website',
    children: [
      {
        name: 'website',
        path: '/website',
        component: () => import('@/pages/dashboard/website/index.vue')
      },
      {
        name: 'tool',
        path: '/tool',
        redirect: '/tool/index',
        children: [
          {
            path: '/tool/index',
            name: 'tool-index',
            meta: {
              root: 'tool'
            },
            component: () => import('@/pages/dashboard/tool/index.vue')
          },
          {
            path: '/tool/route',
            name: 'tool-route',
            component: () => import('@/pages/dashboard/tool/route.vue')
          }
        ]
      },
      {
        name: 'authority',
        path: '/authority',
        component: () => import('@/pages/dashboard/authority/index.vue'),
        redirect: '/authority/user',
        children: [
          {
            path: '/authority/user',
            name: 'authority-user',
            meta: {
              root: 'authority'
            },
            component: () => import('@/pages/dashboard/authority/user.vue')
          },
          {
            path: '/authority/role',
            name: 'authority-role',
            meta: {
              root: 'authority'
            },
            component: () => import('@/pages/dashboard/authority/role.vue')
          },
          {
            path: '/authority/permissions',
            name: 'authority-permissions',
            meta: {
              root: 'authority'
            },
            component: () => import('@/pages/dashboard/authority/permissions.vue')
          }
        ]
      },
      {
        name: 'audit',
        path: '/audit',
        component: () => import('@/pages/dashboard/audit/index.vue'),
        redirect: '/audit/session',
        children: [
          {
            path: '/audit/session',
            name: 'audit-session',
            meta: {
              root: 'audit'
            },
            component: () => import('@/pages/dashboard/audit/session.vue')
          },
          {
            path: '/audit/operation',
            name: 'audit-operation',
            meta: {
              root: 'audit'
            },
            component: () => import('@/pages/dashboard/audit/operation.vue')
          }
        ]
      },
      {
        name: 'service',
        path: '/service',
        component: () => import('@/pages/dashboard/service/index.vue')
      },
      {
        name: 'setting',
        path: '/setting',
        component: () => import('@/pages/dashboard/setting/index.vue'),
        redirect: '/setting/preference',
        children: [
          {
            path: '/setting/preference',
            name: 'setting-preference',
            meta: {
              root: 'setting'
            },
            component: () => import('@/pages/dashboard/setting/preference.vue')
          },
          {
            path: '/setting/ips',
            name: 'setting-ips',
            meta: {
              root: 'setting'
            },
            component: () => import('@/pages/dashboard/setting/ips.vue')
          },
          {
            path: '/setting/certificate',
            name: 'setting-certificate',
            meta: {
              root: 'setting'
            },
            component: () => import('@/pages/dashboard/setting/certificate.vue')
          },
          {
            path: '/setting/auths',
            name: 'setting-auths',
            meta: {
              root: 'setting'
            },
            component: () => import('@/pages/dashboard/setting/auths.vue')
          },
          {
            path: '/setting/provider',
            name: 'setting-provider',
            meta: {
              root: 'setting'
            },
            component: () => import('@/pages/dashboard/setting/provider.vue')
          }
        ]
      }
    ]
  },
  {
    path: '/zone/:id',
    name: 'zone',
    redirect: to => {
      return `/zone/route/${to.params.id}`
    },
    component: ZoneLayout,
    children: [
      {
        path: '/zone/route/:id',
        name: 'zone-route',
        redirect: to => {
          return `/zone/route/root/${to.params.id}`
        },
        children: [
          {
            path: '/zone/route/root/:id',
            name: 'zone-route-root',
            meta: {
              root: 'route'
            },
            component: () => import('@/pages/zone/route/index.vue')
          },
          {
            path: '/zone/route/add/:id',
            name: 'zone-route-add',
            component: () => import('@/components/zone/route/add.vue')
          },
          {
            path: '/zone/route/edit/:id/:rid',
            name: 'zone-route-edit',
            component: () => import('@/components/zone/route/edit.vue')
          }
        ]
      },
      {
        path: '/zone/upstream/:id',
        name: 'zone-upstream',
        redirect: to => {
          return `/zone/upstream/root/${to.params.id}`
        },
        children: [
          {
            path: '/zone/upstream/root/:id',
            name: 'zone-upstream-root',
            meta: {
              root: 'upstream'
            },
            component: () => import('@/pages/zone/upstream/index.vue')
          },
          {
            path: '/zone/upstream/add/:id',
            name: 'zone-upstream-add',
            component: () => import('@/components/zone/upstream/add.vue')
          },
          {
            path: '/zone/upstream/edit/:id/:uid',
            name: 'zone-upstream-edit',
            component: () => import('@/components/zone/upstream/edit.vue')
          }
        ]
      },
      {
        path: '/zone/protection/:id',
        name: 'zone-protection',
        redirect: to => {
          return `/zone/protection/rule/${to.params.id}`
        },
        children: [
          {
            path: '/zone/protection/rule/:id',
            name: 'zone-protection-rule',
            component: () => import('@/pages/zone/protection/rule.vue')
          },
          {
            path: '/zone/protection/rule/add/:id',
            name: 'zone-protection-rule-add',
            component: () => import('@/components/zone/protection/rule/add.vue')
          },
          {
            path: '/zone/protection/rule/edit/:id/:rid',
            name: 'zone-protection-rule-edit',
            component: () => import('@/components/zone/protection/rule/edit.vue')
          },
          {
            path: '/zone/protection/limit/:id',
            name: 'zone-protection-limit',
            component: () => import('@/pages/zone/protection/limit.vue')
          },
          {
            path: '/zone/protection/limit/add/:id',
            name: 'zone-protection-limit-add',
            component: () => import('@/components/zone/protection/limit/add.vue')
          },
          {
            path: '/zone/protection/limit/edit/:id/:rid',
            name: 'zone-protection-limit-edit',
            component: () => import('@/components/zone/protection/limit/edit.vue')
          },
          {
            path: '/zone/protection/semantics/:id',
            name: 'zone-protection-semantics',
            component: () => import('@/pages/zone/protection/semantics.vue')
          }
        ]
      },
      {
        path: '/zone/cache/:id',
        name: 'zone-cache',
        redirect: to => {
          return `/zone/cache/rule/${to.params.id}`
        },
        children: [
          {
            path: '/zone/cache/rule/:id',
            name: 'zone-cache-rule',
            component: () => import('@/pages/zone/cache/rule.vue')
          },
          {
            path: '/zone/cache/rule/add/:id',
            name: 'zone-cache-rule-add',
            component: () => import('@/components/zone/cache/rule/add.vue')
          },
          {
            path: '/zone/cache/rule/edit/:id/:rid',
            name: 'zone-cache-rule-edit',
            component: () => import('@/components/zone/cache/rule/edit.vue')
          }
        ]
      },
      {
        path: '/zone/rewrite/:id',
        name: 'zone-rewrite',
        redirect: to => {
          return `/zone/rewrite/request/${to.params.id}`
        },
        children: [
          {
            path: '/zone/rewrite/request/:id',
            name: 'zone-rewrite-request',
            component: () => import('@/pages/zone/rewrite/request.vue')
          },
          {
            path: '/zone/rewrite/request/add/:id',
            name: 'zone-rewrite-request-add',
            component: () => import('@/components/zone/rewrite/request/add.vue')
          },
          {
            path: '/zone/rewrite/request/edit/:id/:rid',
            name: 'zone-rewrite-request-edit',
            component: () => import('@/components/zone/rewrite/request/edit.vue')
          },
          {
            path: '/zone/rewrite/response/:id',
            name: 'zone-rewrite-response',
            component: () => import('@/pages/zone/rewrite/response.vue')
          },
          {
            path: '/zone/rewrite/response/add/:id',
            name: 'zone-rewrite-response-add',
            component: () => import('@/components/zone/rewrite/response/add.vue')
          },
          {
            path: '/zone/rewrite/response/edit/:id/:rid',
            name: 'zone-rewrite-response-edit',
            component: () => import('@/components/zone/rewrite/response/edit.vue')
          },
          {
            path: '/zone/rewrite/redirect/:id',
            name: 'zone-rewrite-redirect',
            component: () => import('@/pages/zone/rewrite/redirect.vue')
          },
          {
            path: '/zone/rewrite/redirect/add/:id',
            name: 'zone-rewrite-redirect-add',
            component: () => import('@/components/zone/rewrite/redirect/add.vue')
          },
          {
            path: '/zone/rewrite/redirect/edit/:id/:rid',
            name: 'zone-rewrite-redirect-edit',
            component: () => import('@/components/zone/rewrite/redirect/edit.vue')
          }
        ]
      },
      {
        path: '/zone/plugin/:id',
        name: 'zone-plugin',
        component: () => import('@/pages/zone/plugin/index.vue')
      },
      {
        path: '/zone/page/:id',
        name: 'zone-page',
        component: () => import('@/pages/zone/page/index.vue')
      }
    ]
  },
  {
    path: '/auth/login',
    component: () => import('@/pages/auth/login.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/error/404.html'
  },
  {
    path: '/error/404.html',
    component: () => import('@/layouts/error.vue')
  },
  {
    path: '/error/400.html',
    component: () => import('@/layouts/400.vue')
  }
]

export default routes
