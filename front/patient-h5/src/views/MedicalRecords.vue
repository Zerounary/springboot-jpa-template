<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../utils/http'
import { formatDateTime } from '../utils/format'

const loading = ref(false)

const query = reactive({
  recordStatus: null,
  keyword: '',
})

const page = ref(0)
const size = ref(10)
const total = ref(0)
const list = ref([])

const dialogVisible = ref(false)
const detail = ref(null)

async function loadList() {
  loading.value = true
  try {
    const res = await http.get('/api/medical-records', {
      params: {
        page: page.value,
        size: size.value,
        recordStatus: query.recordStatus ?? undefined,
        keyword: query.keyword || undefined,
      },
    })
    list.value = res.records || []
    total.value = Number(res.total || 0)
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function onSearch() {
  page.value = 0
  await loadList()
}

async function onPageChange(p) {
  page.value = p - 1
  await loadList()
}

async function openDetail(item) {
  try {
    detail.value = await http.get(`/api/medical-records/${item.recordId}`)
    dialogVisible.value = true
  } catch (e) {
    ElMessage.error(e?.message || '加载详情失败')
  }
}

function statusLabel(v) {
  if (v === 0) return '草稿'
  if (v === 1) return '已完成'
  return String(v ?? '')
}

onMounted(async () => {
  await loadList()
})
</script>

<template>
  <el-card shadow="never">
    <div style="font-weight: 600; margin-bottom: 8px">病例查询</div>

    <el-form label-position="top" @submit.prevent>
      <el-form-item label="关键词（诊断/主诉）">
        <el-input v-model="query.keyword" placeholder="例如：高血压" clearable />
      </el-form-item>

      <el-form-item label="状态">
        <el-select v-model="query.recordStatus" placeholder="全部" clearable style="width: 100%">
          <el-option label="草稿" :value="0" />
          <el-option label="已完成" :value="1" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" style="width: 100%" :loading="loading" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-divider content-position="left">结果 {{ total }}</el-divider>

    <el-skeleton :loading="loading" animated>
      <template #default>
        <div v-if="list.length === 0" style="color: #909399">暂无数据</div>

        <div v-for="r in list" :key="r.recordId" class="rec-card" @click="openDetail(r)">
          <div class="row">
            <div class="title">{{ r.diagnosis || '（未填写诊断）' }}</div>
            <div class="status">{{ statusLabel(r.recordStatus) }}</div>
          </div>
          <div class="meta">就诊时间：{{ formatDateTime(r.visitDate) }}</div>
          <div class="meta">医生：{{ r.doctorRealName }}</div>
          <div class="meta" v-if="r.chiefComplaint">主诉：{{ r.chiefComplaint }}</div>
        </div>

        <div style="display: flex; justify-content: center; margin-top: 12px" v-if="total > size">
          <el-pagination
            background
            layout="prev, pager, next"
            :page-size="size"
            :total="total"
            :current-page="page.value + 1"
            @current-change="onPageChange"
          />
        </div>
      </template>
    </el-skeleton>
  </el-card>

  <el-dialog v-model="dialogVisible" title="病历详情" width="92%">
    <div v-if="detail">
      <div class="d-row"><span class="k">就诊时间</span><span class="v">{{ formatDateTime(detail.visitDate) }}</span></div>
      <div class="d-row"><span class="k">医生</span><span class="v">{{ detail.doctorRealName }}</span></div>
      <div class="d-row"><span class="k">诊断</span><span class="v">{{ detail.diagnosis }}</span></div>
      <div class="d-row"><span class="k">主诉</span><span class="v">{{ detail.chiefComplaint }}</span></div>
      <div class="d-row" v-if="detail.presentIllness"><span class="k">现病史</span><span class="v">{{ detail.presentIllness }}</span></div>
      <div class="d-row" v-if="detail.pastHistory"><span class="k">既往史</span><span class="v">{{ detail.pastHistory }}</span></div>
      <div class="d-row" v-if="detail.physicalExamination"><span class="k">体格检查</span><span class="v">{{ detail.physicalExamination }}</span></div>
      <div class="d-row" v-if="detail.auxiliaryExamination"><span class="k">辅助检查</span><span class="v">{{ detail.auxiliaryExamination }}</span></div>
      <div class="d-row" v-if="detail.treatmentPlan"><span class="k">治疗方案</span><span class="v">{{ detail.treatmentPlan }}</span></div>
    </div>
  </el-dialog>
</template>

<style scoped>
.rec-card {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  margin-bottom: 10px;
  background: #fff;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 6px;
}

.title {
  font-weight: 700;
}

.status {
  font-size: 12px;
  color: #409eff;
}

.meta {
  font-size: 12px;
  color: #606266;
  line-height: 18px;
}

.d-row {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
  font-size: 13px;
}

.k {
  width: 70px;
  color: #909399;
  flex: 0 0 auto;
}

.v {
  flex: 1;
  color: #303133;
}
</style>
