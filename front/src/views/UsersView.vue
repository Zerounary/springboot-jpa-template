<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  userCreateApi,
  userDeleteApi,
  userPageApi,
  userUpdateApi,
  type UserCreateRequest,
  type UserDto,
  type UserUpdateRequest,
} from '../api/users'

const loading = ref(false)
const rows = ref<UserDto[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const query = reactive({
  keyword: '',
})

const modalOpen = ref(false)
const modalLoading = ref(false)
const editingId = ref<number | null>(null)

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
})

const modalTitle = computed(() => (editingId.value ? '编辑用户' : '新增用户'))

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

function resetForm() {
  form.username = ''
  form.password = ''
  form.nickname = ''
  form.email = ''
}

function openCreate() {
  editingId.value = null
  resetForm()
  modalOpen.value = true
}

function openEdit(r: UserDto) {
  editingId.value = r.id
  form.username = r.username
  form.password = ''
  form.nickname = r.nickname || ''
  form.email = r.email || ''
  modalOpen.value = true
}

async function load() {
  loading.value = true
  try {
    const data = await userPageApi({
      page: page.value - 1,
      size: pageSize.value,
      keyword: query.keyword || null,
    })
    rows.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!editingId.value) {
    if (!form.username) {
      message.warning('请输入用户名')
      return
    }
    if (!form.password || form.password.length < 6) {
      message.warning('请输入至少 6 位密码')
      return
    }
  }

  if (form.email && !/^\S+@\S+\.\S+$/.test(form.email)) {
    message.warning('邮箱格式不正确')
    return
  }

  modalLoading.value = true
  try {
    if (!editingId.value) {
      const req: UserCreateRequest = {
        username: form.username,
        password: form.password,
        nickname: form.nickname || undefined,
        email: form.email || undefined,
      }
      await userCreateApi(req)
      message.success('创建成功')
    } else {
      const req: UserUpdateRequest = {
        nickname: form.nickname || undefined,
        email: form.email || undefined,
        password: form.password || undefined,
      }
      await userUpdateApi(editingId.value, req)
      message.success('更新成功')
    }

    modalOpen.value = false
    await load()
  } finally {
    modalLoading.value = false
  }
}

function confirmDelete(r: UserDto) {
  Modal.confirm({
    title: '确认删除',
    content: `确定删除用户 ${r.username}（ID=${r.id}）吗？`,
    async onOk() {
      await userDeleteApi(r.id)
      message.success('已删除')
      await load()
    },
  })
}

onMounted(load)
</script>

<template>
  <a-card>
    <template #title>用户管理</template>

    <a-form layout="inline" style="margin-bottom: 12px" @submit.prevent>
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="username/nickname" style="width: 260px" />
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
      :scroll="{ x: 900 }"
    >
      <a-table-column title="ID" data-index="id" width="80" />
      <a-table-column title="用户名" data-index="username" width="160" />
      <a-table-column title="昵称" data-index="nickname" width="160" />
      <a-table-column title="邮箱" data-index="email" />
      <a-table-column title="创建时间" data-index="createdAt" width="180" />
      <a-table-column title="更新时间" data-index="updatedAt" width="180" />
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
      width="620px"
    >
      <a-form layout="vertical">
        <a-form-item label="用户名" required>
          <a-input v-model:value="form.username" :disabled="!!editingId" autocomplete="username" />
        </a-form-item>

        <a-form-item :label="editingId ? '新密码（可选）' : '密码'" :required="!editingId">
          <a-input-password v-model:value="form.password" :placeholder="editingId ? '不修改则留空' : ''" />
        </a-form-item>

        <a-form-item label="昵称">
          <a-input v-model:value="form.nickname" />
        </a-form-item>

        <a-form-item label="邮箱">
          <a-input v-model:value="form.email" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>
