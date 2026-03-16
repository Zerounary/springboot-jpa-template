<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useAuthStore } from '../stores/auth'
import { registerApi } from '../api/auth'

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
  <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 24px">
    <a-card title="登录" style="width: 420px">
      <a-form layout="vertical" @submit.prevent="onSubmit">
        <a-form-item label="用户名">
          <a-input v-model:value="form.username" autocomplete="username" />
        </a-form-item>
        <a-form-item label="密码">
          <a-input-password v-model:value="form.password" autocomplete="current-password" />
        </a-form-item>
        <a-button type="primary" block :loading="loading" @click="onSubmit">登录</a-button>
        <a-button style="margin-top: 8px" block @click="registerOpen = true">注册</a-button>
      </a-form>
    </a-card>

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
      </a-form>
    </a-modal>
  </div>
</template>
