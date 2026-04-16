<template>
  <div v-if="route.name === 'Login' || route.name === 'ReportViewer'" class="h-screen w-screen">
    <router-view></router-view>
  </div>
  <div v-else class="app-container h-screen flex overflow-hidden bg-gray-100">
    <!-- Sidebar -->
    <aside class="w-64 bg-slate-800 text-slate-300 flex flex-col shadow-xl z-10">
      <div class="h-16 flex items-center justify-center border-b border-slate-700">
        <h1 class="text-xl font-bold text-white tracking-wider">在线管理平台</h1>
      </div>
      <el-menu
          :default-active="route.path"
          class="flex-1 border-r-0"
          background-color="#1e293b"
          text-color="#cbd5e1"
          active-text-color="#ffffff"
          router
      >
        <template v-for="menu in userMenus" :key="menu.id">
          <!-- no children -->
          <el-menu-item v-if="!menu.children || menu.children.length === 0" :index="menu.path || String(menu.id)">
            <el-icon v-if="menu.icon"><component :is="Icons[menu.icon as keyof typeof Icons] || Icons.Menu" /></el-icon>
            <span>{{ menu.name }}</span>
          </el-menu-item>

          <!-- with children -->
          <el-sub-menu v-else :index="menu.path || String(menu.id)">
            <template #title>
              <el-icon v-if="menu.icon"><component :is="Icons[menu.icon as keyof typeof Icons] || Icons.Menu" /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item v-for="child in menu.children" :key="child.id" :index="child.path || String(child.id)">
              <el-icon v-if="child.icon"><component :is="Icons[child.icon as keyof typeof Icons] || Icons.Menu" /></el-icon>
              <span>{{ child.name }}</span>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </aside>

    <!-- Main Content -->
    <main class="flex-1 flex flex-col overflow-hidden">
      <header class="bg-white shadow-sm h-16 flex items-center justify-between px-6 z-0" v-if="route.name !== 'SqlWorkbench' && route.name !== 'ReportDesigner'">
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
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as Icons from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import request from './utils/request'

const route = useRoute()
const router = useRouter()
const username = ref('')
const userMenus = ref<any[]>([])

const routeNames: Record<string, string> = {
  'Home': '首页概览',
  'SystemManage': '系统管理',
  'ApiManage': '接口管理',
  'DataSourceManage': '数据源管理',
  'CallLog': '调用日志',
  'SqlWorkbench': 'SQL 智慧工作台',
  'CodeGen': '代码生成',
  'ReportTemplateManage': '报表模板管理',
  'ReportInstanceManage': '报表实例管理',
  'ReportDesigner': '报表设计器',
  'UserManage': '用户管理',
  'RoleManage': '角色管理',
  'MenuManage': '菜单管理'
}
const currentRouteName = computed(() => {
  if (routeNames[route.name as string]) {
    return routeNames[route.name as string]
  }
  return route.meta?.title as string || ''
})

const fetchUserMenus = async () => {
  try {
    const res = await request.get('/admin/sys/menu/userMenus')
    if (res) {
      userMenus.value = res as any
    }
  } catch (error) {
    console.error('Failed to fetch user menus:', error)
  }
}

watch(() => route.path, (newPath, oldPath) => {
  if (oldPath === '/login' && newPath !== '/login') {
    username.value = localStorage.getItem('username') || 'Admin'
    fetchUserMenus()
  }
})

onMounted(() => {
  username.value = localStorage.getItem('username') || 'Admin'
  if (route.name !== 'Login') {
    fetchUserMenus()
  }
})

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    userMenus.value = []
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
