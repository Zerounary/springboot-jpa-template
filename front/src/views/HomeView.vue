<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const roleText = computed(() => {
  if (auth.user?.role === 'ADMIN') return '管理员'
  if (auth.user?.role === 'DOCTOR') return '医生'
  return '患者'
})
</script>

<template>
  <a-card>
    <a-typography-title :level="4">系统概览</a-typography-title>
    <a-typography-paragraph>已登录用户：{{ auth.user?.username || '-' }}</a-typography-paragraph>
    <a-typography-paragraph>当前角色：{{ roleText }}</a-typography-paragraph>
    <a-typography-paragraph>
      {{ auth.user?.role === 'PATIENT' ? '你可以查看自己的档案、体检、预测结果和健康指导。' : '你可以从左侧菜单进入业务模块。' }}
    </a-typography-paragraph>
  </a-card>
</template>
