<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { patientDetailApi, patientPageApi, type PatientDto } from '../api/patients'
import {
  fusionDetailApi,
  fusionPageApi,
  fusionSyncAllApi,
  fusionSyncApi,
  type HypertensionFusionDto,
} from '../api/hypertensionFusion'

const loading = ref(false)
const rows = ref<HypertensionFusionDto[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const patientOptions = ref<PatientDto[]>([])
const patientLabelMap = ref<Record<number, string>>({})
const patientSearching = ref(false)

const query = reactive({
  keyword: '',
})

const detailOpen = ref(false)
const detailLoading = ref(false)
const detail = ref<HypertensionFusionDto | null>(null)

const syncOpen = ref(false)
const syncLoading = ref(false)
const syncForm = reactive({
  patientId: null as number | null,
  hypertensionLabel: null as number | null,
})

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
  query.keyword = ''
  page.value = 1
  load()
}

async function load() {
  loading.value = true
  try {
    const data = await fusionPageApi({
      page: page.value - 1,
      size: pageSize.value,
      keyword: query.keyword || null,
    })
    rows.value = data.records
    total.value = data.total

    await ensurePatientLabels(data.records.map((r) => r.patientId))
  } finally {
    loading.value = false
  }
}

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

function patientRender({ record }: { record: HypertensionFusionDto }) {
  return patientLabelMap.value[record.patientId] || String(record.patientId)
}

function genderText(v: number) {
  return v === 1 ? '男' : '女'
}

function familyHypertensionText(v: number) {
  if (v === 1) return '有'
  if (v === 2) return '不详'
  return '无'
}

function smokingText(v: number) {
  if (v === 1) return '偶尔'
  if (v === 2) return '长期'
  return '不吸烟'
}

function dietText(v: number) {
  if (v === 0) return '低盐'
  if (v === 2) return '高盐'
  return '正常'
}

function labelText(v: number) {
  return v === 1 ? '高风险' : '低风险'
}

function genderRender({ record }: { record: HypertensionFusionDto }) {
  return genderText(record.gender)
}

function familyHypertensionRender({ record }: { record: HypertensionFusionDto }) {
  return familyHypertensionText(record.familyHypertension)
}

function smokingRender({ record }: { record: HypertensionFusionDto }) {
  return smokingText(record.smoking)
}

function dietRender({ record }: { record: HypertensionFusionDto }) {
  return dietText(record.dietPreference)
}

function labelRender({ record }: { record: HypertensionFusionDto }) {
  return labelText(record.hypertensionLabel)
}

const detailTitle = computed(() => (detail.value ? `融合详情 - patientId=${detail.value.patientId}` : '融合详情'))

async function openDetail(r: HypertensionFusionDto) {
  detailOpen.value = true
  detailLoading.value = true
  try {
    detail.value = await fusionDetailApi(r.patientId)
  } finally {
    detailLoading.value = false
  }
}

function openSync(r?: HypertensionFusionDto) {
  syncForm.patientId = r?.patientId ?? null
  syncForm.hypertensionLabel = null
  syncOpen.value = true
}

async function doSync() {
  if (!syncForm.patientId) {
    message.warning('请输入 patientId')
    return
  }
  syncLoading.value = true
  try {
    await fusionSyncApi({
      patientId: syncForm.patientId,
      hypertensionLabel: syncForm.hypertensionLabel,
    })
    message.success('同步成功')
    syncOpen.value = false
    await load()
  } finally {
    syncLoading.value = false
  }
}

function confirmSyncAll() {
  Modal.confirm({
    title: '确认全量同步',
    content: '将对所有患者重新生成/更新融合数据，可能耗时较长，是否继续？',
    async onOk() {
      const r = await fusionSyncAllApi()
      message.success(`完成：总数 ${r.total}，成功 ${r.success}，失败 ${r.failed}`)
      await load()
    },
  })
}

onMounted(load)
</script>

