<script setup lang="ts">
defineProps<{ title: string }>()
</script>

<template>
  <a-card>
    <a-typography-title :level="4">{{ title }}</a-typography-title>
    <a-typography-paragraph>该页面待实现。</a-typography-paragraph>
  </a-card>
</template>
