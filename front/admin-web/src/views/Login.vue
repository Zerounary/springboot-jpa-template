<script setup>
import { reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const form = reactive({
  username: '',
  password: '',
})

async function onSubmit() {
  await auth.login(form.username, form.password)
  const redirect = route.query.redirect || '/'
  router.replace(redirect)
}
</script>

<template>
  <div class="admin-login-page">
    <div class="admin-login-card">
      <div class="admin-login-mark">医</div>
      <div class="admin-login-title">医院后台登录</div>
      <div class="admin-login-subtitle">面向医院运营、挂号管理、病历维护与用户管理的一体化工作台入口。</div>

      <el-form :model="form" label-position="top" @submit.prevent style="margin-top: 22px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" autocomplete="current-password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="auth.loading" style="width: 100%" @click="onSubmit">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
