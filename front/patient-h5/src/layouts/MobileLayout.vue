<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const auth = useAuthStore()

const tabs = [
  { path: '/home', label: '首页', icon: '首' },
  { path: '/doctors', label: '医生', icon: '医' },
  { path: '/registrations', label: '挂号', icon: '挂' },
  { path: '/medical-records', label: '病历', icon: '历' },
  { path: '/health', label: '健康', icon: '健' },
  { path: '/me', label: '我的', icon: '我' },
]

const title = computed(() => {
  const map = {
    '/home': '健康首页',
    '/doctors': '医生查询',
    '/registrations': '在线挂号',
    '/medical-records': '病例查询',
    '/health': '健康查询',
    '/me': '我的',
  }
  return map[route.path] || '患者端'
})

const subtitle = computed(() => {
  const map = {
    '/home': '查看健康概览与常用入口',
    '/doctors': '按科室与擅长快速找医生',
    '/registrations': '在线预约并查看挂号记录',
    '/medical-records': '随时查阅个人病历',
    '/health': '记录健康数据与趋势变化',
    '/me': '查看个人信息与账号状态',
  }
  return map[route.path] || '移动患者服务'
})
</script>

<template>
  <div class="mobile-shell">
    <div class="mobile-topbar">
      <div>
        <div class="mobile-topbar__title">{{ title }}</div>
        <div class="mobile-topbar__subtitle">{{ subtitle }}</div>
      </div>
      <div class="mobile-topbar__user">{{ auth.me?.realName || auth.me?.username || '患者' }}</div>
    </div>

    <div class="mobile-content">
      <router-view />
    </div>

    <nav class="mobile-tabbar">
      <router-link
        v-for="tab in tabs"
        :key="tab.path"
        :to="tab.path"
        class="mobile-tabbar__item"
        :class="{ 'is-active': route.path === tab.path }"
      >
        <span class="mobile-tabbar__icon">{{ tab.icon }}</span>
        <span>{{ tab.label }}</span>
      </router-link>
    </nav>
  </div>
</template>
