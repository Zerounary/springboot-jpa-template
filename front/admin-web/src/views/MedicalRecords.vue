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

const editDialogVisible = ref(false)
const editFormLoading = ref(false)
const editForm = reactive({
  recordId: null,
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

const prescriptionItems = ref([])

const statsDialogVisible = ref(false)
const statsLoading = ref(false)
const patientStats = ref(null)
const patientPrescriptions = ref([])

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

async function openEdit(row) {
  editFormLoading.value = true
  try {
    const record = await http.get(`/api/medical-records/${row.recordId}`)
    editForm.recordId = record.recordId
    editForm.patientId = record.patientId
    editForm.doctorId = record.doctorId
    editForm.deptId = record.deptId
    editForm.registrationId = record.registrationId
    editForm.visitDate = record.visitDate
    editForm.chiefComplaint = record.chiefComplaint || ''
    editForm.presentIllness = record.presentIllness || ''
    editForm.pastHistory = record.pastHistory || ''
    editForm.physicalExamination = record.physicalExamination || ''
    editForm.auxiliaryExamination = record.auxiliaryExamination || ''
    editForm.diagnosis = record.diagnosis || ''
    editForm.treatmentPlan = record.treatmentPlan || ''
    editForm.recordStatus = record.recordStatus || 0
    
    // Load existing prescriptions
    try {
      const prescriptions = await http.get(`/api/prescriptions/patient/${record.patientId}`)
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
    
    editDialogVisible.value = true
  } finally {
    editFormLoading.value = false
  }
}

async function submitEdit() {
  editFormLoading.value = true
  try {
    await http.put(`/api/medical-records/${editForm.recordId}`, editForm)
    
    // Handle prescription
    if (prescriptionItems.value.length > 0) {
      // Check if prescription already exists for this record and delete it
      try {
        const prescriptions = await http.get(`/api/prescriptions/patient/${editForm.patientId}`)
        const existingPrescription = prescriptions.find(p => p.recordId === editForm.recordId)
        
        if (existingPrescription) {
          await http.delete(`/api/prescriptions/${existingPrescription.prescriptionId}`)
        }
      } catch {
        // Ignore error if no prescriptions exist or delete fails
      }
      
      const prescriptionData = {
        recordId: editForm.recordId,
        patientId: editForm.patientId,
        doctorId: editForm.doctorId,
        title: editForm.diagnosis || '处方',
        treatmentPlan: editForm.treatmentPlan,
        visitDate: editForm.visitDate,
        startDate: editForm.visitDate,
        endDate: new Date(new Date(editForm.visitDate).getTime() + 7 * 24 * 60 * 60 * 1000).toISOString(),
        instructions: '请遵医嘱服药',
        items: prescriptionItems.value,
      }
      await http.post('/api/prescriptions', prescriptionData)
      ElMessage.success('病例和处方更新成功')
    } else {
      // If no medication items, check if there's an existing prescription and delete it
      try {
        const prescriptions = await http.get(`/api/prescriptions/patient/${editForm.patientId}`)
        const existingPrescription = prescriptions.find(p => p.recordId === editForm.recordId)
        
        if (existingPrescription) {
          await http.delete(`/api/prescriptions/${existingPrescription.prescriptionId}`)
        }
      } catch {
        // Ignore error
      }
      ElMessage.success('病例更新成功')
    }
    
    editDialogVisible.value = false
    prescriptionItems.value = []
    fetchPage()
  } catch (error) {
    ElMessage.error(error?.message || '更新病例失败')
  } finally {
    editFormLoading.value = false
  }
}

async function openPatientStats(row) {
  statsDialogVisible.value = true
  statsLoading.value = true
  try {
    const [stats, prescriptions] = await Promise.all([
      http.get(`/api/prescriptions/patient/${row.patientId}/adherence-stats`),
      http.get(`/api/prescriptions/patient/${row.patientId}`),
    ])
    patientStats.value = stats
    patientPrescriptions.value = prescriptions || []
  } catch (error) {
    ElMessage.error(error?.message || '加载用药统计失败')
  } finally {
    statsLoading.value = false
  }
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
        <el-table-column label="操作" width="400" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              size="small"
              type="primary"
              :disabled="row.recordStatus === 1"
              @click="openEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="success"
              :disabled="row.recordStatus === 1"
              @click="doComplete(row)"
            >
              完成
            </el-button>
            <el-button size="small" type="info" @click="openPatientStats(row)">用药统计</el-button>
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
          @update:current-page="(p) => { page = p - 1; fetchPage() }"
          @update:page-size="(s) => { size = s; page = 0; fetchPage() }"
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

  <el-dialog v-model="editDialogVisible" title="编辑病例" width="900px">
    <el-form v-loading="editFormLoading" label-width="120px">
      <el-form-item label="就诊时间">
        <el-date-picker v-model="editForm.visitDate" type="datetime" style="width: 100%" />
      </el-form-item>
      <el-form-item label="主诉">
        <el-input v-model="editForm.chiefComplaint" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="现病史">
        <el-input v-model="editForm.presentIllness" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="既往史">
        <el-input v-model="editForm.pastHistory" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="体格检查">
        <el-input v-model="editForm.physicalExamination" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="辅助检查">
        <el-input v-model="editForm.auxiliaryExamination" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="诊断">
        <el-input v-model="editForm.diagnosis" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="治疗方案">
        <el-input v-model="editForm.treatmentPlan" type="textarea" :rows="3" />
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
      <el-button @click="editDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitEdit" :loading="editFormLoading">保存病例和处方</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="statsDialogVisible" title="患者用药统计" width="700px">
    <div v-loading="statsLoading">
      <template v-if="patientStats">
        <el-row :gutter="20" style="margin-bottom: 20px">
          <el-col :span="8">
            <el-statistic title="计划服药次数" :value="patientStats.expectedCount" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="实际服药次数" :value="patientStats.takenCount" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="依从率" :value="patientStats.rate" suffix="%" />
          </el-col>
        </el-row>
        
        <el-divider />
        
        <div style="margin-bottom: 20px">
          <h4>处方列表</h4>
          <div v-if="patientPrescriptions.length === 0" style="color: #999; padding: 20px 0;">
            暂无处方
          </div>
          <div v-for="prescription in patientPrescriptions" :key="prescription.id" style="margin-bottom: 12px; padding: 12px; border: 1px solid #eee; border-radius: 8px;">
            <div style="font-weight: bold; margin-bottom: 8px;">{{ prescription.title }}</div>
            <div style="font-size: 13px; color: #666; margin-bottom: 4px;">医生：{{ prescription.doctorName }}</div>
            <div style="font-size: 13px; color: #666; margin-bottom: 4px;">开具时间：{{ prescription.createdAt }}</div>
            <div style="font-size: 13px; color: #666; margin-bottom: 8px;">提醒时间：{{ prescription.reminderTimes?.join('、') || '-' }}</div>
            <div v-if="prescription.items && prescription.items.length > 0" style="margin-top: 8px;">
              <div v-for="(item, index) in prescription.items" :key="index" style="font-size: 13px; padding: 4px 0; border-top: 1px dashed #eee;">
                {{ item.medicationName }} - {{ item.dosage }} - {{ item.frequency }} - {{ item.duration }}
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
    <template #footer>
      <el-button @click="statsDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>
