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
    if (!auth.isPatient) {
      auth.logout()
      errorMsg.value = '该账号不是患者账号'
      return
    }
    const redirect = route.query.redirect || '/'
    router.replace(redirect)
  } catch (e) {
    errorMsg.value = e?.message || '登录失败'
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-title">患者端登录</div>
      <div class="auth-subtitle">使用患者账号登录后，可进行挂号、病历查询和健康数据记录。</div>

      <el-form :model="form" label-position="top" @submit.prevent style="margin-top: 18px">
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
          <el-button @click="$router.push('/register')">没有账号？去注册</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
 </template>
