<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  realName: '',
  phone: '',
})

const formRef = ref()
const loading = ref(false)
const errorMsg = ref('')

const rules = {
  username: [
    {
      validator: (_rule, value, callback) => {
        const v = (value || '').trim()
        if (!v) {
          callback(new Error('请输入用户名'))
          return
        }
        if (v.length > 64) {
          callback(new Error('用户名长度不能超过 64 个字符'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  password: [
    {
      validator: (_rule, value, callback) => {
        const v = value || ''
        if (!v.trim()) {
          callback(new Error('请输入密码'))
          return
        }
        if (v.length < 6 || v.length > 64) {
          callback(new Error('密码长度需为 6-64 个字符'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  nickname: [
    {
      validator: (_rule, value, callback) => {
        if ((value || '').length > 64) {
          callback(new Error('昵称长度不能超过 64 个字符'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  realName: [
    {
      validator: (_rule, value, callback) => {
        if ((value || '').length > 20) {
          callback(new Error('真实姓名长度不能超过 20 个字符'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
  phone: [
    {
      validator: (_rule, value, callback) => {
        if ((value || '').length > 11) {
          callback(new Error('手机号长度不能超过 11 个字符'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
}

async function onSubmit() {
  errorMsg.value = ''
  const valid = await formRef.value?.validate?.().catch(() => false)
  if (!valid) {
    return
  }

  const payload = {
    username: form.username.trim(),
    password: form.password,
    nickname: form.nickname.trim() || null,
    realName: form.realName.trim() || null,
    phone: form.phone.trim() || null,
  }

  loading.value = true
  try {
    await auth.register(payload)
    router.replace('/login')
  } catch (e) {
    errorMsg.value = e?.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-title">患者注册</div>
      <div class="auth-subtitle">填写基础信息后即可创建患者账号，后续可直接在手机端完成挂号与查看病历。</div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent style="margin-top: 18px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="username" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" autocomplete="new-password" show-password maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" show-word-limit />
        </el-form-item>

        <el-alert v-if="errorMsg" :title="errorMsg" type="error" :closable="false" style="margin-bottom: 12px" />

        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="onSubmit">
            注册
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button @click="$router.replace('/login')">已有账号？去登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
 </template>
