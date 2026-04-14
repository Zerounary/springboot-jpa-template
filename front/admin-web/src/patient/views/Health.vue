<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { DataAnalysis, Histogram, Opportunity, TrendCharts } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import http from '../../utils/http'
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
let resizeHandler = null

const overviewCards = computed(() => {
  const latest = list.value[0] || {}
  const avgGlucose = list.value.length > 0
    ? (list.value.reduce((sum, item) => sum + Number(item.bloodGlucose || 0), 0) / Math.max(list.value.filter((item) => item.bloodGlucose != null).length, 1)).toFixed(1)
    : '0.0'
  return [
    { label: '健康记录', value: total.value, desc: '当前已保存的监测次数', icon: Histogram },
    { label: '最新血压', value: `${latest.systolicPressure ?? '-'} / ${latest.diastolicPressure ?? '-'}`, desc: '最近一次血压记录', icon: TrendCharts },
    { label: '平均血糖', value: avgGlucose, desc: '当前页监测均值', icon: DataAnalysis },
    { label: '最新体重', value: latest.weight ?? '-', desc: '最近一次体重记录', icon: Opportunity },
  ]
})

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
  resizeHandler = () => chart && chart.resize()
  window.addEventListener('resize', resizeHandler)
})

onUnmounted(() => {
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
  }
  if (chart) {
    chart.dispose()
    chart = null
  }
})
</script>

<template>
  <div class="patient-page-stack">
    <section class="patient-panel-card">
      <div class="patient-section-head">
        <div>
          <div class="patient-section-title">健康监测</div>
          <div class="patient-section-subtitle">随手记录关键体征数据，适合手机端快速录入</div>
        </div>
      </div>

      <div class="patient-kpi-grid" style="margin-bottom: 12px">
        <div v-for="card in overviewCards" :key="card.label" class="patient-kpi-card">
          <div class="patient-kpi-card__icon">
            <el-icon><component :is="card.icon" /></el-icon>
          </div>
          <div class="patient-kpi-card__body">
            <div class="patient-kpi-card__label">{{ card.label }}</div>
            <div class="patient-kpi-card__value">{{ card.value }}</div>
            <div class="patient-kpi-card__desc">{{ card.desc }}</div>
          </div>
        </div>
      </div>

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
          <el-button type="primary" style="width: 100%" :loading="loadingCreate" @click="createOne">保存记录</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="patient-panel-card">
      <div class="patient-section-head">
        <div>
          <div class="patient-section-title">趋势图</div>
          <div class="patient-section-subtitle">基于当前页历史数据展示变化趋势</div>
        </div>
      </div>
      <div ref="chartEl" style="height: 260px; width: 100%" />
    </section>

    <section class="patient-panel-card">
      <div class="patient-section-head">
        <div>
          <div class="patient-section-title">历史记录</div>
          <div class="patient-section-subtitle">查看并管理既往健康监测记录</div>
        </div>
        <div class="patient-accent">共 {{ total }} 条</div>
      </div>

      <el-skeleton :loading="loadingList" animated>
        <template #default>
          <div v-if="list.length === 0" class="patient-empty-text">暂无健康记录</div>

          <div v-for="h in list" :key="h.monitorId" class="patient-hm-card">
            <div class="patient-list-head">
              <div class="patient-title">{{ formatDateTime(h.monitorDate) }}</div>
              <el-button size="small" type="danger" plain @click="removeOne(h)">删除</el-button>
            </div>
            <div class="patient-meta">血压：{{ h.systolicPressure ?? '-' }}/{{ h.diastolicPressure ?? '-' }}</div>
            <div class="patient-meta">血糖：{{ h.bloodGlucose ?? '-' }} | 心率：{{ h.heartRate ?? '-' }}</div>
            <div class="patient-meta">体温：{{ h.bodyTemperature ?? '-' }} | 体重：{{ h.weight ?? '-' }}</div>
            <div v-if="h.remark" class="patient-meta">备注：{{ h.remark }}</div>
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
</template>
