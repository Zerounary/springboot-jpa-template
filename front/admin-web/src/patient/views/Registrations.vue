<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { Calendar, DataAnalysis, Money, Tickets } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import http from '../../utils/http'
import { formatDate } from '../utils/format'

const loadingCreate = ref(false)
const loadingList = ref(false)

const deptId = ref(null)
const doctorId = ref(null)

const deptOptions = ref([])
const deptNameMap = ref(new Map())
const doctorOptions = ref([])
const doctorMap = ref(new Map())

const createForm = reactive({
  scheduleDate: null,
  timeSlot: '上午',
  remark: '',
})

const page = ref(0)
const size = ref(10)
const total = ref(0)
const list = ref([])

const chartEl = ref(null)
let chart = null
let resizeHandler = null

const statusFilter = reactive({
  payStatus: null,
  registrationStatus: null,
})

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
  doctorId.value = null
  doctorOptions.value = []
  doctorMap.value = new Map()

  if (!deptId.value) return

  const res = await http.get('/api/doctors', {
    params: {
      page: 0,
      size: 200,
      deptId: deptId.value,
      scheduleDate: createForm.scheduleDate ? formatDate(createForm.scheduleDate) : undefined,
    },
  })

  const records = res.records || []
  const map = new Map()
  for (const d of records) {
    map.set(d.doctorId, d)
  }
  doctorMap.value = map
  doctorOptions.value = records
}

const selectedDoctor = computed(() => {
  if (!doctorId.value) return null
  return doctorMap.value.get(doctorId.value) || null
})

const isDoctorSoldOut = computed(() => {
  if (!selectedDoctor.value) return false
  const remaining = selectedDoctor.value.remainingAppointmentCount
  return Number.isFinite(remaining) && remaining <= 0
})

async function createRegistration() {
  if (!deptId.value) {
    ElMessage.warning('请选择科室')
    return
  }
  if (!doctorId.value) {
    ElMessage.warning('请选择医生')
    return
  }
  if (!createForm.scheduleDate) {
    ElMessage.warning('请选择就诊日期')
    return
  }
  if (isDoctorSoldOut.value) {
    ElMessage.warning('当前医生当日号源已满，请更换日期或医生')
    return
  }

  loadingCreate.value = true
  try {
    const payload = {
      doctorId: doctorId.value,
      deptId: deptId.value,
      scheduleDate: formatDate(createForm.scheduleDate),
      timeSlot: createForm.timeSlot,
      remark: createForm.remark || undefined,
    }

    await http.post('/api/registrations', payload)
    ElMessage.success('挂号成功')

    createForm.remark = ''
    page.value = 0
    await loadList()
  } catch (e) {
    ElMessage.error(e?.message || '挂号失败')
  } finally {
    loadingCreate.value = false
  }
}

function payLabel(v) {
  if (v === 0) return '未支付'
  if (v === 1) return '已支付'
  if (v === 2) return '已退款'
  return String(v ?? '')
}

function regLabel(v) {
  if (v === 0) return '待就诊'
  if (v === 1) return '已就诊'
  if (v === 2) return '已取消'
  if (v === 3) return '已过期'
  return String(v ?? '')
}

async function loadList() {
  loadingList.value = true
  try {
    const res = await http.get('/api/registrations', {
      params: {
        page: page.value,
        size: size.value,
        payStatus: statusFilter.payStatus ?? undefined,
        registrationStatus: statusFilter.registrationStatus ?? undefined,
      },
    })
    list.value = res.records || []
    total.value = Number(res.total || 0)
    await nextTick()
    renderChart()
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loadingList.value = false
  }
}

const overviewCards = computed(() => {
  const unpaid = list.value.filter((item) => item.payStatus === 0).length
  const pending = list.value.filter((item) => item.registrationStatus === 0).length
  const visited = list.value.filter((item) => item.registrationStatus === 1).length
  const totalFee = list.value.reduce((sum, item) => sum + Number(item.registrationFee || 0), 0)
  return [
    { label: '挂号总数', value: total.value, desc: '当前筛选条件下的预约记录', icon: Tickets },
    { label: '待就诊', value: pending, desc: '仍需前往医院就诊', icon: Calendar },
    { label: '待支付', value: unpaid, desc: '尚未完成支付的挂号', icon: Money },
    { label: '累计费用', value: `￥${totalFee.toFixed(0)}`, desc: '当前页挂号费用汇总', icon: DataAnalysis },
    { label: '已就诊', value: visited, desc: '已完成就诊流程', icon: DataAnalysis },
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
  const statusData = [
    { name: '待就诊', value: list.value.filter((item) => item.registrationStatus === 0).length },
    { name: '已就诊', value: list.value.filter((item) => item.registrationStatus === 1).length },
    { name: '已取消', value: list.value.filter((item) => item.registrationStatus === 2).length },
  ]
  instance.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['46%', '70%'],
      center: ['50%', '42%'],
      itemStyle: { borderColor: '#fff', borderWidth: 2, borderRadius: 10 },
      data: statusData,
    }],
  })
}

async function onPageChange(p) {
  page.value = p - 1
  await loadList()
}

async function onPayStatusChange() {
  page.value = 0
  await loadList()
}

async function onRegistrationStatusChange() {
  page.value = 0
  await loadList()
}

async function doPay(item) {
  try {
    await http.post(`/api/registrations/${item.registrationId}/pay`)
    ElMessage.success('支付成功')
    await loadList()
  } catch (e) {
    ElMessage.error(e?.message || '支付失败')
  }
}

