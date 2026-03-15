import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const Login = () => import('../views/Login.vue')
const Register = () => import('../views/Register.vue')
const MobileLayout = () => import('../layouts/MobileLayout.vue')

const Home = () => import('../views/Home.vue')
const Doctors = () => import('../views/Doctors.vue')
const Registrations = () => import('../views/Registrations.vue')
const MedicalRecords = () => import('../views/MedicalRecords.vue')
const Health = () => import('../views/Health.vue')
const Me = () => import('../views/Me.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: Login, meta: { public: true } },
    { path: '/register', name: 'register', component: Register, meta: { public: true } },
    {
      path: '/',
      component: MobileLayout,
      children: [
        { path: '', redirect: '/home' },
        { path: 'home', name: 'home', component: Home },
        { path: 'doctors', name: 'doctors', component: Doctors },
        { path: 'registrations', name: 'registrations', component: Registrations },
        { path: 'medical-records', name: 'medical-records', component: MedicalRecords },
        { path: 'health', name: 'health', component: Health },
        { path: 'me', name: 'me', component: Me },
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
        if (auth.isPatient) {
          return { path: '/' }
        }
        auth.logout()
        return true
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

  if (!auth.isPatient) {
    auth.logout()
    return { path: '/login' }
  }

  return true
})

export default router
