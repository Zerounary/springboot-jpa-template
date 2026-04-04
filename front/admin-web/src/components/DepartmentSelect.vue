<template>
  <RemoteSelect
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    api="/api/departments"
    query-param="keyword"
    label-key="deptName"
    value-key="deptId"
    placeholder="请搜索科室"
    :initial-load="true"
    v-bind="$attrs"
    @change="$emit('change', $event)"
    @clear="$emit('clear')"
  />
</template>

<script setup>
import RemoteSelect from './RemoteSelect.vue'

defineProps({
  modelValue: {
    type: [String, Number],
    default: undefined
  }
})

defineEmits(['update:modelValue', 'change', 'clear'])
</script>
