<script setup>
import { onMounted, ref, nextTick } from 'vue'
import http from '../utils/http'
import * as echarts from 'echarts'

const me = ref(null)
const dashboardStats = ref(null)
const loading = ref(true)

// Chart refs
const weeklyTrendChart = ref(null)
const monthlyTrendChart = ref(null)
const departmentChart = ref(null)
const statusChart = ref(null)
const paymentChart = ref(null)
const topDoctorsChart = ref(null)

// Chart instances
let weeklyChartInstance = null
let monthlyChartInstance = null
let departmentChartInstance = null
let statusChartInstance = null
let paymentChartInstance = null
let topDoctorsChartInstance = null

onMounted(async () => {
  try {
    me.value = await http.get('/api/auth/me')
    dashboardStats.value = await http.get('/api/statistics/dashboard')
    
    // Initialize charts after data is loaded
    await nextTick()
    initCharts()
  } catch (error) {
    console.error('加载工作台数据失败:', error)
  } finally {
    loading.value = false
  }
})

const initCharts = () => {
  if (!dashboardStats.value) return
  
  // Weekly trend chart
  if (weeklyTrendChart.value) {
    weeklyChartInstance = echarts.init(weeklyTrendChart.value)
    weeklyChartInstance.setOption({
      title: {
        text: '7日挂号趋势',
        textStyle: { fontSize: 14, fontWeight: 'normal' }
      },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: dashboardStats.value.weeklyTrend.map(item => item.date)
      },
      yAxis: { type: 'value' },
      series: [{
        data: dashboardStats.value.weeklyTrend.map(item => item.count),
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.3 },
        itemStyle: { color: '#409EFF' }
      }]
    })
  }
  
  // Monthly trend chart
  if (monthlyTrendChart.value) {
    monthlyChartInstance = echarts.init(monthlyTrendChart.value)
    monthlyChartInstance.setOption({
      title: {
        text: '12月挂号趋势',
        textStyle: { fontSize: 14, fontWeight: 'normal' }
      },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: dashboardStats.value.monthlyTrend.map(item => item.month)
      },
      yAxis: { type: 'value' },
      series: [{
        data: dashboardStats.value.monthlyTrend.map(item => item.count),
        type: 'bar',
        itemStyle: { color: '#67C23A' }
      }]
    })
  }
  
  // Department distribution chart
  if (departmentChart.value) {
    departmentChartInstance = echarts.init(departmentChart.value)
    departmentChartInstance.setOption({
      title: {
        text: '前10个科室',
        textStyle: { fontSize: 14, fontWeight: 'normal' }
      },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: dashboardStats.value.departmentStats.map(item => ({
          name: item.deptName || item.dept_name,
          value: item.count
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })
  }
  
  // Registration status chart
  if (statusChart.value) {
    statusChartInstance = echarts.init(statusChart.value)
    statusChartInstance.setOption({
      title: {
        text: '挂号状态',
        textStyle: { fontSize: 14, fontWeight: 'normal' }
      },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '60%',
        data: dashboardStats.value.registrationStatusStats,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })
  }
  
  // Payment status chart
  if (paymentChart.value) {
    paymentChartInstance = echarts.init(paymentChart.value)
    paymentChartInstance.setOption({
      title: {
        text: '支付状态',
        textStyle: { fontSize: 14, fontWeight: 'normal' }
      },
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '60%',
        data: dashboardStats.value.paymentStats,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })
  }
  
  // Top doctors chart
  if (topDoctorsChart.value) {
    topDoctorsChartInstance = echarts.init(topDoctorsChart.value)
    topDoctorsChartInstance.setOption({
      title: {
        text: '挂号量前8名医生',
        textStyle: { fontSize: 14, fontWeight: 'normal' }
      },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'value'
      },
      yAxis: {
        type: 'category',
        data: dashboardStats.value.topDoctors.map(item => item.realName || item.real_name || item.username)
      },
      series: [{
        type: 'bar',
        data: dashboardStats.value.topDoctors.map(item => item.count),
        itemStyle: { color: '#E6A23C' }
      }]
    })
  }
}

// Handle window resize
window.addEventListener('resize', () => {
  weeklyChartInstance?.resize()
  monthlyChartInstance?.resize()
  departmentChartInstance?.resize()
  statusChartInstance?.resize()
  paymentChartInstance?.resize()
  topDoctorsChartInstance?.resize()
})
</script>

<template>
  <div class="admin-page">
    <section class="admin-hero">
      <div class="admin-hero__eyebrow">医院运营中心</div>
      <div class="admin-hero__title">
        {{ me ? `欢迎回来，${me.realName || me.username}` : '欢迎进入医院后台驾驶舱' }}
      </div>
      <div class="admin-hero__desc">
        统一整合挂号、病历、患者、医生与基础信息管理能力，帮助医院在一个后台完成高频运营与医疗信息维护工作。
      </div>
    </section>

    <!-- Statistics Overview Cards -->
    <section class="admin-stats-grid" v-if="dashboardStats">
      <div class="admin-stat-card">
        <div class="admin-stat-card__icon"> 患者</div>
        <div class="admin-stat-card__value">{{ dashboardStats.totalPatients.toLocaleString() }}</div>
        <div class="admin-stat-card__label">患者总数</div>
      </div>
      <div class="admin-stat-card">
        <div class="admin-stat-card__icon"> 医生</div>
        <div class="admin-stat-card__value">{{ dashboardStats.totalDoctors.toLocaleString() }}</div>
        <div class="admin-stat-card__label">医生总数</div>
      </div>
      <div class="admin-stat-card">
        <div class="admin-stat-card__icon"> 科室</div>
        <div class="admin-stat-card__value">{{ dashboardStats.totalDepartments.toLocaleString() }}</div>
        <div class="admin-stat-card__label">科室总数</div>
      </div>
      <div class="admin-stat-card">
        <div class="admin-stat-card__icon"> 挂号</div>
        <div class="admin-stat-card__value">{{ dashboardStats.totalRegistrations.toLocaleString() }}</div>
        <div class="admin-stat-card__label">挂号总数</div>
      </div>
      <div class="admin-stat-card">
        <div class="admin-stat-card__icon"> 病历</div>
        <div class="admin-stat-card__value">{{ dashboardStats.totalMedicalRecords.toLocaleString() }}</div>
        <div class="admin-stat-card__label">病历总数</div>
      </div>
      <div class="admin-stat-card">
        <div class="admin-stat-card__icon"> 今日</div>
        <div class="admin-stat-card__value">{{ dashboardStats.todayRegistrations.toLocaleString() }}</div>
        <div class="admin-stat-card__label">今日挂号</div>
      </div>
    </section>

    <!-- Charts Grid -->
    <section class="admin-charts-grid" v-if="dashboardStats">
      <!-- First Row -->
      <div class="admin-chart-card">
        <div ref="weeklyTrendChart" class="chart-container"></div>
      </div>
      <div class="admin-chart-card">
        <div ref="monthlyTrendChart" class="chart-container"></div>
      </div>
      
      <!-- Second Row -->
      <div class="admin-chart-card">
        <div ref="departmentChart" class="chart-container"></div>
      </div>
      <div class="admin-chart-card">
        <div ref="statusChart" class="chart-container"></div>
      </div>
      
      <!-- Third Row -->
      <div class="admin-chart-card">
        <div ref="paymentChart" class="chart-container"></div>
      </div>
      <div class="admin-chart-card">
        <div ref="topDoctorsChart" class="chart-container"></div>
      </div>
    </section>

    <!-- Loading State -->
    <section v-if="loading" class="admin-loading">
      <div class="loading-spinner">正在加载工作台数据...</div>
    </section>

    <section class="admin-grid-2">
      <div class="admin-section">
        <div class="admin-section__head">
          <div>
            <div class="admin-section__title">工作台重点</div>
            <div class="admin-section__subtitle">围绕医院核心管理流程组织功能结构</div>
          </div>
          <div class="admin-note">专业 · 克制 · 高级</div>
        </div>

        <div class="admin-feature-list">
          <div class="admin-feature-item">
            <div class="admin-feature-item__title">挂号与就诊流程统一管理</div>
            <div class="admin-feature-item__desc">集中处理预约记录、支付状态与就诊进度，减少跨页面切换成本。</div>
          </div>
          <div class="admin-feature-item">
            <div class="admin-feature-item__title">电子病历信息规范沉淀</div>
            <div class="admin-feature-item__desc">统一管理诊断、主诉、处置建议与病历详情，强化医疗信息可读性。</div>
          </div>
          <div class="admin-feature-item">
            <div class="admin-feature-item__title">医院组织与人员档案清晰可控</div>
            <div class="admin-feature-item__desc">管理员可维护科室、医生、患者与系统账号，形成稳定的后台治理结构。</div>
          </div>
        </div>
      </div>

      <div class="admin-section">
        <div class="admin-section__head">
          <div>
            <div class="admin-section__title">快捷入口建议</div>
            <div class="admin-section__subtitle">适合作为后台首页的高频工作路径</div>
          </div>
        </div>

        <div class="admin-quick-list">
          <div class="admin-quick-item">
            <div class="admin-quick-item__title">挂号管理</div>
            <div class="admin-quick-item__desc">处理订单状态、支付进展和患者预约记录。</div>
          </div>
          <div class="admin-quick-item">
            <div class="admin-quick-item__title">电子病历</div>
            <div class="admin-quick-item__desc">查看与维护病历数据，保持医疗记录完整性。</div>
          </div>
          <div class="admin-quick-item">
            <div class="admin-quick-item__title">医生与科室管理</div>
            <div class="admin-quick-item__desc">配置医生档案、科室归属、出诊安排与挂号费用。</div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.admin-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin: 32px 0;
}

.admin-stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.admin-stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.admin-stat-card__icon {
  font-size: 24px;
  margin-bottom: 8px;
  color: #409EFF;
}

.admin-stat-card__value {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.admin-stat-card__label {
  font-size: 14px;
  color: #909399;
}

.admin-charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
  margin: 32px 0;
}

.admin-chart-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e4e7ed;
  min-height: 350px;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.admin-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  background: white;
  border-radius: 12px;
  margin: 32px 0;
}

.loading-spinner {
  font-size: 16px;
  color: #909399;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .admin-stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 16px;
  }
  
  .admin-charts-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .admin-chart-card {
    padding: 16px;
    min-height: 280px;
  }
  
  .chart-container {
    height: 250px;
  }
  
  .admin-stat-card__value {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .admin-stats-grid {
    grid-template-columns: 1fr;
  }
  
  .admin-stat-card {
    padding: 16px;
  }
  
  .admin-stat-card__value {
    font-size: 20px;
  }
}
</style>
