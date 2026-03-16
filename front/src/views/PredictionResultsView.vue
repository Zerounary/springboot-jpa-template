<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import dayjs, { type Dayjs } from 'dayjs'
import {
  predictionResultCreateApi,
  predictionResultDeleteApi,
  predictionResultPageApi,
  predictionResultUpdateApi,
  type PredictionResultCreateRequest,
  type PredictionResultDto,
  type PredictionResultUpdateRequest,
} from '../api/predictionResults'

const loading = ref(false)
const rows = ref<PredictionResultDto[]>([])
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
  predictionTime: null as Dayjs | null,
  predictionProb: 0.5,
  predictionLabel: 0,
  coreRiskFactors: '',
  warningStatus: 0,
})

const modalTitle = computed(() => (editingId.value ? '编辑预测结果' : '新增预测结果'))

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
  form.predictionTime = null
  form.predictionProb = 0.5
  form.predictionLabel = 0
  form.coreRiskFactors = ''
  form.warningStatus = 0
}

function openCreate() {
  editingId.value = null
  resetForm()
  modalOpen.value = true
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
    const data = await predictionResultPageApi({
      page: page.value - 1,
      size: pageSize.value,
      patientId: query.patientId,
    })
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function labelText(v: number) {
  return v === 1 ? '高风险' : '低风险'
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

onMounted(load)
</script>

<template>
  <a-card>
    <template #title>预测结果</template>

    <a-form layout="inline" style="margin-bottom: 12px" @submit.prevent>
      <a-form-item label="patientId">
        <a-input-number v-model:value="query.patientId" :min="1" style="width: 180px" placeholder="按患者过滤" />
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
      :scroll="{ x: 1100 }"
    >
      <a-table-column title="ID" data-index="id" width="80" />
      <a-table-column title="patientId" data-index="patientId" width="100" />
      <a-table-column title="预测时间" data-index="predictionTime" width="180" />
      <a-table-column title="概率" :customRender="probRender" width="100" />
      <a-table-column title="标签" :customRender="labelRender" width="100" />
      <a-table-column title="预警状态" :customRender="warningRender" width="120" />
      <a-table-column title="核心风险因素" data-index="coreRiskFactors" />
      <a-table-column title="操作" width="180" fixed="right">
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
      width="820px"
    >
      <a-form layout="vertical">
        <a-form-item v-if="!editingId" label="patientId" required>
          <a-input-number v-model:value="form.patientId" :min="1" style="width: 100%" />
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
