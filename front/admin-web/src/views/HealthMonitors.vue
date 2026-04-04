<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'
import { useAuthStore } from '../stores/auth'
import PatientSelect from '../components/PatientSelect.vue'

const auth = useAuthStore()
const isAdmin = computed(() => auth.roleType === 1)

const loading = ref(false)

const filters = reactive({
  patientId: null,
  dateFrom: null,
  dateTo: null,
})

const page = ref(0)
const size = ref(10)
const total = ref(0)
const records = ref([])

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

const createVisible = ref(false)
const createLoading = ref(false)
const createFormRef = ref(null)
const createForm = reactive({
  patientId: null,
  monitorDate: '',
  systolicPressure: null,
  diastolicPressure: null,
  bloodGlucose: null,
  heartRate: null,
  bodyTemperature: null,
  weight: null,
  remark: '',
})

async function fetchPage() {
  loading.value = true
  try {
    const data = await http.get('/api/health-monitors', {
      params: {
        page: page.value,
        size: size.value,
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

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    detail.value = await http.get(`/api/health-monitors/${row.monitorId}`)
  } finally {
    detailLoading.value = false
  }
}

function openCreate() {
  createForm.patientId = null
  createForm.monitorDate = ''
  createForm.systolicPressure = null
  createForm.diastolicPressure = null
  createForm.bloodGlucose = null
  createForm.heartRate = null
  createForm.bodyTemperature = null
  createForm.weight = null
  createForm.remark = ''
  createVisible.value = true
}

async function submitCreate() {
  await createFormRef.value?.validate?.()

  createLoading.value = true
  try {
    await http.post('/api/health-monitors', {
      patientId: createForm.patientId,
      monitorDate: createForm.monitorDate,
      systolicPressure: createForm.systolicPressure,
      diastolicPressure: createForm.diastolicPressure,
      bloodGlucose: createForm.bloodGlucose,
      heartRate: createForm.heartRate,
      bodyTemperature: createForm.bodyTemperature,
      weight: createForm.weight,
      remark: createForm.remark || null,
    })
    ElMessage.success('已新增')
    createVisible.value = false
    fetchPage()
  } finally {
    createLoading.value = false
  }
}

async function doDelete(row) {
  await ElMessageBox.confirm('确认删除该健康数据？此操作不可恢复。', '提示', { type: 'warning' })
  await http.delete(`/api/health-monitors/${row.monitorId}`)
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
  await fetchPage()
})
</script>

<template>
  <div class="admin-page">
    <section class="admin-section">
      <div class="admin-section__head">
        <div>
          <div class="admin-section__title">健康数据管理</div>
          <div class="admin-section__subtitle">统一查看患者血压、血糖、体温、心率与体重等监测记录</div>
        </div>
        <div class="admin-note">共 {{ total }} 条监测数据</div>
      </div>

      <div class="admin-stats-grid">
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">当前记录数</div>
          <div class="admin-stat-soft__value">{{ total }}</div>
          <div class="admin-stat-soft__desc">支持按患者与时间区间快速筛选</div>
        </div>
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">监测重点</div>
          <div class="admin-stat-soft__value">多指标留痕</div>
          <div class="admin-stat-soft__desc">覆盖血压、血糖、心率、体温、体重等常见健康指标</div>
        </div>
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">管理目标</div>
          <div class="admin-stat-soft__value">趋势支撑</div>
          <div class="admin-stat-soft__desc">为患者健康追踪与临床判断提供结构化数据</div>
        </div>
      </div>
    </section>

    <el-card class="admin-table-card">
      <div class="admin-toolbar">
        <div class="admin-toolbar__group">
          <PatientSelect 
            v-model="filters.patientId" 
            placeholder="患者"
            style="width: 140px"
            @change="resetAndSearch"
          />

          <el-date-picker
            v-model="filters.dateFrom"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="开始时间"
            style="width: 180px"
            @change="resetAndSearch"
          />
          <el-date-picker
            v-model="filters.dateTo"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="结束时间"
            style="width: 180px"
            @change="resetAndSearch"
          />

          <el-button type="primary" @click="resetAndSearch">查询</el-button>
        </div>

        <div class="admin-toolbar__group">
          <div class="admin-toolbar__meta">支持新增、查看与删除健康监测数据</div>
          <el-button type="primary" @click="openCreate">新增健康数据</el-button>
        </div>
      </div>

      <el-table :data="records" v-loading="loading" style="width: 100%">
        <el-table-column prop="monitorId" label="ID" width="90" />
        <el-table-column prop="monitorDate" label="监测时间" width="170" />
        <el-table-column prop="patientId" label="患者ID" width="100" />
        <el-table-column prop="patientRealName" label="患者" width="120" />
        <el-table-column prop="systolicPressure" label="收缩压" width="90" />
        <el-table-column prop="diastolicPressure" label="舒张压" width="90" />
        <el-table-column prop="bloodGlucose" label="血糖" width="90" />
        <el-table-column prop="heartRate" label="心率" width="90" />
        <el-table-column prop="bodyTemperature" label="体温" width="90" />
        <el-table-column prop="weight" label="体重" width="90" />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
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

  <el-dialog v-model="detailVisible" title="健康数据详情" width="760px">
    <div v-loading="detailLoading">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ detail.monitorId }}</el-descriptions-item>
          <el-descriptions-item label="监测时间">{{ detail.monitorDate }}</el-descriptions-item>
          <el-descriptions-item label="患者ID">{{ detail.patientId }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.patientRealName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="患者电话">{{ detail.patientPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收缩压">{{ detail.systolicPressure ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="舒张压">{{ detail.diastolicPressure ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="血糖">{{ detail.bloodGlucose ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="心率">{{ detail.heartRate ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="体温">{{ detail.bodyTemperature ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="体重">{{ detail.weight ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </div>
    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="createVisible" title="新增健康数据" width="760px">
    <el-form ref="createFormRef" :model="createForm" label-width="110px" :disabled="createLoading">
      <el-form-item label="患者ID" prop="patientId" :rules="[{ required: true, message: '请输入患者ID', trigger: 'blur' }]">
        <el-input-number v-model="createForm.patientId" :min="1" style="width: 200px" />
      </el-form-item>
      <el-form-item label="监测时间" prop="monitorDate" :rules="[{ required: true, message: '请选择监测时间', trigger: 'change' }]">
        <el-date-picker v-model="createForm.monitorDate" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
      </el-form-item>
      <el-form-item label="收缩压" prop="systolicPressure">
        <el-input-number v-model="createForm.systolicPressure" :min="0" :max="300" style="width: 200px" />
      </el-form-item>
      <el-form-item label="舒张压" prop="diastolicPressure">
        <el-input-number v-model="createForm.diastolicPressure" :min="0" :max="200" style="width: 200px" />
      </el-form-item>
      <el-form-item label="血糖" prop="bloodGlucose">
        <el-input-number v-model="createForm.bloodGlucose" :min="0" :max="100" :precision="2" :step="0.1" style="width: 200px" />
      </el-form-item>
      <el-form-item label="心率" prop="heartRate">
        <el-input-number v-model="createForm.heartRate" :min="0" :max="300" style="width: 200px" />
      </el-form-item>
      <el-form-item label="体温" prop="bodyTemperature">
        <el-input-number v-model="createForm.bodyTemperature" :min="0" :max="50" :precision="2" :step="0.1" style="width: 200px" />
      </el-form-item>
      <el-form-item label="体重" prop="weight">
        <el-input-number v-model="createForm.weight" :min="0" :max="500" :precision="2" :step="0.1" style="width: 200px" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="createForm.remark" type="textarea" :rows="3" maxlength="255" show-word-limit />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="createVisible = false">取消</el-button>
      <el-button type="primary" :loading="createLoading" @click="submitCreate">保存</el-button>
    </template>
  </el-dialog>
</template>
