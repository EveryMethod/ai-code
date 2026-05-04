<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <GlobalHeader />

    <!-- 主要内容区域 -->
    <a-layout-content :class="['main-content', { 'main-content--bleed': isBleed }]">
      <router-view />
    </a-layout-content>

    <!-- 底部版权信息（主页不显示） -->
    <GlobalFooter v-if="!isBleed" />
  </a-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'

const route = useRoute()
const isBleed = computed(() => Boolean(route.meta.fullBleed))
</script>

<style scoped>
.basic-layout {
  background: #f8fafc;
  min-height: 100vh;
}

.main-content {
  min-height: calc(100vh - 64px - 60px);
  padding: 24px;
  background: #f8fafc;
  margin: 0 auto;
}

/* 全宽布局：去除内边距和背景色限制 */
.main-content--bleed {
  padding: 0;
  background: transparent;
  min-height: auto;
  height: auto;
}
</style>
