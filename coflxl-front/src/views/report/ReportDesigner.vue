<template>
  <div class="h-full flex flex-col bg-gray-100 -m-6">
    <!-- Header -->
    <div class="bg-white px-6 py-4 flex items-center justify-between shadow-sm z-10">
      <div class="flex items-center">
        <el-button link @click="router.back()" class="text-gray-600 hover:text-blue-600">
          <el-icon class="mr-1 text-lg"><Back /></el-icon> <span class="text-base">返回</span>
        </el-button>
        <div class="h-6 w-px bg-gray-300 mx-4"></div>
        <h2 class="text-xl font-medium text-gray-800">
          报表设计器
          <span v-if="form.id" class="text-sm text-gray-500 ml-2 font-normal">- 编辑: {{ form.name }}</span>
        </h2>
      </div>
      <div>
        <el-button @click="previewReport">预览数据</el-button>
        <el-button type="primary" @click="saveTemplate" :loading="saving">保存模板</el-button>
      </div>
    </div>

    <!-- Content -->
    <div class="flex-1 flex overflow-hidden p-6 gap-6">
      <!-- Left Panel: Configuration -->
      <div class="w-[400px] flex flex-col bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden flex-shrink-0">
        <div class="px-6 py-4 border-b border-gray-100">
          <h3 class="text-base font-medium text-gray-700">基础配置</h3>
        </div>
        <div class="p-6 flex-1 overflow-y-auto">
          <el-form :model="form" label-width="80px" label-position="top">
            <el-form-item label="模板名称" required>
              <el-input v-model="form.name" />
            </el-form-item>
            <el-form-item label="描述">
              <el-input v-model="form.description" type="textarea" />
            </el-form-item>
            <el-form-item label="分类">
              <el-input v-model="form.category" />
            </el-form-item>

            <el-divider>参数定义</el-divider>
            <div class="mb-2">
              <el-button type="primary" plain size="small" @click="addParam">添加参数</el-button>
              <el-button type="success" plain size="small" @click="parseSqlParams">解析SQL参数</el-button>
            </div>
            <div v-for="(param, index) in paramsList" :key="index" class="flex flex-col gap-2 mb-4 bg-gray-50 p-3 rounded border border-gray-200">
              <div class="flex gap-2 items-center">
                <el-input v-model="param.name" placeholder="参数名(如 startDate)" size="small" class="flex-1" />
                <el-input v-model="param.label" placeholder="显示名称(如 开始日期)" size="small" class="flex-1" />
                <el-button link type="danger" size="small" @click="paramsList.splice(index, 1)"><el-icon><Delete /></el-icon></el-button>
              </div>
              <div class="flex gap-2 items-center">
                <el-select v-model="param.type" size="small" class="flex-1" placeholder="数据类型">
                  <el-option label="String" value="string" />
                  <el-option label="Number" value="number" />
                  <el-option label="Date" value="date" />
                </el-select>
                <el-select v-model="param.componentType" size="small" class="flex-1" placeholder="组件类型">
                  <el-option label="文本框" value="input" />
                  <el-option label="下拉框" value="select" />
                  <el-option label="日期选择" value="date" />
                </el-select>
                <div class="flex items-center gap-1 ml-2">
                  <span class="text-xs text-gray-500">显示</span>
                  <el-switch v-model="param.visible" size="small" />
                </div>
              </div>
              <div class="flex gap-2 items-center mt-1" v-if="param.componentType === 'select'">
                <el-input v-model="param.options" placeholder="下拉选项，格式如: 1:是,0:否" size="small" class="w-full" />
              </div>
            </div>

            <el-divider>可视化配置 (JSON)</el-divider>
            <div class="h-[200px] border border-gray-200 rounded-md overflow-hidden">
              <MonacoEditor v-model="form.visualizationConfigJson" language="json" height="100%" theme="vs" />
            </div>
          </el-form>
        </div>
      </div>

      <!-- Right Panel: SQL Definition -->
      <div class="flex-1 flex flex-col bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
          <h3 class="text-base font-medium text-gray-700">SQL 数据集定义</h3>
          <el-select v-model="previewDataSource" placeholder="选择测试数据源" size="small" style="width: 150px">
            <el-option v-for="ds in dataSources" :key="ds.code" :label="ds.code" :value="ds.code" />
          </el-select>
        </div>
        <div class="flex-1 p-4 bg-[#1e1e1e]">
          <MonacoEditor
              v-model="form.sqlQuery"
              language="sql"
              height="100%"
              theme="vs-dark"
              @blur="parseSqlParams"
          />
        </div>

        <!-- Preview Result Area -->
        <div v-if="previewData || previewLoading" class="h-[300px] border-t border-gray-200 flex flex-col" v-loading="previewLoading">
          <div class="px-4 py-2 bg-gray-50 border-b border-gray-200 flex justify-between items-center">
            <span class="text-sm font-medium text-gray-700">数据预览 (前 50 条)</span>
            <el-button link @click="previewData = null"><el-icon><Close /></el-icon></el-button>
          </div>
          <div class="flex-1 p-4 overflow-auto bg-white">
            <el-table v-if="previewData" :data="previewData" border stripe size="small">
              <DynamicTableColumn v-for="(col, index) in tableColumns" :key="index" :col="col" />
            </el-table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, shallowRef, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Back, Delete, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import MonacoEditor from '../../components/MonacoEditor.vue'
