<template>
  <div class="h-full w-full bg-white relative">
    <iframe v-if="iframeUrl" :src="iframeUrl" frameborder="0" class="w-full h-full border-none shadow-sm rounded"></iframe>
    <div v-else class="flex h-full items-center justify-center text-gray-400">
      <el-empty description="未配置内嵌地址" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const iframeUrl = computed(() => {
  return route.query.url as string || ''
})
</script>
