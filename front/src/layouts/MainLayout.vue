<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const selectedKeys = computed(() => {
  const n = route.name
  return typeof n === 'string' ? [n] : []
})

function onMenuClick(e: { key: string }) {
  router.push({ name: e.key })
}

function logout() {
  auth.logout()
  router.replace({ name: 'login' })
}
</script>

<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider collapsible>
      <div style="height: 48px; margin: 16px; color: #fff; font-weight: 600; display: flex; align-items: center">
        高血压风险预测
      </div>
      <a-menu theme="dark" mode="inline" :selected-keys="selectedKeys" @click="onMenuClick">
        <a-menu-item key="home">概览</a-menu-item>
        <a-menu-item key="patients">患者</a-menu-item>
        <a-menu-item key="health-records">健康档案</a-menu-item>
        <a-menu-item key="physical-exams">体检数据</a-menu-item>
        <a-menu-item key="questionnaires">问卷数据</a-menu-item>
        <a-menu-item key="fusion">融合数据</a-menu-item>
        <a-menu-item key="prediction-results">预测结果</a-menu-item>
        <a-menu-item key="spark">Spark</a-menu-item>
        <a-menu-item key="users">用户</a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <a-layout-header
        style="background: #fff; padding: 0 16px; display: flex; align-items: center; justify-content: flex-end; gap: 12px"
      >
        <a-typography-text v-if="auth.user">{{ auth.user.nickname || auth.user.username }}</a-typography-text>
        <a-button type="link" @click="logout">退出登录</a-button>
      </a-layout-header>

      <a-layout-content style="margin: 16px">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>
