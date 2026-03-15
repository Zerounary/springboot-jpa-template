<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const isAdmin = computed(() => auth.roleType === 1)

const menus = computed(() => {
  const base = [
    { path: '/dashboard', label: '首页' },
    { path: '/registrations', label: '挂号管理' },
    { path: '/medical-records', label: '电子病历' },
    { path: '/system-news', label: '资讯管理' },
  ]

  if (isAdmin.value) {
    base.push(
      { path: '/health-monitors', label: '健康数据管理' },
      { path: '/users', label: '用户管理' },
      { path: '/departments', label: '科室管理' },
      { path: '/doctors', label: '医生管理' },
      { path: '/patients', label: '患者管理' },
    )
  }
  return base
})

function logout() {
  auth.logout()
  router.replace('/login')
}
</script>

<template>
  <el-container style="height: 100vh">
    <el-aside width="220px">
      <div class="brand">医疗档案系统</div>
      <el-menu :default-active="route.path" router>
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          {{ m.label }}
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left"></div>
        <div class="header-right">
          <span class="me">{{ auth.me?.realName || auth.me?.username }}</span>
          <el-button size="small" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.brand {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  font-weight: 700;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.me {
  font-size: 13px;
  color: #303133;
}
</style>
