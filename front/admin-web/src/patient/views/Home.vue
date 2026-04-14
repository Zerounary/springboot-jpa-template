<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../../utils/http'

const router = useRouter()
const me = ref(null)

onMounted(async () => {
  me.value = await http.get('/api/auth/me')
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
        在这里你可以快速预约医生、查看病历、记录健康数据，常用功能都适配了手机竖屏操作。
      </div>
    </section>

    <section class="patient-panel-card">
      <div class="patient-section-head">
        <div>
          <div class="patient-section-title">今日概览</div>
          <div class="patient-section-subtitle">常用服务一眼可见</div>
        </div>
      </div>
      <div class="patient-stats-grid">
        <div class="patient-stats-item">
          <div class="patient-stats-item__label">账号状态</div>
          <div class="patient-stats-item__value">{{ me ? '正常' : '加载中' }}</div>
        </div>
        <div class="patient-stats-item">
          <div class="patient-stats-item__label">当前身份</div>
          <div class="patient-stats-item__value">患者</div>
        </div>
      </div>
    </section>

    <section class="patient-panel-card">
      <div class="patient-section-head">
        <div>
          <div class="patient-section-title">快捷入口</div>
          <div class="patient-section-subtitle">为高频操作做了手机端简化</div>
        </div>
      </div>
      <div class="patient-quick-grid">
        <div class="patient-quick-item" @click="router.push('/patient/doctors')">
          <div class="patient-quick-item__label">找医生</div>
          <div class="patient-quick-item__title">医生查询</div>
          <div class="patient-quick-item__desc">按科室、擅长方向快速筛选医生</div>
        </div>
        <div class="patient-quick-item" @click="router.push('/patient/registrations')">
          <div class="patient-quick-item__label">去挂号</div>
          <div class="patient-quick-item__title">在线预约</div>
          <div class="patient-quick-item__desc">选择医生与日期后即可完成预约</div>
        </div>
        <div class="patient-quick-item" @click="router.push('/patient/medical-records')">
          <div class="patient-quick-item__label">查病历</div>
          <div class="patient-quick-item__title">病例记录</div>
          <div class="patient-quick-item__desc">随时查看诊断、主诉与治疗方案</div>
        </div>
        <div class="patient-quick-item" @click="router.push('/patient/health')">
          <div class="patient-quick-item__label">做记录</div>
          <div class="patient-quick-item__title">健康监测</div>
          <div class="patient-quick-item__desc">记录血压血糖等健康指标与趋势</div>
        </div>
        <div class="patient-quick-item" @click="router.push('/patient/medication')">
          <div class="patient-quick-item__label">管用药</div>
          <div class="patient-quick-item__title">用药管理</div>
          <div class="patient-quick-item__desc">查看处方方案、设置提醒并记录服药情况</div>
        </div>
      </div>
    </section>
  </div>
</template>
