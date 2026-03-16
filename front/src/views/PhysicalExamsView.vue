<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import dayjs, { type Dayjs } from 'dayjs'
import {
  physicalExamCreateApi,
  physicalExamDeleteApi,
  physicalExamPageApi,
  physicalExamUpdateApi,
  type PhysicalExamCreateRequest,
  type PhysicalExamDto,
  type PhysicalExamUpdateRequest,
} from '../api/physicalExams'

const loading = ref(false)
const rows = ref<PhysicalExamDto[]>([])
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
  examTime: null as Dayjs | null,
  systolicBp: 120,
  diastolicBp: 80,
  bmi: 24,
  cholesterol: 5,
  fastingBloodSugar: 5,
  height: 170,
  weight: 65,
  heartRate: null as number | null,
  liverFunction: null as number | null,
})

const modalTitle = computed(() => (editingId.value ? '编辑体检记录' : '新增体检记录'))

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
  form.examTime = null
  form.systolicBp = 120
  form.diastolicBp = 80
  form.bmi = 24
  form.cholesterol = 5
  form.fastingBloodSugar = 5
  form.height = 170
  form.weight = 65
  form.heartRate = null
  form.liverFunction = null
}

function openCreate() {
  editingId.value = null
  resetForm()
  modalOpen.value = true
}

function openEdit(r: PhysicalExamDto) {
  editingId.value = r.id
  form.patientId = r.patientId
  form.examTime = r.examTime ? dayjs(r.examTime) : null
  form.systolicBp = r.systolicBp
  form.diastolicBp = r.diastolicBp
  form.bmi = Number(r.bmi)
  form.cholesterol = Number(r.cholesterol)
  form.fastingBloodSugar = Number(r.fastingBloodSugar)
  form.height = Number(r.height)
  form.weight = Number(r.weight)
  form.heartRate = r.heartRate ?? null
  form.liverFunction = r.liverFunction ?? null
  modalOpen.value = true
}

async function load() {
  loading.value = true
  try {
    const data = await physicalExamPageApi({
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

function examTimeRender({ record }: { record: PhysicalExamDto }) {
  return record.examTime || ''
}

async function submit() {
  if (!form.examTime) {
    message.warning('请选择体检时间')
    return
  }
  if (!form.systolicBp || !form.diastolicBp) {
    message.warning('请输入血压')
    return
  }

  modalLoading.value = true
  try {
    const examTimeStr = form.examTime.format('YYYY-MM-DDTHH:mm:ss')

    if (!editingId.value) {
      if (!form.patientId) {
        message.warning('请输入 patientId')
        return
      }
      const req: PhysicalExamCreateRequest = {
        patientId: form.patientId,
        examTime: examTimeStr,
        systolicBp: form.systolicBp,
        diastolicBp: form.diastolicBp,
        bmi: form.bmi,
        cholesterol: form.cholesterol,
        fastingBloodSugar: form.fastingBloodSugar,
        height: form.height,
        weight: form.weight,
        heartRate: form.heartRate ?? undefined,
        liverFunction: form.liverFunction ?? undefined,
      }
      await physicalExamCreateApi(req)
      message.success('创建成功')
    } else {
      const req: PhysicalExamUpdateRequest = {
        examTime: examTimeStr,
        systolicBp: form.systolicBp,
        diastolicBp: form.diastolicBp,
        bmi: form.bmi,
        cholesterol: form.cholesterol,
        fastingBloodSugar: form.fastingBloodSugar,
        height: form.height,
        weight: form.weight,
        heartRate: form.heartRate ?? undefined,
        liverFunction: form.liverFunction ?? undefined,
      }
      await physicalExamUpdateApi(editingId.value, req)
      message.success('更新成功')
    }

    modalOpen.value = false
    await load()
  } finally {
    modalLoading.value = false
  }
}

function confirmDelete(r: PhysicalExamDto) {
  Modal.confirm({
    title: '确认删除',
    content: `确定删除体检记录（ID=${r.id}）吗？`,
    async onOk() {
      await physicalExamDeleteApi(r.id)
      message.success('已删除')
      await load()
    },
  })
}

onMounted(load)
</script>

<template>
  <a-card>
    <template #title>体检数据</template>

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
    >
      <a-table-column title="ID" data-index="id" width="80" />
      <a-table-column title="patientId" data-index="patientId" width="100" />
      <a-table-column title="体检时间" :customRender="examTimeRender" width="180" />
      <a-table-column title="收缩压" data-index="systolicBp" width="90" />
      <a-table-column title="舒张压" data-index="diastolicBp" width="90" />
      <a-table-column title="BMI" data-index="bmi" width="90" />
      <a-table-column title="胆固醇" data-index="cholesterol" width="100" />
      <a-table-column title="空腹血糖" data-index="fastingBloodSugar" width="110" />
      <a-table-column title="身高" data-index="height" width="90" />
      <a-table-column title="体重" data-index="weight" width="90" />
      <a-table-column title="心率" data-index="heartRate" width="90" />
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
      width="780px"
    >
      <a-form layout="vertical">
        <a-form-item v-if="!editingId" label="patientId" required>
          <a-input-number v-model:value="form.patientId" :min="1" style="width: 100%" />
        </a-form-item>

        <a-form-item label="体检时间" required>
          <a-date-picker v-model:value="form.examTime" show-time style="width: 100%" />
        </a-form-item>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="收缩压(mmHg)" required>
              <a-input-number v-model:value="form.systolicBp" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="舒张压(mmHg)" required>
              <a-input-number v-model:value="form.diastolicBp" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="BMI" required>
              <a-input-number v-model:value="form.bmi" :min="0" :step="0.1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="胆固醇(mmol/L)" required>
              <a-input-number v-model:value="form.cholesterol" :min="0" :step="0.1" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="空腹血糖(mmol/L)" required>
              <a-input-number v-model:value="form.fastingBloodSugar" :min="0" :step="0.1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="心率(次/分钟)">
              <a-input-number v-model:value="form.heartRate" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="身高(cm)" required>
              <a-input-number v-model:value="form.height" :min="0" :step="0.1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="体重(kg)" required>
              <a-input-number v-model:value="form.weight" :min="0" :step="0.1" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="肝功能指标">
          <a-input-number v-model:value="form.liverFunction" :min="0" :step="0.1" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>
