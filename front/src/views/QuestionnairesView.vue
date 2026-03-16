<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import dayjs, { type Dayjs } from 'dayjs'
import {
  questionnaireCreateApi,
  questionnaireDeleteApi,
  questionnairePageApi,
  questionnaireUpdateApi,
  type QuestionnaireCreateRequest,
  type QuestionnaireDto,
  type QuestionnaireUpdateRequest,
} from '../api/questionnaires'

const loading = ref(false)
const rows = ref<QuestionnaireDto[]>([])
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
  questionnaireTime: null as Dayjs | null,
  smoking: 0,
  drinking: 0,
  dietPreference: 1,
  exerciseFrequency: 1,
  workRest: null as number | null,
  stressLevel: null as number | null,
  habitRemark: '',
})

const modalTitle = computed(() => (editingId.value ? '编辑问卷' : '新增问卷'))

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
  form.questionnaireTime = null
  form.smoking = 0
  form.drinking = 0
  form.dietPreference = 1
  form.exerciseFrequency = 1
  form.workRest = null
  form.stressLevel = null
  form.habitRemark = ''
}

function openCreate() {
  editingId.value = null
  resetForm()
  modalOpen.value = true
}

function openEdit(r: QuestionnaireDto) {
  editingId.value = r.id
  form.patientId = r.patientId
  form.questionnaireTime = r.questionnaireTime ? dayjs(r.questionnaireTime) : null
  form.smoking = r.smoking
  form.drinking = r.drinking
  form.dietPreference = r.dietPreference
  form.exerciseFrequency = r.exerciseFrequency
  form.workRest = r.workRest ?? null
  form.stressLevel = r.stressLevel ?? null
  form.habitRemark = r.habitRemark || ''
  modalOpen.value = true
}

