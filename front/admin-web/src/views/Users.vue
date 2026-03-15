<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()

const loading = ref(false)

const keyword = ref('')

const page = ref(0)
const size = ref(10)
const total = ref(0)
const records = ref([])

const editVisible = ref(false)
const editMode = ref('create')
const editLoading = ref(false)
const editFormRef = ref(null)

const editForm = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  realName: '',
  phone: '',
  idCard: '',
  gender: null,
  roleType: 3,
  status: 1,
  avatar: '',
})

function roleText(v) {
  if (v === 1) return '管理员'
  if (v === 2) return '医生'
  if (v === 3) return '患者'
  return '-'
}

function statusText(v) {
  if (v === 1) return '启用'
  if (v === 0) return '禁用'
  return '-'
}

async function fetchPage() {
  loading.value = true
  try {
    const data = await http.get('/api/users', {
      params: {
        page: page.value,
        size: size.value,
        keyword: keyword.value?.trim() || undefined,
      },
    })
    records.value = data?.records || []
    total.value = Number(data?.total || 0)
  } finally {
    loading.value = false
  }
}

function resetAndSearch() {
  page.value = 0
  fetchPage()
}

function openCreate() {
  editMode.value = 'create'
  editForm.id = null
  editForm.username = ''
  editForm.password = ''
  editForm.nickname = ''
  editForm.email = ''
  editForm.realName = ''
  editForm.phone = ''
  editForm.idCard = ''
  editForm.gender = null
  editForm.roleType = 3
  editForm.status = 1
  editForm.avatar = ''
  editVisible.value = true
}

async function openEdit(row) {
  editMode.value = 'edit'
  editVisible.value = true
  editLoading.value = true
  try {
    const d = await http.get(`/api/users/${row.id}`)
    editForm.id = d.id
    editForm.username = d.username || ''
    editForm.password = ''
    editForm.nickname = d.nickname || ''
    editForm.email = d.email || ''
    editForm.realName = d.realName || ''
    editForm.phone = d.phone || ''
    editForm.idCard = d.idCard || ''
    editForm.gender = d.gender ?? null
    editForm.roleType = d.roleType ?? 3
    editForm.status = d.status ?? 1
    editForm.avatar = d.avatar || ''
  } finally {
    editLoading.value = false
  }
}

async function submitEdit() {
  await editFormRef.value?.validate?.()

  editLoading.value = true
  try {
    if (editMode.value === 'create') {
      await http.post('/api/users', {
        username: editForm.username,
        password: editForm.password,
        nickname: editForm.nickname || null,
        email: editForm.email || null,
        realName: editForm.realName || null,
        phone: editForm.phone || null,
        idCard: editForm.idCard || null,
        gender: editForm.gender ?? null,
        roleType: editForm.roleType ?? 3,
        status: editForm.status ?? 1,
        avatar: editForm.avatar || null,
      })
      ElMessage.success('已创建')
    } else {
      await http.put(`/api/users/${editForm.id}`, {
        nickname: editForm.nickname || null,
        email: editForm.email || null,
        password: editForm.password || null,
        realName: editForm.realName || null,
        phone: editForm.phone || null,
        idCard: editForm.idCard || null,
        gender: editForm.gender ?? null,
        roleType: editForm.roleType ?? null,
        status: editForm.status ?? null,
        avatar: editForm.avatar || null,
      })
      ElMessage.success('已保存')
    }

    editVisible.value = false
    fetchPage()
  } finally {
    editLoading.value = false
  }
}

async function doDelete(row) {
  await ElMessageBox.confirm(`确认删除用户 ${row.username}？`, '提示', { type: 'warning' })
  await http.delete(`/api/users/${row.id}`)
  ElMessage.success('已删除')
  fetchPage()
}

onMounted(async () => {
  if (!auth.me) {
    try {
      await auth.fetchMe()
    } catch {
      auth.logout()
      return
    }
  }
  fetchPage()
})
</script>

