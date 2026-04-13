<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { patientDetailApi, patientPageApi, type PatientDto } from '../api/patients'
import {
  healthRecordCreateApi,
  healthRecordDeleteApi,
  healthRecordPageApi,
  healthRecordUpdateApi,
  type HealthRecordCreateRequest,
  type HealthRecordDto,
  type HealthRecordUpdateRequest,
} from '../api/healthRecords'

const loading = ref(false)
const rows = ref<HealthRecordDto[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const patientOptions = ref<PatientDto[]>([])
const patientLabelMap = ref<Record<number, string>>({})
const patientSearching = ref(false)

const query = reactive({
  patientId: null as number | null,
  keyword: '',
})

const modalOpen = ref(false)
const modalLoading = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  patientId: null as number | null,
  familyHypertension: 0,
  pastHypertension: 0,
  comorbidity: '',
  drugHistory: '',
  treatmentRecord: '',
  allergyHistory: '',
})

const modalTitle = computed(() => (editingId.value ? '编辑健康档案' : '新增健康档案'))

function patientLabel(p: PatientDto) {
  const phone = p.phone ? ` / ${p.phone}` : ''
  return `${p.userId}${phone}`
}

async function searchPatients(keyword: string) {
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
  query.keyword = ''
  page.value = 1
  load()
}

function resetForm() {
  form.patientId = null
  form.familyHypertension = 0
  form.pastHypertension = 0
  form.comorbidity = ''
  form.drugHistory = ''
  form.treatmentRecord = ''
  form.allergyHistory = ''
}

function openCreate() {
  editingId.value = null
  resetForm()
  modalOpen.value = true
}

function openEdit(r: HealthRecordDto) {
  editingId.value = r.id
  form.patientId = r.patientId
  form.familyHypertension = r.familyHypertension
  form.pastHypertension = r.pastHypertension
  form.comorbidity = r.comorbidity
  form.drugHistory = r.drugHistory
  form.treatmentRecord = r.treatmentRecord || ''
  form.allergyHistory = r.allergyHistory || ''
  modalOpen.value = true
}

async function load() {
  loading.value = true
  try {
    const data = await healthRecordPageApi({
      page: page.value - 1,
      size: pageSize.value,
      patientId: query.patientId,
      keyword: query.keyword || null,
    })
    rows.value = data.records
    total.value = data.total

    await ensurePatientLabels(data.records.map((r) => r.patientId))
  } finally {
    loading.value = false
  }
}

function patientRender({ record }: { record: HealthRecordDto }) {
  return patientLabelMap.value[record.patientId] || String(record.patientId)
}

function familyHypertensionText(v: number) {
  if (v === 1) return '有'
  if (v === 2) return '不详'
  return '无'
}

function pastHypertensionText(v: number) {
  if (v === 1) return '有'
  if (v === 2) return '既往有/现已控制'
  return '无'
}

function truncateText(s: string, max = 30) {
  if (!s) return ''
  return s.length > max ? `${s.slice(0, max)}...` : s
}

function familyHypertensionRender({ record }: { record: HealthRecordDto }) {
  return familyHypertensionText(record.familyHypertension)
}

function pastHypertensionRender({ record }: { record: HealthRecordDto }) {
  return pastHypertensionText(record.pastHypertension)
}

function comorbidityRender({ record }: { record: HealthRecordDto }) {
  return truncateText(record.comorbidity, 40)
}

function drugHistoryRender({ record }: { record: HealthRecordDto }) {
  return truncateText(record.drugHistory, 40)
}

async function submit() {
  if (!form.familyHypertension && form.familyHypertension !== 0) {
    message.warning('请选择家族高血压病史')
    return
  }
  if (!form.pastHypertension && form.pastHypertension !== 0) {
    message.warning('请选择既往高血压病史')
    return
  }
  if (!form.comorbidity) {
    message.warning('请输入合并症')
    return
  }
  if (!form.drugHistory) {
    message.warning('请输入既往用药史')
    return
  }

  modalLoading.value = true
  try {
    if (!editingId.value) {
      if (!form.patientId) {
        message.warning('请输入 patientId')
        return
      }
      const req: HealthRecordCreateRequest = {
        patientId: form.patientId,
        familyHypertension: form.familyHypertension,
        pastHypertension: form.pastHypertension,
        comorbidity: form.comorbidity,
        drugHistory: form.drugHistory,
        treatmentRecord: form.treatmentRecord || undefined,
        allergyHistory: form.allergyHistory || undefined,
      }
      await healthRecordCreateApi(req)
      message.success('创建成功')
    } else {
      const req: HealthRecordUpdateRequest = {
        familyHypertension: form.familyHypertension,
        pastHypertension: form.pastHypertension,
        comorbidity: form.comorbidity,
        drugHistory: form.drugHistory,
        treatmentRecord: form.treatmentRecord || undefined,
        allergyHistory: form.allergyHistory || undefined,
      }
      await healthRecordUpdateApi(editingId.value, req)
      message.success('更新成功')
    }

    modalOpen.value = false
    await load()
  } finally {
    modalLoading.value = false
  }
}

function confirmDelete(r: HealthRecordDto) {
  Modal.confirm({
    title: '确认删除',
    content: `确定删除健康档案（ID=${r.id}）吗？`,
    async onOk() {
      await healthRecordDeleteApi(r.id)
      message.success('已删除')
      await load()
    },
  })
}

onMounted(load)
</script>

<template>
  <a-card>
    <template #title>健康档案</template>

    <a-form layout="inline" style="margin-bottom: 12px" @submit.prevent>
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
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" style="width: 240px" placeholder="合并症关键字" />
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
    >
      <a-table-column title="ID" data-index="id" width="80" />
      
      <a-table-column title="患者" :customRender="patientRender" width="200" />
      <a-table-column
        title="家族史"
        width="120"
        :customRender="familyHypertensionRender"
      />
      <a-table-column
        title="既往高血压"
        width="150"
        :customRender="pastHypertensionRender"
      />
      <a-table-column title="合并症" :customRender="comorbidityRender" />
      <a-table-column title="用药史" :customRender="drugHistoryRender" />
      <a-table-column title="创建时间" data-index="createdAt" width="180" />
      <a-table-column title="更新时间" data-index="updatedAt" width="180" />
      <a-table-column title="操作" width="180">
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
      width="720px"
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

        <a-form-item label="家族高血压病史" required>
          <a-select v-model:value="form.familyHypertension">
            <a-select-option :value="0">无</a-select-option>
            <a-select-option :value="1">有</a-select-option>
            <a-select-option :value="2">不详</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="既往高血压病史" required>
          <a-select v-model:value="form.pastHypertension">
            <a-select-option :value="0">无</a-select-option>
            <a-select-option :value="1">有</a-select-option>
            <a-select-option :value="2">既往有/现已控制</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="合并症" required>
          <a-input v-model:value="form.comorbidity" placeholder="例如 糖尿病/高血脂/冠心病" />
        </a-form-item>

        <a-form-item label="既往用药史" required>
          <a-textarea v-model:value="form.drugHistory" :rows="3" />
        </a-form-item>

        <a-form-item label="既往诊疗记录">
          <a-textarea v-model:value="form.treatmentRecord" :rows="3" />
        </a-form-item>

        <a-form-item label="过敏史">
          <a-textarea v-model:value="form.allergyHistory" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>
