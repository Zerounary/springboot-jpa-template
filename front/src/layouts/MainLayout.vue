<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { userUpdateMeApi } from '../api/users'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const role = computed(() => auth.user?.role || 'PATIENT')
const isAdmin = computed(() => role.value === 'ADMIN')
const isDoctor = computed(() => role.value === 'DOCTOR')
const isPatient = computed(() => role.value === 'PATIENT')
const showOrganizations = computed(() => isAdmin.value)
const showFusion = computed(() => isAdmin.value)
const showSpark = computed(() => isAdmin.value)
const passwordModalOpen = ref(false)
const passwordSaving = ref(false)
const passwordForm = reactive({
  password: '',
  confirmPassword: '',
})
const displayName = computed(() => auth.user?.nickname || auth.user?.username || '用户')
const avatarText = computed(() => displayName.value.slice(0, 1).toUpperCase())

const selectedKeys = computed(() => {
  const n = route.name
  return typeof n === 'string' ? [n] : []
})

function onMenuClick(e: { key: string }) {
  router.push({ name: e.key })
}

function resetPasswordForm() {
  passwordForm.password = ''
  passwordForm.confirmPassword = ''
}

function openChangePassword() {
  resetPasswordForm()
  passwordModalOpen.value = true
}

async function submitPasswordChange() {
  if (!passwordForm.password) {
    message.warning('请输入新密码')
    return
  }
  if (passwordForm.password.length < 6) {
    message.warning('密码长度至少 6 位')
    return
  }
  if (passwordForm.password !== passwordForm.confirmPassword) {
    message.warning('两次输入的密码不一致')
    return
  }

  passwordSaving.value = true
  try {
    const user = await userUpdateMeApi({ password: passwordForm.password })
    auth.user = user
    passwordModalOpen.value = false
    resetPasswordForm()
    message.success('密码修改成功，请使用新密码重新登录')
    logout()
  } finally {
    passwordSaving.value = false
  }
}

function onUserMenuClick(e: { key: string }) {
  if (e.key === 'change-password') {
    openChangePassword()
    return
  }
  if (e.key === 'logout') {
    logout()
  }
}

function logout() {
  auth.logout()
  router.replace({ name: 'login' })
}
</script>

<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider collapsible>
      <div style="height: 48px; margin: 16px; color: #fff; font-weight: 600; display: flex; align-items: center">
        高血压风险预测
      </div>
      <a-menu theme="dark" mode="inline" :selected-keys="selectedKeys" @click="onMenuClick">
        <a-menu-item key="home">概览</a-menu-item>
        <a-menu-item key="patients">{{ isPatient ? '我的档案' : '患者' }}</a-menu-item>
        <a-menu-item v-if="showOrganizations" key="organizations">机构维护</a-menu-item>
        <a-menu-item v-if="!isPatient" key="health-records">健康档案</a-menu-item>
        <a-menu-item key="physical-exams">{{ isPatient ? '我的体检' : '体检数据' }}</a-menu-item>
        <a-menu-item v-if="!isPatient" key="questionnaires">问卷数据</a-menu-item>
        <a-menu-item v-if="showFusion" key="fusion">融合数据</a-menu-item>
        <a-menu-item key="prediction-results">{{ isPatient ? '我的预测' : '指标预测' }}</a-menu-item>
        <a-menu-item key="health-guidances">{{ isPatient ? '我的健康指导' : '健康指导' }}</a-menu-item>
        <a-menu-item v-if="showSpark" key="spark">Spark</a-menu-item>
        <a-menu-item key="users">{{ isAdmin ? '用户管理' : '我的账户' }}</a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <a-layout-header
        style="background: #fff; padding: 0 16px; display: flex; align-items: center; justify-content: flex-end; gap: 12px"
      >
        <a-tag :color="isAdmin ? 'red' : isDoctor ? 'blue' : 'green'">
          {{ role }}
        </a-tag>
        <a-dropdown>
          <a-space style="cursor: pointer">
            <a-avatar style="background-color: #1677ff">{{ avatarText }}</a-avatar>
            <a-typography-text>{{ displayName }}</a-typography-text>
          </a-space>
          <template #overlay>
            <a-menu @click="onUserMenuClick">
              <a-menu-item key="change-password">修改密码</a-menu-item>
              <a-menu-item key="logout">退出登录</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </a-layout-header>

      <a-layout-content style="margin: 16px">
        <router-view />
      </a-layout-content>

      <a-modal
        v-model:open="passwordModalOpen"
        title="修改密码"
        :confirm-loading="passwordSaving"
        @ok="submitPasswordChange"
        @cancel="() => { passwordModalOpen = false }"
      >
        <a-form layout="vertical">
          <a-form-item label="新密码" required>
            <a-input-password v-model:value="passwordForm.password" placeholder="请输入新密码" />
          </a-form-item>
          <a-form-item label="确认密码" required>
            <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
          </a-form-item>
        </a-form>
      </a-modal>
    </a-layout>
  </a-layout>
</template>
