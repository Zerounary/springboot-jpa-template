<template>
  <el-select
    v-model="selectedValue"
    :placeholder="placeholder"
    :remote="true"
    :remote-method="remoteMethod"
    :loading="loading"
    :clearable="clearable"
    :disabled="disabled"
    :filterable="filterable"
    :multiple="multiple"
    style="width: 100%"
    @change="handleChange"
    @clear="handleClear"
    @focus="handleFocus"
  >
    <el-option
      v-for="item in options"
      :key="item[valueKey]"
      :label="item[labelKey]"
      :value="item[valueKey]"
    >
      <slot name="option" :item="item">
        {{ item[labelKey] }}
      </slot>
    </el-option>
  </el-select>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import http from '../utils/http'

const props = defineProps({
  // API配置
  api: {
    type: String,
    required: true
  },
  // 查询参数
  queryParam: {
    type: String,
    default: 'keyword'
  },
  // 显示字段
  labelKey: {
    type: String,
    default: 'label'
  },
  // 值字段
  valueKey: {
    type: String,
    default: 'value'
  },
  // v-model绑定值
  modelValue: {
    type: [String, Number, Array],
    default: undefined
  },
  // 占位符
  placeholder: {
    type: String,
    default: '请输入关键词搜索'
  },
  // 是否可清空
  clearable: {
    type: Boolean,
    default: true
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否可搜索
  filterable: {
    type: Boolean,
    default: true
  },
  // 是否多选
  multiple: {
    type: Boolean,
    default: false
  },
  // 最小搜索字符数
  minQueryLength: {
    type: Number,
    default: 1
  },
  // 初始加载
  initialLoad: {
    type: Boolean,
    default: false
  },
  // 额外的查询参数
  extraParams: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'change', 'clear', 'select'])

const loading = ref(false)
const options = ref([])
const selectedValue = ref(props.modelValue)

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  selectedValue.value = newVal
}, { immediate: true })

// 监听内部值变化
watch(selectedValue, (newVal) => {
  emit('update:modelValue', newVal)
})

// 远程搜索方法
const remoteMethod = async (query) => {
  // 如果查询字符串太短且不是空字符串（空字符串用于初始加载），则不搜索
  if (query.length < props.minQueryLength && query !== '') {
    options.value = []
    return
  }

  loading.value = true
  try {
    const params = {
      [props.queryParam]: query,
      size: 50, // 限制返回数量
      ...props.extraParams
    }
    
    const response = await http.get(props.api, { params })
    
    // 处理不同的响应格式
    let data = response
    if (response.data) {
      data = response.data
    } else if (response.records) {
      data = response.records
    }
    
    // 转换数据格式
    options.value = Array.isArray(data) ? data.map(item => ({
      ...item,
      [props.labelKey]: item[props.labelKey] || item.name || item.title || item.label || String(item[props.valueKey] || ''),
      [props.valueKey]: item[props.valueKey] || item.id || item.value
    })) : []
  } catch (error) {
    console.error('远程搜索失败:', error)
    options.value = []
  } finally {
    loading.value = false
  }
}

// 初始化加载
const initLoad = async () => {
  if (!props.initialLoad) return
  
  loading.value = true
  try {
    const params = {
      size: 20,
      ...props.extraParams
    }
    
    const response = await http.get(props.api, { params })
    
    let data = response
    if (response.data) {
      data = response.data
    } else if (response.records) {
      data = response.records
    }
    
    options.value = Array.isArray(data) ? data.map(item => ({
      ...item,
      [props.labelKey]: item[props.labelKey] || item.name || item.title || item.label || String(item[props.valueKey] || ''),
      [props.valueKey]: item[props.valueKey] || item.id || item.value
    })) : []
  } catch (error) {
    console.error('初始加载失败:', error)
    options.value = []
  } finally {
    loading.value = false
  }
}

// 处理焦点事件，点击时自动搜索
const handleFocus = () => {
  // 如果当前没有选项或选项很少，自动加载一些数据
  if (options.value.length === 0) {
    if (props.initialLoad) {
      initLoad()
    } else {
      // 使用空字符串进行搜索，获取默认数据
      remoteMethod('')
    }
  }
}

// 处理选择变化
const handleChange = (value) => {
  const selectedItem = options.value.find(item => item[props.valueKey] === value)
  emit('change', value, selectedItem)
  emit('select', selectedItem)
}

// 处理清空
const handleClear = () => {
  emit('clear')
}

// 根据ID获取显示文本
const getLabelByValue = (value) => {
  const item = options.value.find(item => item[props.valueKey] === value)
  return item ? item[props.labelKey] : ''
}

// 暴露方法给父组件
defineExpose({
  remoteMethod,
  initLoad,
  getLabelByValue,
  options
})

onMounted(() => {
  if (props.initialLoad) {
    initLoad()
  }
})
</script>

<style scoped>
/* 可以添加自定义样式 */
</style>
