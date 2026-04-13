<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useAuthStore } from '../stores/auth'
import {
  organizationCreateApi,
  organizationDeleteApi,
  organizationListApi,
  organizationUpdateApi,
  type OrganizationCreateRequest,
  type OrganizationDto,
  type OrganizationUpdateRequest,
} from '../api/organizations'

const auth = useAuthStore()
const role = computed(() => auth.user?.role || 'PATIENT')
const canManage = computed(() => role.value === 'ADMIN' || role.value === 'DOCTOR')

const loading = ref(false)
const rows = ref<OrganizationDto[]>([])

const query = reactive({
  keyword: '',
})

const modalOpen = ref(false)
const modalLoading = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  orgCode: '',
  orgName: '',
})

const modalTitle = computed(() => (editingId.value ? '编辑机构' : '新增机构'))

function onSearch() {
  load()
}

function onReset() {
  query.keyword = ''
  load()
}

function resetForm() {
  form.orgCode = ''
  form.orgName = ''
}

function openCreate() {
  if (!canManage.value) return
  editingId.value = null
  resetForm()
  modalOpen.value = true
}

function openEdit(r: OrganizationDto) {
  if (!canManage.value) return
  editingId.value = r.id
  form.orgCode = r.orgCode || ''
  form.orgName = r.orgName
  modalOpen.value = true
}

async function load() {
  loading.value = true
  try {
    rows.value = await organizationListApi({ keyword: query.keyword || null })
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!form.orgName) {
    message.warning('请输入机构名称')
    return
  }

  modalLoading.value = true
  try {
    if (!editingId.value) {
      const req: OrganizationCreateRequest = {
        orgCode: form.orgCode || undefined,
        orgName: form.orgName,
      }
      await organizationCreateApi(req)
      message.success('创建成功')
    } else {
      const req: OrganizationUpdateRequest = {
        orgCode: form.orgCode || undefined,
        orgName: form.orgName,
      }
      await organizationUpdateApi(editingId.value, req)
      message.success('更新成功')
    }

    modalOpen.value = false
    await load()
  } finally {
    modalLoading.value = false
  }
}

function confirmDelete(r: OrganizationDto) {
  if (!canManage.value) return
  Modal.confirm({
    title: '确认删除',
    content: `确定删除机构（ID=${r.id} / ${r.orgName}）吗？`,
    async onOk() {
      await organizationDeleteApi(r.id)
      message.success('已删除')
      await load()
    },
  })
}

onMounted(load)
</script>

<template>
  <a-card>
    <template #title>机构维护</template>

    <a-form layout="inline" style="margin-bottom: 12px" @submit.prevent>
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="机构名称/编码" style="width: 260px" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" :loading="loading" @click="onSearch">查询</a-button>
      </a-form-item>
      <a-form-item>
        <a-button @click="onReset">重置</a-button>
      </a-form-item>
      <a-form-item>
        <a-button v-if="canManage" type="primary" @click="openCreate">新增</a-button>
      </a-form-item>
    </a-form>

    <a-table row-key="id" :loading="loading" :data-source="rows" :pagination="false">
      <a-table-column title="ID" data-index="id" width="90" />
      <a-table-column title="机构编码" data-index="orgCode" width="160" />
      <a-table-column title="机构名称" data-index="orgName" />
      <a-table-column title="创建时间" data-index="createdAt" width="180" />
      <a-table-column title="更新时间" data-index="updatedAt" width="180" />
      <a-table-column v-if="canManage" title="操作" width="180" fixed="right">
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
      width="680px"
    >
      <a-form layout="vertical">
        <a-form-item label="机构编码">
          <a-input v-model:value="form.orgCode" placeholder="例如 ORG_A" />
        </a-form-item>
        <a-form-item label="机构名称" required>
          <a-input v-model:value="form.orgName" placeholder="例如 机构A" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>
