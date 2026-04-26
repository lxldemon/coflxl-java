<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index">
      <router-link v-if="item.path && index !== breadcrumbs.length - 1" :to="item.path">{{ item.meta.title }}</router-link>
      <span v-else>{{ item.meta.title }}</span>
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute, RouteLocationMatched } from 'vue-router'

const route = useRoute()
const breadcrumbs = ref<RouteLocationMatched[]>([])

const getBreadcrumbs = () => {
  let matched = route.matched.filter(item => item.meta && item.meta.title)
  // 如果第一个是重定向到 /home 的 /, 则去除
  if (matched.length > 1 && matched[0].path === '/' && matched[1].path === '/home') {
    matched = matched.slice(1);
  }
  breadcrumbs.value = matched
}

watch(
  () => route.path,
  () => getBreadcrumbs(),
  { immediate: true }
)

onMounted(() => {
  getBreadcrumbs()
})
</script>