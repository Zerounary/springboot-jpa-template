<script setup>
import { onMounted, ref } from 'vue'
import http from '../utils/http'

const me = ref(null)

onMounted(async () => {
  me.value = await http.get('/api/auth/me')
})
</script>

<template>
  <div class="admin-page">
    <section class="admin-hero">
      <div class="admin-hero__eyebrow">HOSPITAL OPERATIONS CENTER</div>
      <div class="admin-hero__title">
        {{ me ? `欢迎回来，${me.realName || me.username}` : '欢迎使用医院后台工作台' }}
      </div>
      <div class="admin-hero__desc">
        聚合挂号、病历、患者、医生与资讯管理能力，帮助医院在统一后台中完成高频运营与诊疗信息维护。
      </div>
    </section>

    <section class="admin-kpi-grid">
      <div class="admin-kpi-card">
        <div class="admin-kpi-card__label">当前身份</div>
        <div class="admin-kpi-card__value">{{ me?.roleType === 1 ? '管理员' : '业务人员' }}</div>
        <div class="admin-kpi-card__hint">按权限展示可管理的业务模块</div>
      </div>
      <div class="admin-kpi-card">
        <div class="admin-kpi-card__label">系统定位</div>
        <div class="admin-kpi-card__value">医院运营</div>
        <div class="admin-kpi-card__hint">覆盖挂号、病历、健康与人员档案</div>
      </div>
      <div class="admin-kpi-card">
        <div class="admin-kpi-card__label">管理风格</div>
        <div class="admin-kpi-card__value">简约高效</div>
        <div class="admin-kpi-card__hint">突出信息密度、专业感与可读性</div>
      </div>
      <div class="admin-kpi-card">
        <div class="admin-kpi-card__label">当前账号</div>
        <div class="admin-kpi-card__value">{{ me?.username || '加载中' }}</div>
        <div class="admin-kpi-card__hint">可在右上角查看身份并安全退出</div>
      </div>
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
