<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="max-w-md w-full bg-white rounded-lg shadow-md p-8">
      <div class="text-center mb-8">
        <h2 class="text-3xl font-bold text-gray-800">接口在线管理平台</h2>
        <p class="text-gray-500 mt-2">请登录以继续</p>
      </div>

      <el-form :model="form" :rules="rules" ref="loginForm" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" size="large" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password size="large" @keyup.enter="handleLogin" />
        </el-form-item>

        <el-form-item class="mt-6">
          <el-button type="primary" class="w-full" size="large" :loading="loading" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

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
  if (!loginForm.value) return

  await loginForm.value.validate((valid: boolean) => {
    if (valid) {
      loading.value = true
      // 模拟登录请求
      setTimeout(() => {
        loading.value = false
        if (form.username === 'admin' && form.password === '123456') {
          ElMessage.success('登录成功')
          localStorage.setItem('token', 'mock-token-123456')
          localStorage.setItem('username', form.username)
          router.push('/')
        } else {
          ElMessage.error('用户名或密码错误 (提示: admin / 123456)')
        }
      }, 500)
    }
  })
}
</script>
