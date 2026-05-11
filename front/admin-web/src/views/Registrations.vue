<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'
import { useAuthStore } from '../stores/auth'
import DepartmentSelect from '../components/DepartmentSelect.vue'
import DoctorSelect from '../components/DoctorSelect.vue'
import PatientSelect from '../components/PatientSelect.vue'

const router = useRouter()

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

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

const recordDialogVisible = ref(false)
const recordFormLoading = ref(false)
const recordForm = reactive({
  patientId: null,
  doctorId: null,
  deptId: null,
  registrationId: null,
  visitDate: null,
  chiefComplaint: '',
  presentIllness: '',
  pastHistory: '',
  physicalExamination: '',
  auxiliaryExamination: '',
  diagnosis: '',
  treatmentPlan: '',
  recordStatus: 0,
})

const historyDialogVisible = ref(false)
const historyLoading = ref(false)
const historyRecords = ref([])
const selectedHistoryRecord = ref(null)

const prescriptionItems = ref([])

function addPrescriptionItem() {
  prescriptionItems.value.push({
    medicationName: '',
    dosage: '',
    frequency: '每日2次',
    duration: '7天',
    note: '',
    quantity: 1,
    unit: '盒',
  })
}

function removePrescriptionItem(index) {
  prescriptionItems.value.splice(index, 1)
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
  recordForm.patientId = row.patientId
  recordForm.doctorId = row.doctorId
  recordForm.deptId = row.deptId
  recordForm.registrationId = row.registrationId
  
  // Check if patient has historical records
  try {
    const data = await http.get('/api/medical-records', {
      params: {
        page: 0,
        size: 100,
        patientId: row.patientId,
      },
    })
    
    if (data.records && data.records.length > 0) {
      // Show history dialog
      historyRecords.value = data.records
      selectedHistoryRecord.value = null
      historyDialogVisible.value = true
    } else {
      // No history, directly open new record dialog
      openNewRecordDialog()
    }
  } catch {
    // On error, directly open new record dialog
    openNewRecordDialog()
  }
}

function openNewRecordDialog() {
  recordForm.visitDate = new Date().toISOString()
  recordForm.chiefComplaint = ''
  recordForm.presentIllness = ''
  recordForm.pastHistory = ''
  recordForm.physicalExamination = ''
  recordForm.auxiliaryExamination = ''
  recordForm.diagnosis = ''
  recordForm.treatmentPlan = ''
  recordForm.recordStatus = 0
  prescriptionItems.value = []
  recordDialogVisible.value = true
}

async function selectHistoryRecord(record) {
  selectedHistoryRecord.value = record
  
  // Load full record details
  try {
    const detail = await http.get(`/api/medical-records/${record.recordId}`)
    
    // Import data from history
    recordForm.chiefComplaint = detail.chiefComplaint || ''
    recordForm.presentIllness = detail.presentIllness || ''
    recordForm.pastHistory = detail.pastHistory || ''
    recordForm.physicalExamination = detail.physicalExamination || ''
    recordForm.auxiliaryExamination = detail.auxiliaryExamination || ''
    recordForm.diagnosis = detail.diagnosis || ''
    recordForm.treatmentPlan = detail.treatmentPlan || ''
    
    // Load prescriptions if any
    try {
      const prescriptions = await http.get(`/api/prescriptions/patient/${recordForm.patientId}`)
      if (prescriptions && prescriptions.length > 0) {
        const recordPrescription = prescriptions.find(p => p.recordId === record.recordId)
        if (recordPrescription && recordPrescription.items) {
          prescriptionItems.value = recordPrescription.items.map(item => ({
            medicationName: item.medicationName,
            dosage: item.dosage,
            frequency: item.frequency,
            duration: item.duration,
            note: item.note,
            quantity: item.quantity,
            unit: item.unit,
          }))
        } else {
          prescriptionItems.value = []
        }
      } else {
        prescriptionItems.value = []
      }
    } catch {
      prescriptionItems.value = []
    }
  } catch {
    // Ignore error loading details
  }
}

