<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const auth = useAuthStore()

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
</script>

<template>
  <div class="page">
    <div class="topbar">
      <div class="topbar-title">{{ title }}</div>
      <div class="topbar-user">{{ auth.me?.realName || auth.me?.username }}</div>
    </div>

    <div class="content">
      <router-view />
    </div>

    <el-menu class="bottombar" mode="horizontal" router :default-active="route.path">
      <el-menu-item index="/home">首页</el-menu-item>
      <el-menu-item index="/doctors">医生</el-menu-item>
      <el-menu-item index="/registrations">挂号</el-menu-item>
      <el-menu-item index="/medical-records">病例</el-menu-item>
      <el-menu-item index="/health">健康</el-menu-item>
      <el-menu-item index="/me">我的</el-menu-item>
    </el-menu>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.topbar {
  height: 48px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
}

.topbar-title {
  font-weight: 600;
}

.topbar-user {
  font-size: 12px;
  color: #606266;
}

.content {
  flex: 1;
  padding: 12px;
}

.bottombar {
  border-top: 1px solid #e4e7ed;
}
</style>
