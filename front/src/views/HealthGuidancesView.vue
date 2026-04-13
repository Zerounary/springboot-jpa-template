<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { type Dayjs } from 'dayjs'
import { mlModelListApi, type MlModelDto } from '../api/mlModels'
import { predictionResultDetailApi } from '../api/predictionResults'
import { useAuthStore } from '../stores/auth'
import { patientDetailApi, patientPageApi, type PatientDto } from '../api/patients'
import { userDetailApi } from '../api/users'
import {
  healthGuidanceCreateApi,
  healthGuidanceDetailApi,
  healthGuidanceDeleteApi,
  healthGuidanceMineApi,
  healthGuidancePageApi,
  healthGuidanceUpdateApi,
  type HealthGuidanceCreateRequest,
  type HealthGuidanceDto,
  type HealthGuidanceUpdateRequest,
} from '../api/healthGuidances'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const isPatient = computed(() => auth.user?.role === 'PATIENT')
const canManage = computed(() => auth.user?.role === 'DOCTOR' || auth.user?.role === 'ADMIN')

const patientOptions = ref<PatientDto[]>([])
const patientLabelMap = ref<Record<number, string>>({})
const patientSearching = ref(false)
const doctorLabelMap = ref<Record<number, string>>({})
const modelLabelMap = ref<Record<number, string>>({})
const predictionModelLabelMap = ref<Record<number, string>>({})

const loading = ref(false)
const rows = ref<HealthGuidanceDto[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const query = reactive({
  patientId: null as number | null,
  timeRange: null as [Dayjs, Dayjs] | null,
})

const modalOpen = ref(false)
const modalLoading = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  patientId: null as number | null,
  predictionResultId: null as number | null,
  guidanceTitle: '',
  guidanceContent: '',
  guidanceLevel: 1,
})

const modalTitle = computed(() => (editingId.value ? '编辑健康指导' : '新增健康指导'))

const detailOpen = ref(false)
const detailLoading = ref(false)
const detail = ref<HealthGuidanceDto | null>(null)

function patientLabel(p: PatientDto) {
  const phone = p.phone ? ` / ${p.phone}` : ''
  return `${p.userId}${phone}`
}

function modelLabel(m: MlModelDto) {
  return `${m.modelName} / ${m.versionTag} / ${m.algorithm}`
}

async function loadModels() {
  try {
    const models = await mlModelListApi({ activeOnly: false })
    const map: Record<number, string> = {}
    for (const m of models) {
      map[m.id] = modelLabel(m)
    }
    modelLabelMap.value = map
  } catch {
    modelLabelMap.value = { ...modelLabelMap.value }
  }
}

async function searchPatients(keyword: string) {
  if (!canManage.value) {
    return
  }
  patientSearching.value = true
  try {
    const pageData = await patientPageApi({ page: 0, size: 20, keyword: keyword || null })
    patientOptions.value = pageData.records
    const map = { ...patientLabelMap.value }
    for (const p of pageData.records) {
      map[p.id] = patientLabel(p)
    }
    patientLabelMap.value = map
  } finally {
    patientSearching.value = false
  }
}

async function ensurePatientLabels(ids: number[]) {
  if (!canManage.value) {
    return
  }
  const missing = Array.from(new Set(ids)).filter((id) => !patientLabelMap.value[id])
  if (!missing.length) {
    return
  }
  const details = await Promise.all(missing.map((id) => patientDetailApi(id).catch(() => null)))
  const map = { ...patientLabelMap.value }
  for (const p of details) {
    if (p) {
      map[p.id] = patientLabel(p)
    }
  }
  patientLabelMap.value = map
}

async function ensureDoctorLabels(ids: number[]) {
  const missing = Array.from(new Set(ids)).filter((id) => id && !doctorLabelMap.value[id])
  if (!missing.length) {
    return
  }
  const details = await Promise.all(missing.map((id) => userDetailApi(id).catch(() => null)))
  const map = { ...doctorLabelMap.value }
  for (const u of details) {
    if (u) {
      map[u.id] = u.nickname || u.username
    }
  }
  doctorLabelMap.value = map
}

