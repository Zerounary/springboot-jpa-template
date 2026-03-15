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
  <div class="login-page">
    <el-card class="login-card" shadow="always">
      <template #header>
        <div class="login-title">患者端登录</div>
      </template>

      <el-form :model="form" label-position="top" @submit.prevent>
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
          <el-button style="width: 100%" @click="$router.push('/register')">没有账号？去注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: #f5f7fa;
}

.login-card {
  width: 420px;
  max-width: 100%;
}

.login-title {
  font-size: 16px;
  font-weight: 600;
}
</style>
