<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useAuthStore } from '../stores/auth'
import { registerApi, type UserRole } from '../api/auth'
import heroBg from '../assets/hero.png'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const loading = ref(false)
const registerOpen = ref(false)
const registerLoading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const registerForm = reactive({
  username: '',
  password: '',
  nickname: '',
  role: 'PATIENT' as UserRole,
})

async function onSubmit() {
  if (!form.username || !form.password) {
    message.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.replace(redirect)
  } finally {
    loading.value = false
  }
}

async function onRegister() {
  if (!registerForm.username || !registerForm.password) {
    message.warning('请输入用户名和密码')
    return
  }
  if (registerForm.password.length < 6) {
    message.warning('密码至少 6 位')
    return
  }
  registerLoading.value = true
  try {
    await registerApi({
      username: registerForm.username,
      password: registerForm.password,
      nickname: registerForm.nickname || undefined,
      role: registerForm.role,
    })
    message.success('注册成功，请登录')
    form.username = registerForm.username
    form.password = registerForm.password
    registerOpen.value = false
  } finally {
    registerLoading.value = false
  }
}
</script>

<template>
  <div class="login-page" :style="{ backgroundImage: `url(${heroBg})` }">
    <div class="login-mask" />
    <div class="login-container">
      <div class="login-header">
        <a-typography-title :level="3" style="margin: 0">社区医院信息平台登录</a-typography-title>
        <a-typography-text type="secondary">请使用账号密码登录系统</a-typography-text>
      </div>

      <a-card class="login-card" :bordered="false">
        <a-form layout="vertical" @submit.prevent="onSubmit">
          <a-form-item label="用户名">
            <a-input v-model:value="form.username" autocomplete="username" size="large" />
          </a-form-item>
          <a-form-item label="密码">
            <a-input-password v-model:value="form.password" autocomplete="current-password" size="large" />
          </a-form-item>
          <a-button type="primary" block size="large" :loading="loading" @click="onSubmit">登录</a-button>
          <a-button style="margin-top: 10px" block size="large" @click="registerOpen = true">注册</a-button>
        </a-form>
      </a-card>
    </div>

    <a-modal v-model:open="registerOpen" title="注册" :confirm-loading="registerLoading" @ok="onRegister">
      <a-form layout="vertical">
        <a-form-item label="用户名" required>
          <a-input v-model:value="registerForm.username" autocomplete="username" />
        </a-form-item>
        <a-form-item label="密码" required>
          <a-input-password v-model:value="registerForm.password" autocomplete="new-password" />
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model:value="registerForm.nickname" />
        </a-form-item>
        <a-form-item label="注册角色">
          <a-select v-model:value="registerForm.role">
            <a-select-option value="ADMIN">管理员</a-select-option>
            <a-select-option value="PATIENT">患者</a-select-option>
            <a-select-option value="DOCTOR">医生</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background-size: cover;
  background-position: center;
  position: relative;
}

.login-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.78), rgba(2, 132, 199, 0.35));
  backdrop-filter: blur(1px);
}

.login-container {
  position: relative;
  width: 100%;
  max-width: 520px;
  z-index: 1;
}

.login-header {
  color: #fff;
  margin-bottom: 16px;
}

.login-card {
  border-radius: 14px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.28);
}
</style>
