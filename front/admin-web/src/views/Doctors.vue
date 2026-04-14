<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'
import { useAuthStore } from '../stores/auth'
import DoctorSelect from '../components/DoctorSelect.vue'
import DepartmentSelect from '../components/DepartmentSelect.vue'

const auth = useAuthStore()

const loading = ref(false)

const filters = reactive({
  deptId: null,
  keyword: '',
})

const page = ref(0)
const size = ref(10)
const total = ref(0)
const records = ref([])

const deptOptions = ref([])

const editVisible = ref(false)
const editMode = ref('create')
const editLoading = ref(false)
const editFormRef = ref(null)

const editForm = reactive({
  doctorId: null,
  userId: null,
  deptId: null,
  jobTitle: '',
  specialty: '',
  introduction: '',
  registrationFee: 0,
  dailyAppointmentLimit: 0,
  schedule: '',
})

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

async function loadDeptOptions() {
  const tree = await http.get('/api/departments/tree', { params: { includeDisabled: true } })
  deptOptions.value = flattenDeptTree(tree)
}

async function fetchPage() {
  loading.value = true
  try {
    const data = await http.get('/api/doctors', {
      params: {
        page: page.value,
        size: size.value,
        deptId: filters.deptId ?? undefined,
        keyword: filters.keyword?.trim() || undefined,
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
  editForm.doctorId = null
  editForm.userId = null
  editForm.deptId = null
  editForm.jobTitle = ''
  editForm.specialty = ''
  editForm.introduction = ''
  editForm.registrationFee = 0
  editForm.dailyAppointmentLimit = 0
  editForm.schedule = ''
  editVisible.value = true
}

async function openEdit(row) {
  editMode.value = 'edit'
  editVisible.value = true
  editLoading.value = true
  try {
    const d = await http.get(`/api/doctors/${row.doctorId}`)
    editForm.doctorId = d.doctorId
    editForm.userId = d.userId
    editForm.deptId = d.deptId
    editForm.jobTitle = d.jobTitle || ''
    editForm.specialty = d.specialty || ''
    editForm.introduction = d.introduction || ''
    editForm.registrationFee = Number(d.registrationFee || 0)
    editForm.dailyAppointmentLimit = Number(d.dailyAppointmentLimit || 0)
    editForm.schedule = d.schedule || ''
  } finally {
    editLoading.value = false
  }
}

async function submitEdit() {
  await editFormRef.value?.validate?.()

  editLoading.value = true
  try {
    if (editMode.value === 'create') {
      await http.post('/api/doctors', {
        userId: editForm.userId,
        deptId: editForm.deptId,
        jobTitle: editForm.jobTitle,
        specialty: editForm.specialty,
        introduction: editForm.introduction || null,
        registrationFee: editForm.registrationFee,
        dailyAppointmentLimit: editForm.dailyAppointmentLimit,
        schedule: editForm.schedule || null,
      })
      ElMessage.success('已创建')
    } else {
      await http.put(`/api/doctors/${editForm.doctorId}`, {
        deptId: editForm.deptId,
        jobTitle: editForm.jobTitle,
        specialty: editForm.specialty,
        introduction: editForm.introduction || null,
        registrationFee: editForm.registrationFee,
        dailyAppointmentLimit: editForm.dailyAppointmentLimit,
        schedule: editForm.schedule || null,
      })
      ElMessage.success('已保存')
    }
    editVisible.value = false
    fetchPage()
  } finally {
    editLoading.value = false
  }
}

async function doDelete(row) {
  await ElMessageBox.confirm(`确认删除医生档案（ID=${row.doctorId}）？`, '提示', { type: 'warning' })
  await http.delete(`/api/doctors/${row.doctorId}`)
  ElMessage.success('已删除')
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
  await loadDeptOptions()
  fetchPage()
})
</script>

<template>
  <div class="admin-page">
    <section class="admin-section">
      <div class="admin-section__head">
        <div>
          <div class="admin-section__title">医生管理</div>
          <div class="admin-section__subtitle">维护医生档案、科室归属、职称、专长与出诊安排</div>
        </div>
        <div class="admin-note">共 {{ total }} 位医生</div>
      </div>

      <div class="admin-stats-grid">
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">当前医生数量</div>
          <div class="admin-stat-soft__value">{{ total }}</div>
          <div class="admin-stat-soft__desc">可按科室与关键词快速筛选</div>
        </div>
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">管理重点</div>
          <div class="admin-stat-soft__value">排班与专长</div>
          <div class="admin-stat-soft__desc">突出医疗服务能力与预约效率</div>
        </div>
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">使用场景</div>
          <div class="admin-stat-soft__value">档案维护</div>
          <div class="admin-stat-soft__desc">支持新增、编辑、删除医生业务档案</div>
        </div>
      </div>
    </section>

    <el-card class="admin-table-card">
      <div class="admin-toolbar">
        <div class="admin-toolbar__group">
          <el-select v-model="filters.deptId" placeholder="科室" clearable style="width: 220px" @change="resetAndSearch">
            <el-option v-for="d in deptOptions" :key="d.value" :value="d.value" :label="d.label" />
          </el-select>
          <el-input v-model="filters.keyword" placeholder="关键词（用户名/姓名/手机号）" clearable style="width: 240px" @keyup.enter="resetAndSearch" />
          <el-button type="primary" @click="resetAndSearch">查询</el-button>
        </div>

        <div class="admin-toolbar__group">
          <div class="admin-toolbar__meta">突出医生档案的专业性与可维护性</div>
          <el-button type="primary" @click="openCreate">新增医生</el-button>
        </div>
      </div>

      <el-table :data="records" v-loading="loading" style="width: 100%">
        <el-table-column prop="doctorId" label="ID" width="90" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="姓名" width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="deptId" label="科室ID" width="100" />
        <el-table-column prop="jobTitle" label="职称" width="140" show-overflow-tooltip />
        <el-table-column prop="specialty" label="专长" min-width="200" show-overflow-tooltip />
        <el-table-column prop="registrationFee" label="挂号费" width="100" />
        <el-table-column prop="dailyAppointmentLimit" label="每日限号" width="110" />
        <el-table-column prop="schedule" label="出诊安排" min-width="160" show-overflow-tooltip />
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
          @update:current-page="(p) => { page = p - 1; fetchPage() }"
          @update:page-size="(s) => { size = s; page = 0; fetchPage() }"
        />
      </div>
    </el-card>
  </div>

  <el-dialog v-model="editVisible" :title="editMode === 'create' ? '新增医生' : '编辑医生'" width="760px">
    <el-form ref="editFormRef" :model="editForm" label-width="100px" :disabled="editLoading">
      <el-form-item
        label="用户"
        prop="userId"
        :rules="editMode === 'create' ? [{ required: true, message: '请选择用户（需为医生角色）', trigger: 'change' }] : []"
      >
        <DoctorSelect 
          v-model="editForm.userId" 
          :disabled="editMode !== 'create'"
          style="width: 260px"
          placeholder="请搜索并选择医生用户"
        />
      </el-form-item>

      <el-form-item label="科室" prop="deptId" :rules="[{ required: true, message: '请选择科室', trigger: 'change' }]">
        <DepartmentSelect 
          v-model="editForm.deptId" 
          style="width: 260px"
          placeholder="请搜索并选择科室"
        />
      </el-form-item>

      <el-form-item label="职称" prop="jobTitle" :rules="[{ required: true, message: '请输入职称', trigger: 'blur' }]">
        <el-input v-model="editForm.jobTitle" maxlength="30" />
      </el-form-item>

      <el-form-item label="专长" prop="specialty" :rules="[{ required: true, message: '请输入专长', trigger: 'blur' }]">
        <el-input v-model="editForm.specialty" maxlength="255" />
      </el-form-item>

      <el-form-item label="简介" prop="introduction">
        <el-input v-model="editForm.introduction" type="textarea" :rows="3" />
      </el-form-item>

      <el-form-item label="挂号费" prop="registrationFee" :rules="[{ required: true, message: '请输入挂号费', trigger: 'change' }]">
        <el-input-number v-model="editForm.registrationFee" :min="0" :precision="2" :step="1" style="width: 220px" />
      </el-form-item>

      <el-form-item label="每日限号" prop="dailyAppointmentLimit" :rules="[{ required: true, message: '请输入每日限号', trigger: 'change' }]">
        <el-input-number v-model="editForm.dailyAppointmentLimit" :min="0" :precision="0" :step="1" style="width: 220px" />
      </el-form-item>

      <el-form-item label="出诊安排" prop="schedule">
        <el-input v-model="editForm.schedule" maxlength="255" placeholder="如：周一/三上午" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" :loading="editLoading" @click="submitEdit">保存</el-button>
    </template>
  </el-dialog>
</template>
