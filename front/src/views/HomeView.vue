<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { patientPageApi } from '../api/patients'
import { physicalExamMineApi, physicalExamPageApi } from '../api/physicalExams'
import { predictionResultMineApi, predictionResultPageApi } from '../api/predictionResults'
import { healthGuidanceMineApi, healthGuidancePageApi } from '../api/healthGuidances'

const auth = useAuthStore()
const router = useRouter()
const role = computed(() => auth.user?.role || 'PATIENT')
const roleText = computed(() => {
  if (auth.user?.role === 'ADMIN') return '管理员'
  if (auth.user?.role === 'DOCTOR') return '医生'
  return '患者'
})

const loadingStats = ref(false)
const stats = ref({
  patientsTotal: 0,
  physicalExamsTotal: 0,
  predictionsTotal: 0,
  guidancesTotal: 0,
  highRiskPercent: 0,
})

async function loadStats() {
  loadingStats.value = true
  try {
    const isPatient = role.value === 'PATIENT'

    const [patients, exams, preds, guidances] = await Promise.all([
      isPatient ? Promise.resolve({ total: 1, records: [] as any[] }) : patientPageApi({ page: 0, size: 1, keyword: null }),
      isPatient ? physicalExamMineApi({ page: 0, size: 1 }) : physicalExamPageApi({ page: 0, size: 1, patientId: null }),
      isPatient ? predictionResultMineApi({ page: 0, size: 1 }) : predictionResultPageApi({ page: 0, size: 1, patientId: null }),
      isPatient ? healthGuidanceMineApi({ page: 0, size: 1 }) : healthGuidancePageApi({ page: 0, size: 1, patientId: null }),
    ])

    const predsSample = isPatient
      ? await predictionResultMineApi({ page: 0, size: 20 })
      : await predictionResultPageApi({ page: 0, size: 20, patientId: null })

    const sampleTotal = predsSample.records.length
    const highRiskCount = predsSample.records.filter((r) => r.predictionLabel === 1).length
    const highRiskPercent = sampleTotal ? Math.round((highRiskCount / sampleTotal) * 100) : 0

    stats.value = {
      patientsTotal: isPatient ? 1 : patients.total,
      physicalExamsTotal: exams.total,
      predictionsTotal: preds.total,
      guidancesTotal: guidances.total,
      highRiskPercent,
    }
  } finally {
    loadingStats.value = false
  }
}

function go(name: string) {
  router.push({ name })
}

onMounted(loadStats)
</script>

<template>
  <a-space direction="vertical" style="width: 100%" :size="12">
    <a-card>
      <a-typography-title :level="4">系统概览</a-typography-title>
      <a-typography-paragraph>已登录用户：{{ auth.user?.username || '-' }}</a-typography-paragraph>
      <a-typography-paragraph>当前角色：{{ roleText }}</a-typography-paragraph>
      <a-typography-paragraph>
        {{ role === 'PATIENT' ? '你可以查看自己的档案、体检、预测结果和健康指导。' : '你可以从左侧菜单进入业务模块。' }}
      </a-typography-paragraph>
    </a-card>

    <a-card title="快捷入口" :loading="loadingStats">
      <a-space wrap>
        <a-button type="primary" @click="go('patients')">{{ role === 'PATIENT' ? '我的档案' : '患者' }}</a-button>
        <a-button @click="go('physical-exams')">{{ role === 'PATIENT' ? '我的体检' : '体检数据' }}</a-button>
        <a-button @click="go('prediction-results')">{{ role === 'PATIENT' ? '我的预测' : '预测结果' }}</a-button>
        <a-button @click="go('health-guidances')">{{ role === 'PATIENT' ? '我的健康指导' : '健康指导' }}</a-button>
      </a-space>
    </a-card>

    <a-row :gutter="12">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :loading="loadingStats">
          <a-statistic title="患者数量" :value="stats.patientsTotal" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :loading="loadingStats">
          <a-statistic title="体检记录" :value="stats.physicalExamsTotal" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :loading="loadingStats">
          <a-statistic title="预测结果" :value="stats.predictionsTotal" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card :loading="loadingStats">
          <a-statistic title="健康指导" :value="stats.guidancesTotal" />
        </a-card>
      </a-col>
    </a-row>

    <a-card title="统计概览" :loading="loadingStats">
      <a-typography-paragraph>最近 20 条预测样本：高风险占比</a-typography-paragraph>
      <a-progress :percent="stats.highRiskPercent" status="active" />
      <a-typography-paragraph style="margin-top: 8px" type="secondary">
        说明：该比例基于最新的 20 条预测结果快速统计，仅用于概览展示。
      </a-typography-paragraph>
    </a-card>

    <a-card>
      <a-typography-title :level="5">系统介绍</a-typography-title>
      <a-typography-paragraph>
        社区医院信息平台用于居民高血压风险预测与随访管理，覆盖患者档案、体检数据、问卷数据、风险预测、健康指导等核心流程。
      </a-typography-paragraph>

      <a-typography-title :level="5">功能模块</a-typography-title>
      <a-typography-paragraph>
        主要模块包括：患者档案、健康档案、体检数据、问卷数据、融合数据、预测结果、健康指导。
      </a-typography-paragraph>

      <a-typography-title :level="5">使用指引</a-typography-title>
      <a-typography-paragraph v-if="role === 'PATIENT'">
        你可以在左侧菜单进入“我的档案 / 我的体检 / 我的预测 / 我的健康指导”，查看自己的健康信息，并在“我的档案”中维护基础资料。
      </a-typography-paragraph>
      <a-typography-paragraph v-else-if="role === 'DOCTOR'">
        你可以在左侧菜单进入患者列表，维护患者档案、体检与问卷数据，生成预测结果，并对患者创建健康指导。
      </a-typography-paragraph>
      <a-typography-paragraph v-else>
        你可以进行系统级的用户与数据管理，确保账号与权限配置正确，并辅助医生完成业务维护。
      </a-typography-paragraph>
    </a-card>
  </a-space>
</template>