async function confirmHistorySelection() {
  historyDialogVisible.value = false
  
  // Always create new record
  recordForm.visitDate = new Date().toISOString()
  recordForm.recordStatus = 0
  recordDialogVisible.value = true
}

function skipHistorySelection() {
  historyDialogVisible.value = false
  openNewRecordDialog()
}

async function submitRecord() {
  recordFormLoading.value = true
  try {
    // Always create new record
    const record = await http.post('/api/medical-records', recordForm)
    
    // Handle prescription
    if (prescriptionItems.value.length > 0) {
      const prescriptionData = {
        recordId: record.recordId,
        patientId: recordForm.patientId,
        doctorId: recordForm.doctorId,
        title: recordForm.diagnosis || '处方',
        treatmentPlan: recordForm.treatmentPlan,
        visitDate: recordForm.visitDate,
        startDate: recordForm.visitDate,
        endDate: new Date(new Date(recordForm.visitDate).getTime() + 7 * 24 * 60 * 60 * 1000).toISOString(),
        instructions: '请遵医嘱服药',
        items: prescriptionItems.value,
      }
      await http.post('/api/prescriptions', prescriptionData)
      ElMessage.success('病例和处方创建成功')
    } else {
      ElMessage.success('病例创建成功')
    }
    
    recordDialogVisible.value = false
    prescriptionItems.value = []
    
    // Mark registration as visited
    await http.post(`/api/registrations/${recordForm.registrationId}/visit`)
    
    // Navigate to medical records page
    router.push('/medical-records')
  } catch (error) {
    ElMessage.error(error?.message || '保存病例失败')
  } finally {
    recordFormLoading.value = false
  }
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
            @clear="resetAndSearch"
          />

          <DoctorSelect
            v-if="isAdmin"
            v-model="filters.doctorId"
            placeholder="医生"
            style="width: 180px"
            @change="resetAndSearch"
            @clear="resetAndSearch"
          />

          <PatientSelect
            v-if="isAdmin"
            v-model="filters.patientId"
            placeholder="患者"
            style="width: 140px"
            @change="resetAndSearch"
            @clear="resetAndSearch"
          />

          <el-date-picker
            v-model="filters.dateFrom"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="开始日期"
            style="width: 140px"
            @change="resetAndSearch"
            @clear="resetAndSearch"
          />
          <el-date-picker
            v-model="filters.dateTo"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="结束日期"
            style="width: 140px"
            @change="resetAndSearch"
            @clear="resetAndSearch"
          />

          <el-button type="primary" @click="resetAndSearch">查询</el-button>
        </div>
      </div>

      <el-table :data="records" v-loading="loading" style="width: 100%">
        <el-table-column prop="registrationId" label="ID" width="90" />
        <el-table-column prop="registrationNo" label="挂号单号" width="190" />
        <el-table-column prop="scheduleDate" label="日期" width="120" />
        <el-table-column prop="timeSlot" label="时段" width="100" />
        <el-table-column prop="deptName" label="科室" width="140" />
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

  <el-dialog v-model="recordDialogVisible" title="新增病例" width="900px">
    <el-form v-loading="recordFormLoading" label-width="120px">
      <el-form-item label="就诊时间">
        <el-date-picker v-model="recordForm.visitDate" type="datetime" style="width: 100%" />
      </el-form-item>
      <el-form-item label="主诉">
        <el-input v-model="recordForm.chiefComplaint" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="现病史">
        <el-input v-model="recordForm.presentIllness" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="既往史">
        <el-input v-model="recordForm.pastHistory" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="体格检查">
        <el-input v-model="recordForm.physicalExamination" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="辅助检查">
        <el-input v-model="recordForm.auxiliaryExamination" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="诊断">
        <el-input v-model="recordForm.diagnosis" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="治疗方案">
        <el-input v-model="recordForm.treatmentPlan" type="textarea" :rows="3" />
      </el-form-item>
      
      <el-divider>开具处方</el-divider>
      
      <div v-if="prescriptionItems.length === 0" style="text-align: center; padding: 20px; color: #999;">
        暂无处方药物，点击下方按钮添加
      </div>
      
      <div v-for="(item, index) in prescriptionItems" :key="index" style="margin-bottom: 16px; padding: 16px; border: 1px solid #eee; border-radius: 8px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
          <span style="font-weight: bold;">药物 #{{ index + 1 }}</span>
          <el-button type="danger" size="small" @click="removePrescriptionItem(index)">删除</el-button>
        </div>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="药物名称" label-width="80px">
              <el-input v-model="item.medicationName" placeholder="请输入药物名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="剂量" label-width="60px">
              <el-input v-model="item.dosage" placeholder="如：1片" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="频次" label-width="60px">
              <el-select v-model="item.frequency" style="width: 100%">
                <el-option label="每日1次" value="每日1次" />
                <el-option label="每日2次" value="每日2次" />
                <el-option label="每日3次" value="每日3次" />
                <el-option label="每日4次" value="每日4次" />
                <el-option label="隔日1次" value="隔日1次" />
                <el-option label="每周1次" value="每周1次" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="疗程" label-width="60px">
              <el-input v-model="item.duration" placeholder="如：7天" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="数量" label-width="60px">
              <el-input-number v-model="item.quantity" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="单位" label-width="60px">
              <el-select v-model="item.unit" style="width: 100%">
                <el-option label="盒" value="盒" />
                <el-option label="瓶" value="瓶" />
                <el-option label="袋" value="袋" />
                <el-option label="支" value="支" />
                <el-option label="片" value="片" />
                <el-option label="粒" value="粒" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注" label-width="60px">
              <el-input v-model="item.note" placeholder="用药说明" />
            </el-form-item>
          </el-col>
        </el-row>
      </div>
      
      <el-button type="primary" plain style="width: 100%" @click="addPrescriptionItem">+ 添加药物</el-button>
    </el-form>
    <template #footer>
      <el-button @click="recordDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitRecord" :loading="recordFormLoading">保存并跳转到病例列表</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="historyDialogVisible" title="选择历史病例" width="800px">
    <div v-loading="historyLoading">
      <el-table :data="historyRecords" @row-click="selectHistoryRecord" highlight-current-row :current-row-key="selectedHistoryRecord?.recordId" row-key="recordId" style="cursor: pointer">
        <el-table-column prop="visitDate" label="就诊时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.visitDate).toLocaleString('zh-CN') }}
          </template>
        </el-table-column>
        <el-table-column prop="patientRealName" label="患者姓名" width="120" />
        <el-table-column prop="diagnosis" label="诊断" width="150" />
        <el-table-column prop="chiefComplaint" label="主诉" show-overflow-tooltip />
        <el-table-column prop="treatmentPlan" label="治疗方案" show-overflow-tooltip />
      </el-table>
      <div v-if="selectedHistoryRecord" style="margin-top: 20px; padding: 15px; background: #f5f7fa; border-radius: 8px;">
        <div style="font-weight: bold; margin-bottom: 10px;">已选择病例详情：</div>
        <div><strong>就诊时间：</strong>{{ new Date(selectedHistoryRecord.visitDate).toLocaleString('zh-CN') }}</div>
        <div><strong>诊断：</strong>{{ selectedHistoryRecord.diagnosis || '-' }}</div>
        <div><strong>主诉：</strong>{{ selectedHistoryRecord.chiefComplaint || '-' }}</div>
        <div><strong>现病史：</strong>{{ selectedHistoryRecord.presentIllness || '-' }}</div>
        <div><strong>治疗方案：</strong>{{ selectedHistoryRecord.treatmentPlan || '-' }}</div>
      </div>
    </div>
    <template #footer>
      <el-button @click="skipHistorySelection">不选择，直接开新病例</el-button>
      <el-button type="primary" @click="confirmHistorySelection" :disabled="!selectedHistoryRecord">导入数据并开新病例</el-button>
    </template>
  </el-dialog>
</template>