async function ensurePredictionModelLabels(ids: number[]) {
  const missing = Array.from(new Set(ids)).filter((id) => id && !predictionModelLabelMap.value[id])
  if (!missing.length) {
    return
  }
  if (!Object.keys(modelLabelMap.value).length) {
    await loadModels()
  }
  const details = await Promise.all(missing.map((id) => predictionResultDetailApi(id).catch(() => null)))
  const map = { ...predictionModelLabelMap.value }
  for (const item of details) {
    if (item && item.id) {
      map[item.id] = item.modelId ? (modelLabelMap.value[item.modelId] || `#${item.modelId}`) : '-'
    }
  }
  predictionModelLabelMap.value = map
}

function showTotal(t: number) {
  return `共 ${t} 条`
}

function onTableChange(p: { current?: number; pageSize?: number }) {
  page.value = p.current || 1
  pageSize.value = p.pageSize || 10
  load()
}

function onSearch() {
  page.value = 1
  load()
}

function onReset() {
  query.patientId = null
  query.timeRange = null
  page.value = 1
  load()
}

function resetForm() {
  form.patientId = null
  form.predictionResultId = null
  form.guidanceTitle = ''
  form.guidanceContent = ''
  form.guidanceLevel = 1
}

function openCreate() {
  if (isPatient.value) {
    return
  }
  editingId.value = null
  resetForm()
  modalOpen.value = true
}

function openCreatePrefilled() {
  if (!canManage.value) {
    return
  }
  const patientId = typeof route.query.patientId === 'string' ? Number(route.query.patientId) : null
  const predictionResultId = typeof route.query.predictionResultId === 'string' ? Number(route.query.predictionResultId) : null

  editingId.value = null
  resetForm()
  if (patientId) {
    form.patientId = patientId
    query.patientId = patientId
  }
  if (predictionResultId) {
    form.predictionResultId = predictionResultId
  }
  modalOpen.value = true

  router.replace({ name: 'health-guidances' })
}

function openEdit(r: HealthGuidanceDto) {
  editingId.value = r.id
  form.patientId = r.patientId
  form.predictionResultId = r.predictionResultId ?? null
  form.guidanceTitle = r.guidanceTitle
  form.guidanceContent = r.guidanceContent
  form.guidanceLevel = r.guidanceLevel
  modalOpen.value = true
}

async function load() {
  loading.value = true
  try {
    const startTime = query.timeRange ? query.timeRange[0].format('YYYY-MM-DDTHH:mm:ss') : null
    const endTime = query.timeRange ? query.timeRange[1].format('YYYY-MM-DDTHH:mm:ss') : null
    const data = isPatient.value
      ? await healthGuidanceMineApi({
          page: page.value - 1,
          size: pageSize.value,
          startTime,
          endTime,
        })
      : await healthGuidancePageApi({
          page: page.value - 1,
          size: pageSize.value,
          patientId: query.patientId,
          startTime,
          endTime,
        })
    rows.value = data.records
    total.value = data.total

    await ensurePatientLabels(data.records.map((r) => r.patientId))
    await ensureDoctorLabels(data.records.map((r) => r.doctorUserId))
    await ensurePredictionModelLabels(data.records.map((r) => r.predictionResultId || 0))
  } finally {
    loading.value = false
  }
}

function patientRender({ record }: { record: HealthGuidanceDto }) {
  if (isPatient.value) {
    return ''
  }
  return patientLabelMap.value[record.patientId] || String(record.patientId)
}

function doctorRender({ record }: { record: HealthGuidanceDto }) {
  return doctorLabelMap.value[record.doctorUserId] || `#${record.doctorUserId}`
}

function modelRender({ record }: { record: HealthGuidanceDto }) {
  if (!record.predictionResultId) {
    return '-'
  }
  return predictionModelLabelMap.value[record.predictionResultId] || `#${record.predictionResultId}`
}

