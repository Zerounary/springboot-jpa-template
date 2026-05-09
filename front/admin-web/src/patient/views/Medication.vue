<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { AlarmClock, DataAnalysis, FirstAidKit, Opportunity } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import http from '../../utils/http'
import { formatDate, formatDateTime } from '../utils/format'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()

const loading = ref(false)
const calendarDate = ref(new Date())
const prescriptions = ref([])
const medicationRecords = ref([])
const reminderDialogVisible = ref(false)
const recordDialogVisible = ref(false)
const chartEl = ref(null)
let chart = null
let resizeHandler = null

const reminderForm = reactive({
  prescriptionId: '',
  medicationName: '',
  reminderTimes: [],
})

const recordForm = reactive({
  prescriptionId: '',
  medicationName: '',
  plannedTime: '',
  takenAt: new Date(),
  status: 'taken',
  notes: '',
})

function userStorageKey(name) {
  const key = auth.me?.userId || auth.me?.username || 'patient'
  return `${name}_${key}`
}

function readJson(key, fallback) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch {
    return fallback
  }
}

function writeJson(key, value) {
  localStorage.setItem(key, JSON.stringify(value))
}

function normalizeTextLines(text) {
  return String(text || '')
    .split(/\r?\n|；|;|。/)
    .map((item) => item.trim())
    .filter(Boolean)
}

function parseMedicationItems(planText) {
  const lines = normalizeTextLines(planText)
  if (lines.length === 0) {
    return [{
      name: '遵医嘱用药',
      dosage: '按医嘱',
      frequency: '每日 2 次',
      duration: '按医嘱',
      note: '当前病历未拆分到具体药品，先按治疗方案展示。',
    }]
  }

  return lines.map((line, index) => {
    const parts = line.split(/[，,:：]/).map((item) => item.trim()).filter(Boolean)
    return {
      name: parts[0] || `方案 ${index + 1}`,
      dosage: parts[1] || '按医嘱',
      frequency: parts[2] || '每日 2 次',
      duration: parts[3] || '按医嘱',
      note: parts.slice(4).join('，') || line,
    }
  })
}

function defaultReminderTimes(frequency) {
  const text = String(frequency || '')
  if (text.includes('每日 3 次') || text.includes('一天三次')) {
    return ['08:00', '14:00', '20:00']
  }
  if (text.includes('每日 1 次') || text.includes('一天一次')) {
    return ['08:00']
  }
  return ['08:00', '20:00']
}

