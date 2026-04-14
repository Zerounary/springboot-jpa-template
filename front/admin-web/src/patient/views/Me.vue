<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const router = useRouter()

function logout() {
  auth.logout()
  router.replace('/login')
}
</script>

<template>
  <div class="patient-page-stack">
    <section class="patient-profile-card">
      <div class="patient-profile-summary">
        <div class="patient-hero-card__eyebrow" style="color: var(--patient-primary)">个人中心</div>
        <div class="patient-profile-name">{{ auth.me?.realName || auth.me?.username || '患者用户' }}</div>
        <div class="patient-helper-text">账号信息与基础资料一目了然</div>
      </div>

      <div class="patient-profile-line">
        <div class="patient-profile-line__label">用户名</div>
        <div class="patient-profile-line__value">{{ auth.me?.username || '-' }}</div>
      </div>
      <div class="patient-profile-line">
        <div class="patient-profile-line__label">姓名</div>
        <div class="patient-profile-line__value">{{ auth.me?.realName || '-' }}</div>
      </div>
      <div class="patient-profile-line">
        <div class="patient-profile-line__label">手机号</div>
        <div class="patient-profile-line__value">{{ auth.me?.phone || '-' }}</div>
      </div>
      <div class="patient-profile-line">
        <div class="patient-profile-line__label">角色</div>
        <div class="patient-profile-line__value">患者</div>
      </div>

      <div class="patient-actions">
        <el-button type="danger" style="width: 100%" @click="logout">退出登录</el-button>
      </div>
    </section>
  </div>
</template>
