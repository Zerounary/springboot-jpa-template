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
    @clear="$emit('clear')"
  />
</template>

<script setup>
import { watch } from 'vue'
import RemoteSelect from './RemoteSelect.vue'

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: undefined
  }
})

const emit = defineEmits(['update:modelValue', 'change', 'clear'])

watch(() => props.modelValue, (newVal) => {
  emit('change', newVal)
})
</script>
