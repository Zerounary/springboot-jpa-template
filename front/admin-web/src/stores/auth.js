import { defineStore } from 'pinia'
import http from '../utils/http'

export const useAuthStore = defineStore('auth', {
  state: () => {
    const token = localStorage.getItem('token') || ''
    const meRaw = localStorage.getItem('me')
    const me = meRaw ? JSON.parse(meRaw) : null
    return {
      token,
      me,
      loading: false,
    }
  },
  getters: {
    isAuthed: (s) => !!s.token,
    roleType: (s) => s.me?.roleType,
    isAdmin: (s) => s.me?.roleType === 1,
    isDoctor: (s) => s.me?.roleType === 2,
    isPatient: (s) => s.me?.roleType === 3,
    homePath: (s) => {
      if (s.me?.roleType === 3) {
        return '/patient/home'
      }
      return '/dashboard'
    },
  },
  actions: {
    async login(username, password) {
      this.loading = true
      try {
        const token = await http.post('/api/auth/login', { username, password })
        this.token = token
        localStorage.setItem('token', token)
        await this.fetchMe()
      } finally {
        this.loading = false
      }
    },
    async register(payload) {
      return await http.post('/api/auth/register', payload)
    },
    async fetchMe() {
      const me = await http.get('/api/auth/me')
      this.me = me
      localStorage.setItem('me', JSON.stringify(me))
      return me
    },
    logout() {
      this.token = ''
      this.me = null
      localStorage.removeItem('token')
      localStorage.removeItem('me')
    },
  },
})
