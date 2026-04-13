<template>
  <div class="h-full flex flex-col bg-gray-100 -m-6">
    <!-- Header -->
    <div class="bg-white px-6 py-4 flex items-center shadow-sm z-10">
      <el-button link @click="router.back()" class="text-gray-600 hover:text-blue-600">
        <el-icon class="mr-1 text-lg"><Back /></el-icon> <span class="text-base">Back</span>
      </el-button>
      <div class="h-6 w-px bg-gray-300 mx-4"></div>
      <h2 class="text-xl font-medium text-gray-800">
        SQL 智慧工作台
        <span v-if="currentApiCode" class="text-sm text-gray-500 ml-2 font-normal">- 调试接口: {{ currentApiCode }}</span>
      </h2>
    </div>

    <!-- Content -->
    <div class="flex-1 flex overflow-hidden p-6 gap-6">
      <!-- Left Panel: SQL Definition -->
      <div class="flex-1 flex flex-col bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-100">
          <h3 class="text-base font-medium text-gray-700">SQL 定义 (支持 SQLToy 标签与动态参数)</h3>
        </div>
        <div class="flex-1 p-4 bg-[#1e1e1e]">
          <MonacoEditor
              v-model="sqlText"
              language="sql"
              height="100%"
              theme="vs-dark"
              :readOnly="true"
          />
        </div>
      </div>

      <!-- Right Panel: Debug Panel -->
      <div class="w-[400px] flex flex-col bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden flex-shrink-0">
        <div class="px-6 py-4 border-b border-gray-100">
          <h3 class="text-base font-medium text-gray-700">参数探测与调试面板</h3>
        </div>
        <div class="p-6 flex-1 overflow-y-auto flex flex-col gap-5">
          <div>
            <div class="text-sm text-gray-600 mb-2">操作类型 (operationType)</div>
            <el-select v-model="operationType" class="w-full" disabled>
              <el-option label="查询 (QUERY)" value="QUERY" />
              <el-option label="分页 (PAGE)" value="PAGE" />
              <el-option label="新增 (INSERT)" value="INSERT" />
              <el-option label="修改 (UPDATE)" value="UPDATE" />
              <el-option label="删除 (DELETE)" value="DELETE" />
              <el-option label="批量新增 (BATCH_INSERT)" value="BATCH_INSERT" />
            </el-select>
          </div>

          <div class="flex-1 flex flex-col min-h-[200px]">
            <div class="text-sm text-gray-600 mb-2">请求 JSON (Map/List)</div>
            <div class="flex-1 border border-gray-200 rounded-md overflow-hidden">
              <MonacoEditor
                  v-model="requestJson"
                  language="json"
                  height="100%"
                  theme="vs"
              />
            </div>
          </div>

          <el-button type="success" class="w-full shadow-sm" size="large" @click="executeTest" :loading="loading">
            一键执行 API 网关测试
          </el-button>

          <div class="mt-2 flex-1 flex flex-col min-h-[200px]">
            <h4 class="font-medium text-gray-800 mb-2">执行结果:</h4>
            <div class="flex-1 border border-gray-200 rounded-md overflow-hidden">
              <MonacoEditor
                  v-model="resultText"
                  language="json"
                  height="100%"
                  theme="vs-dark"
                  :readOnly="true"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Back } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import MonacoEditor from '../../components/MonacoEditor.vue'

const router = useRouter()
const route = useRoute()

const sqlText = ref('SELECT * FROM users WHERE #[name like :name]')
const operationType = ref('QUERY')
const requestJson = ref('{\n  "name": "张三"\n}')
const resultText = ref('')
const loading = ref(false)
const currentApiCode = ref('')

onMounted(async () => {
  const apiCode = route.query.apiCode as string
  if (apiCode) {
    currentApiCode.value = apiCode
    try {
      const detail: any = await request.get(`/admin/api/detail/${apiCode}`)
      if (detail.sql) {
        sqlText.value = detail.sql.sqlText || ''
      }
      if (detail.api) {
        operationType.value = detail.api.operationType || 'QUERY'
      }
      if (detail.params && detail.params.length > 0) {
        const jsonTemplate: Record<string, any> = {}
        detail.params.forEach((p: any) => {
          if (p.dataType === 'Integer' || p.dataType === 'Long') {
            jsonTemplate[p.paramCode] = p.defaultValue ? Number(p.defaultValue) : 0
          } else if (p.dataType === 'Boolean') {
            jsonTemplate[p.paramCode] = p.defaultValue === 'true'
          } else {
            jsonTemplate[p.paramCode] = p.defaultValue || ''
          }
        })
        requestJson.value = JSON.stringify(jsonTemplate, null, 2)
      } else {
        requestJson.value = '{\n\n}'
      }
    } catch (e) {
      ElMessage.error('获取接口详情失败')
    }
  }
})

const executeTest = async () => {
  if (!currentApiCode.value) {
    ElMessage.warning('请先指定要调试的接口编码')
    return
  }

  let parsedJson = {}
  try {
    parsedJson = JSON.parse(requestJson.value)
  } catch (e) {
    ElMessage.error('请求 JSON 格式不正确')
    return
  }

  loading.value = true
  resultText.value = '执行中...'

  try {
    const res = await request.post(`/api/open/${currentApiCode.value}`, parsedJson)
    resultText.value = JSON.stringify(res, null, 2)
  } catch (error: any) {
    resultText.value = error.message || '执行失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 可以根据需要添加额外样式 */
</style>
