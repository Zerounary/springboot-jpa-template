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
    <div style="font-weight: 600; margin-bottom: 8px">健康概览</div>
    <div v-if="me">你好，{{ me.realName || me.username }}</div>
    <div v-else>加载中...</div>
  </el-card>

  <div style="height: 12px" />

  <el-card>
    <div style="font-weight: 600; margin-bottom: 8px">快捷入口</div>
    <el-row :gutter="12">
      <el-col :span="8"><el-button style="width: 100%" @click="$router.push('/doctors')">医生查询</el-button></el-col>
      <el-col :span="8"><el-button style="width: 100%" @click="$router.push('/registrations')">在线挂号</el-button></el-col>
      <el-col :span="8"><el-button style="width: 100%" @click="$router.push('/health')">健康监测</el-button></el-col>
    </el-row>
  </el-card>
</template>
