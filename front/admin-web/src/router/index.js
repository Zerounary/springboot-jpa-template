import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const Login = () => import('../views/Login.vue')
const Register = () => import('../patient/views/Register.vue')
const AdminLayout = () => import('../layouts/AdminLayout.vue')
const MobileLayout = () => import('../patient/layouts/MobileLayout.vue')
const Dashboard = () => import('../views/Dashboard.vue')

const Registrations = () => import('../views/Registrations.vue')
const MedicalRecords = () => import('../views/MedicalRecords.vue')
const SystemNews = () => import('../views/SystemNews.vue')
const HealthMonitors = () => import('../views/HealthMonitors.vue')

const Users = () => import('../views/Users.vue')
const Departments = () => import('../views/Departments.vue')
const Doctors = () => import('../views/Doctors.vue')
const Patients = () => import('../views/Patients.vue')
const PatientHome = () => import('../patient/views/Home.vue')
const PatientDoctors = () => import('../patient/views/Doctors.vue')
const PatientRegistrations = () => import('../patient/views/Registrations.vue')
const PatientMedicalRecords = () => import('../patient/views/MedicalRecords.vue')
const PatientHealth = () => import('../patient/views/Health.vue')
const PatientMedication = () => import('../patient/views/Medication.vue')
const PatientMe = () => import('../patient/views/Me.vue')

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
      path: '/register',
      name: 'register',
      component: Register,
      meta: { public: true },
    },
    {
      path: '/',
      component: AdminLayout,
      meta: { roles: [1, 2] },
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
    {
      path: '/patient',
      component: MobileLayout,
      meta: { roles: [3] },
      children: [
        { path: '', redirect: '/patient/home' },
        { path: 'home', name: 'patient-home', component: PatientHome },
        { path: 'doctors', name: 'patient-doctors', component: PatientDoctors },
        { path: 'registrations', name: 'patient-registrations', component: PatientRegistrations },
        { path: 'medical-records', name: 'patient-medical-records', component: PatientMedicalRecords },
        { path: 'health', name: 'patient-health', component: PatientHealth },
        { path: 'medication', name: 'patient-medication', component: PatientMedication },
        { path: 'me', name: 'patient-me', component: PatientMe },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: Placeholder,
      meta: { public: true },
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  const isPublic = to.matched.some((record) => record.meta?.public)

  if (isPublic) {
    if ((to.path === '/login' || to.path === '/register') && auth.isAuthed) {
      try {
        if (!auth.me) {
          await auth.fetchMe()
        }
        return { path: auth.homePath }
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

  const roleRecord = [...to.matched].reverse().find((record) => {
    const roles = record.meta?.roles
    return Array.isArray(roles) && roles.length > 0
  })
  const roles = roleRecord?.meta?.roles
  if (roles && Array.isArray(roles) && roles.length > 0) {
    if (!roles.includes(auth.roleType)) {
      return { path: auth.homePath }
    }
  }

  return true
})

export default router
