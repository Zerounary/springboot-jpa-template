<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const auth = useAuthStore()

const tabs = [
  { path: '/patient/home', label: '首页', icon: '首' },
  { path: '/patient/doctors', label: '医生', icon: '医' },
  { path: '/patient/registrations', label: '挂号', icon: '挂' },
  { path: '/patient/medical-records', label: '病历', icon: '历' },
  { path: '/patient/health', label: '健康', icon: '健' },
  { path: '/patient/medication', label: '用药', icon: '药' },
  { path: '/patient/me', label: '我的', icon: '我' },
]

const title = computed(() => {
  const map = {
    '/patient/home': '健康首页',
    '/patient/doctors': '医生查询',
    '/patient/registrations': '在线挂号',
    '/patient/medical-records': '病例查询',
    '/patient/health': '健康查询',
    '/patient/medication': '用药管理',
    '/patient/me': '我的',
  }
  return map[route.path] || '患者端'
})

const subtitle = computed(() => {
  const map = {
    '/patient/home': '查看健康概览与常用入口',
    '/patient/doctors': '按科室与擅长快速找医生',
    '/patient/registrations': '在线预约并查看挂号记录',
    '/patient/medical-records': '随时查阅个人病历',
    '/patient/health': '记录健康数据与趋势变化',
    '/patient/medication': '查看处方、设置提醒并记录服药情况',
    '/patient/me': '查看个人信息与账号状态',
  }
  return map[route.path] || '移动患者服务'
})
</script>

<template>
  <div class="patient-app">
    <div class="patient-shell">
      <div class="patient-topbar">
        <div>
          <div class="patient-topbar__title">{{ title }}</div>
          <div class="patient-topbar__subtitle">{{ subtitle }}</div>
        </div>
        <div class="patient-topbar__user">{{ auth.me?.realName || auth.me?.username || '患者' }}</div>
      </div>

      <div class="patient-content">
        <router-view />
      </div>

      <nav class="patient-tabbar">
        <router-link
          v-for="tab in tabs"
          :key="tab.path"
          :to="tab.path"
          class="patient-tabbar__item"
          :class="{ 'is-active': route.path === tab.path }"
        >
          <span class="patient-tabbar__icon">{{ tab.icon }}</span>
          <span>{{ tab.label }}</span>
        </router-link>
      </nav>
    </div>
  </div>
</template>