async function doCancel(item) {
  try {
    await http.post(`/api/registrations/${item.registrationId}/cancel`)
    ElMessage.success('取消成功')
    await loadList()
  } catch (e) {
    ElMessage.error(e?.message || '取消失败')
  }
}

watch(
  () => deptId.value,
  async () => {
    await loadDoctors()
  },
)

watch(
  () => createForm.scheduleDate,
  async () => {
    if (deptId.value) {
      await loadDoctors()
    }
  },
)

onMounted(async () => {
  await loadDepartments()
  await loadList()
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
          <div class="patient-section-title">在线挂号</div>
          <div class="patient-section-subtitle">选择科室、医生和就诊时间，快速完成预约</div>
        </div>
      </div>

      <div class="patient-kpi-grid" style="margin-bottom: 12px">
        <div v-for="card in overviewCards.slice(0, 4)" :key="card.label" class="patient-kpi-card">
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
        <el-form-item label="科室">
          <el-select v-model="deptId" placeholder="请选择科室" clearable style="width: 100%">
            <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
          </el-select>
        </el-form-item>

        <el-form-item label="医生">
          <el-select v-model="doctorId" placeholder="请选择医生" clearable style="width: 100%" :disabled="!deptId">
            <el-option
              v-for="d in doctorOptions"
              :key="d.doctorId"
              :label="`${d.realName || d.username}（￥${d.registrationFee}，余号 ${d.remainingAppointmentCount ?? d.dailyAppointmentLimit ?? '-'}）`"
              :value="d.doctorId"
              :disabled="createForm.scheduleDate && Number.isFinite(d.remainingAppointmentCount) && d.remainingAppointmentCount <= 0"
            />
          </el-select>
          <div v-if="selectedDoctor" class="patient-helper-text">
            擅长：{{ selectedDoctor.specialty }}
            <span v-if="selectedDoctor.dailyAppointmentLimit != null">
              ｜每日限号：{{ selectedDoctor.dailyAppointmentLimit }}
            </span>
            <span v-if="selectedDoctor.remainingAppointmentCount != null">
              ｜剩余号：{{ selectedDoctor.remainingAppointmentCount }}
            </span>
          </div>
        </el-form-item>

        <el-form-item label="就诊日期">
          <el-date-picker v-model="createForm.scheduleDate" type="date" placeholder="选择日期" style="width: 100%" />
        </el-form-item>

        <el-form-item label="就诊时段">
          <el-segmented v-model="createForm.timeSlot" :options="['上午', '下午', '夜间']" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="createForm.remark" maxlength="255" show-word-limit clearable />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loadingCreate" :disabled="isDoctorSoldOut" @click="createRegistration">
            提交挂号
          </el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="patient-panel-card">
      <div class="patient-section-head">
        <div>
          <div class="patient-section-title">我的挂号</div>
          <div class="patient-section-subtitle">查看预约进度、支付状态与历史记录</div>
        </div>
        <div class="patient-accent">共 {{ total }} 条</div>
      </div>

      <div class="patient-chart-card" style="margin-bottom: 12px">
        <div class="patient-chart-card__head">
          <div>
            <div class="patient-chart-card__title">挂号状态饼图</div>
            <div class="patient-chart-card__desc">用图形方式查看待就诊、已就诊和取消分布</div>
          </div>
          <div class="patient-chart-card__meta">{{ overviewCards[4]?.value ?? 0 }} 条已就诊</div>
        </div>
        <div ref="chartEl" class="patient-chart" />
      </div>

      <el-row :gutter="8" style="margin-bottom: 8px">
        <el-col :span="12">
          <el-select v-model="statusFilter.payStatus" placeholder="支付状态" clearable style="width: 100%" @change="onPayStatusChange">
            <el-option label="未支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="已退款" :value="2" />
          </el-select>
        </el-col>
        <el-col :span="12">
          <el-select v-model="statusFilter.registrationStatus" placeholder="挂号状态" clearable style="width: 100%" @change="onRegistrationStatusChange">
            <el-option label="待就诊" :value="0" />
            <el-option label="已就诊" :value="1" />
            <el-option label="已取消" :value="2" />
            <el-option label="已过期" :value="3" />
          </el-select>
        </el-col>
      </el-row>

      <el-skeleton :loading="loadingList" animated>
        <template #default>
          <div v-if="list.length === 0" class="patient-empty-text">暂无挂号记录</div>

          <div v-for="r in list" :key="r.registrationId" class="patient-reg-card">
            <div class="patient-list-head">
              <div class="patient-title">{{ r.doctorRealName }}</div>
              <div class="patient-fee">￥{{ r.registrationFee }}</div>
            </div>
            <div class="patient-meta">科室：{{ deptNameMap.get(r.deptId) || r.deptId }}</div>
            <div class="patient-meta">日期：{{ r.scheduleDate }}（{{ r.timeSlot }}）</div>
            <div class="patient-meta">支付：{{ payLabel(r.payStatus) }} | 状态：{{ regLabel(r.registrationStatus) }}</div>
            <div v-if="r.remark" class="patient-meta">备注：{{ r.remark }}</div>

            <div class="patient-actions">
              <el-button v-if="r.payStatus === 0" size="small" type="primary" @click="doPay(r)">支付</el-button>
              <el-button v-if="r.registrationStatus === 0" size="small" @click="doCancel(r)">取消</el-button>
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
