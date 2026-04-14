<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../utils/http'
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
  <div class="patient-page-stack">
    <section class="patient-panel-card">
      <div class="patient-section-head">
        <div>
          <div class="patient-section-title">病例查询</div>
          <div class="patient-section-subtitle">按诊断、主诉和状态快速筛选个人病历</div>
        </div>
        <div class="patient-accent">共 {{ total }} 条</div>
      </div>

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
          <el-button type="primary" style="width: 100%" :loading="loading" @click="onSearch">查询病历</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="patient-panel-card">
      <div class="patient-section-head">
        <div>
          <div class="patient-section-title">病历列表</div>
          <div class="patient-section-subtitle">点击卡片可查看完整病历详情</div>
        </div>
      </div>

      <el-skeleton :loading="loading" animated>
        <template #default>
          <div v-if="list.length === 0" class="patient-empty-text">暂无病历数据</div>

          <div v-for="r in list" :key="r.recordId" class="patient-rec-card" @click="openDetail(r)">
            <div class="patient-list-head">
              <div class="patient-title">{{ r.diagnosis || '（未填写诊断）' }}</div>
              <div class="patient-status">{{ statusLabel(r.recordStatus) }}</div>
            </div>
            <div class="patient-meta">就诊时间：{{ formatDateTime(r.visitDate) }}</div>
            <div class="patient-meta">医生：{{ r.doctorRealName }}</div>
            <div class="patient-meta" v-if="r.chiefComplaint">主诉：{{ r.chiefComplaint }}</div>
          </div>

          <div v-if="total > size" style="display: flex; justify-content: center; margin-top: 12px">
            <el-pagination
              background
              layout="prev, pager, next"
              :page-size="size"
              :total="total"
              :current-page="page + 1"
              @current-change="onPageChange"
            />
          </div>
        </template>
      </el-skeleton>
    </section>
  </div>

  <el-dialog v-model="dialogVisible" title="病历详情" width="92%">
    <div v-if="detail">
      <div class="patient-dialog-row"><span class="patient-dialog-key">就诊时间</span><span class="patient-dialog-value">{{ formatDateTime(detail.visitDate) }}</span></div>
      <div class="patient-dialog-row"><span class="patient-dialog-key">医生</span><span class="patient-dialog-value">{{ detail.doctorRealName }}</span></div>
      <div class="patient-dialog-row"><span class="patient-dialog-key">诊断</span><span class="patient-dialog-value">{{ detail.diagnosis }}</span></div>
      <div class="patient-dialog-row"><span class="patient-dialog-key">主诉</span><span class="patient-dialog-value">{{ detail.chiefComplaint }}</span></div>
      <div class="patient-dialog-row" v-if="detail.presentIllness"><span class="patient-dialog-key">现病史</span><span class="patient-dialog-value">{{ detail.presentIllness }}</span></div>
      <div class="patient-dialog-row" v-if="detail.pastHistory"><span class="patient-dialog-key">既往史</span><span class="patient-dialog-value">{{ detail.pastHistory }}</span></div>
      <div class="patient-dialog-row" v-if="detail.physicalExamination"><span class="patient-dialog-key">体格检查</span><span class="patient-dialog-value">{{ detail.physicalExamination }}</span></div>
      <div class="patient-dialog-row" v-if="detail.auxiliaryExamination"><span class="patient-dialog-key">辅助检查</span><span class="patient-dialog-value">{{ detail.auxiliaryExamination }}</span></div>
      <div class="patient-dialog-row" v-if="detail.treatmentPlan"><span class="patient-dialog-key">治疗方案</span><span class="patient-dialog-value">{{ detail.treatmentPlan }}</span></div>
    </div>
  </el-dialog>
</template>
