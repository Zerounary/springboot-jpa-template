<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, DataAnalysis, Document, FirstAidKit, User } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import http from '../../utils/http'

const router = useRouter()

const me = ref(null)
const loading = ref(false)
const doctors = ref([])
const registrations = ref([])
const medicalRecords = ref([])
const healthRecords = ref([])

const serviceChartEl = ref(null)
const registrationChartEl = ref(null)
const heatmapChartEl = ref(null)

let serviceChart = null
let registrationChart = null
let heatmapChart = null
let resizeHandler = null

const quickEntries = [
  { label: '找医生', title: '医生查询', desc: '按科室与擅长快速筛选医生', icon: User, path: '/patient/doctors' },
  { label: '去挂号', title: '在线预约', desc: '选择日期后快速完成挂号', icon: Calendar, path: '/patient/registrations' },
  { label: '查病历', title: '病例记录', desc: '快速查阅诊断、主诉与治疗方案', icon: Document, path: '/patient/medical-records' },
  { label: '做记录', title: '健康监测', desc: '追踪血压血糖和体重趋势', icon: DataAnalysis, path: '/patient/health' },
  { label: '管用药', title: '用药管理', desc: '查看处方、提醒和服药依从性', icon: FirstAidKit, path: '/patient/medication' },
]

const overviewCards = computed(() => {
  const pendingRegistrations = registrations.value.filter((item) => item.registrationStatus === 0).length
  const completedRecords = medicalRecords.value.filter((item) => item.recordStatus === 1).length
  const treatmentPlans = medicalRecords.value.filter((item) => item.treatmentPlan).length
  return [
    { label: '可预约医生', value: doctors.value.length, desc: '匹配当前患者可见医生资源', icon: User },
    { label: '待就诊预约', value: pendingRegistrations, desc: '跟踪近期预约进度', icon: Calendar },
    { label: '完成病历', value: completedRecords, desc: '诊疗结果可随时回顾', icon: Document },
    { label: '用药方案', value: treatmentPlans, desc: '已生成治疗方案病历数', icon: FirstAidKit },
  ]
})

function ensureChart(targetEl, instance) {
  if (!targetEl.value) return null
  if (!instance) {
    return echarts.init(targetEl.value)
  }
  return instance
}

function renderServiceChart() {
  serviceChart = ensureChart(serviceChartEl, serviceChart)
  if (!serviceChart) return
  serviceChart.setOption({
    title: {
      text: '服务使用柱状图',
      textStyle: { fontSize: 14, fontWeight: 'normal' }
    },
    tooltip: { trigger: 'axis' },
    grid: { left: 18, right: 12, top: 18, bottom: 18, containLabel: true },
    xAxis: {
      type: 'category',
      data: ['医生', '挂号', '病历', '健康', '用药'],
      axisTick: { show: false },
    },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      barWidth: 22,
      itemStyle: { borderRadius: [8, 8, 0, 0], color: '#2f7cff' },
      data: [
        doctors.value.length,
        registrations.value.length,
        medicalRecords.value.length,
        healthRecords.value.length,
        medicalRecords.value.filter((item) => item.treatmentPlan).length,
      ],
    }],
  })
}

function renderRegistrationChart() {
  registrationChart = ensureChart(registrationChartEl, registrationChart)
  if (!registrationChart) return
  const statusData = [
    { name: '待就诊', value: registrations.value.filter((item) => item.registrationStatus === 0).length },
    { name: '已就诊', value: registrations.value.filter((item) => item.registrationStatus === 1).length },
    { name: '已取消', value: registrations.value.filter((item) => item.registrationStatus === 2).length },
  ]
  registrationChart.setOption({
    title: {
      text: '预约状态饼图',
      textStyle: { fontSize: 14, fontWeight: 'normal' }
    },
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['48%', '72%'],
      center: ['50%', '44%'],
      itemStyle: { borderColor: '#fff', borderWidth: 2, borderRadius: 10 },
      data: statusData,
    }],
  })
}

function renderHeatmapChart() {
  heatmapChart = ensureChart(heatmapChartEl, heatmapChart)
  if (!heatmapChart) return
  const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const hours = Array.from({ length: 24 }, (_, index) => `${index}时`)
  const map = new Map()

  for (const item of healthRecords.value) {
    const date = new Date(item.monitorDate)
    if (Number.isNaN(date.getTime())) continue
    const week = (date.getDay() + 6) % 7
    const hour = date.getHours()
    const key = `${hour}_${week}`
    map.set(key, (map.get(key) || 0) + 1)
  }

  const data = []
  for (let hour = 0; hour < 24; hour += 1) {
    for (let week = 0; week < 7; week += 1) {
      data.push([hour, week, map.get(`${hour}_${week}`) || 0])
    }
  }

  heatmapChart.setOption({
    tooltip: { position: 'top' },
    grid: { left: 28, right: 12, top: 12, bottom: 36, containLabel: true },
    xAxis: { type: 'category', data: hours, splitArea: { show: true }, axisLabel: { interval: 3 } },
    yAxis: { type: 'category', data: weekdays, splitArea: { show: true } },
    visualMap: {
      min: 0,
      max: Math.max(...data.map((item) => item[2]), 1),
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 0,
      inRange: { color: ['#eef5ff', '#96c0ff', '#2f7cff'] },
    },
    series: [{ type: 'heatmap', data }],
  })
}