function recordId() {
  return `${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
}

function prescriptionWindow(prescription) {
  const start = new Date(prescription.startDate)
  const end = prescription.endDate ? new Date(prescription.endDate) : new Date(start.getTime() + 6 * 24 * 60 * 60 * 1000)
  return { start, end }
}

function isPrescriptionActiveOn(prescription, date) {
  const { start, end } = prescriptionWindow(prescription)
  const current = new Date(date)
  current.setHours(0, 0, 0, 0)
  start.setHours(0, 0, 0, 0)
  end.setHours(0, 0, 0, 0)
  return current >= start && current <= end
}

async function loadPrescriptions() {
  loading.value = true
  try {
    // Ensure we have user info
    if (!auth.me) {
      await auth.fetchMe()
    }

    // Load prescriptions - backend will filter by current user
    const prescriptionRes = await http.get('/api/prescriptions/patient/me')
    const reminderMap = readJson(userStorageKey('patient_medication_reminders'), {})

    if (prescriptionRes && prescriptionRes.length > 0) {
      // Use real prescription data
      prescriptions.value = prescriptionRes.map((item) => {
        const savedTimes = reminderMap[item.prescriptionId]
        const items = item.items?.map((i) => ({
          name: i.medicationName,
          dosage: i.dosage,
          frequency: i.frequency,
          duration: i.duration,
          note: i.note,
        })) || []

        return {
          prescriptionId: item.prescriptionId,
          id: item.id,
          recordId: item.recordId,
          title: item.title || '处方',
          doctorName: item.doctorName || '医生',
          visitDate: item.visitDate,
          createdAt: item.createdAt,
          startDate: formatDate(item.startDate || new Date()),
          endDate: formatDate(item.endDate || new Date(new Date().getTime() + 7 * 24 * 60 * 60 * 1000)),
          treatmentPlan: item.treatmentPlan || item.instructions || '请遵医嘱服药',
          items,
          reminderTimes: Array.isArray(savedTimes) && savedTimes.length > 0 ? savedTimes : (item.reminderTimes || ['08:00', '14:00', '20:00']),
        }
      })
    } else {
      // Fallback to parsing from medical records if no prescriptions
      const listRes = await http.get('/api/medical-records', {
        params: {
          page: 0,
          size: 30,
          recordStatus: 1,
        },
      })

      const rows = listRes.records || []
      const details = await Promise.all(
        rows.map(async (item) => {
          try {
            return await http.get(`/api/medical-records/${item.recordId}`)
          } catch {
            return item
          }
        }),
      )

      prescriptions.value = details
        .filter((item) => item?.treatmentPlan || item?.diagnosis)
        .map((item) => {
          const items = parseMedicationItems(item.treatmentPlan || item.diagnosis)
          const first = items[0]
          const savedTimes = reminderMap[item.recordId]
          return {
            prescriptionId: String(item.recordId),
            recordId: item.recordId,
            title: item.diagnosis || '治疗方案',
            doctorName: item.doctorRealName || '医生',
            visitDate: item.visitDate,
            createdAt: item.createTime,
            startDate: formatDate(item.visitDate || new Date()),
            endDate: formatDate(new Date(new Date(item.visitDate || Date.now()).getTime() + 6 * 24 * 60 * 60 * 1000)),
            treatmentPlan: item.treatmentPlan || '请遵医嘱执行当前治疗方案。',
            items,
            reminderTimes: Array.isArray(savedTimes) && savedTimes.length > 0 ? savedTimes : defaultReminderTimes(first.frequency),
          }
        })
    }
  } catch (e) {
    ElMessage.error(e?.message || '加载用药方案失败')
  } finally {
    loading.value = false
  }
}

function loadMedicationRecords() {
  medicationRecords.value = readJson(userStorageKey('patient_medication_records'), [])
}

function saveMedicationRecords() {
  writeJson(userStorageKey('patient_medication_records'), medicationRecords.value)
}

function saveReminderSettings() {
  const reminderMap = Object.fromEntries(prescriptions.value.map((item) => [item.prescriptionId, item.reminderTimes]))
  writeJson(userStorageKey('patient_medication_reminders'), reminderMap)
}

const todayTasks = computed(() => {
  const today = new Date()
  const tasksByTime = new Map()
  
  prescriptions.value
    .filter((item) => isPrescriptionActiveOn(item, today))
    .forEach((prescription) => {
      prescription.reminderTimes.forEach((time) => {
        if (!tasksByTime.has(time)) {
          tasksByTime.set(time, [])
        }
        // Add each medication item as a separate task
        prescription.items.forEach((item) => {
          tasksByTime.get(time).push({
            prescriptionId: prescription.prescriptionId,
            recordId: prescription.recordId,
            medicationName: item.name,
            dosage: item.dosage,
            frequency: item.frequency,
            duration: item.duration,
            note: item.note,
            doctorName: prescription.doctorName,
            diagnosis: prescription.diagnosis,
            time,
          })
        })
      })
    })
  
  // Convert to sorted array
  return Array.from(tasksByTime.entries())
    .map(([time, medications]) => ({ time, medications }))
    .sort((a, b) => a.time.localeCompare(b.time))
})

const upcomingReminders = computed(() => {
  const now = new Date()
  return todayTasks.value
    .map((task) => {
      const [hours, minutes] = task.time.split(':').map(Number)
      const target = new Date()
      target.setHours(hours, minutes, 0, 0)
      return { ...task, target }
    })
    .filter((task) => task.target >= now && task.target.getTime() - now.getTime() <= 6 * 60 * 60 * 1000)
    .sort((a, b) => a.target - b.target)
    .map((task) => ({
      time: task.time,
      medications: task.medications,
    }))
})

const calendarMap = computed(() => {
  const map = new Map()
  for (const item of medicationRecords.value) {
    const key = formatDate(item.takenAt)
    if (!map.has(key)) {
      map.set(key, [])
    }
    map.get(key).push(item)
  }
  return map
})

// const adherenceStats = computed(() => {
//   const end = new Date()
//   end.setHours(23, 59, 59, 999)
//   const start = new Date()
//   start.setDate(start.getDate() - 29)
//   start.setHours(0, 0, 0, 0)

//   let expectedCount = 0
//   for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
//     for (const prescription of prescriptions.value) {
//       if (isPrescriptionActiveOn(prescription, d)) {
//         // Count each medication item for each reminder time
//         expectedCount += prescription.reminderTimes.length * prescription.items.length
//       }
//     }
//   }

//   const takenCount = medicationRecords.value.filter((item) => {
//     const time = new Date(item.takenAt)
//     return item.status === 'taken' && time >= start && time <= end
//   }).length
//   const missedCount = Math.max(expectedCount - takenCount, 0)
//   const rate = expectedCount > 0 ? Math.round((takenCount / expectedCount) * 100) : 0
//   return { expectedCount, takenCount, missedCount, rate }
// })

const overviewCards = computed(() => [
  { 
    label: '今日待服药物', 
    value: todayTasks.value.reduce((sum, task) => sum + task.medications.length, 0), 
    desc: '按今日提醒时间生成的药物任务', 
    icon: FirstAidKit 
  },
  { label: '近期提醒', value: upcomingReminders.value.length, desc: '未来 6 小时内待提醒', icon: AlarmClock },
  // { label: '30 天计划', value: adherenceStats.value.expectedCount, desc: '依从性统计基准次数', icon: DataAnalysis },
  // { label: '依从率', value: `${adherenceStats.value.rate}%`, desc: '近 30 天执行完成情况', icon: Opportunity },
])

function ensureChart() {
  if (!chartEl.value) return null
  if (!chart) {
    chart = echarts.init(chartEl.value)
  }
  return chart
}

// function renderChart() {
//   const instance = ensureChart()
//   if (!instance) return
//   const stats = adherenceStats.value
//   instance.setOption({
//     tooltip: { trigger: 'item' },
//     grid: { left: 24, right: 24, top: 40, bottom: 20, containLabel: true },
//     xAxis: {
//       type: 'category',
//       data: ['已服用', '漏服', '计划总次数'],
//       axisTick: { show: false },
//     },
//     yAxis: { type: 'value' },
//     series: [
//       {
//         type: 'bar',
//         barWidth: 34,
//         data: [stats.takenCount, stats.missedCount, stats.expectedCount],
//         itemStyle: {
//           color: ({ dataIndex }) => ['#2f7cff', '#f59e0b', '#94a3b8'][dataIndex],
//           borderRadius: [8, 8, 0, 0],
//         },
//       },
//     ],
//   })
// }

function openReminderDialog(prescription) {
  reminderForm.prescriptionId = prescription.prescriptionId
  reminderForm.medicationName = prescription.medicationName
  reminderForm.reminderTimes = [...prescription.reminderTimes]
  reminderDialogVisible.value = true
}

function addReminderTime() {
  reminderForm.reminderTimes.push('08:00')
}

function removeReminderTime(index) {
  reminderForm.reminderTimes.splice(index, 1)
}

function submitReminderSettings() {
  const target = prescriptions.value.find((item) => item.prescriptionId === reminderForm.prescriptionId)
  if (!target) return
  const valid = reminderForm.reminderTimes.filter(Boolean)
  if (valid.length === 0) {
    ElMessage.warning('至少保留一个提醒时间')
    return
  }
  target.reminderTimes = [...new Set(valid)].sort()
  saveReminderSettings()
  reminderDialogVisible.value = false
  ElMessage.success('提醒设置已保存')
}

function openRecordDialog(task) {
  recordForm.prescriptionId = task.prescriptionId
  recordForm.medicationName = task.medicationName
  recordForm.plannedTime = task.time || ''
  recordForm.takenAt = new Date()
  recordForm.status = 'taken'
  recordForm.notes = ''
  recordDialogVisible.value = true
}

function submitMedicationRecord() {
  medicationRecords.value.unshift({
    id: recordId(),
    prescriptionId: recordForm.prescriptionId,
    medicationName: recordForm.medicationName,
    plannedTime: recordForm.plannedTime,
    takenAt: recordForm.takenAt,
    status: recordForm.status,
    notes: recordForm.notes,
  })
  saveMedicationRecords()
  recordDialogVisible.value = false
  renderChart()
  ElMessage.success('用药记录已保存')
}

function calendarCount(date) {
  return calendarMap.value.get(formatDate(date))?.length || 0
}

watch(
  () => [prescriptions.value.length, medicationRecords.value.length],
  async () => {
    await nextTick()
    renderChart()
  },
)

onMounted(async () => {
  if (!auth.me) {
    try {
      await auth.fetchMe()
    } catch {
      // ignore
    }
  }
  loadMedicationRecords()
  await loadPrescriptions()
  await nextTick()
  renderChart()
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
    <section class="patient-hero-card">
      <div class="patient-hero-card__eyebrow">用药管理</div>
      <div class="patient-hero-card__title">处方查看、提醒与记录集中管理</div>
      <div class="patient-hero-card__desc">当前版本先基于病历治疗方案生成可执行的用药计划，帮助你管理用药记录。</div>
    </section>

    <section class="patient-panel-card">
      <div class="patient-section-head">
        <div>
          <div class="patient-section-title">核心概览</div>
          <div class="patient-section-subtitle">今日计划与提醒一目了然</div>
        </div>
      </div>
      <div class="patient-kpi-grid">
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
    </section>

    <div class="patient-med-grid">
      <section class="patient-panel-card">
        <div class="patient-section-head">
          <div>
            <div class="patient-section-title">今日用药提醒</div>
            <div class="patient-section-subtitle">按提醒时间记录服药情况</div>
          </div>
          <div class="patient-accent">{{ todayTasks.length }} 个时间点</div>
        </div>

        <el-skeleton :loading="loading" animated>
          <template #default>
            <div v-if="todayTasks.length === 0" class="patient-empty-text">当前没有可执行的用药计划</div>

            <div v-for="task in todayTasks" :key="task.time" class="patient-med-card">
              <div class="patient-list-head">
                <div class="patient-title">{{ task.time }}</div>
                <div class="patient-status">{{ task.medications.length }} 种药物</div>
              </div>
              <div class="patient-med-item-list">
                <div v-for="(med, index) in task.medications" :key="`${task.time}_${index}`" class="patient-med-item">
                  <div class="patient-name">{{ med.medicationName }}</div>
                  <div class="patient-meta">{{ med.dosage }} - {{ med.frequency }} - {{ med.duration || '按医嘱' }}</div>
                  <div class="patient-actions">
                    <el-button type="primary" size="small" @click="openRecordDialog({ ...med, time: task.time })">记录服药</el-button>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </section>

      <section class="patient-panel-card">
        <div class="patient-section-head">
          <div>
            <div class="patient-section-title">近期提醒</div>
            <div class="patient-section-subtitle">未来 6 小时内即将触发的提醒</div>
          </div>
          <div class="patient-accent">{{ upcomingReminders.length }} 条</div>
        </div>

        <div v-if="upcomingReminders.length === 0" class="patient-empty-text">暂无即将到来的提醒</div>
        <div v-for="item in upcomingReminders" :key="item.time" class="patient-reminder-card">
          <div class="patient-list-head">
            <div class="patient-title">{{ item.time }}</div>
            <div class="patient-status">{{ item.medications.length }} 种药物</div>
          </div>
          <div class="patient-med-item-list">
            <div v-for="(med, index) in item.medications" :key="`${item.time}_${index}`" class="patient-med-item">
              <div class="patient-name">{{ med.medicationName }}</div>
              <div class="patient-meta">{{ med.dosage }} - {{ med.frequency }}</div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <div class="patient-med-grid">
      <section class="patient-panel-card">
        <div class="patient-section-head">
          <div>
            <div class="patient-section-title">用药周期日历</div>
            <div class="patient-section-subtitle">按天查看已记录的服药次数</div>
          </div>
        </div>

        <el-calendar v-model="calendarDate">
          <template #date-cell="{ data }">
            <div class="patient-calendar-cell" :class="{ 'is-active': calendarCount(data.date) > 0 }">
              <div>{{ data.day.split('-').slice(2).join('') }}</div>
              <div v-if="calendarCount(data.date) > 0" class="patient-calendar-cell__count">{{ calendarCount(data.date) }} 次</div>
            </div>
          </template>
        </el-calendar>
      </section>

      <!-- <section class="patient-panel-card">
        <div class="patient-section-head">
          <div>
            <div class="patient-section-title">服药依从性统计</div>
            <div class="patient-section-subtitle">统计近 30 天计划与实际服药完成情况</div>
          </div>
          <div class="patient-accent">{{ adherenceStats.rate }}%</div>
        </div>

        <div ref="chartEl" class="patient-med-chart"></div>

        <div class="patient-stats-grid" style="margin-top: 12px">
          <div class="patient-stats-item">
            <div class="patient-stats-item__label">已服用</div>
            <div class="patient-stats-item__value">{{ adherenceStats.takenCount }}</div>
          </div>
          <div class="patient-stats-item">
            <div class="patient-stats-item__label">漏服</div>
            <div class="patient-stats-item__value">{{ adherenceStats.missedCount }}</div>
          </div>
        </div>
      </section> -->
    </div>

    <section class="patient-panel-card">
      <div class="patient-section-head">
        <div>
          <div class="patient-section-title">处方 / 治疗方案</div>
          <div class="patient-section-subtitle">基于已完成病历的治疗方案生成当前可执行的用药视图</div>
        </div>
        <div class="patient-accent">{{ prescriptions.length }} 条</div>
      </div>

      <el-skeleton :loading="loading" animated>
        <template #default>
          <div v-if="prescriptions.length === 0" class="patient-empty-text">暂无可用的处方或治疗方案</div>

          <div v-for="prescription in prescriptions" :key="prescription.prescriptionId" class="patient-reg-card">
            <div class="patient-list-head">
              <div class="patient-title">{{ prescription.title }}</div>
              <div class="patient-fee">{{ prescription.doctorName }}</div>
            </div>
            <div class="patient-meta">开具时间：{{ formatDateTime(prescription.visitDate || prescription.createdAt) }}</div>
            <div class="patient-meta">提醒时间：{{ prescription.reminderTimes.join('、') }}</div>
            <div class="patient-meta">用药周期：{{ formatDate(prescription.startDate) }} 至 {{ formatDate(prescription.endDate) }}</div>

            <div v-if="prescription.items && prescription.items.length > 0" class="patient-med-item-list">
              <div v-for="(item, index) in prescription.items" :key="`${prescription.prescriptionId}_${index}`" class="patient-med-item">
                <div class="patient-name">{{ item.name }}</div>
                <div class="patient-meta">{{ item.dosage }} - {{ item.frequency }} - {{ item.duration || '按医嘱' }}</div>
                <div v-if="item.note" class="patient-meta">说明：{{ item.note }}</div>
              </div>
            </div>

            <div class="patient-actions">
              <el-button type="primary" size="small" @click="openReminderDialog(prescription)">提醒设置</el-button>
              <el-button size="small" @click="openRecordDialog({ ...prescription, time: prescription.reminderTimes[0] })">记录一次服药</el-button>
            </div>
          </div>
        </template>
      </el-skeleton>
    </section>

    <section class="patient-panel-card">
      <div class="patient-section-head">
        <div>
          <div class="patient-section-title">用药记录</div>
          <div class="patient-section-subtitle">本地保存患者服药记录，可用于日历查看</div>
        </div>
        <div class="patient-accent">{{ medicationRecords.length }} 条</div>
      </div>

      <div v-if="medicationRecords.length === 0" class="patient-empty-text">暂无用药记录</div>
      <div v-for="item in medicationRecords.slice(0, 20)" :key="item.id" class="patient-hm-card">
        <div class="patient-list-head">
          <div class="patient-name">{{ item.medicationName }}</div>
          <div class="patient-status">{{ item.status === 'taken' ? '已服用' : '漏服' }}</div>
        </div>
        <div class="patient-meta">记录时间：{{ formatDateTime(item.takenAt) }}</div>
        <div v-if="item.plannedTime" class="patient-meta">计划提醒：{{ item.plannedTime }}</div>
        <div v-if="item.notes" class="patient-meta">备注：{{ item.notes }}</div>
      </div>
    </section>

    <el-dialog v-model="reminderDialogVisible" title="提醒设置" width="92%">
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="药品 / 方案">
          <el-input v-model="reminderForm.medicationName" readonly />
        </el-form-item>
        <el-form-item label="提醒时间">
          <div class="patient-reminder-editor">
            <div v-for="(time, index) in reminderForm.reminderTimes" :key="`${time}_${index}`" class="patient-reminder-editor__row">
              <el-time-picker v-model="reminderForm.reminderTimes[index]" value-format="HH:mm" format="HH:mm" style="width: 100%" />
              <el-button type="danger" plain @click="removeReminderTime(index)">删除</el-button>
            </div>
            <el-button @click="addReminderTime">新增提醒时间</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reminderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReminderSettings">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="recordDialogVisible" title="记录服药" width="92%">
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="药品 / 方案">
          <el-input v-model="recordForm.medicationName" readonly />
        </el-form-item>
        <el-form-item label="计划提醒时间">
          <el-input v-model="recordForm.plannedTime" readonly />
        </el-form-item>
        <el-form-item label="实际记录时间">
          <el-date-picker v-model="recordForm.takenAt" type="datetime" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="recordForm.status">
            <el-radio label="taken">已服用</el-radio>
            <el-radio label="missed">漏服</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="recordForm.notes" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMedicationRecord">保存记录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.patient-med-grid {
  display: grid;
  gap: 14px;
}

.patient-med-card,
.patient-reminder-card,
.patient-reminder-item,
.patient-med-item {
  padding: 14px;
  border-radius: 16px;
  background: var(--patient-surface-strong);
  border: 1px solid var(--patient-border);
}

.patient-reminder-card {
  margin-bottom: 10px;
}

.patient-reminder-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.patient-med-item-list {
  display: grid;
  gap: 10px;
  margin-top: 12px;
}

.patient-calendar-cell {
  min-height: 64px;
  padding: 6px;
  border-radius: 10px;
}

.patient-calendar-cell.is-active {
  background: var(--patient-primary-soft);
  color: var(--patient-primary);
}

.patient-calendar-cell__count {
  margin-top: 4px;
  font-size: 12px;
  font-weight: 600;
}

.patient-med-chart {
  height: 260px;
  width: 100%;
}

.patient-reminder-editor {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.patient-reminder-editor__row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
  align-items: center;
}

@media (min-width: 1024px) {
  .patient-med-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .patient-med-item-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
