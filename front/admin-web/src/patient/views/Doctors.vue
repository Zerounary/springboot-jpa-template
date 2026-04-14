<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../utils/http'

const loading = ref(false)
const keyword = ref('')
const deptId = ref(null)

const deptOptions = ref([])
const deptNameMap = ref(new Map())

const page = ref(0)
const size = ref(10)
const total = ref(0)
const list = ref([])

async function loadDepartments() {
  const tree = await http.get('/api/departments/tree', { params: { includeDisabled: false } })
  const flat = []
  const map = new Map()

  function dfs(nodes) {
    if (!nodes) return
    for (const n of nodes) {
      flat.push({ deptId: n.deptId, deptName: n.deptName })
      map.set(n.deptId, n.deptName)
      if (n.children && n.children.length) dfs(n.children)
    }
  }
  dfs(tree)

  deptOptions.value = flat
  deptNameMap.value = map
}

async function loadDoctors() {
  loading.value = true
  try {
    const res = await http.get('/api/doctors', {
      params: {
        page: page.value,
        size: size.value,
        deptId: deptId.value || undefined,
        keyword: keyword.value || undefined,
      },
    })
    list.value = res.records || []
    total.value = Number(res.total || 0)
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const deptLabel = computed(() => {
  if (!deptId.value) return ''
  return deptNameMap.value.get(deptId.value) || ''
})

async function onSearch() {
  page.value = 0
  await loadDoctors()
}

async function onPageChange(p) {
  page.value = p - 1
  await loadDoctors()
}

onMounted(async () => {
  await loadDepartments()
  await loadDoctors()
})
</script>

<template>
  <div class="patient-page-stack">
    <section class="patient-panel-card">
      <div class="patient-section-head">
        <div>
          <div class="patient-section-title">医生查询</div>
          <div class="patient-section-subtitle">按科室与擅长方向筛选合适的医生</div>
        </div>
        <div class="patient-accent">共 {{ total }} 位</div>
      </div>

      <el-form label-position="top" @submit.prevent>
        <el-form-item label="关键词（姓名/擅长）">
          <el-input v-model="keyword" placeholder="请输入关键词" clearable />
        </el-form-item>

        <el-form-item label="科室">
          <el-select v-model="deptId" placeholder="全部科室" clearable style="width: 100%">
            <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="onSearch">立即查询</el-button>
        </el-form-item>
      </el-form>

      <div v-if="deptLabel" class="patient-helper-text">当前科室：{{ deptLabel }}</div>
    </section>

    <section class="patient-panel-card">
      <div class="patient-section-head">
        <div>
          <div class="patient-section-title">查询结果</div>
          <div class="patient-section-subtitle">展示医生基础信息、擅长方向与出诊安排</div>
        </div>
      </div>

      <el-skeleton :loading="loading" animated>
        <template #default>
          <div v-if="list.length === 0" class="patient-empty-text">暂无符合条件的医生</div>

          <div v-for="d in list" :key="d.doctorId" class="patient-doctor-card">
            <div class="patient-list-head">
              <div class="patient-name">{{ d.realName || d.username }}</div>
              <div class="patient-fee">￥{{ d.registrationFee }}</div>
            </div>
            <div class="patient-meta">科室：{{ deptNameMap.get(d.deptId) || d.deptId }}</div>
            <div class="patient-meta">职称：{{ d.jobTitle }}</div>
            <div class="patient-meta">擅长：{{ d.specialty }}</div>
            <div v-if="d.schedule" class="patient-meta">出诊：{{ d.schedule }}</div>
          </div>

          <div v-if="total > size" style="display: flex; justify-content: center; margin-top: 12px">
            <el-pagination
              background
              layout="prev, pager, next"
              :page-size="size"
              :total="total"
              :current-page="page + 1"
              @current-change="onPageChange"
            />
          </div>
        </template>
      </el-skeleton>
    </section>
  </div>
</template>
