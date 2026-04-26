<template>
  <div v-if="route.name === 'Login' || route.name === 'ReportViewer'" class="h-screen w-screen">
    <router-view></router-view>
  </div>
  <div v-else class="app-shell h-screen overflow-hidden" :data-theme="currentTheme">
    <div class="app-container flex h-full overflow-hidden" :class="themeConfig.shellClass">
      <aside class="flex w-64 shrink-0 flex-col border-r shadow-xl" :class="themeConfig.asideClass">
        <div class="border-b px-6 py-5" :class="themeConfig.brandClass">
          <div class="text-lg font-semibold tracking-wide" :class="themeConfig.brandTitleClass">在线管理平台</div>
          <p class="mt-1 text-xs" :class="themeConfig.brandSubtitleClass">统一接口能力与数据服务管理</p>
        </div>

        <div class="px-3 py-4">
          <el-menu
            :default-active="route.path"
            class="app-menu border-r-0"
            :background-color="themeConfig.menuBackground"
            :text-color="themeConfig.menuTextColor"
            :active-text-color="themeConfig.menuActiveTextColor"
            :unique-opened="true"
            router
          >
            <template v-for="menu in userMenus" :key="menu.id">
              <el-menu-item
                v-if="!menu.children || menu.children.length === 0"
                :index="menu.typeFlag === 'IFRAME' ? `/iframe?url=${encodeURIComponent(menu.iframeUrl || '')}&title=${encodeURIComponent(menu.name)}` : (menu.path || String(menu.id))"
              >
                <el-icon v-if="menu.icon"><component :is="Icons[menu.icon as keyof typeof Icons] || Icons.Menu" /></el-icon>
                <span>{{ menu.name }}</span>
              </el-menu-item>

              <el-sub-menu v-else :index="menu.path || String(menu.id)">
                <template #title>
                  <el-icon v-if="menu.icon"><component :is="Icons[menu.icon as keyof typeof Icons] || Icons.Menu" /></el-icon>
                  <span>{{ menu.name }}</span>
                </template>
                <el-menu-item
                  v-for="child in menu.children"
                  :key="child.id"
                  :index="child.typeFlag === 'IFRAME' ? `/iframe?url=${encodeURIComponent(child.iframeUrl || '')}&title=${encodeURIComponent(child.name)}` : (child.path || String(child.id))"
                >
                  <el-icon v-if="child.icon"><component :is="Icons[child.icon as keyof typeof Icons] || Icons.Menu" /></el-icon>
                  <span>{{ child.name }}</span>
                </el-menu-item>
              </el-sub-menu>
            </template>
          </el-menu>
        </div>
      </aside>

      <main class="flex-1 flex flex-col overflow-hidden">
        <header
          v-if="route.name !== 'SqlWorkbench' && route.name !== 'ReportDesigner'"
          class="flex h-16 items-center justify-between border-b px-6"
          :class="themeConfig.headerClass"
        >
          <div class="flex flex-col">
            <Breadcrumb class="mt-1"></Breadcrumb>
          </div>

          <div class="flex items-center gap-3">
            <el-dropdown @command="handleThemeCommand" trigger="click">
              <button
                type="button"
                class="flex items-center gap-2 rounded-lg border px-3 py-2 text-sm transition-colors"
                :class="themeConfig.themeButtonClass"
              >
                <el-icon><component :is="Icons.Brush" /></el-icon>
                <span>{{ themeConfig.label }}</span>
                <el-icon><component :is="Icons.ArrowDown" /></el-icon>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="theme in themeOptions"
                    :key="theme.value"
                    :command="theme.value"
                  >
                    {{ theme.label }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <el-dropdown @command="handleCommand" trigger="click">
              <span class="el-dropdown-link flex items-center cursor-pointer text-sm transition" :class="themeConfig.userTextClass">
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

        <div class="flex-1 overflow-auto p-6" :class="themeConfig.contentClass">
          <router-view></router-view>
        </div>
      </main>

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
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as Icons from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from './utils/request'
import Breadcrumb from './components/Breadcrumb.vue'

const route = useRoute()
const router = useRouter()
const username = ref('')
const userMenus = ref<any[]>([])
const THEME_STORAGE_KEY = 'app-theme'

type ThemeKey = 'default' | 'slateBlue' | 'light'

type ThemeConfig = {
  label: string
  shellClass: string
  asideClass: string
  brandClass: string
  brandTitleClass: string
  brandSubtitleClass: string
  menuBackground: string
  menuTextColor: string
  menuActiveTextColor: string
  headerClass: string
  headerTitleClass: string
  themeButtonClass: string
  userTextClass: string
  contentClass: string
}

const themeOptions: Array<{ label: string; value: ThemeKey }> = [
  { label: '默认深蓝', value: 'default' },
  { label: '商务蓝灰', value: 'slateBlue' },
  { label: '简洁浅色', value: 'light' }
]

const themeMap: Record<ThemeKey, ThemeConfig> = {
  default: {
    label: '默认深蓝',
    shellClass: 'bg-slate-100',
    asideClass: 'border-slate-700 bg-slate-900 text-slate-200',
    brandClass: 'border-slate-800 bg-slate-900',
    brandTitleClass: 'text-white',
    brandSubtitleClass: 'text-slate-400',
    menuBackground: '#0f172a',
    menuTextColor: '#cbd5e1',
    menuActiveTextColor: '#ffffff',
    headerClass: 'border-slate-200 bg-white',
    headerTitleClass: 'text-slate-800',
    themeButtonClass: 'border-slate-200 bg-white text-slate-600 hover:border-blue-300 hover:text-blue-600',
    userTextClass: 'text-slate-600 hover:text-blue-600',
    contentClass: 'bg-slate-100'
  },
  slateBlue: {
    label: '商务蓝灰',
    shellClass: 'bg-slate-200/70',
    asideClass: 'border-slate-600 bg-slate-800 text-slate-100',
    brandClass: 'border-slate-700 bg-slate-800',
    brandTitleClass: 'text-white',
    brandSubtitleClass: 'text-slate-300',
    menuBackground: '#1e293b',
    menuTextColor: '#dbe4f0',
    menuActiveTextColor: '#eff6ff',
    headerClass: 'border-slate-300 bg-slate-50',
    headerTitleClass: 'text-slate-900',
    themeButtonClass: 'border-slate-300 bg-white text-slate-700 hover:border-sky-300 hover:text-sky-700',
    userTextClass: 'text-slate-700 hover:text-sky-700',
    contentClass: 'bg-slate-200/70'
  },
  light: {
    label: '简洁浅色',
    shellClass: 'bg-slate-50',
    asideClass: 'border-slate-200 bg-white text-slate-700',
    brandClass: 'border-slate-200 bg-slate-50',
    brandTitleClass: 'text-slate-900',
    brandSubtitleClass: 'text-slate-500',
    menuBackground: '#ffffff',
    menuTextColor: '#475569',
    menuActiveTextColor: '#0f172a',
    headerClass: 'border-slate-200 bg-white',
    headerTitleClass: 'text-slate-900',
    themeButtonClass: 'border-slate-200 bg-white text-slate-600 hover:border-indigo-300 hover:text-indigo-600',
    userTextClass: 'text-slate-600 hover:text-indigo-600',
    contentClass: 'bg-slate-50'
  }
}

const currentTheme = ref<ThemeKey>('default')
const themeConfig = computed(() => themeMap[currentTheme.value])

const setTheme = (theme: ThemeKey) => {
  currentTheme.value = theme
  localStorage.setItem(THEME_STORAGE_KEY, theme)
}

const restoreTheme = () => {
  const savedTheme = localStorage.getItem(THEME_STORAGE_KEY) as ThemeKey | null
  if (savedTheme && themeMap[savedTheme]) {
    currentTheme.value = savedTheme
  }
}

const handleThemeCommand = (command: string | number | object) => {
  const theme = command as ThemeKey
  if (themeMap[theme]) {
    setTheme(theme)
  }
}

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
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码长度至少为6位', trigger: 'blur' }],
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

const currentRouteName = computed(() => {
  if (route.name === 'IFrameView') {
    return route.query.title ? String(route.query.title) : '自定义页面'
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
      userMenus.value = filterNavigationMenus(allResources)

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
  restoreTheme()
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
