import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'home', component: () => import('../views/HomeView.vue') },
      { path: 'patients', name: 'patients', component: () => import('../views/PatientsView.vue') },
      {
        path: 'health-records',
        name: 'health-records',
        component: () => import('../views/HealthRecordsView.vue'),
      },
      {
        path: 'physical-exams',
        name: 'physical-exams',
        component: () => import('../views/PhysicalExamsView.vue'),
      },
      {
        path: 'questionnaires',
        name: 'questionnaires',
        component: () => import('../views/QuestionnairesView.vue'),
      },
      {
        path: 'fusion',
        name: 'fusion',
        component: () => import('../views/FusionView.vue'),
      },
      {
        path: 'prediction-results',
        name: 'prediction-results',
        component: () => import('../views/PredictionResultsView.vue'),
      },
      {
        path: 'health-guidances',
        name: 'health-guidances',
        component: () => import('../views/HealthGuidancesView.vue'),
      },
      {
        path: 'spark',
        name: 'spark',
        component: () => import('../views/SparkView.vue'),
      },
      {
        path: 'users',
        name: 'users',
        component: () => import('../views/UsersView.vue'),
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (to.name === 'login') {
    if (auth.token) {
      return { name: 'home' }
    }
    return true
  }

  if (to.meta.requiresAuth && !auth.token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (auth.token && !auth.user && !auth.loadingMe) {
    try {
      await auth.fetchMe()
    } catch {
      auth.logout()
      return { name: 'login', query: { redirect: to.fullPath } }
    }
  }

  if (auth.user?.role === 'PATIENT') {
    const blockedRoutes = new Set(['health-records', 'questionnaires', 'fusion', 'spark'])
    if (typeof to.name === 'string' && blockedRoutes.has(to.name)) {
      return { name: 'home' }
    }
  }

  return true
})
