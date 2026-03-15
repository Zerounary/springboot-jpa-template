<script setup>
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

function logout() {
  auth.logout()
  router.replace('/login')
}
</script>

<template>
  <div class="page-stack">
    <section class="profile-card">
      <div class="profile-summary">
        <div class="hero-card__eyebrow" style="color: var(--h5-primary)">个人中心</div>
        <div class="profile-name">{{ auth.me?.realName || auth.me?.username || '患者用户' }}</div>
        <div class="helper-text">账号信息与基础资料一目了然</div>
      </div>

      <div class="profile-line">
        <div class="profile-line__label">用户名</div>
        <div class="profile-line__value">{{ auth.me?.username || '-' }}</div>
      </div>
      <div class="profile-line">
        <div class="profile-line__label">姓名</div>
        <div class="profile-line__value">{{ auth.me?.realName || '-' }}</div>
      </div>
      <div class="profile-line">
        <div class="profile-line__label">手机号</div>
        <div class="profile-line__value">{{ auth.me?.phone || '-' }}</div>
      </div>
      <div class="profile-line">
        <div class="profile-line__label">角色</div>
        <div class="profile-line__value">患者</div>
      </div>

      <div class="actions">
        <el-button type="danger" style="width: 100%" @click="logout">退出登录</el-button>
      </div>
    </section>
  </div>
</template>
