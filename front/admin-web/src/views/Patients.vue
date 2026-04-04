<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'
import { useAuthStore } from '../stores/auth'
import PatientSelect from '../components/PatientSelect.vue'

const auth = useAuthStore()

const loading = ref(false)

const keyword = ref('')

const page = ref(0)
const size = ref(10)
const total = ref(0)
const records = ref([])

const editVisible = ref(false)
const editMode = ref('create')
const editLoading = ref(false)
const editFormRef = ref(null)

const editForm = reactive({
  patientId: null,
  userId: null,
  birthDate: null,
  age: null,
  bloodType: '',
  maritalStatus: null,
  address: '',
  emergencyContact: '',
  emergencyPhone: '',
  allergyHistory: '',
  pastMedicalHistory: '',
})

function genderText(v) {
  if (v === 1) return '男'
  if (v === 2) return '女'
  return '-'
}

async function fetchPage() {
  loading.value = true
  try {
    const data = await http.get('/api/patients', {
      params: {
        page: page.value,
        size: size.value,
        keyword: keyword.value?.trim() || undefined,
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
  editForm.patientId = null
  editForm.userId = null
  editForm.birthDate = null
  editForm.age = null
  editForm.bloodType = ''
  editForm.maritalStatus = null
  editForm.address = ''
  editForm.emergencyContact = ''
  editForm.emergencyPhone = ''
  editForm.allergyHistory = ''
  editForm.pastMedicalHistory = ''
  editVisible.value = true
}

async function openEdit(row) {
  editMode.value = 'edit'
  editVisible.value = true
  editLoading.value = true
  try {
    const d = await http.get(`/api/patients/${row.patientId}`)
    editForm.patientId = d.patientId
    editForm.userId = d.userId
    editForm.birthDate = d.birthDate || null
    editForm.age = d.age ?? null
    editForm.bloodType = d.bloodType || ''
    editForm.maritalStatus = d.maritalStatus ?? null
    editForm.address = d.address || ''
    editForm.emergencyContact = d.emergencyContact || ''
    editForm.emergencyPhone = d.emergencyPhone || ''
    editForm.allergyHistory = d.allergyHistory || ''
    editForm.pastMedicalHistory = d.pastMedicalHistory || ''
  } finally {
    editLoading.value = false
  }
}

async function submitEdit() {
  await editFormRef.value?.validate?.()

  editLoading.value = true
  try {
    if (editMode.value === 'create') {
      await http.post('/api/patients', {
        userId: editForm.userId,
        birthDate: editForm.birthDate || null,
        age: editForm.age ?? null,
        bloodType: editForm.bloodType || null,
        maritalStatus: editForm.maritalStatus ?? null,
        address: editForm.address || null,
        emergencyContact: editForm.emergencyContact || null,
        emergencyPhone: editForm.emergencyPhone || null,
        allergyHistory: editForm.allergyHistory || null,
        pastMedicalHistory: editForm.pastMedicalHistory || null,
      })
      ElMessage.success('已创建')
    } else {
      await http.put(`/api/patients/${editForm.patientId}`, {
        birthDate: editForm.birthDate || null,
        age: editForm.age ?? null,
        bloodType: editForm.bloodType || null,
        maritalStatus: editForm.maritalStatus ?? null,
        address: editForm.address || null,
        emergencyContact: editForm.emergencyContact || null,
        emergencyPhone: editForm.emergencyPhone || null,
        allergyHistory: editForm.allergyHistory || null,
        pastMedicalHistory: editForm.pastMedicalHistory || null,
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
  await ElMessageBox.confirm(`确认删除患者档案（ID=${row.patientId}）？`, '提示', { type: 'warning' })
  await http.delete(`/api/patients/${row.patientId}`)
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
  fetchPage()
})
</script>

<template>
  <div class="admin-page">
    <section class="admin-section">
      <div class="admin-section__head">
        <div>
          <div class="admin-section__title">患者管理</div>
          <div class="admin-section__subtitle">维护患者基础档案、紧急联系人与健康背景信息</div>
        </div>
        <div class="admin-note">共 {{ total }} 位患者</div>
      </div>

      <div class="admin-stats-grid">
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">当前患者数</div>
          <div class="admin-stat-soft__value">{{ total }}</div>
          <div class="admin-stat-soft__desc">支持按用户名、姓名、手机号快速检索</div>
        </div>
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">档案重点</div>
          <div class="admin-stat-soft__value">基础信息</div>
          <div class="admin-stat-soft__desc">统一维护年龄、血型、婚姻状态与紧急联系人</div>
        </div>
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">服务目标</div>
          <div class="admin-stat-soft__value">连续管理</div>
          <div class="admin-stat-soft__desc">为挂号、病历与健康监测提供完整患者主数据</div>
        </div>
      </div>
    </section>

    <el-card class="admin-table-card">
      <div class="admin-toolbar">
        <div class="admin-toolbar__group">
          <el-input v-model="keyword" placeholder="关键词（用户名/姓名/手机号）" clearable style="width: 240px" @keyup.enter="resetAndSearch" />
          <el-button type="primary" @click="resetAndSearch">查询</el-button>
        </div>

        <div class="admin-toolbar__group">
          <div class="admin-toolbar__meta">支持新增、编辑与删除患者档案</div>
          <el-button type="primary" @click="openCreate">新增患者</el-button>
        </div>
      </div>

      <el-table :data="records" v-loading="loading" style="width: 100%">
        <el-table-column prop="patientId" label="患者ID" width="100" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="姓名" width="140" />
        <el-table-column label="性别" width="90">
          <template #default="{ row }">{{ genderText(row.gender) }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="birthDate" label="生日" width="120" />
        <el-table-column prop="age" label="年龄" width="90" />
        <el-table-column prop="bloodType" label="血型" width="100" />
        <el-table-column prop="maritalStatus" label="婚姻" width="90" />
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

  <el-dialog v-model="editVisible" :title="editMode === 'create' ? '新增患者' : '编辑患者'" width="760px">
    <el-form ref="editFormRef" :model="editForm" label-width="110px" :disabled="editLoading">
      <el-form-item
        label="用户"
        prop="userId"
        :rules="editMode === 'create' ? [{ required: true, message: '请选择用户（需为患者角色）', trigger: 'change' }] : []"
      >
        <PatientSelect 
          v-model="editForm.userId" 
          :disabled="editMode !== 'create'"
          style="width: 260px"
          placeholder="请搜索并选择患者用户"
        />
      </el-form-item>

      <el-form-item label="生日" prop="birthDate">
        <el-date-picker v-model="editForm.birthDate" type="date" value-format="YYYY-MM-DD" style="width: 220px" />
      </el-form-item>

      <el-form-item label="年龄" prop="age">
        <el-input-number v-model="editForm.age" :min="0" :max="200" style="width: 220px" />
      </el-form-item>

      <el-form-item label="血型" prop="bloodType">
        <el-input v-model="editForm.bloodType" maxlength="10" style="width: 220px" />
      </el-form-item>

      <el-form-item label="婚姻状态" prop="maritalStatus">
        <el-select v-model="editForm.maritalStatus" clearable style="width: 220px">
          <el-option :value="1" label="未婚" />
          <el-option :value="2" label="已婚" />
          <el-option :value="3" label="离异" />
          <el-option :value="4" label="丧偶" />
        </el-select>
      </el-form-item>

      <el-form-item label="地址" prop="address">
        <el-input v-model="editForm.address" maxlength="255" />
      </el-form-item>

      <el-form-item label="紧急联系人" prop="emergencyContact">
        <el-input v-model="editForm.emergencyContact" maxlength="20" style="width: 220px" />
      </el-form-item>

      <el-form-item label="紧急联系电话" prop="emergencyPhone">
        <el-input v-model="editForm.emergencyPhone" maxlength="11" style="width: 220px" />
      </el-form-item>

      <el-form-item label="过敏史" prop="allergyHistory">
        <el-input v-model="editForm.allergyHistory" type="textarea" :rows="3" />
      </el-form-item>

      <el-form-item label="既往史" prop="pastMedicalHistory">
        <el-input v-model="editForm.pastMedicalHistory" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" :loading="editLoading" @click="submitEdit">保存</el-button>
    </template>
  </el-dialog>
</template>
