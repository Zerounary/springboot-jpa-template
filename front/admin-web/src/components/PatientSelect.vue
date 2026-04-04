<template>
  <RemoteSelect
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    api="/api/users"
    query-param="keyword"
    label-key="realName"
    value-key="id"
    :extra-params="{ roleType: 3 }"
    placeholder="请搜索患者"
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