function renderCharts() {
  renderServiceChart()
  renderRegistrationChart()
  renderHeatmapChart()
}

async function safeRequest(promise, fallback) {
  try {
    return await promise
  } catch {
    return fallback
  }
}

async function loadDashboard() {
  loading.value = true
  me.value = await safeRequest(http.get('/api/auth/me'), null)
  const [doctorRes, registrationRes, medicalRes, healthRes] = await Promise.all([
    safeRequest(http.get('/api/doctors', { params: { page: 0, size: 20 } }), { records: [] }),
    safeRequest(http.get('/api/registrations', { params: { page: 0, size: 30 } }), { records: [] }),
    safeRequest(http.get('/api/medical-records', { params: { page: 0, size: 30 } }), { records: [] }),
    safeRequest(http.get('/api/health-monitors', { params: { page: 0, size: 50 } }), { records: [] }),
  ])

  doctors.value = doctorRes?.records || []
  registrations.value = registrationRes?.records || []
  medicalRecords.value = medicalRes?.records || []
  healthRecords.value = healthRes?.records || []

  await nextTick()
  renderCharts()
  loading.value = false
}

onMounted(async () => {
  await loadDashboard()
  resizeHandler = () => {
    serviceChart?.resize()
    registrationChart?.resize()
    heatmapChart?.resize()
  }
  window.addEventListener('resize', resizeHandler)
})

onUnmounted(() => {
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
  }
  serviceChart?.dispose()
  registrationChart?.dispose()
  heatmapChart?.dispose()
})
</script>

<template>
  <div class="patient-page-stack">
    <section class="patient-hero-card">
      <div class="patient-hero-card__eyebrow">患者服务中心</div>
      <div class="patient-hero-card__title">
        {{ me ? `你好，${me.realName || me.username}` : '欢迎使用患者端' }}
      </div>
      <div class="patient-hero-card__desc">
        在这里你可以快速预约医生、查看病历、记录健康数据，并通过图表实时掌握个人健康服务动态。
      </div>
    </section>

    <section class="patient-panel-card">
      <div class="patient-section-head">
        <div>
          <div class="patient-section-title">今日概览</div>
          <div class="patient-section-subtitle">图标化统计卡快速掌握预约、病历和用药重点</div>
        </div>
      </div>
      <div class="patient-kpi-grid">
        <div v-for="card in overviewCards" :key="card.label" class="patient-kpi-card">
          <div class="patient-kpi-card__icon">
            <el-icon><component :is="card.icon" /></el-icon>
          </div>
          <div class="patient-kpi-card__body">
            <div class="patient-kpi-card__label">{{ card.label }}</div>
            <div class="patient-kpi-card__value">{{ loading ? '-' : card.value }}</div>
            <div class="patient-kpi-card__desc">{{ card.desc }}</div>
          </div>
        </div>
      </div>
    </section>

    <section class="patient-panel-card">
      <div class="patient-section-head">
        <div>
          <div class="patient-section-title">首页数据驾驶舱</div>
          <div class="patient-section-subtitle">用柱状图、饼图和热力图展示近期服务活跃度</div>
        </div>
      </div>
      <div class="patient-viz-grid">
        <div class="patient-chart-card">
          <div class="patient-chart-card__head">
            <div>
              <div class="patient-chart-card__title">服务使用柱状图</div>
              <div class="patient-chart-card__desc">对比医生、挂号、病历、健康与用药模块活跃度</div>
            </div>
            <div class="patient-chart-card__meta">5 类模块</div>
          </div>
          <div ref="serviceChartEl" class="patient-chart" />
        </div>

        <div class="patient-chart-card">
          <div class="patient-chart-card__head">
            <div>
              <div class="patient-chart-card__title">预约状态饼图</div>
              <div class="patient-chart-card__desc">查看当前待就诊、已完成与取消占比</div>
            </div>
            <div class="patient-chart-card__meta">{{ registrations.length }} 条预约</div>
          </div>
          <div ref="registrationChartEl" class="patient-chart" />
        </div>

        <div class="patient-chart-card" style="grid-column: 1 / -1">
          <div class="patient-chart-card__head">
            <div>
              <div class="patient-chart-card__title">健康活跃热力图</div>
              <div class="patient-chart-card__desc">按星期与时段查看你的健康记录分布</div>
            </div>
            <div class="patient-chart-card__meta">{{ healthRecords.length }} 条监测</div>
          </div>
          <div ref="heatmapChartEl" class="patient-chart" />
        </div>
      </div>
    </section>

    <section class="patient-panel-card">
      <div class="patient-section-head">
        <div>
          <div class="patient-section-title">快捷入口</div>
          <div class="patient-section-subtitle">高频操作统一配图标与说明，手机端一眼可达</div>
        </div>
      </div>
      <div class="patient-quick-grid">
        <div v-for="entry in quickEntries" :key="entry.path" class="patient-quick-item" @click="router.push(entry.path)">
          <div class="patient-kpi-card__icon">
            <el-icon><component :is="entry.icon" /></el-icon>
          </div>
          <div class="patient-quick-item__label">{{ entry.label }}</div>
          <div class="patient-quick-item__title">{{ entry.title }}</div>
          <div class="patient-quick-item__desc">{{ entry.desc }}</div>
        </div>
      </div>
    </section>
  </div>
</template>
