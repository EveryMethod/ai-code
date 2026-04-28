<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'

import { check } from '@/api/healthController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()


check().then((res) => {
  console.log(res)
})
</script>

<template>
  <BasicLayout />
</template>

<style>
</style>
