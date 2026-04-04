<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'
import { useAuthStore } from '../stores/auth'
import DepartmentSelect from '../components/DepartmentSelect.vue'
import DoctorSelect from '../components/DoctorSelect.vue'
import PatientSelect from '../components/PatientSelect.vue'

const auth = useAuthStore()
const isAdmin = computed(() => auth.roleType === 1)

const loading = ref(false)

const filters = reactive({
  keyword: '',
  recordStatus: null,
  patientId: null,
  doctorId: null,
  deptId: null,
  registrationId: null,
  visitFrom: null,
  visitTo: null,
})

const page = ref(0)
const size = ref(10)
const total = ref(0)
const records = ref([])

const deptOptions = ref([])
const doctorOptions = ref([])

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

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

async function loadDoctorOptions() {
  const data = await http.get('/api/doctors', {
    params: {
      page: 0,
      size: 200,
      deptId: filters.deptId || undefined,
    },
  })
  doctorOptions.value = (data?.records || []).map((d) => ({
    value: d.doctorId,
    label: `${d.realName || d.username || d.doctorId}`,
  }))
}

function recordStatusText(v) {
  if (v === 0) return '草稿'
  if (v === 1) return '已完成'
  return '-'
}

async function fetchPage() {
  loading.value = true
  try {
    const data = await http.get('/api/medical-records', {
      params: {
        page: page.value,
        size: size.value,
        keyword: filters.keyword?.trim() || undefined,
        recordStatus: filters.recordStatus ?? undefined,
        patientId: isAdmin.value ? filters.patientId ?? undefined : undefined,
        doctorId: isAdmin.value ? filters.doctorId ?? undefined : undefined,
        deptId: filters.deptId ?? undefined,
        registrationId: filters.registrationId ?? undefined,
        visitFrom: filters.visitFrom ?? undefined,
        visitTo: filters.visitTo ?? undefined,
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

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    detail.value = await http.get(`/api/medical-records/${row.recordId}`)
  } finally {
    detailLoading.value = false
  }
}

async function doComplete(row) {
  await ElMessageBox.confirm('确认将该病历标记为已完成？完成后医生不可再编辑。', '提示', { type: 'warning' })
  await http.post(`/api/medical-records/${row.recordId}/complete`)
  ElMessage.success('已完成')
  fetchPage()
}

async function doDelete(row) {
  await ElMessageBox.confirm('确认删除该病历？此操作不可恢复。', '提示', { type: 'warning' })
  await http.delete(`/api/medical-records/${row.recordId}`)
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
  await loadDoctorOptions()
  await fetchPage()
})
</script>

<template>
  <div class="admin-page">
    <section class="admin-section">
      <div class="admin-section__head">
        <div>
          <div class="admin-section__title">电子病历</div>
          <div class="admin-section__subtitle">围绕诊断、主诉与诊疗过程统一管理病历信息</div>
        </div>
        <div class="admin-note">共 {{ total }} 份病历</div>
      </div>

      <div class="admin-stats-grid">
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">当前病历数</div>
          <div class="admin-stat-soft__value">{{ total }}</div>
          <div class="admin-stat-soft__desc">支持按时间、科室、医生、患者等维度检索</div>
        </div>
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">核心动作</div>
          <div class="admin-stat-soft__value">查看与归档</div>
          <div class="admin-stat-soft__desc">支持查看详情、完成病历与删除病历</div>
        </div>
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">后台原则</div>
          <div class="admin-stat-soft__value">准确留痕</div>
          <div class="admin-stat-soft__desc">强化医疗信息的结构化与完整性</div>
        </div>
      </div>
    </section>

    <el-card class="admin-table-card">
      <div class="admin-toolbar">
        <div class="admin-toolbar__group">
          <el-input
            v-model="filters.keyword"
            placeholder="关键词（姓名/手机号/诊断等）"
            clearable
            style="width: 240px"
            @keyup.enter="resetAndSearch"
          />

          <el-select v-model="filters.recordStatus" placeholder="病历状态" clearable style="width: 140px" @change="resetAndSearch">
            <el-option :value="0" label="草稿" />
            <el-option :value="1" label="已完成" />
          </el-select>

          <DepartmentSelect 
            v-model="filters.deptId" 
            placeholder="科室"
            style="width: 200px"
            @change="resetAndSearch"
          />

          <DoctorSelect
            v-if="isAdmin"
            v-model="filters.doctorId"
            placeholder="医生"
            style="width: 180px"
            @change="resetAndSearch"
          />

          <PatientSelect
            v-if="isAdmin"
            v-model="filters.patientId"
            placeholder="患者"
            style="width: 140px"
            @change="resetAndSearch"
          />

          <el-input
            v-model="filters.registrationId"
            placeholder="挂号ID"
            clearable
            style="width: 140px"
            @keyup.enter="resetAndSearch"
          />

          <el-date-picker
            v-model="filters.visitFrom"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="就诊开始"
            style="width: 180px"
            @change="resetAndSearch"
          />
          <el-date-picker
            v-model="filters.visitTo"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="就诊结束"
            style="width: 180px"
            @change="resetAndSearch"
          />

          <el-button type="primary" @click="resetAndSearch">查询</el-button>
        </div>
      </div>

      <el-table :data="records" v-loading="loading" style="width: 100%">
        <el-table-column prop="recordId" label="ID" width="90" />
        <el-table-column prop="visitDate" label="就诊时间" width="170" />
        <el-table-column prop="deptId" label="科室ID" width="100" />
        <el-table-column prop="registrationId" label="挂号ID" width="100" />
        <el-table-column prop="doctorRealName" label="医生" width="120" />
        <el-table-column prop="patientRealName" label="患者" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.recordStatus === 1 ? 'success' : 'info'">
              {{ recordStatusText(row.recordStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="diagnosis" label="诊断" min-width="220" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              size="small"
              type="primary"
              :disabled="row.recordStatus === 1"
              @click="doComplete(row)"
            >
              完成
            </el-button>
            <el-button v-if="isAdmin" size="small" type="danger" @click="doDelete(row)">删除</el-button>
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

  <el-dialog v-model="detailVisible" title="病历详情" width="720px">
    <div v-loading="detailLoading">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="病历ID">{{ detail.recordId }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ recordStatusText(detail.recordStatus) }}</el-descriptions-item>
          <el-descriptions-item label="就诊时间">{{ detail.visitDate }}</el-descriptions-item>
          <el-descriptions-item label="挂号ID">{{ detail.registrationId }}</el-descriptions-item>
          <el-descriptions-item label="科室ID">{{ detail.deptId }}</el-descriptions-item>
          <el-descriptions-item label="医生">{{ detail.doctorRealName || detail.doctorId }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.patientRealName || detail.patientId }}</el-descriptions-item>
          <el-descriptions-item label="患者电话">{{ detail.patientPhone || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <el-descriptions :column="1" border>
          <el-descriptions-item label="主诉">{{ detail.chiefComplaint || '-' }}</el-descriptions-item>
          <el-descriptions-item label="现病史">{{ detail.presentIllness || '-' }}</el-descriptions-item>
          <el-descriptions-item label="既往史">{{ detail.pastHistory || '-' }}</el-descriptions-item>
          <el-descriptions-item label="体格检查">{{ detail.physicalExamination || '-' }}</el-descriptions-item>
          <el-descriptions-item label="辅助检查">{{ detail.auxiliaryExamination || '-' }}</el-descriptions-item>
          <el-descriptions-item label="诊断">{{ detail.diagnosis || '-' }}</el-descriptions-item>
          <el-descriptions-item label="治疗方案">{{ detail.treatmentPlan || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </div>
    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>
