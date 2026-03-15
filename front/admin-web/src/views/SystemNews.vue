<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../utils/http'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const isAdmin = computed(() => auth.roleType === 1)

const loading = ref(false)

const filters = reactive({
  title: '',
  status: null,
  isTop: null,
})

const page = ref(0)
const size = ref(10)
const total = ref(0)
const records = ref([])

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

const editVisible = ref(false)
const editMode = ref('create')
const editLoading = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  newsId: null,
  title: '',
  coverImage: '',
  content: '',
  isTop: 0,
  status: 1,
})

function statusText(v) {
  if (v === 1) return '已发布'
  if (v === 2) return '未发布'
  return '-'
}

async function fetchPage() {
  loading.value = true
  try {
    const data = await http.get('/api/system-news', {
      params: {
        page: page.value,
        size: size.value,
        title: filters.title?.trim() || undefined,
        status: isAdmin.value ? filters.status ?? undefined : undefined,
        isTop: isAdmin.value ? filters.isTop ?? undefined : undefined,
        includeContent: false,
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

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    detail.value = await http.get(`/api/system-news/${row.newsId}`)
  } finally {
    detailLoading.value = false
  }
}

function openCreate() {
  editMode.value = 'create'
  editForm.newsId = null
  editForm.title = ''
  editForm.coverImage = ''
  editForm.content = ''
  editForm.isTop = 0
  editForm.status = 1
  editVisible.value = true
}

async function openEdit(row) {
  editMode.value = 'edit'
  editVisible.value = true
  editLoading.value = true
  try {
    const d = await http.get(`/api/system-news/${row.newsId}`)
    editForm.newsId = d.newsId
    editForm.title = d.title || ''
    editForm.coverImage = d.coverImage || ''
    editForm.content = d.content || ''
    editForm.isTop = d.isTop ?? 0
    editForm.status = d.status ?? 1
  } finally {
    editLoading.value = false
  }
}

async function submitEdit() {
  if (!isAdmin.value) return

  await editFormRef.value?.validate?.()

  editLoading.value = true
  try {
    if (editMode.value === 'create') {
      await http.post('/api/system-news', {
        title: editForm.title,
        coverImage: editForm.coverImage || null,
        content: editForm.content,
        isTop: editForm.isTop,
        status: editForm.status,
      })
      ElMessage.success('已创建')
    } else {
      await http.put(`/api/system-news/${editForm.newsId}`, {
        title: editForm.title,
        coverImage: editForm.coverImage || null,
        content: editForm.content,
        isTop: editForm.isTop,
        status: editForm.status,
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
  await ElMessageBox.confirm('确认删除该资讯？此操作不可恢复。', '提示', { type: 'warning' })
  await http.delete(`/api/system-news/${row.newsId}`)
  ElMessage.success('已删除')
  fetchPage()
}

async function doPublish(row) {
  await http.post(`/api/system-news/${row.newsId}/publish`)
  ElMessage.success('已发布')
  fetchPage()
}

async function doUnpublish(row) {
  await http.post(`/api/system-news/${row.newsId}/unpublish`)
  ElMessage.success('已下架')
  fetchPage()
}

async function doTop(row) {
  await http.post(`/api/system-news/${row.newsId}/top`)
  ElMessage.success('已置顶')
  fetchPage()
}

async function doUntop(row) {
  await http.post(`/api/system-news/${row.newsId}/untop`)
  ElMessage.success('已取消置顶')
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
  await fetchPage()
})
</script>

<template>
  <el-card>
    <div style="display: flex; gap: 12px; flex-wrap: wrap; align-items: center; justify-content: space-between">
      <div style="display: flex; gap: 12px; flex-wrap: wrap; align-items: center">
        <el-input
          v-model="filters.title"
          placeholder="标题"
          clearable
          style="width: 240px"
          @keyup.enter="resetAndSearch"
        />

        <el-select v-if="isAdmin" v-model="filters.status" placeholder="状态" clearable style="width: 140px" @change="resetAndSearch">
          <el-option :value="1" label="已发布" />
          <el-option :value="2" label="未发布" />
        </el-select>

        <el-select v-if="isAdmin" v-model="filters.isTop" placeholder="置顶" clearable style="width: 140px" @change="resetAndSearch">
          <el-option :value="1" label="置顶" />
          <el-option :value="0" label="不置顶" />
        </el-select>

        <el-button type="primary" @click="resetAndSearch">查询</el-button>
      </div>

      <el-button v-if="isAdmin" type="primary" @click="openCreate">新增资讯</el-button>
    </div>

    <el-table :data="records" v-loading="loading" style="width: 100%; margin-top: 12px">
      <el-table-column prop="newsId" label="ID" width="90" />
      <el-table-column prop="title" label="标题" min-width="260" show-overflow-tooltip />
      <el-table-column prop="author" label="作者" width="120" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="置顶" width="90">
        <template #default="{ row }">
          <el-tag :type="row.isTop === 1 ? 'warning' : 'info'">{{ row.isTop === 1 ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="90" />
      <el-table-column prop="publishTime" label="发布时间" width="170" />
      <el-table-column prop="updateTime" label="更新时间" width="170" />
      <el-table-column label="操作" width="340" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDetail(row)">详情</el-button>
          <el-button v-if="isAdmin" size="small" @click="openEdit(row)">编辑</el-button>

          <el-button
            v-if="isAdmin"
            size="small"
            type="success"
            :disabled="row.status === 1"
            @click="doPublish(row)"
          >
            发布
          </el-button>
          <el-button
            v-if="isAdmin"
            size="small"
            type="warning"
            :disabled="row.status !== 1"
            @click="doUnpublish(row)"
          >
            下架
          </el-button>

          <el-button
            v-if="isAdmin"
            size="small"
            type="primary"
            :disabled="row.isTop === 1"
            @click="doTop(row)"
          >
            置顶
          </el-button>
          <el-button
            v-if="isAdmin"
            size="small"
            type="info"
            :disabled="row.isTop !== 1"
            @click="doUntop(row)"
          >
            取消置顶
          </el-button>

          <el-button v-if="isAdmin" size="small" type="danger" @click="doDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="display: flex; justify-content: flex-end; margin-top: 12px">
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

  <el-dialog v-model="detailVisible" title="资讯详情" width="860px">
    <div v-loading="detailLoading">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ detail.newsId }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusText(detail.status) }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ detail.author || '-' }}</el-descriptions-item>
          <el-descriptions-item label="浏览量">{{ detail.viewCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="置顶">{{ detail.isTop === 1 ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ detail.publishTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <div style="font-weight: 700; margin-bottom: 8px">{{ detail.title }}</div>
        <div style="white-space: pre-wrap; line-height: 1.6">{{ detail.content }}</div>
      </template>
    </div>
    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="editVisible" :title="editMode === 'create' ? '新增资讯' : '编辑资讯'" width="860px">
    <el-form ref="editFormRef" :model="editForm" label-width="90px" :disabled="editLoading">
      <el-form-item label="标题" prop="title" :rules="[{ required: true, message: '请输入标题', trigger: 'blur' }]">
        <el-input v-model="editForm.title" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="封面图" prop="coverImage">
        <el-input v-model="editForm.coverImage" maxlength="255" placeholder="图片 URL（可选）" />
      </el-form-item>
      <el-form-item label="置顶" prop="isTop">
        <el-select v-model="editForm.isTop" style="width: 140px">
          <el-option :value="1" label="置顶" />
          <el-option :value="0" label="不置顶" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="editForm.status" style="width: 140px">
          <el-option :value="1" label="发布" />
          <el-option :value="2" label="未发布" />
        </el-select>
      </el-form-item>
      <el-form-item label="内容" prop="content" :rules="[{ required: true, message: '请输入内容', trigger: 'blur' }]">
        <el-input v-model="editForm.content" type="textarea" :rows="10" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" :loading="editLoading" @click="submitEdit">保存</el-button>
    </template>
  </el-dialog>
</template>
