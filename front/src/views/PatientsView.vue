<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import dayjs, { type Dayjs } from 'dayjs'
import { useAuthStore } from '../stores/auth'
import {
  patientCreateApi,
  patientDeleteApi,
  patientMeApi,
  patientPageApi,
  patientUpdateMeApi,
  patientUpdateApi,
  type PatientCreateRequest,
  type PatientDto,
  type PatientUpdateRequest,
} from '../api/patients'

const auth = useAuthStore()
const loading = ref(false)
const rows = ref<PatientDto[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const isPatient = computed(() => auth.user?.role === 'PATIENT')

const query = reactive({
  keyword: '',
})

const modalOpen = ref(false)
const modalLoading = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  userId: '',
  accountId: null as number | null,
  gender: 1,
  age: 40,
  birthDate: null as Dayjs | null,
  phone: '',
  medicalInstitution: '',
  nation: '',
})

const modalTitle = computed(() => {
  if (isPatient.value) return '编辑我的档案'
  return editingId.value ? '编辑患者' : '新增患者'
})

function showTotal(t: number) {
  return `共 ${t} 条`
}

function onTableChange(p: { current?: number; pageSize?: number }) {
  page.value = p.current || 1
  pageSize.value = p.pageSize || 10
  load()
}

function genderRender({ record }: { record: PatientDto }) {
  return record.gender === 1 ? '男' : '女'
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
    if (isPatient.value) {
      const me = await patientMeApi()
      rows.value = [me]
      total.value = 1
      page.value = 1
    } else {
      const data = await patientPageApi({
        page: page.value - 1,
        size: pageSize.value,
        keyword: query.keyword || null,
      })
      rows.value = data.records
      total.value = data.total
    }
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.userId = ''
  form.accountId = null
  form.gender = 1
  form.age = 40
  form.birthDate = null
  form.phone = ''
  form.medicalInstitution = ''
  form.nation = ''
}

function openCreate() {
  if (isPatient.value) {
    return
  }
  editingId.value = null
  resetForm()
  modalOpen.value = true
}

function openEdit(r: PatientDto) {
  editingId.value = r.id
  form.userId = r.userId
  form.accountId = r.accountId ?? null
  form.gender = r.gender
  form.age = r.age
  form.birthDate = r.birthDate ? dayjs(r.birthDate) : null
  form.phone = r.phone
  form.medicalInstitution = r.medicalInstitution
  form.nation = r.nation || ''
  modalOpen.value = true
}

function openSelfEdit() {
  if (rows.value[0]) {
    openEdit(rows.value[0])
  }
}

async function submit() {
  if (!form.gender && form.gender !== 0) {
    message.warning('请选择性别')
    return
  }
  if (!form.age) {
    message.warning('请输入年龄')
    return
  }
  if (!form.birthDate) {
    message.warning('请选择出生日期')
    return
  }
  if (!form.phone) {
    message.warning('请输入手机号')
    return
  }
  if (!form.medicalInstitution) {
    message.warning('请输入医疗机构')
    return
  }
  modalLoading.value = true
  try {
    const birthDateStr = form.birthDate.format('YYYY-MM-DD')
    if (!editingId.value) {
      if (!form.userId) {
        message.warning('请输入用户唯一标识')
        return
      }
      const req: PatientCreateRequest = {
        userId: form.userId,
        accountId: form.accountId ?? undefined,
        gender: form.gender,
        age: form.age,
        birthDate: birthDateStr,
        phone: form.phone,
        medicalInstitution: form.medicalInstitution,
        nation: form.nation || undefined,
      }
      await patientCreateApi(req)
      message.success('创建成功')
    } else {
      const req: PatientUpdateRequest = {
        gender: form.gender,
        age: form.age,
        birthDate: birthDateStr,
        phone: form.phone || undefined,
        medicalInstitution: form.medicalInstitution || undefined,
        nation: form.nation || undefined,
      }
      if (isPatient.value) {
        await patientUpdateMeApi(req)
      } else {
        await patientUpdateApi(editingId.value, req)
      }
      message.success('更新成功')
    }
    modalOpen.value = false
    await load()
  } finally {
    modalLoading.value = false
  }
}

function confirmDelete(r: PatientDto) {
  if (isPatient.value) {
    return
  }
  Modal.confirm({
    title: '确认删除',
    content: `确定删除患者 ${r.userId} 吗？`,
    async onOk() {
      await patientDeleteApi(r.id)
      message.success('已删除')
      await load()
    },
  })
}

onMounted(load)
</script>

<template>
  <a-card>
    <template #title>{{ isPatient ? '我的档案' : '患者管理' }}</template>

    <a-form v-if="!isPatient" layout="inline" style="margin-bottom: 12px" @submit.prevent>
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="userId/phone/医疗机构" style="width: 260px" />
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

    <div v-else style="margin-bottom: 12px">
      <a-button type="primary" @click="openSelfEdit">编辑我的档案</a-button>
    </div>

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
      <a-table-column title="用户标识" data-index="userId" />
      <a-table-column v-if="!isPatient" title="账号ID" data-index="accountId" width="120" />
      <a-table-column title="性别" :customRender="genderRender" width="90" />
      <a-table-column title="年龄" data-index="age" width="90" />
      <a-table-column title="出生日期" data-index="birthDate" width="130" />
      <a-table-column title="手机号" data-index="phone" width="150" />
      <a-table-column title="医疗机构" data-index="medicalInstitution" />
      <a-table-column title="民族" data-index="nation" width="120" />
      <a-table-column v-if="!isPatient" title="操作" width="180">
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
    >
      <a-form layout="vertical">
        <a-form-item v-if="!isPatient" label="用户唯一标识" required>
          <a-input v-model:value="form.userId" :disabled="!!editingId" placeholder="例如 U1001" />
        </a-form-item>
        <a-form-item v-if="!isPatient && !editingId" label="绑定账号ID">
          <a-input-number v-model:value="form.accountId" :min="1" style="width: 100%" placeholder="例如 10001" />
        </a-form-item>
        <a-form-item label="性别" required>
          <a-radio-group v-model:value="form.gender">
            <a-radio :value="1">男</a-radio>
            <a-radio :value="0">女</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="年龄" required>
          <a-input-number v-model:value="form.age" :min="0" :max="150" style="width: 100%" />
        </a-form-item>
        <a-form-item label="出生日期" required>
          <a-date-picker v-model:value="form.birthDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="手机号" required>
          <a-input v-model:value="form.phone" />
        </a-form-item>
        <a-form-item label="所属基层医疗机构" required>
          <a-input v-model:value="form.medicalInstitution" />
        </a-form-item>
        <a-form-item label="民族">
          <a-input v-model:value="form.nation" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>