function guidanceLevelText(v: number) {
  if (v >= 3) return '高'
  if (v === 2) return '中'
  return '低'
}

function guidanceLevelRender({ record }: { record: HealthGuidanceDto }) {
  return guidanceLevelText(record.guidanceLevel)
}

function truncateText(s: string, max = 40) {
  if (!s) return ''
  return s.length > max ? `${s.slice(0, max)}...` : s
}

function contentRender({ record }: { record: HealthGuidanceDto }) {
  return truncateText(record.guidanceContent, 80)
}

async function openDetail(r: HealthGuidanceDto) {
  detailOpen.value = true
  detailLoading.value = true
  detail.value = null
  try {
    detail.value = await healthGuidanceDetailApi(r.id)
  } finally {
    detailLoading.value = false
  }
}

async function submit() {
  if (!form.guidanceTitle) {
    message.warning('请输入指导标题')
    return
  }
  if (!form.guidanceContent) {
    message.warning('请输入指导内容')
    return
  }
  if (!editingId.value && !form.patientId) {
    message.warning('请输入 patientId')
    return
  }

  modalLoading.value = true
  try {
    if (!editingId.value) {
      const req: HealthGuidanceCreateRequest = {
        patientId: form.patientId as number,
        predictionResultId: form.predictionResultId ?? undefined,
        guidanceTitle: form.guidanceTitle,
        guidanceContent: form.guidanceContent,
        guidanceLevel: form.guidanceLevel,
      }
      await healthGuidanceCreateApi(req)
      message.success('创建成功')
    } else {
      const req: HealthGuidanceUpdateRequest = {
        predictionResultId: form.predictionResultId ?? undefined,
        guidanceTitle: form.guidanceTitle,
        guidanceContent: form.guidanceContent,
        guidanceLevel: form.guidanceLevel,
      }
      await healthGuidanceUpdateApi(editingId.value, req)
      message.success('更新成功')
    }
    modalOpen.value = false
    await load()
  } finally {
    modalLoading.value = false
  }
}

function confirmDelete(r: HealthGuidanceDto) {
  if (isPatient.value) {
    return
  }
  Modal.confirm({
    title: '确认删除',
    content: `确定删除健康指导（ID=${r.id}）吗？`,
    async onOk() {
      await healthGuidanceDeleteApi(r.id)
      message.success('已删除')
      await load()
    },
  })
}

onMounted(load)
onMounted(loadModels)

onMounted(() => {
  if (!canManage.value) {
    return
  }
  if (route.query.create === '1') {
    openCreatePrefilled()
  }
})
</script>

