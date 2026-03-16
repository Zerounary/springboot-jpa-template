<script setup lang="ts">
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
</script>

<template>
  <a-card>
    <a-typography-title :level="4">系统概览</a-typography-title>
    <a-typography-paragraph>已登录用户：{{ auth.user?.username || '-' }}</a-typography-paragraph>
    <a-typography-paragraph>请从左侧菜单进入各模块。</a-typography-paragraph>
  </a-card>
</template>
