<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { Calendar, DataAnalysis, Money, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import http from '../../utils/http'

const loading = ref(false)
const keyword = ref('')
const deptId = ref(null)
const scheduleDate = ref(new Date())

const deptOptions = ref([])
const deptNameMap = ref(new Map())

const page = ref(0)
const size = ref(10)
const total = ref(0)
const list = ref([])

const chartEl = ref(null)
let chart = null
let resizeHandler = null

async function loadDepartments() {
  const tree = await http.get('/api/departments/tree', { params: { includeDisabled: false } })
  const flat = []
  const map = new Map()

  function dfs(nodes) {
    if (!nodes) return
    for (const n of nodes) {
      flat.push({ deptId: n.deptId, deptName: n.deptName })
      map.set(n.deptId, n.deptName)
      if (n.children && n.children.length) dfs(n.children)
    }
  }
  dfs(tree)

  deptOptions.value = flat
  deptNameMap.value = map
}

async function loadDoctors() {
  loading.value = true
  try {
    const res = await http.get('/api/doctors', {
      params: {
        page: page.value,
        size: size.value,
        deptId: deptId.value || undefined,
        keyword: keyword.value || undefined,
        scheduleDate: scheduleDate.value ? new Date(scheduleDate.value).toISOString().split('T')[0] : undefined,
      },
    })
    list.value = res.records || []
    total.value = Number(res.total || 0)
    await nextTick()
    renderChart()
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const overviewCards = computed(() => {
  const avgFee = list.value.length > 0
    ? (list.value.reduce((sum, item) => sum + Number(item.registrationFee || 0), 0) / list.value.length).toFixed(1)
    : '0.0'
  const totalRemaining = list.value.reduce((sum, item) => sum + Number(item.remainingAppointmentCount ?? item.dailyAppointmentLimit ?? 0), 0)
  return [
    { label: '当前医生数', value: total.value, desc: '支持科室与关键词筛选', icon: User },
    { label: '当前科室数', value: deptOptions.value.length, desc: deptLabel.value || '覆盖全部科室', icon: Calendar },
    { label: '平均挂号费', value: `￥${avgFee}`, desc: '基于当前查询结果计算', icon: Money },
    { label: '可预约余号', value: totalRemaining, desc: '按所选日期统计余号总量', icon: DataAnalysis },
  ]
})

function ensureChart() {
  if (!chartEl.value) return null
  if (!chart) {
    chart = echarts.init(chartEl.value)
  }
  return chart
}

function renderChart() {
  const instance = ensureChart()
  if (!instance) return
  const topDoctors = [...list.value]
    .sort((a, b) => Number(b.remainingAppointmentCount ?? 0) - Number(a.remainingAppointmentCount ?? 0))
    .slice(0, 6)
  instance.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 18, right: 12, top: 18, bottom: 18, containLabel: true },
    xAxis: {
      type: 'category',
      data: topDoctors.map((item) => item.realName || item.username || `医生${item.doctorId}`),
      axisLabel: { interval: 0, rotate: 18 },
    },
    yAxis: { type: 'value' },
    series: [{
      name: '剩余号源',
      type: 'bar',
      barWidth: 22,
      itemStyle: { borderRadius: [8, 8, 0, 0], color: '#2f7cff' },
      data: topDoctors.map((item) => Number(item.remainingAppointmentCount ?? item.dailyAppointmentLimit ?? 0)),
    }],
  })
}

const deptLabel = computed(() => {
  if (!deptId.value) return ''
  return deptNameMap.value.get(deptId.value) || ''
})

async function onSearch() {
  page.value = 0
  await loadDoctors()
}

async function onPageChange(p) {
  page.value = p - 1
  await loadDoctors()
}

onMounted(async () => {
  await loadDepartments()
  await loadDoctors()
  resizeHandler = () => chart?.resize()
  window.addEventListener('resize', resizeHandler)
})

onUnmounted(() => {
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
  }
  chart?.dispose()
})
</script>

<template>
  <div class="patient-page-stack">
    <section class="patient-panel-card">
      <div class="patient-section-head">
        <div>
          <div class="patient-section-title">医生查询</div>
          <div class="patient-section-subtitle">按科室与擅长方向筛选合适的医生</div>
        </div>
        <div class="patient-accent">共 {{ total }} 位</div>
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
        <el-form-item label="关键词（姓名/擅长）">
          <el-input v-model="keyword" placeholder="请输入关键词" clearable />
        </el-form-item>

        <el-form-item label="科室">
          <el-select v-model="deptId" placeholder="全部科室" clearable style="width: 100%">
            <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
          </el-select>
        </el-form-item>

        <el-form-item label="查看日期">
          <el-date-picker v-model="scheduleDate" type="date" placeholder="选择日期" style="width: 100%" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="onSearch">立即查询</el-button>
        </el-form-item>
      </el-form>

      <div v-if="deptLabel" class="patient-helper-text">当前科室：{{ deptLabel }}</div>
    </section>

    <section class="patient-panel-card">
      <div class="patient-section-head">
        <div>
          <div class="patient-section-title">查询结果</div>
          <div class="patient-section-subtitle">展示医生基础信息、擅长方向、收费与余号分布</div>
        </div>
      </div>

      <div class="patient-chart-card" style="margin-bottom: 12px">
        <div class="patient-chart-card__head">
          <div>
            <div class="patient-chart-card__title">余号柱状图</div>
            <div class="patient-chart-card__desc">展示当前查询结果中余号最多的医生</div>
          </div>
          <div class="patient-chart-card__meta">{{ scheduleDate ? '按选定日期统计' : '默认今日' }}</div>
        </div>
        <div ref="chartEl" class="patient-chart" />
      </div>

      <el-skeleton :loading="loading" animated>
        <template #default>
          <div v-if="list.length === 0" class="patient-empty-text">暂无符合条件的医生</div>

          <div v-for="d in list" :key="d.doctorId" class="patient-doctor-card">
            <div class="patient-list-head">
              <div class="patient-name">{{ d.realName || d.username }}</div>
              <div class="patient-fee">￥{{ d.registrationFee }}</div>
            </div>
            <div class="patient-meta">科室：{{ deptNameMap.get(d.deptId) || d.deptId }}</div>
            <div class="patient-meta">职称：{{ d.jobTitle }}</div>
            <div class="patient-meta">擅长：{{ d.specialty }}</div>
            <div v-if="d.schedule">出诊：{{ d.schedule }}</div>
            <div class="patient-meta">
              当日余号：
              <span :class="Number(d.remainingAppointmentCount ?? 0) > 0 ? 'patient-success' : 'patient-danger'">
                {{ d.remainingAppointmentCount ?? d.dailyAppointmentLimit ?? 0 }}/{{ d.dailyAppointmentLimit ?? 0 }}
              </span>
            </div>
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