<template>
  <a-card>
    <template #title>{{ isPatient ? '我的健康指导' : '健康指导' }}</template>

    <a-form v-if="!isPatient" layout="inline" style="margin-bottom: 12px" @submit.prevent>
      <a-form-item label="患者">
        <a-select
          v-model:value="query.patientId"
          show-search
          allow-clear
          :filter-option="false"
          :not-found-content="patientSearching ? '搜索中...' : '无数据'"
          style="width: 260px"
          placeholder="搜索患者（userId/phone）"
          @search="searchPatients"
        >
          <a-select-option v-for="p in patientOptions" :key="p.id" :value="p.id">
            {{ patientLabel(p) }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="时间范围">
        <a-range-picker v-model:value="query.timeRange" show-time />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" :loading="loading" @click="onSearch">查询</a-button>
      </a-form-item>
      <a-form-item>
        <a-button @click="onReset">重置</a-button>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="openCreate">新增</a-button>
      </a-form-item>
    </a-form>

    <a-table
      row-key="id"
      :loading="loading"
      :data-source="rows"
      :pagination="{
        current: page,
        pageSize,
        total,
        showSizeChanger: true,
        showTotal,
      }"
      @change="onTableChange"
      :scroll="{ x: 1000 }"
    >
      <a-table-column title="ID" data-index="id" width="80" />
      <a-table-column v-if="!isPatient" title="patientId" data-index="patientId" width="100" />
      <a-table-column v-if="!isPatient" title="患者" :customRender="patientRender" width="200" />
      <a-table-column v-if="!isPatient" title="医生" :customRender="doctorRender" width="160" />
      <a-table-column title="预测结果ID" data-index="predictionResultId" width="120" />
      <a-table-column title="模型" :customRender="modelRender" width="240" />
      <a-table-column title="标题" data-index="guidanceTitle" width="220" />
      <a-table-column title="等级" :customRender="guidanceLevelRender" width="100" />
      <a-table-column title="内容" :customRender="contentRender" />
      <a-table-column title="创建时间" data-index="createdAt" width="180" />
      <a-table-column title="操作" width="220" fixed="right">
        <template #default="{ record }">
          <a-space>
            <a-button type="link" @click="() => openDetail(record)">详情</a-button>
            <a-button v-if="!isPatient" type="link" @click="() => openEdit(record)">编辑</a-button>
            <a-button v-if="!isPatient" type="link" danger @click="() => confirmDelete(record)">删除</a-button>
          </a-space>
        </template>
      </a-table-column>
    </a-table>

    <a-modal v-model:open="detailOpen" title="健康指导详情" :footer="null" width="820px">
      <a-spin :spinning="detailLoading">
        <a-descriptions v-if="detail" bordered :column="2">
          <a-descriptions-item label="ID">{{ detail.id }}</a-descriptions-item>
          <a-descriptions-item label="患者">
            {{ isPatient ? '' : (patientLabelMap[detail.patientId] || detail.patientId) }}
          </a-descriptions-item>
          <a-descriptions-item label="医生">{{ doctorLabelMap[detail.doctorUserId] || `#${detail.doctorUserId}` }}</a-descriptions-item>
          <a-descriptions-item label="预测结果ID">{{ detail.predictionResultId ?? '' }}</a-descriptions-item>
          <a-descriptions-item label="模型">{{ detail.predictionResultId ? (predictionModelLabelMap[detail.predictionResultId] || `#${detail.predictionResultId}`) : '-' }}</a-descriptions-item>
          <a-descriptions-item label="标题" :span="2">{{ detail.guidanceTitle }}</a-descriptions-item>
          <a-descriptions-item label="等级">{{ guidanceLevelText(detail.guidanceLevel) }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ detail.createdAt || '' }}</a-descriptions-item>
          <a-descriptions-item label="内容" :span="2">{{ detail.guidanceContent }}</a-descriptions-item>
        </a-descriptions>
      </a-spin>
    </a-modal>

    <a-modal
      v-model:open="modalOpen"
      :title="modalTitle"
      :confirm-loading="modalLoading"
      @ok="submit"
      @cancel="() => { modalOpen = false }"
      width="760px"
    >
      <a-form layout="vertical">
        <a-form-item v-if="!editingId" label="患者" required>
          <a-select
            v-model:value="form.patientId"
            show-search
            :filter-option="false"
            :not-found-content="patientSearching ? '搜索中...' : '无数据'"
            placeholder="搜索患者（userId/phone）"
            @search="searchPatients"
          >
            <a-select-option v-for="p in patientOptions" :key="p.id" :value="p.id">
              {{ patientLabel(p) }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="预测结果ID">
          <a-input-number v-model:value="form.predictionResultId" :min="1" style="width: 100%" />
        </a-form-item>
        <a-form-item label="指导标题" required>
          <a-input v-model:value="form.guidanceTitle" />
        </a-form-item>
        <a-form-item label="指导等级" required>
          <a-select v-model:value="form.guidanceLevel">
            <a-select-option :value="1">1 - 低</a-select-option>
            <a-select-option :value="2">2 - 中</a-select-option>
            <a-select-option :value="3">3 - 高</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="指导内容" required>
          <a-textarea v-model:value="form.guidanceContent" :rows="5" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>