import DynamicTableColumn from '../../components/DynamicTableColumn.vue'

const router = useRouter()
const route = useRoute()

const saving = ref(false)
const previewLoading = ref(false)
const dataSources = ref<any[]>([])
const previewDataSource = ref('')
const previewData = shallowRef<any[] | null>(null)

const tableColumns = computed(() => {
  try {
    if (form.value.visualizationConfigJson) {
      const config = JSON.parse(form.value.visualizationConfigJson)
      if (config.columns && config.columns.length > 0) {
        return config.columns
      }
    }
  } catch (e) {
    console.error('Failed to parse visualizationConfigJson:', e)
  }

  if (previewData.value && previewData.value.length > 0) {
    return Object.keys(previewData.value[0]).map(key => ({ prop: key, label: key }))
  }
  return []
})

const paramsList = ref<any[]>([])

const form = ref({
  id: undefined,
  name: '',
  description: '',
  category: '',
  dataSourceCode: '',
  sqlQuery: 'SELECT * FROM users LIMIT 10',
  visualizationConfigJson: '{\n  "type": "table",\n  "columns": []\n}',
  layoutConfigJson: '{}'
})

const parseSqlParams = () => {
  const newSql = form.value.sqlQuery
  if (!newSql) return
  // 匹配 :paramName 格式的参数
  const regex = /:([a-zA-Z0-9_]+)/g
  const matches = newSql.match(regex)
  if (matches) {
    // 去重
    const paramNames = [...new Set(matches.map(m => m.substring(1)))]
    paramNames.forEach(name => {
      // 如果参数列表中不存在该参数，则自动添加
      if (!paramsList.value.find(p => p.name === name)) {
        paramsList.value.push({
          name,
          type: 'string',
          defaultValue: '',
          label: name,
          componentType: 'input',
          options: '',
          visible: true
        })
      }
    })
  }
}

onMounted(async () => {
  await fetchDataSources()
  const id = route.query.id
  if (id) {
    try {
      const detail: any = await request.get(`/admin/report/template/detail/${id}`)
      form.value = {
        id: detail.id,
        name: detail.name,
        description: detail.description,
        category: detail.category,
        dataSourceCode: detail.dataSourceCode || '',
        sqlQuery: detail.sqlQuery,
        visualizationConfigJson: detail.visualizationConfigJson || '{\n  "type": "table",\n  "columns": []\n}',
        layoutConfigJson: detail.layoutConfigJson || '{}'
      }
      if (detail.dataSourceCode) {
        previewDataSource.value = detail.dataSourceCode
      }
      if (detail.parametersJson) {
        try {
          const parsed = JSON.parse(detail.parametersJson)
          paramsList.value = Array.isArray(parsed) ? parsed : []
        } catch (e) {
          console.error('Failed to parse parametersJson:', e)
          paramsList.value = []
        }
      }
    } catch (e) {
      ElMessage.error('获取模板详情失败')
    }
  }
})

const fetchDataSources = async () => {
  dataSources.value = await request.get('/admin/data-source/list')
  if (dataSources.value.length > 0) {
    previewDataSource.value = dataSources.value[0].code
  }
}

const addParam = () => {
  paramsList.value.push({ name: '', type: 'string', defaultValue: '', label: '', componentType: 'input', visible: true })
}

const previewReport = async () => {
  if (!form.value.sqlQuery) {
    ElMessage.warning('请输入 SQL')
    return
  }
  previewLoading.value = true
  try {
    // 构造测试参数
    const testParams: any = {}
    paramsList.value.forEach(p => {
      testParams[p.name] = p.defaultValue || (p.type === 'number' ? 0 : '')
    })

    const res: any = await request.post('/admin/report/execute/preview', {
      sql: form.value.sqlQuery,
      dataSourceCode: previewDataSource.value,
      params: testParams
    })
    previewData.value = res
    ElMessage.success('预览成功')
  } catch (e: any) {
    ElMessage.error(e.message || '预览失败')
  } finally {
    previewLoading.value = false
  }
}

const saveTemplate = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入模板名称')
    return
  }
  saving.value = true
  try {
    const payload = {
      ...form.value,
      dataSourceCode: previewDataSource.value,
      parametersJson: JSON.stringify(paramsList.value)
    }
    await request.post('/admin/report/template/save', payload)
    ElMessage.success('保存成功')
    router.back()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script>
