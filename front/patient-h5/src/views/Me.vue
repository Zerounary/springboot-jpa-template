<script setup>
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

function logout() {
  auth.logout()
  router.replace('/login')
}
</script>

<template>
  <el-card>
    <div style="font-weight: 600; margin-bottom: 8px">我的</div>
    <div style="margin-bottom: 12px">
      <div>用户名：{{ auth.me?.username }}</div>
      <div>姓名：{{ auth.me?.realName }}</div>
      <div>电话：{{ auth.me?.phone }}</div>
    </div>
    <el-button type="danger" style="width: 100%" @click="logout">退出登录</el-button>
  </el-card>
</template>
