<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const form = reactive({
  username: '',
  password: '',
})

const errorMsg = ref('')

async function onSubmit() {
  errorMsg.value = ''
  try {
    await auth.login(form.username, form.password)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    router.replace(redirect || auth.homePath)
  } catch (e) {
    errorMsg.value = e?.message || '登录失败'
  }
}
</script>

<template>
  <div class="admin-login-page">
    <div class="admin-login-card">
      <div class="admin-login-mark">医</div>
      <div class="admin-login-title">统一登录入口</div>
      <div class="admin-login-subtitle">医院后台与患者端共用一个前端项目，登录后会根据你的角色自动跳转到对应首页。</div>

      <el-form :model="form" label-position="top" @submit.prevent style="margin-top: 22px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" autocomplete="current-password" show-password />
        </el-form-item>
        <el-alert v-if="errorMsg" :title="errorMsg" type="error" :closable="false" style="margin-bottom: 12px" />
        <el-form-item>
          <el-button type="primary" :loading="auth.loading" style="width: 100%" @click="onSubmit">
            登录
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button style="width: 100%" @click="$router.push('/register')">患者没有账号？去注册</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
