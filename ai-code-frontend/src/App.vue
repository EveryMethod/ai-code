<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'

import { check } from '@/api/healthController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser().catch(() => {
  // 后端不可达时静默处理，不影响页面渲染
})

check()
  .then((res) => {
    console.log(res)
  })
  .catch(() => {
    // 健康检查失败时静默处理
  })
</script>

<template>
  <BasicLayout />
</template>

<style></style>
