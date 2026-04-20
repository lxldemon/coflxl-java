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
          <el-menu-item v-if="!menu.children || menu.children.length === 0" :index="menu.typeFlag === 'IFRAME' ? `/iframe?url=${encodeURIComponent(menu.iframeUrl || '')}&title=${encodeURIComponent(menu.name)}` : (menu.path || String(menu.id))">
            <el-icon v-if="menu.icon"><component :is="Icons[menu.icon as keyof typeof Icons] || Icons.Menu" /></el-icon>
            <span>{{ menu.name }}</span>
          </el-menu-item>

          <!-- with children -->
          <el-sub-menu v-else :index="menu.path || String(menu.id)">
            <template #title>
              <el-icon v-if="menu.icon"><component :is="Icons[menu.icon as keyof typeof Icons] || Icons.Menu" /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item v-for="child in menu.children" :key="child.id" :index="child.typeFlag === 'IFRAME' ? `/iframe?url=${encodeURIComponent(child.iframeUrl || '')}&title=${encodeURIComponent(child.name)}` : (child.path || String(child.id))">
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
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="el-dropdown-link flex items-center cursor-pointer text-sm text-gray-600 hover:text-blue-600 transition">
              欢迎, {{ username }}
              <el-icon class="ml-1"><component :is="Icons.ArrowDown" /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><component :is="Icons.User" /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item command="changePwd">
                  <el-icon><component :is="Icons.Lock" /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout" class="text-red-500">
                  <el-icon><component :is="Icons.SwitchButton" /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <div class="flex-1 overflow-auto p-6">
        <router-view></router-view>
      </div>
    </main>

    <!-- Change Password Dialog -->
    <el-dialog title="修改密码" v-model="pwdDialogVisible" width="400px" append-to-body>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitChangePwd" :loading="pwdSaving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as Icons from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from './utils/request'

const route = useRoute()
const router = useRouter()
const username = ref('')
const userMenus = ref<any[]>([])

// --- Password Change Logic ---
const pwdDialogVisible = ref(false)
const pwdSaving = ref(false)
const pwdFormRef = ref()
const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPwd = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== pwdForm.value.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码长度至少为6位', trigger: 'blur'}],
  confirmPassword: [{ required: true, validator: validateConfirmPwd, trigger: 'blur' }]
}

const submitChangePwd = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      pwdSaving.value = true
      try {
        await request.post('/admin/auth/updatePwd', {
          oldPassword: pwdForm.value.oldPassword,
          newPassword: pwdForm.value.newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        pwdDialogVisible.value = false
        setTimeout(() => {
          handleLogout(true)
        }, 1500)
      } finally {
        pwdSaving.value = false
      }
    }
  })
}

// --- Dropdown command handler ---
const handleCommand = (command: string) => {
  if (command === 'logout') {
    handleLogout()
  } else if (command === 'changePwd') {
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    pwdDialogVisible.value = true
    if (pwdFormRef.value) pwdFormRef.value.clearValidate()
  } else if (command === 'profile') {
    ElMessage.info('当前登录用户：' + username.value)
  }
}

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
  'MenuManage': '菜单管理',
  'ProcessDesigner': '流程设计器',
  'ProcessStart': '发起流程',
  'WfDefManage': '流程定义管理',
  'FormManage': '发起流程表单设计',
  'MyTasks': '我的待办'
}
const currentRouteName = computed(() => {
  if (route.name === 'IFrameView') {
    return route.query.title ? String(route.query.title) : '自定义页面'
  }
  if (routeNames[route.name as string]) {
    return routeNames[route.name as string]
  }
  return route.meta?.title as string || ''
})

const filterNavigationMenus = (menus: any[]) => {
  return menus
      .filter(m => m.typeFlag !== 'BUTTON')
      .map(m => {
        if (m.children && m.children.length > 0) {
          return { ...m, children: filterNavigationMenus(m.children) }
        }
        return m
      })
}

const fetchUserMenus = async () => {
  const token = localStorage.getItem('token')
  if (!token) return

  try {
    const res = await request.get('/admin/sys/menu/userMenus')
    if (res) {
      const allResources = res as any[]
      // Render only standard menus in the sidebar nav
      userMenus.value = filterNavigationMenus(allResources)

      // Store all permissions internally or mapped for fast lookup
      // Assuming res has flattened permissions or we can flatten them
      const flattenPerms = (list: any[]): string[] => {
        let perms: string[] = []
        list.forEach(m => {
          if (m.typeFlag === 'BUTTON' && m.permissionCode) {
            perms.push(m.permissionCode)
          }
          if (m.children && m.children.length > 0) {
            perms.push(...flattenPerms(m.children))
          }
        })
        return perms
      }
      const myPerms = flattenPerms(allResources)
      localStorage.setItem('permissions', JSON.stringify(myPerms))
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

const handleLogout = (skipConfirm = false) => {
  if (skipConfirm === true) {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    userMenus.value = []
    router.push('/login')
    return
  }

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