async function load() {
  loading.value = true
  try {
    const data = await questionnairePageApi({
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

function smokingText(v: number) {
  if (v === 1) return '偶尔'
  if (v === 2) return '长期'
  return '不吸烟'
}

function drinkingText(v: number) {
  if (v === 1) return '偶尔'
  if (v === 2) return '长期'
  return '不饮酒'
}

function dietText(v: number) {
  if (v === 0) return '低盐'
  if (v === 2) return '高盐'
  return '正常'
}

function exerciseText(v: number) {
  if (v === 0) return '不运动'
  if (v === 1) return '每周1-2次'
  if (v === 2) return '每周3-5次'
  return '每天运动'
}

function workRestText(v?: number) {
  if (v === 1) return '偶尔熬夜'
  if (v === 2) return '长期熬夜'
  return '规律'
}

function stressText(v?: number) {
  if (v === 1) return '轻度'
  if (v === 2) return '中度'
  if (v === 3) return '重度'
  return '无'
}

function truncateText(s: string | undefined, max = 24) {
  if (!s) return ''
  return s.length > max ? `${s.slice(0, max)}...` : s
}

function smokingRender({ record }: { record: QuestionnaireDto }) {
  return smokingText(record.smoking)
}

function drinkingRender({ record }: { record: QuestionnaireDto }) {
  return drinkingText(record.drinking)
}

function dietRender({ record }: { record: QuestionnaireDto }) {
  return dietText(record.dietPreference)
}

function exerciseRender({ record }: { record: QuestionnaireDto }) {
  return exerciseText(record.exerciseFrequency)
}

function workRestRender({ record }: { record: QuestionnaireDto }) {
  return workRestText(record.workRest)
}

function stressRender({ record }: { record: QuestionnaireDto }) {
  return stressText(record.stressLevel)
}

function habitRemarkRender({ record }: { record: QuestionnaireDto }) {
  return truncateText(record.habitRemark, 30)
}

async function submit() {
  if (!form.questionnaireTime) {
    message.warning('请选择问卷填写时间')
    return
  }

  modalLoading.value = true
  try {
    const timeStr = form.questionnaireTime.format('YYYY-MM-DDTHH:mm:ss')

    if (!editingId.value) {
      if (!form.patientId) {
        message.warning('请输入 patientId')
        return
      }
      const req: QuestionnaireCreateRequest = {
        patientId: form.patientId,
        questionnaireTime: timeStr,
        smoking: form.smoking,
        drinking: form.drinking,
        dietPreference: form.dietPreference,
        exerciseFrequency: form.exerciseFrequency,
        workRest: form.workRest ?? undefined,
        stressLevel: form.stressLevel ?? undefined,
        habitRemark: form.habitRemark || undefined,
      }
      await questionnaireCreateApi(req)
      message.success('创建成功')
    } else {
      const req: QuestionnaireUpdateRequest = {
        questionnaireTime: timeStr,
        smoking: form.smoking,
        drinking: form.drinking,
        dietPreference: form.dietPreference,
        exerciseFrequency: form.exerciseFrequency,
        workRest: form.workRest ?? undefined,
        stressLevel: form.stressLevel ?? undefined,
        habitRemark: form.habitRemark || undefined,
      }
      await questionnaireUpdateApi(editingId.value, req)
      message.success('更新成功')
    }

    modalOpen.value = false
    await load()
  } finally {
    modalLoading.value = false
  }
}

function confirmDelete(r: QuestionnaireDto) {
  Modal.confirm({
    title: '确认删除',
    content: `确定删除问卷（ID=${r.id}）吗？`,
    async onOk() {
      await questionnaireDeleteApi(r.id)
      message.success('已删除')
      await load()
    },
  })
}

onMounted(load)
</script>

<template>
  <a-card>
    <template #title>问卷数据</template>

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
      <a-table-column title="填写时间" data-index="questionnaireTime" width="180" />
      <a-table-column title="吸烟" :customRender="smokingRender" width="100" />
      <a-table-column title="饮酒" :customRender="drinkingRender" width="100" />
      <a-table-column title="饮食" :customRender="dietRender" width="100" />
      <a-table-column title="运动" :customRender="exerciseRender" width="120" />
      <a-table-column title="作息" :customRender="workRestRender" width="100" />
      <a-table-column title="压力" :customRender="stressRender" width="100" />
      <a-table-column title="备注" :customRender="habitRemarkRender" />
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

        <a-form-item label="问卷填写时间" required>
          <a-date-picker v-model:value="form.questionnaireTime" show-time style="width: 100%" />
        </a-form-item>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="吸烟情况" required>
              <a-select v-model:value="form.smoking">
                <a-select-option :value="0">不吸烟</a-select-option>
                <a-select-option :value="1">偶尔吸烟</a-select-option>
                <a-select-option :value="2">长期吸烟</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="饮酒情况" required>
              <a-select v-model:value="form.drinking">
                <a-select-option :value="0">不饮酒</a-select-option>
                <a-select-option :value="1">偶尔饮酒</a-select-option>
                <a-select-option :value="2">长期饮酒</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="饮食偏好" required>
              <a-select v-model:value="form.dietPreference">
                <a-select-option :value="0">低盐</a-select-option>
                <a-select-option :value="1">正常</a-select-option>
                <a-select-option :value="2">高盐</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="运动频率" required>
              <a-select v-model:value="form.exerciseFrequency">
                <a-select-option :value="0">不运动</a-select-option>
                <a-select-option :value="1">每周1-2次</a-select-option>
                <a-select-option :value="2">每周3-5次</a-select-option>
                <a-select-option :value="3">每天运动</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="作息情况">
              <a-select v-model:value="form.workRest" allow-clear>
                <a-select-option :value="0">规律作息</a-select-option>
                <a-select-option :value="1">偶尔熬夜</a-select-option>
                <a-select-option :value="2">长期熬夜</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="压力水平">
              <a-select v-model:value="form.stressLevel" allow-clear>
                <a-select-option :value="0">无压力</a-select-option>
                <a-select-option :value="1">轻度压力</a-select-option>
                <a-select-option :value="2">中度压力</a-select-option>
                <a-select-option :value="3">重度压力</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="生活习惯备注">
          <a-textarea v-model:value="form.habitRemark" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>
