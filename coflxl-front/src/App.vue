<template>
  <div v-if="route.name === 'Login'" class="h-screen w-screen">
    <router-view></router-view>
  </div>
  <div v-else class="app-container h-screen flex overflow-hidden bg-gray-100">
    <!-- Sidebar -->
    <aside class="w-64 bg-slate-800 text-slate-300 flex flex-col shadow-xl z-10">
      <div class="h-16 flex items-center justify-center border-b border-slate-700">
        <h1 class="text-xl font-bold text-white tracking-wider">接口在线管理平台</h1>
      </div>
      <el-menu
          :default-active="route.path"
          class="flex-1 border-r-0"
          background-color="#1e293b"
          text-color="#cbd5e1"
          active-text-color="#ffffff"
          router
      >
        <el-menu-item index="/home">
          <el-icon><House /></el-icon>
          <span>首页概览</span>
        </el-menu-item>

        <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统配置</span>
          </template>
          <el-menu-item index="/data-source-manage">
            <el-icon><Coin /></el-icon>
            <span>数据连接</span>
          </el-menu-item>
          <el-menu-item index="/system-manage">
            <el-icon><Platform /></el-icon>
            <span>系统管理</span>
          </el-menu-item>
          <el-menu-item index="/code-gen">
            <el-icon><MagicStick /></el-icon>
            <span>代码生成</span>
          </el-menu-item>
          <el-menu-item index="/champion-details">
            <el-icon><MagicStick /></el-icon>
            <span>LOL英雄</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="api">
          <template #title>
            <el-icon><Connection /></el-icon>
            <span>接口服务</span>
          </template>
          <el-menu-item index="/api-manage">
            <el-icon><Monitor /></el-icon>
            <span>接口管理</span>
          </el-menu-item>
          <el-menu-item index="/call-log">
            <el-icon><Document /></el-icon>
            <span>调用日志</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </aside>

    <!-- Main Content -->
    <main class="flex-1 flex flex-col overflow-hidden">
      <header class="bg-white shadow-sm h-16 flex items-center justify-between px-6 z-0" v-if="route.name !== 'SqlWorkbench'">
        <h2 class="text-lg font-medium text-gray-800">{{ currentRouteName }}</h2>
        <div class="flex items-center">
          <span class="mr-4 text-sm text-gray-600">欢迎, {{ username }}</span>
          <el-button link type="danger" @click="handleLogout">退出登录</el-button>
        </div>
      </header>
      <div class="flex-1 overflow-auto p-6">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Setting, Coin, Document, Monitor, Platform, House, Connection } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const username = ref('')

const routeNames: Record<string, string> = {
  'Home': '首页概览',
  'SystemManage': '系统管理',
  'ApiManage': '接口管理',
  'DataSourceManage': '数据源管理',
  'CallLog': '调用日志',
  'SqlWorkbench': 'SQL 智慧工作台'
}
const currentRouteName = computed(() => routeNames[route.name as string] || '')

onMounted(() => {
  username.value = localStorage.getItem('username') || 'Admin'
})

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    router.push('/login')
  }).catch(() => {})
}
</script>

<style>
.el-menu {
  border-right: none !important;
}
.el-menu-item.is-active {
  background-color: #2563eb !important;
}
.el-menu-item:hover {
  background-color: #334155 !important;
}
.el-sub-menu__title:hover {
  background-color: #334155 !important;
}
</style>
