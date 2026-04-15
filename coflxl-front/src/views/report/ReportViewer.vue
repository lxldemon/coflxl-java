<template>
  <div class="h-screen w-screen bg-gray-50 flex flex-col">
    <div v-if="loading" class="flex-1 flex items-center justify-center">
      <el-spinner size="large" />
      <span class="ml-2 text-gray-500">加载报表中...</span>
    </div>

    <div v-else-if="error" class="flex-1 flex items-center justify-center">
      <el-result icon="error" title="加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="loadReport">重试</el-button>
        </template>
      </el-result>
    </div>

    <div v-else-if="reportData" class="flex-1 flex flex-col overflow-hidden">
      <div class="bg-white px-6 py-4 shadow-sm z-10 border-b border-gray-200">
        <div class="flex justify-between items-center">
          <div>
            <h1 class="text-2xl font-bold text-gray-800">{{ reportData.name }}</h1>
            <p class="text-gray-500 text-sm mt-1">{{ reportData.description }}</p>
          </div>
          <el-button @click="loadReport">刷新数据</el-button>
        </div>

        <!-- Dynamic Parameters Form -->
        <el-form :inline="true" :model="searchParams" class="mt-4" v-if="parameters.length > 0">
          <el-form-item v-for="param in parameters" :key="param.name" :label="param.label || param.name">
            <el-date-picker
                v-if="param.componentType === 'date'"
                v-model="searchParams[param.name]"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                style="width: 200px"
            />
            <el-select
                v-else-if="param.componentType === 'select'"
                v-model="searchParams[param.name]"
                placeholder="请选择"
                style="width: 200px"
                clearable
            >
              <el-option
                  v-for="opt in parseOptions(param.options)"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
              />
            </el-select>
            <el-input
                v-else
                v-model="searchParams[param.name]"
                placeholder="请输入"
                style="width: 200px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadReport">查询</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="flex-1 p-6 overflow-auto">
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
          <el-table :data="reportData.data" border stripe style="width: 100%">
            <el-table-column v-for="col in columns" :key="col" :prop="col" :label="col" />
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, shallowRef, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const loading = ref(true)
const error = ref('')
const reportData = shallowRef<any>(null)
const searchParams = ref<any>({})
const parameters = ref<any[]>([])

const columns = computed(() => {
  if (reportData.value && reportData.value.data && reportData.value.data.length > 0) {
    return Object.keys(reportData.value.data[0])
  }
  return []
})

const parseOptions = (optionsStr: string) => {
  if (!optionsStr) return []
  return optionsStr.split(',').map(item => {
    const [value, label] = item.split(':')
    return { value: value || item, label: label || value || item }
  })
}

const loadReport = async () => {
  const id = route.params.id
  if (!id) {
    error.value = '报表 ID 不能为空'
    loading.value = false
    return
  }

  loading.value = true
  error.value = ''

  try {
    const res: any = await request.post(`/admin/report/execute/instance/${id}`, searchParams.value)
    reportData.value = res
    if (res.parameters) {
      try {
        const parsed = JSON.parse(res.parameters)
        parameters.value = Array.isArray(parsed) ? parsed : []
      } catch (e) {
        console.error('Failed to parse parameters:', e)
        parameters.value = []
      }
    }
  } catch (e: any) {
    error.value = e.message || '获取报表数据失败，请检查报表是否已发布或权限不足'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadReport()
})
</script>
