<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()

const loading = ref(false)

const filters = reactive({
  keyword: '',
  status: null,
})

const page = ref(0)
const size = ref(10)
const total = ref(0)
const records = ref([])

const parentOptions = ref([{ value: 0, label: '顶级科室' }])

const editVisible = ref(false)
const editMode = ref('create')
const editLoading = ref(false)
const editFormRef = ref(null)

const editForm = reactive({
  deptId: null,
  deptName: '',
  deptCode: '',
  parentId: 0,
  description: '',
  sort: 0,
  status: 1,
})

function statusText(v) {
  if (v === 1) return '启用'
  if (v === 0) return '禁用'
  return '-'
}

function flattenDeptTree(list, out = [], prefix = '') {
  if (!Array.isArray(list)) return out
  for (const n of list) {
    out.push({ value: n.deptId, label: prefix ? `${prefix} / ${n.deptName}` : n.deptName })
    if (Array.isArray(n.children) && n.children.length > 0) {
      flattenDeptTree(n.children, out, prefix ? `${prefix} / ${n.deptName}` : n.deptName)
    }
  }
  return out
}

async function loadParentOptions() {
  const tree = await http.get('/api/departments/tree', { params: { includeDisabled: true } })
  const opts = flattenDeptTree(tree)
  parentOptions.value = [{ value: 0, label: '顶级科室' }, ...opts]
}

async function fetchPage() {
  loading.value = true
  try {
    const data = await http.get('/api/departments', {
      params: {
        page: page.value,
        size: size.value,
        keyword: filters.keyword?.trim() || undefined,
        status: filters.status ?? undefined,
      },
    })
    records.value = data?.records || []
    total.value = Number(data?.total || 0)
  } finally {
    loading.value = false
  }
}

function resetAndSearch() {
  page.value = 0
  fetchPage()
}

function openCreate() {
  editMode.value = 'create'
  editForm.deptId = null
  editForm.deptName = ''
  editForm.deptCode = ''
  editForm.parentId = 0
  editForm.description = ''
  editForm.sort = 0
  editForm.status = 1
  editVisible.value = true
}

async function openEdit(row) {
  editMode.value = 'edit'
  editVisible.value = true
  editLoading.value = true
  try {
    const d = await http.get(`/api/departments/${row.deptId}`)
    editForm.deptId = d.deptId
    editForm.deptName = d.deptName || ''
    editForm.deptCode = d.deptCode || ''
    editForm.parentId = d.parentId ?? 0
    editForm.description = d.description || ''
    editForm.sort = d.sort ?? 0
    editForm.status = d.status ?? 1
  } finally {
    editLoading.value = false
  }
}

async function submitEdit() {
  await editFormRef.value?.validate?.()

  editLoading.value = true
  try {
    if (editMode.value === 'create') {
      await http.post('/api/departments', {
        deptName: editForm.deptName,
        deptCode: editForm.deptCode,
        parentId: editForm.parentId,
        description: editForm.description || null,
        sort: editForm.sort ?? 0,
        status: editForm.status ?? 1,
      })
      ElMessage.success('已创建')
    } else {
      await http.put(`/api/departments/${editForm.deptId}`, {
        deptName: editForm.deptName,
        deptCode: editForm.deptCode,
        parentId: editForm.parentId,
        description: editForm.description || null,
        sort: editForm.sort,
        status: editForm.status,
      })
      ElMessage.success('已保存')
    }
    editVisible.value = false
    await loadParentOptions()
    fetchPage()
  } finally {
    editLoading.value = false
  }
}

async function doDelete(row) {
  await ElMessageBox.confirm(`确认删除科室 ${row.deptName}？`, '提示', { type: 'warning' })
  await http.delete(`/api/departments/${row.deptId}`)
  ElMessage.success('已删除')
  await loadParentOptions()
  fetchPage()
}

onMounted(async () => {
  if (!auth.me) {
    try {
      await auth.fetchMe()
    } catch {
      auth.logout()
      return
    }
  }
  await loadParentOptions()
  fetchPage()
})
</script>

