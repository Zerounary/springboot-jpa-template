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
  payStatus: null,
  registrationStatus: null,
  deptId: null,
  doctorId: null,
  patientId: null,
  dateFrom: null,
  dateTo: null,
})

const page = ref(0)
const size = ref(10)
const total = ref(0)
const records = ref([])

const deptOptions = ref([])
const doctorOptions = ref([])

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

async function fetchPage() {
  loading.value = true
  try {
    const data = await http.get('/api/registrations', {
      params: {
        page: page.value,
        size: size.value,
        payStatus: filters.payStatus ?? undefined,
        registrationStatus: filters.registrationStatus ?? undefined,
        deptId: filters.deptId ?? undefined,
        doctorId: isAdmin.value ? filters.doctorId ?? undefined : undefined,
        patientId: isAdmin.value ? filters.patientId ?? undefined : undefined,
        dateFrom: filters.dateFrom ?? undefined,
        dateTo: filters.dateTo ?? undefined,
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

async function doPay(row) {
  await ElMessageBox.confirm('确认支付该挂号？', '提示', { type: 'warning' })
  await http.post(`/api/registrations/${row.registrationId}/pay`)
  ElMessage.success('已支付')
  fetchPage()
}

async function doCancel(row) {
  await ElMessageBox.confirm('确认取消该挂号？', '提示', { type: 'warning' })
  await http.post(`/api/registrations/${row.registrationId}/cancel`)
  ElMessage.success('已取消')
  fetchPage()
}

async function doVisit(row) {
  await ElMessageBox.confirm('确认标记为已就诊？', '提示', { type: 'warning' })
  await http.post(`/api/registrations/${row.registrationId}/visit`)
  ElMessage.success('已标记')
  fetchPage()
}

function payStatusText(v) {
  if (v === 0) return '未支付'
  if (v === 1) return '已支付'
  if (v === 2) return '已退款'
  return '-'
}

function registrationStatusText(v) {
  if (v === 0) return '待就诊'
  if (v === 1) return '已就诊'
  if (v === 2) return '已取消'
  return '-'
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
          <div class="admin-section__title">挂号管理</div>
          <div class="admin-section__subtitle">统一跟踪预约、支付、取消和就诊状态</div>
        </div>
        <div class="admin-note">共 {{ total }} 条挂号</div>
      </div>

      <div class="admin-stats-grid">
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">当前数据量</div>
          <div class="admin-stat-soft__value">{{ total }}</div>
          <div class="admin-stat-soft__desc">适合按状态、科室和时间筛选</div>
        </div>
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">流程关键点</div>
          <div class="admin-stat-soft__value">支付与就诊</div>
          <div class="admin-stat-soft__desc">确保挂号链路状态清晰且可操作</div>
        </div>
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">后台目标</div>
          <div class="admin-stat-soft__value">高效流转</div>
          <div class="admin-stat-soft__desc">帮助医院前台与管理岗协同处理预约记录</div>
        </div>
      </div>
    </section>

    <el-card class="admin-table-card">
      <div class="admin-toolbar">
        <div class="admin-toolbar__group">
          <el-select v-model="filters.payStatus" placeholder="支付状态" clearable style="width: 140px" @change="resetAndSearch">
            <el-option :value="0" label="未支付" />
            <el-option :value="1" label="已支付" />
            <el-option :value="2" label="已退款" />
          </el-select>

          <el-select
            v-model="filters.registrationStatus"
            placeholder="挂号状态"
            clearable
            style="width: 140px"
            @change="resetAndSearch"
          >
            <el-option :value="0" label="待就诊" />
            <el-option :value="1" label="已就诊" />
            <el-option :value="2" label="已取消" />
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

          <el-date-picker
            v-model="filters.dateFrom"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="开始日期"
            style="width: 140px"
            @change="resetAndSearch"
          />
          <el-date-picker
            v-model="filters.dateTo"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="结束日期"
            style="width: 140px"
            @change="resetAndSearch"
          />

          <el-button type="primary" @click="resetAndSearch">查询</el-button>
        </div>
      </div>

      <el-table :data="records" v-loading="loading" style="width: 100%">
        <el-table-column prop="registrationId" label="ID" width="90" />
        <el-table-column prop="registrationNo" label="挂号单号" width="190" />
        <el-table-column prop="scheduleDate" label="日期" width="120" />
        <el-table-column prop="timeSlot" label="时段" width="100" />
        <el-table-column prop="deptId" label="科室ID" width="100" />
        <el-table-column prop="doctorRealName" label="医生" width="120" />
        <el-table-column prop="patientRealName" label="患者" width="120" />
        <el-table-column prop="registrationFee" label="费用" width="90" />
        <el-table-column label="支付状态" width="100">
          <template #default="{ row }">{{ payStatusText(row.payStatus) }}</template>
        </el-table-column>
        <el-table-column label="挂号状态" width="100">
          <template #default="{ row }">{{ registrationStatusText(row.registrationStatus) }}</template>
        </el-table-column>
        <el-table-column prop="visitSerialNumber" label="序号" width="90" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="success"
              :disabled="row.registrationStatus !== 0 || row.payStatus !== 0"
              @click="doPay(row)"
            >
              支付
            </el-button>
            <el-button
              size="small"
              type="warning"
              :disabled="row.registrationStatus !== 0"
              @click="doCancel(row)"
            >
              取消
            </el-button>
            <el-button
              size="small"
              type="primary"
              :disabled="row.registrationStatus !== 0"
              @click="doVisit(row)"
            >
              就诊
            </el-button>
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
</template>
