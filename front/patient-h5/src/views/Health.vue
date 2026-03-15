<script setup>
import { onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import http from '../utils/http'
import { formatDateTime, toIsoLocalDateTime } from '../utils/format'

const loadingCreate = ref(false)
const loadingList = ref(false)

const form = reactive({
  monitorDate: new Date(),
  systolicPressure: null,
  diastolicPressure: null,
  bloodGlucose: null,
  heartRate: null,
  bodyTemperature: null,
  weight: null,
  remark: '',
})

const page = ref(0)
const size = ref(10)
const total = ref(0)
const list = ref([])

const chartEl = ref(null)
let chart = null

async function createOne() {
  loadingCreate.value = true
  try {
    const payload = {
      monitorDate: toIsoLocalDateTime(form.monitorDate),
      systolicPressure: form.systolicPressure ?? undefined,
      diastolicPressure: form.diastolicPressure ?? undefined,
      bloodGlucose: form.bloodGlucose ?? undefined,
      heartRate: form.heartRate ?? undefined,
      bodyTemperature: form.bodyTemperature ?? undefined,
      weight: form.weight ?? undefined,
      remark: form.remark || undefined,
    }
    await http.post('/api/health-monitors', payload)
    ElMessage.success('保存成功')
    form.remark = ''
    page.value = 0
    await loadList()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    loadingCreate.value = false
  }
}

async function loadList() {
  loadingList.value = true
  try {
    const res = await http.get('/api/health-monitors', {
      params: {
        page: page.value,
        size: size.value,
      },
    })
    list.value = res.records || []
    total.value = Number(res.total || 0)
    renderChart()
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loadingList.value = false
  }
}

async function onPageChange(p) {
  page.value = p - 1
  await loadList()
}

async function removeOne(item) {
  try {
    await http.delete(`/api/health-monitors/${item.monitorId}`)
    ElMessage.success('删除成功')
    await loadList()
  } catch (e) {
    ElMessage.error(e?.message || '删除失败')
  }
}

function ensureChart() {
  if (!chartEl.value) return null
  if (!chart) {
    chart = echarts.init(chartEl.value)
  }
  return chart
}

function renderChart() {
  const c = ensureChart()
  if (!c) return

  const rows = [...(list.value || [])]
  rows.sort((a, b) => String(a.monitorDate).localeCompare(String(b.monitorDate)))

  const x = rows.map((r) => formatDateTime(r.monitorDate))
  const sys = rows.map((r) => r.systolicPressure)
  const dia = rows.map((r) => r.diastolicPressure)
  const glu = rows.map((r) => r.bloodGlucose)
  const w = rows.map((r) => r.weight)

  c.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['收缩压', '舒张压', '血糖', '体重'] },
    grid: { left: 24, right: 18, top: 30, bottom: 36, containLabel: true },
    xAxis: { type: 'category', data: x, axisLabel: { rotate: 40 } },
    yAxis: { type: 'value' },
    series: [
      { name: '收缩压', type: 'line', data: sys, connectNulls: true },
      { name: '舒张压', type: 'line', data: dia, connectNulls: true },
      { name: '血糖', type: 'line', data: glu, connectNulls: true },
      { name: '体重', type: 'line', data: w, connectNulls: true },
    ],
  })
}

watch(
  () => chartEl.value,
  () => {
    if (chartEl.value) {
      renderChart()
    }
  },
)

onMounted(async () => {
  await loadList()
  window.addEventListener('resize', () => chart && chart.resize())
})

onUnmounted(() => {
  if (chart) {
    chart.dispose()
    chart = null
  }
})
</script>

<template>
  <el-card shadow="never">
    <div style="font-weight: 600; margin-bottom: 8px">健康监测</div>

    <el-form label-position="top" @submit.prevent>
      <el-form-item label="监测时间">
        <el-date-picker v-model="form.monitorDate" type="datetime" placeholder="选择时间" style="width: 100%" />
      </el-form-item>

      <el-row :gutter="8">
        <el-col :span="12">
          <el-form-item label="收缩压">
            <el-input-number v-model="form.systolicPressure" :min="0" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="舒张压">
            <el-input-number v-model="form.diastolicPressure" :min="0" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="8">
        <el-col :span="12">
          <el-form-item label="血糖(mmol/L)">
            <el-input-number v-model="form.bloodGlucose" :min="0" :step="0.1" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="心率">
            <el-input-number v-model="form.heartRate" :min="0" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="8">
        <el-col :span="12">
          <el-form-item label="体温(℃)">
            <el-input-number v-model="form.bodyTemperature" :min="0" :step="0.1" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="体重(kg)">
            <el-input-number v-model="form.weight" :min="0" :step="0.1" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="备注">
        <el-input v-model="form.remark" maxlength="255" show-word-limit clearable />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" style="width: 100%" :loading="loadingCreate" @click="createOne">保存</el-button>
      </el-form-item>
    </el-form>
  </el-card>

  <div style="height: 12px" />

  <el-card shadow="never">
    <div style="font-weight: 600; margin-bottom: 8px">趋势图（当前页数据）</div>
    <div ref="chartEl" style="height: 260px; width: 100%" />
  </el-card>

  <div style="height: 12px" />

  <el-card shadow="never">
    <div style="font-weight: 600; margin-bottom: 8px">历史记录</div>

    <el-skeleton :loading="loadingList" animated>
      <template #default>
        <div v-if="list.length === 0" style="color: #909399">暂无数据</div>

        <div v-for="h in list" :key="h.monitorId" class="hm-card">
          <div class="row">
            <div class="title">{{ formatDateTime(h.monitorDate) }}</div>
            <el-button size="small" type="danger" plain @click="removeOne(h)">删除</el-button>
          </div>
          <div class="meta">血压：{{ h.systolicPressure ?? '-' }}/{{ h.diastolicPressure ?? '-' }}</div>
          <div class="meta">血糖：{{ h.bloodGlucose ?? '-' }} | 心率：{{ h.heartRate ?? '-' }}</div>
          <div class="meta">体温：{{ h.bodyTemperature ?? '-' }} | 体重：{{ h.weight ?? '-' }}</div>
          <div v-if="h.remark" class="meta">备注：{{ h.remark }}</div>
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
</template>

<style scoped>
.hm-card {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  margin-bottom: 10px;
  background: #fff;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.title {
  font-weight: 700;
}

.meta {
  font-size: 12px;
  color: #606266;
  line-height: 18px;
}
</style>