<template>
  <a-card>
    <template #title>融合数据</template>

    <a-form layout="inline" style="margin-bottom: 12px" @submit.prevent>
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="按 userId 模糊搜索" style="width: 260px" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" :loading="loading" @click="onSearch">查询</a-button>
      </a-form-item>
      <a-form-item>
        <a-button @click="onReset">重置</a-button>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="openSync()">单条同步</a-button>
      </a-form-item>
      <a-form-item>
        <a-button danger @click="confirmSyncAll">全量同步</a-button>
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
      :scroll="{ x: 1200 }"
    >
      <a-table-column title="ID" data-index="id" width="80" />
      
      <a-table-column title="患者" :customRender="patientRender" width="200" />
      <a-table-column title="userId" data-index="userId" width="140" />
      <a-table-column title="年龄" data-index="age" width="90" />
      <a-table-column title="性别" :customRender="genderRender" width="90" />
      <a-table-column title="收缩压" data-index="systolicBp" width="90" />
      <a-table-column title="舒张压" data-index="diastolicBp" width="90" />
      <a-table-column title="BMI" data-index="bmi" width="90" />
      <a-table-column title="胆固醇" data-index="cholesterol" width="100" />
      <a-table-column title="家族史" :customRender="familyHypertensionRender" width="90" />
      <a-table-column title="吸烟" :customRender="smokingRender" width="90" />
      <a-table-column title="饮食" :customRender="dietRender" width="90" />
      <a-table-column title="标签" :customRender="labelRender" width="90" />
      <a-table-column title="最后体检" data-index="lastExamTime" width="180" />
      <a-table-column title="最后问卷" data-index="lastQuestionnaireTime" width="180" />
      <a-table-column title="操作" width="220" fixed="right">
        <template #default="{ record }">
          <a-space>
            <a-button type="link" @click="() => openDetail(record)">详情</a-button>
            <a-button type="link" @click="() => openSync(record)">同步</a-button>
          </a-space>
        </template>
      </a-table-column>
    </a-table>

    <a-modal v-model:open="detailOpen" :title="detailTitle" :footer="null" width="820px">
      <a-spin :spinning="detailLoading">
        <a-descriptions v-if="detail" bordered :column="2">
          <a-descriptions-item label="患者">{{ detail.patientId }}</a-descriptions-item>
          <a-descriptions-item label="userId">{{ detail.userId }}</a-descriptions-item>
          <a-descriptions-item label="年龄">{{ detail.age }}</a-descriptions-item>
          <a-descriptions-item label="性别">{{ genderText(detail.gender) }}</a-descriptions-item>
          <a-descriptions-item label="收缩压">{{ detail.systolicBp }}</a-descriptions-item>
          <a-descriptions-item label="舒张压">{{ detail.diastolicBp }}</a-descriptions-item>
          <a-descriptions-item label="BMI">{{ detail.bmi }}</a-descriptions-item>
          <a-descriptions-item label="胆固醇">{{ detail.cholesterol }}</a-descriptions-item>
          <a-descriptions-item label="家族史">{{ familyHypertensionText(detail.familyHypertension) }}</a-descriptions-item>
          <a-descriptions-item label="吸烟">{{ smokingText(detail.smoking) }}</a-descriptions-item>
          <a-descriptions-item label="饮食">{{ dietText(detail.dietPreference) }}</a-descriptions-item>
          <a-descriptions-item label="标签">{{ labelText(detail.hypertensionLabel) }}</a-descriptions-item>
          <a-descriptions-item label="最后体检">{{ detail.lastExamTime }}</a-descriptions-item>
          <a-descriptions-item label="最后问卷">{{ detail.lastQuestionnaireTime }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ detail.createdAt }}</a-descriptions-item>
          <a-descriptions-item label="更新时间">{{ detail.updatedAt }}</a-descriptions-item>
        </a-descriptions>
      </a-spin>
    </a-modal>

    <a-modal
      v-model:open="syncOpen"
      title="融合数据同步"
      :confirm-loading="syncLoading"
      @ok="doSync"
      @cancel="() => { syncOpen = false }"
      width="520px"
    >
      <a-form layout="vertical">
        <a-form-item label="患者" required>
          <a-select
            v-model:value="syncForm.patientId"
            show-search
            allow-clear
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
        <a-form-item label="hypertensionLabel（可选，空则后端自动推断）">
          <a-select v-model:value="syncForm.hypertensionLabel" allow-clear>
            <a-select-option :value="0">0 - 低风险</a-select-option>
            <a-select-option :value="1">1 - 高风险</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>
