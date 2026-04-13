<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import dayjs, { type Dayjs } from 'dayjs'
import { mlModelListApi, type MlModelDto } from '../api/mlModels'
import { useAuthStore } from '../stores/auth'
import { patientDetailApi, patientPageApi, type PatientDto } from '../api/patients'
import {
  predictionResultCreateApi,
  predictionResultDeleteApi,
  predictionResultGenerateApi,
  predictionResultDetailApi,
  predictionResultMineApi,
  predictionResultPageApi,
  predictionResultUpdateApi,
  type PredictionResultCreateRequest,
  type PredictionResultDto,
  type PredictionResultUpdateRequest,
} from '../api/predictionResults'

const auth = useAuthStore()
const router = useRouter()
const loading = ref(false)
const rows = ref<PredictionResultDto[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const isPatient = computed(() => auth.user?.role === 'PATIENT')
const canManage = computed(() => auth.user?.role === 'DOCTOR' || auth.user?.role === 'ADMIN')

const patientOptions = ref<PatientDto[]>([])
const patientLabelMap = ref<Record<number, string>>({})
const patientSearching = ref(false)
const modelOptions = ref<MlModelDto[]>([])

const query = reactive({
  patientId: null as number | null,
  modelId: null as number | null,
  timeRange: null as [Dayjs, Dayjs] | null,
})

const modalOpen = ref(false)
const modalLoading = ref(false)
const editingId = ref<number | null>(null)

const detailOpen = ref(false)
const detailLoading = ref(false)
const detail = ref<PredictionResultDto | null>(null)

const form = reactive({
  patientId: null as number | null,
  predictionTime: null as Dayjs | null,
  predictionProb: 0.5,
  predictionLabel: 0,
  coreRiskFactors: '',
  warningStatus: 0,
})

const modalTitle = computed(() => (editingId.value ? '编辑预测结果' : '新增预测结果'))

function patientLabel(p: PatientDto) {
  const phone = p.phone ? ` / ${p.phone}` : ''
  return `${p.userId}${phone}`
}

async function loadModels() {
  if (isPatient.value) {
    return
  }
  modelOptions.value = await mlModelListApi({ activeOnly: false })
}

async function searchPatients(keyword: string) {
  if (isPatient.value) {
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
  if (isPatient.value) {
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
  query.modelId = null
  query.timeRange = null
  page.value = 1
  load()
}

function resetForm() {
  form.patientId = null
  form.predictionTime = null
  form.predictionProb = 0.5
  form.predictionLabel = 0
  form.coreRiskFactors = ''
  form.warningStatus = 0
}

function openCreate() {
  if (isPatient.value) {
    return
  }
  editingId.value = null
  resetForm()
  modalOpen.value = true
}

async function generatePrediction() {
  if (!query.patientId) {
    message.warning('请先输入 patientId')
    return
  }
  loading.value = true
  try {
    await predictionResultGenerateApi({ patientId: query.patientId, modelId: query.modelId ?? undefined })
    message.success('预测生成成功')
    await load()
  } finally {
    loading.value = false
  }
}

function openEdit(r: PredictionResultDto) {
  editingId.value = r.id
  form.patientId = r.patientId
  form.predictionTime = r.predictionTime ? dayjs(r.predictionTime) : null
  form.predictionProb = Number(r.predictionProb)
  form.predictionLabel = r.predictionLabel
  form.coreRiskFactors = r.coreRiskFactors
  form.warningStatus = r.warningStatus
  modalOpen.value = true
}

async function load() {
  loading.value = true
  try {
    const startTime = query.timeRange ? query.timeRange[0].format('YYYY-MM-DDTHH:mm:ss') : null
    const endTime = query.timeRange ? query.timeRange[1].format('YYYY-MM-DDTHH:mm:ss') : null
    const data = isPatient.value
      ? await predictionResultMineApi({
          page: page.value - 1,
          size: pageSize.value,
          startTime,
          endTime,
        })
      : await predictionResultPageApi({
          page: page.value - 1,
          size: pageSize.value,
          patientId: query.patientId,
          startTime,
          endTime,
        })
    rows.value = data.records
    total.value = data.total

    if (!isPatient.value) {
      await ensurePatientLabels(data.records.map((r) => r.patientId))
    }
  } finally {
    loading.value = false
  }
}

function patientRender({ record }: { record: PredictionResultDto }) {
  if (isPatient.value) {
    return ''
  }
  return patientLabelMap.value[record.patientId] || String(record.patientId)
}

function labelText(v: number) {
  return v === 1 ? '高风险' : '低风险'
}

function truncateText(s: string, max = 36) {
  if (!s) return ''
  return s.length > max ? `${s.slice(0, max)}...` : s
}

function warningText(v: number) {
  if (v === 1) return '已预警'
  if (v === 2) return '已干预'
  return '无预警'
}

function labelRender({ record }: { record: PredictionResultDto }) {
  return labelText(record.predictionLabel)
}

function warningRender({ record }: { record: PredictionResultDto }) {
  return warningText(record.warningStatus)
}

function coreRiskRender({ record }: { record: PredictionResultDto }) {
  return truncateText(record.coreRiskFactors, 60)
}

async function openDetail(r: PredictionResultDto) {
  detailOpen.value = true
  detailLoading.value = true
  try {
    detail.value = await predictionResultDetailApi(r.id)
  } finally {
    detailLoading.value = false
  }
}

function probRender({ record }: { record: PredictionResultDto }) {
  const p = Number(record.predictionProb)
  if (Number.isNaN(p)) return ''
  return p.toFixed(4)
}

async function submit() {
  if (!form.predictionTime) {
    message.warning('请选择预测时间')
    return
  }
  if (!editingId.value && !form.patientId) {
    message.warning('请输入 patientId')
    return
  }
  if (form.predictionProb == null || Number.isNaN(Number(form.predictionProb))) {
    message.warning('请输入预测概率')
    return
  }
  const prob = Number(form.predictionProb)
  if (prob < 0 || prob > 1) {
    message.warning('预测概率应在 0~1')
    return
  }
  if (!form.coreRiskFactors) {
    message.warning('请输入核心风险因素')
    return
  }

  modalLoading.value = true
  try {
    const timeStr = form.predictionTime.format('YYYY-MM-DDTHH:mm:ss')
    if (!editingId.value) {
      const req: PredictionResultCreateRequest = {
        patientId: form.patientId as number,
        predictionTime: timeStr,
        predictionProb: prob,
        predictionLabel: form.predictionLabel,
        coreRiskFactors: form.coreRiskFactors,
        warningStatus: form.warningStatus,
      }
      await predictionResultCreateApi(req)
      message.success('创建成功')
    } else {
      const req: PredictionResultUpdateRequest = {
        predictionTime: timeStr,
        predictionProb: prob,
        predictionLabel: form.predictionLabel,
        coreRiskFactors: form.coreRiskFactors,
        warningStatus: form.warningStatus,
      }
      await predictionResultUpdateApi(editingId.value, req)
      message.success('更新成功')
    }

    modalOpen.value = false
    await load()
  } finally {
    modalLoading.value = false
  }
}

function confirmDelete(r: PredictionResultDto) {
  if (isPatient.value) {
    return
  }
  Modal.confirm({
    title: '确认删除',
    content: `确定删除预测结果（ID=${r.id}）吗？`,
    async onOk() {
      await predictionResultDeleteApi(r.id)
      message.success('已删除')
      await load()
    },
  })
}

function toGuidanceCreate(r: PredictionResultDto) {
  if (!canManage.value) {
    return
  }
  router.push({
    name: 'health-guidances',
    query: {
      create: '1',
      patientId: String(r.patientId),
      predictionResultId: String(r.id),
    },
  })
}

onMounted(load)
onMounted(loadModels)
</script>

<template>
  <a-card>
    <template #title>{{ isPatient ? '我的预测结果' : '预测结果' }}</template>

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
      <a-form-item label="模型">
        <a-select v-model:value="query.modelId" allow-clear style="width: 260px" placeholder="选择模型版本（默认最新激活）">
          <a-select-option v-for="m in modelOptions" :key="m.id" :value="m.id">
            {{ m.modelName }} / {{ m.versionTag }} / {{ m.algorithm }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="预测时间">
        <a-range-picker v-model:value="query.timeRange" show-time />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" :loading="loading" @click="onSearch">查询</a-button>
      </a-form-item>
      <a-form-item>
        <a-button @click="onReset">重置</a-button>
      </a-form-item>
      <a-form-item>
        <a-button @click="generatePrediction">生成预测</a-button>
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
      :scroll="{ x: 1100 }"
    >
      <a-table-column title="ID" data-index="id" width="80" />
      <a-table-column v-if="!isPatient" title="patientId" data-index="patientId" width="100" />
      <a-table-column v-if="!isPatient" title="modelId" data-index="modelId" width="100" />
      <a-table-column v-if="!isPatient" title="患者" :customRender="patientRender" width="200" />
      <a-table-column title="预测时间" data-index="predictionTime" width="180" />
      <a-table-column title="概率" :customRender="probRender" width="100" />
      <a-table-column title="标签" :customRender="labelRender" width="100" />
      <a-table-column title="预警状态" :customRender="warningRender" width="120" />
      <a-table-column title="核心风险因素" :customRender="coreRiskRender" />
      <a-table-column title="操作" width="220" fixed="right">
        <template #default="{ record }">
          <a-space>
            <a-button type="link" @click="() => openDetail(record)">详情</a-button>
            <a-button v-if="canManage" type="link" @click="() => toGuidanceCreate(record)">写健康指导</a-button>
            <a-button type="link" @click="() => openEdit(record)">编辑</a-button>
            <a-button type="link" danger @click="() => confirmDelete(record)">删除</a-button>
          </a-space>
        </template>
      </a-table-column>
    </a-table>

    <a-modal v-model:open="detailOpen" title="预测详情" :footer="null" width="820px">
      <a-spin :spinning="detailLoading">
        <a-descriptions v-if="detail" bordered :column="2">
          <a-descriptions-item label="ID">{{ detail.id }}</a-descriptions-item>
          <a-descriptions-item label="patientId">{{ detail.patientId }}</a-descriptions-item>
          <a-descriptions-item label="modelId">{{ detail.modelId || '-' }}</a-descriptions-item>
          <a-descriptions-item label="预测时间">{{ detail.predictionTime }}</a-descriptions-item>
          <a-descriptions-item label="概率">{{ detail.predictionProb }}</a-descriptions-item>
          <a-descriptions-item label="标签">{{ labelText(detail.predictionLabel) }}</a-descriptions-item>
          <a-descriptions-item label="预警状态">{{ warningText(detail.warningStatus) }}</a-descriptions-item>
          <a-descriptions-item label="核心风险因素" :span="2">{{ detail.coreRiskFactors }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ detail.createdAt || '-' }}</a-descriptions-item>
          <a-descriptions-item label="更新时间">{{ detail.updatedAt || '-' }}</a-descriptions-item>
        </a-descriptions>
      </a-spin>
    </a-modal>

    <a-modal
      v-model:open="modalOpen"
      :title="modalTitle"
      :confirm-loading="modalLoading"
      @ok="submit"
      @cancel="() => { modalOpen = false }"
      width="820px"
    >
      <a-form layout="vertical">
        <a-form-item v-if="!editingId && !isPatient" label="患者" required>
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

        <a-form-item label="预测时间" required>
          <a-date-picker v-model:value="form.predictionTime" show-time style="width: 100%" />
        </a-form-item>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="预测概率(0~1)" required>
              <a-input-number v-model:value="form.predictionProb" :min="0" :max="1" :step="0.0001" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="风险标签" required>
              <a-select v-model:value="form.predictionLabel">
                <a-select-option :value="0">0 - 低风险</a-select-option>
                <a-select-option :value="1">1 - 高风险</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="预警状态" required>
          <a-select v-model:value="form.warningStatus">
            <a-select-option :value="0">0 - 无预警</a-select-option>
            <a-select-option :value="1">1 - 已预警</a-select-option>
            <a-select-option :value="2">2 - 已干预</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="核心风险因素" required>
          <a-textarea v-model:value="form.coreRiskFactors" :rows="3" placeholder="例如 高盐饮食, 家族史, BMI偏高" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>
