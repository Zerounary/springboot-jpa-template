<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const isAdmin = computed(() => auth.roleType === 1)

const pageTitle = computed(() => {
  const item = menus.value.find((m) => route.path === m.path)
  return item?.label || '后台管理'
})

const pageSubtitle = computed(() => {
  const map = {
    '/dashboard': '查看医院运营概览与常用管理入口',
    '/registrations': '统一处理挂号订单、支付与就诊状态',
    '/medical-records': '管理电子病历与诊疗记录',
    '/system-news': '维护医院公告与健康资讯内容',
    '/health-monitors': '查看患者健康监测数据变化趋势',
    '/users': '统一管理系统账号、角色与状态',
    '/departments': '维护医院科室组织结构',
    '/doctors': '配置医生档案、科室归属与出诊安排',
    '/patients': '查看患者基础档案与关联信息',
  }
  return map[route.path] || '医院后台管理工作台'
})

const userInitial = computed(() => (auth.me?.realName || auth.me?.username || '管').slice(0, 1))

const roleLabel = computed(() => {
  if (auth.roleType === 1) {
    return '系统管理员'
  }
  if (auth.roleType === 2) {
    return '医生'
  }
  return '业务人员'
})

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
      { path: '/users', label: '管理员管理' },
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
  <el-container class="admin-shell">
    <el-aside width="260px" class="admin-sidebar">
      <div class="admin-brand">
        <div class="admin-brand__eyebrow">HOSPITAL ADMIN</div>
        <div class="admin-brand__title">医疗档案系统</div>
        <div class="admin-brand__subtitle">简约、专业、可持续扩展的医院后台工作台</div>
      </div>

      <el-menu class="admin-menu" :default-active="route.path" router>
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          {{ m.label }}
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="admin-main">
      <el-header class="admin-header">
        <div class="admin-header__wrap">
          <div>
            <div class="admin-header__title">{{ pageTitle }}</div>
            <div class="admin-header__subtitle">{{ pageSubtitle }}</div>
          </div>

          <div class="admin-userbox">
            <div class="admin-userbox__badge">{{ userInitial }}</div>
            <div>
              <div class="admin-userbox__name">{{ auth.me?.realName || auth.me?.username }}</div>
              <div class="admin-userbox__role">{{ roleLabel }}</div>
            </div>
            <el-button size="small" @click="logout">退出</el-button>
          </div>
        </div>
      </el-header>
      <el-main class="admin-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