<template>
  <div class="admin-page">
    <section class="admin-section">
      <div class="admin-section__head">
        <div>
          <div class="admin-section__title">科室管理</div>
          <div class="admin-section__subtitle">维护医院组织结构、层级关系、科室编码与启用状态</div>
        </div>
        <div class="admin-note">共 {{ total }} 个科室</div>
      </div>

      <div class="admin-stats-grid">
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">当前科室数量</div>
          <div class="admin-stat-soft__value">{{ total }}</div>
          <div class="admin-stat-soft__desc">支持按名称、编码与状态检索</div>
        </div>
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">管理重点</div>
          <div class="admin-stat-soft__value">结构与编码</div>
          <div class="admin-stat-soft__desc">保证医院组织树清晰、规范、可扩展</div>
        </div>
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">场景目标</div>
          <div class="admin-stat-soft__value">基础治理</div>
          <div class="admin-stat-soft__desc">为医生、挂号、病历等业务提供稳定基础数据</div>
        </div>
      </div>
    </section>

    <el-card class="admin-table-card">
      <div class="admin-toolbar">
        <div class="admin-toolbar__group">
          <el-input v-model="filters.keyword" placeholder="关键词（名称/编码）" clearable style="width: 240px" @keyup.enter="resetAndSearch" />
          <el-select v-model="filters.status" placeholder="状态" clearable style="width: 140px" @change="resetAndSearch">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
          <el-button type="primary" @click="resetAndSearch">查询</el-button>
        </div>

        <div class="admin-toolbar__group">
          <div class="admin-toolbar__meta">支持创建、编辑与维护科室层级</div>
          <el-button type="primary" @click="openCreate">新增科室</el-button>
        </div>
      </div>

      <el-table :data="records" v-loading="loading" style="width: 100%">
        <el-table-column prop="deptId" label="ID" width="90" />
        <el-table-column prop="deptName" label="科室名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="deptCode" label="科室编码" width="140" />
        <el-table-column prop="parentId" label="上级ID" width="100" />
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 16px">
        <el-pagination
          background
          layout="prev, pager, next, sizes, total"
          :total="total"
          :page-size="size"
          :page-sizes="[10, 20, 50]"
          :current-page="page + 1"
          @update:current-page="(p) => { page.value = p - 1; fetchPage() }"
          @update:page-size="(s) => { size.value = s; page.value = 0; fetchPage() }"
        />
      </div>
    </el-card>
  </div>

  <el-dialog v-model="editVisible" :title="editMode === 'create' ? '新增科室' : '编辑科室'" width="720px">
    <el-form ref="editFormRef" :model="editForm" label-width="100px" :disabled="editLoading">
      <el-form-item label="科室名称" prop="deptName" :rules="[{ required: true, message: '请输入科室名称', trigger: 'blur' }]">
        <el-input v-model="editForm.deptName" maxlength="50" />
      </el-form-item>
      <el-form-item label="科室编码" prop="deptCode" :rules="[{ required: true, message: '请输入科室编码', trigger: 'blur' }]">
        <el-input v-model="editForm.deptCode" maxlength="30" />
      </el-form-item>
      <el-form-item label="上级科室" prop="parentId" :rules="[{ required: true, message: '请选择上级科室', trigger: 'change' }]">
        <el-select v-model="editForm.parentId" style="width: 260px">
          <el-option v-for="p in parentOptions" :key="p.value" :value="p.value" :label="p.label" />
        </el-select>
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input v-model="editForm.description" maxlength="255" type="textarea" :rows="3" show-word-limit />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="editForm.sort" :min="0" :max="9999" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="editForm.status" style="width: 160px">
          <el-option :value="1" label="启用" />
          <el-option :value="0" label="禁用" />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" :loading="editLoading" @click="submitEdit">保存</el-button>
    </template>
  </el-dialog>
</template>
