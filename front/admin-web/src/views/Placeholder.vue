<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <div class="admin-page">
    <section class="admin-section">
      <div class="admin-section__head">
        <div>
          <div class="admin-section__title">页面建设中</div>
          <div class="admin-section__subtitle">当前路由已接入后台设计系统，可继续沿用统一页面模板开发</div>
        </div>
        <div class="admin-note">占位页面</div>
      </div>

      <div class="admin-feature-list">
        <div class="admin-feature-item">
          <div class="admin-feature-item__title">当前路径</div>
          <div class="admin-feature-item__desc">{{ route.path }}</div>
        </div>
        <div class="admin-feature-item">
          <div class="admin-feature-item__title">开发建议</div>
          <div class="admin-feature-item__desc">优先复用后台的 `admin-section`、`admin-table-card`、`admin-toolbar` 等统一结构。</div>
        </div>
      </div>
    </section>
  </div>
</template>
