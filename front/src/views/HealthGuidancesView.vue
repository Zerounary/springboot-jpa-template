<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { useAuthStore } from '../stores/auth'
import { patientDetailApi, patientPageApi, type PatientDto } from '../api/patients'
import {
  healthGuidanceCreateApi,
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

const loading = ref(false)
const rows = ref<HealthGuidanceDto[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const query = reactive({
  patientId: null as number | null,
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

function patientLabel(p: PatientDto) {
  const phone = p.phone ? ` / ${p.phone}` : ''
  return `${p.userId}${phone}`
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
    const data = isPatient.value
      ? await healthGuidanceMineApi({
          page: page.value - 1,
          size: pageSize.value,
        })
      : await healthGuidancePageApi({
          page: page.value - 1,
          size: pageSize.value,
          patientId: query.patientId,
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

function patientRender({ record }: { record: HealthGuidanceDto }) {
  if (isPatient.value) {
    return ''
  }
  return patientLabelMap.value[record.patientId] || String(record.patientId)
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
      <a-form-item label="patientId">
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
      <a-table-column v-if="!isPatient" title="医生账号ID" data-index="doctorUserId" width="120" />
      <a-table-column title="预测结果ID" data-index="predictionResultId" width="120" />
      <a-table-column title="标题" data-index="guidanceTitle" width="220" />
      <a-table-column title="等级" :customRender="guidanceLevelRender" width="100" />
      <a-table-column title="内容" :customRender="contentRender" />
      <a-table-column title="创建时间" data-index="createdAt" width="180" />
      <a-table-column v-if="!isPatient" title="操作" width="180" fixed="right">
        <template #default="{ record }">
          <a-space>
            <a-button type="link" @click="() => openEdit(record)">编辑</a-button>
            <a-button type="link" danger @click="() => confirmDelete(record)">删除</a-button>
          </a-space>
        </template>
      </a-table-column>
    </a-table>

    <a-modal
      v-model:open="modalOpen"
      :title="modalTitle"
      :confirm-loading="modalLoading"
      @ok="submit"
      @cancel="() => { modalOpen = false }"
      width="760px"
    >
      <a-form layout="vertical">
        <a-form-item v-if="!editingId" label="patientId" required>
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