<template>
  <div class="admin-page">
    <section class="admin-section">
      <div class="admin-section__head">
        <div>
          <div class="admin-section__title">用户管理</div>
          <div class="admin-section__subtitle">统一管理账号、身份角色、状态和基础信息</div>
        </div>
        <div class="admin-note">共 {{ total }} 位用户</div>
      </div>

      <div class="admin-stats-grid">
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">当前列表总数</div>
          <div class="admin-stat-soft__value">{{ total }}</div>
          <div class="admin-stat-soft__desc">支持分页查看与快速检索</div>
        </div>
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">管理范围</div>
          <div class="admin-stat-soft__value">账号与角色</div>
          <div class="admin-stat-soft__desc">管理员、医生、患者统一纳入管理</div>
        </div>
        <div class="admin-stat-soft">
          <div class="admin-stat-soft__label">工作目标</div>
          <div class="admin-stat-soft__value">信息清晰</div>
          <div class="admin-stat-soft__desc">保证账号信息维护有序、可追踪</div>
        </div>
      </div>
    </section>

    <el-card class="admin-table-card">
      <div class="admin-toolbar">
        <div class="admin-toolbar__group">
          <el-input v-model="keyword" placeholder="关键词（用户名/昵称）" clearable style="width: 240px" @keyup.enter="resetAndSearch" />
          <el-button type="primary" @click="resetAndSearch">查询</el-button>
        </div>

        <div class="admin-toolbar__group">
          <div class="admin-toolbar__meta">支持新增、编辑、删除用户</div>
          <el-button type="primary" @click="openCreate">新增用户</el-button>
        </div>
      </div>

      <el-table :data="records" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="realName" label="姓名" width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="角色" width="110">
          <template #default="{ row }">{{ roleText(row.roleType) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column prop="updatedAt" label="更新时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; justify-content: flex-end; margin-top: 16px">
        <el-pagination
          background
          layout="prev, pager, next, sizes, total"
          :total="total"
          :page-size="size"
          :page-sizes="[10, 20, 50]"
          :current-page="page + 1"
          @update:current-page="(p) => { page.value = p - 1; fetchPage() }"
          @update:page-size="(s) => { size.value = s; page.value = 0; fetchPage() }"
        />
      </div>
    </el-card>
  </div>

  <el-dialog v-model="editVisible" :title="editMode === 'create' ? '新增用户' : '编辑用户'" width="720px">
    <el-form ref="editFormRef" :model="editForm" label-width="90px" :disabled="editLoading">
      <el-form-item
        label="用户名"
        prop="username"
        :rules="editMode === 'create' ? [{ required: true, message: '请输入用户名', trigger: 'blur' }] : []"
      >
        <el-input v-model="editForm.username" :disabled="editMode !== 'create'" maxlength="64" />
      </el-form-item>

      <el-form-item
        label="密码"
        prop="password"
        :rules="editMode === 'create' ? [{ required: true, message: '请输入密码', trigger: 'blur' }] : []"
      >
        <el-input v-model="editForm.password" type="password" show-password maxlength="64" placeholder="编辑时不填表示不修改" />
      </el-form-item>

      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="editForm.nickname" maxlength="64" />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model="editForm.email" maxlength="128" />
      </el-form-item>

      <el-form-item label="姓名" prop="realName">
        <el-input v-model="editForm.realName" maxlength="20" />
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input v-model="editForm.phone" maxlength="11" />
      </el-form-item>

      <el-form-item label="身份证" prop="idCard">
        <el-input v-model="editForm.idCard" maxlength="18" />
      </el-form-item>

      <el-form-item label="性别" prop="gender">
        <el-select v-model="editForm.gender" clearable style="width: 160px">
          <el-option :value="1" label="男" />
          <el-option :value="2" label="女" />
        </el-select>
      </el-form-item>

      <el-form-item label="角色" prop="roleType">
        <el-select v-model="editForm.roleType" style="width: 160px">
          <el-option :value="1" label="管理员" />
          <el-option :value="2" label="医生" />
          <el-option :value="3" label="患者" />
        </el-select>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-select v-model="editForm.status" style="width: 160px">
          <el-option :value="1" label="启用" />
          <el-option :value="0" label="禁用" />
        </el-select>
      </el-form-item>

      <el-form-item label="头像" prop="avatar">
        <el-input v-model="editForm.avatar" maxlength="255" placeholder="图片 URL（可选）" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" :loading="editLoading" @click="submitEdit">保存</el-button>
    </template>
  </el-dialog>
</template>
