<script setup>
import { onMounted, ref } from 'vue'
import http from '../utils/http'

const me = ref(null)

onMounted(async () => {
  me.value = await http.get('/api/auth/me')
})
</script>

<template>
  <el-card>
    <div style="font-weight: 600; margin-bottom: 8px">欢迎使用</div>
    <div v-if="me">当前用户：{{ me.realName || me.username }}（roleType={{ me.roleType }}）</div>
    <div v-else>加载中...</div>
  </el-card>
</template>
