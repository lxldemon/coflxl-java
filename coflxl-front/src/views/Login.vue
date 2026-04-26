<template>
  <div class="min-h-screen bg-slate-100">
    <div class="grid min-h-screen lg:grid-cols-[1fr_440px]">
      <section class="border-r border-slate-200 bg-slate-900 px-6 py-8 text-white sm:px-10 lg:px-12 lg:py-12">
        <div class="mx-auto flex h-full max-w-3xl flex-col">
          <div>
            <div class="inline-flex items-center rounded-md border border-white/10 bg-white/5 px-3 py-1 text-sm font-medium text-slate-100">
              Coflxl API Platform
            </div>
            <div class="mt-8 max-w-2xl">
              <h1 class="text-3xl font-semibold tracking-tight text-white sm:text-4xl">在线管理平台</h1>
              <p class="mt-4 text-sm leading-7 text-slate-300 sm:text-base">
                面向企业级接口管理场景，统一管理 API 发布、数据源配置与 SQL 调试流程，帮助团队稳定、高效地完成数据服务交付。
              </p>
            </div>
          </div>

          <div class="mt-10 border-t border-white/10 pt-6 lg:mt-12">
            <div class="text-sm font-medium text-slate-200">平台核心能力</div>
            <ul class="mt-4 space-y-4 text-sm text-slate-300">
              <li class="flex items-start gap-3">
                <span class="mt-2 h-1.5 w-1.5 shrink-0 rounded-full bg-blue-400"></span>
                <div>
                  <div class="font-medium text-white">动态 API 发布</div>
                  <p class="mt-1 leading-6 text-slate-300">通过 SQL 快速生成标准接口，降低重复开发和维护成本。</p>
                </div>
              </li>
              <li class="flex items-start gap-3">
                <span class="mt-2 h-1.5 w-1.5 shrink-0 rounded-full bg-blue-400"></span>
                <div>
                  <div class="font-medium text-white">数据源统一管理</div>
                  <p class="mt-1 leading-6 text-slate-300">集中维护多数据源配置，支撑不同业务系统稳定接入。</p>
                </div>
              </li>
              <li class="flex items-start gap-3">
                <span class="mt-2 h-1.5 w-1.5 shrink-0 rounded-full bg-blue-400"></span>
                <div>
                  <div class="font-medium text-white">SQL 工作台调试</div>
                  <p class="mt-1 leading-6 text-slate-300">在线验证查询逻辑与接口效果，缩短上线前联调周期。</p>
                </div>
              </li>
            </ul>
          </div>

          <div class="mt-10 border-t border-white/10 pt-6 text-sm text-slate-400 lg:mt-auto">
            统一能力入口 · 稳定交付 · 清晰可控
          </div>
        </div>
      </section>

      <section class="flex items-center justify-center bg-slate-50 px-6 py-10 sm:px-10 lg:px-12">
        <div class="w-full max-w-md">
          <div class="mb-6">
            <p class="text-sm font-medium text-blue-700">欢迎回来</p>
            <h2 class="mt-2 text-3xl font-semibold text-slate-900">登录平台</h2>
            <p class="mt-3 text-sm leading-6 text-slate-500">请输入账号信息以继续访问系统。</p>
          </div>

          <div class="rounded-2xl border border-slate-200 bg-white p-8 shadow-sm sm:p-10">
            <el-form :model="form" :rules="rules" ref="loginForm" label-position="top">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="form.username" placeholder="请输入用户名 (admin)" prefix-icon="User" size="large" :disabled="loading" />
              </el-form-item>

              <el-form-item label="密码" prop="password">
                <el-input v-model="form.password" type="password" placeholder="请输入密码 (123456)" prefix-icon="Lock" show-password size="large" :disabled="loading" @keyup.enter="handleLogin" />
              </el-form-item>

              <el-form-item class="mt-8 mb-0">
                <el-button type="primary" class="!h-12 w-full rounded-lg text-base font-medium" size="large" :loading="loading" :disabled="loading" @click="handleLogin">
                  登录系统
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const loginForm = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginForm.value || loading.value) return
  await loginForm.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        const res = await request.post('/admin/auth/login', {
          username: form.username,
          password: form.password
        })

        ElMessage.success('登录成功')
        localStorage.setItem('token', res.token)
        localStorage.setItem('username', res.user.nickname || res.user.username)
        router.push('/')
      } catch (e: any) {
        // error already handled by request interceptor
      } finally {
        loading.value = false
      }
    }
  })
}
</script>
