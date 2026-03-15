import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const Login = () => import('../views/Login.vue')
const AdminLayout = () => import('../layouts/AdminLayout.vue')
const Dashboard = () => import('../views/Dashboard.vue')

const Registrations = () => import('../views/Registrations.vue')
const MedicalRecords = () => import('../views/MedicalRecords.vue')
const SystemNews = () => import('../views/SystemNews.vue')
const HealthMonitors = () => import('../views/HealthMonitors.vue')

const Users = () => import('../views/Users.vue')
const Departments = () => import('../views/Departments.vue')
const Doctors = () => import('../views/Doctors.vue')
const Patients = () => import('../views/Patients.vue')

const Placeholder = () => import('../views/Placeholder.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: Login,
      meta: { public: true },
    },
    {
      path: '/',
      component: AdminLayout,
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', name: 'dashboard', component: Dashboard },
        { path: 'registrations', name: 'registrations', component: Registrations },
        { path: 'medical-records', name: 'medical-records', component: MedicalRecords },
        { path: 'system-news', name: 'system-news', component: SystemNews },
        { path: 'health-monitors', name: 'health-monitors', component: HealthMonitors, meta: { roles: [1] } },
        { path: 'users', name: 'users', component: Users, meta: { roles: [1] } },
        { path: 'departments', name: 'departments', component: Departments, meta: { roles: [1] } },
        { path: 'doctors', name: 'doctors', component: Doctors, meta: { roles: [1] } },
        { path: 'patients', name: 'patients', component: Patients, meta: { roles: [1] } },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (to.meta.public) {
    if (to.path === '/login' && auth.isAuthed) {
      try {
        if (!auth.me) {
          await auth.fetchMe()
        }
        return { path: '/' }
      } catch {
        auth.logout()
        return true
      }
    }
    return true
  }

  if (!auth.isAuthed) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (!auth.me) {
    try {
      await auth.fetchMe()
    } catch {
      auth.logout()
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }

  const roles = to.meta.roles
  if (roles && Array.isArray(roles) && roles.length > 0) {
    if (!roles.includes(auth.roleType)) {
      return { path: '/dashboard' }
    }
  }

  return true
})

export default router
