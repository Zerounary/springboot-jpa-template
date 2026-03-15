<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <el-card>
    <div style="font-weight: 600; margin-bottom: 8px">{{ route.path }}</div>
    <div>页面开发中</div>
  </el-card>
</template>
