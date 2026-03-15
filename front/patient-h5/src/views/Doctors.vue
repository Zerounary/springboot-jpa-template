<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../utils/http'

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
  <el-card shadow="never">
    <div style="font-weight: 600; margin-bottom: 8px">医生查询</div>

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
        <el-button type="primary" style="width: 100%" :loading="loading" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-divider content-position="left">结果 {{ total }}</el-divider>

    <el-skeleton :loading="loading" animated>
      <template #default>
        <div v-if="list.length === 0" style="color: #909399">暂无数据</div>

        <div v-for="d in list" :key="d.doctorId" class="doctor-card">
          <div class="doctor-title">
            <div class="name">{{ d.realName || d.username }}</div>
            <div class="fee">￥{{ d.registrationFee }}</div>
          </div>
          <div class="meta">科室：{{ deptNameMap.get(d.deptId) || d.deptId }}</div>
          <div class="meta">职称：{{ d.jobTitle }}</div>
          <div class="meta">擅长：{{ d.specialty }}</div>
          <div v-if="d.schedule" class="meta">出诊：{{ d.schedule }}</div>
        </div>

        <div style="display: flex; justify-content: center; margin-top: 12px" v-if="total > size">
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

    <div v-if="deptLabel" style="margin-top: 8px; color: #909399; font-size: 12px">
      当前科室：{{ deptLabel }}
    </div>
  </el-card>
</template>

<style scoped>
.doctor-card {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  margin-bottom: 10px;
  background: #fff;
}

.doctor-title {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 6px;
}

.name {
  font-weight: 700;
}

.fee {
  font-weight: 600;
  color: #409eff;
}

.meta {
  font-size: 12px;
  color: #606266;
  line-height: 18px;
}
</style>
