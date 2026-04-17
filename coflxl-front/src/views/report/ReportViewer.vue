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
        <div class="flex justify-between items-center hide-on-print">
          <div>
            <h1 class="text-2xl font-bold text-gray-800">{{ reportData.name }}</h1>
            <p class="text-gray-500 text-sm mt-1">{{ reportData.description }}</p>
          </div>
          <div>
            <el-button type="success" plain @click="exportExcel">导出 Excel</el-button>
            <el-button type="warning" plain @click="exportPdf">导出 PDF</el-button>
            <el-button type="primary" @click="loadReport(true)">刷新数据</el-button>
          </div>
        </div>

        <!-- Dynamic Parameters Form -->
        <el-form :inline="true" :model="searchParams" class="mt-4 hide-on-print" v-if="visibleParameters.length > 0" @submit.prevent="loadReport(true)">
          <el-form-item v-for="param in visibleParameters" :key="param.name" :label="param.label || param.name">
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
            <el-button type="primary" @click="loadReport(true)">查询</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="flex-1 p-6 overflow-auto">
        <div v-loading="queryLoading">
          <div v-if="layoutItems.length === 0" class="text-gray-500 text-center py-8 bg-white rounded-lg shadow-sm border border-gray-200">请配置可视化组件和布局</div>
          <div class="grid grid-cols-12 gap-4">
            <div v-for="item in layoutItems" :key="item.widgetId"
                 class="bg-white rounded-lg shadow-sm border border-gray-200 p-4 flex flex-col"
                 :style="{ gridColumn: `span ${item.span || 12}`, height: item.height ? item.height + 'px' : '400px' }">
              <div class="mb-3 font-medium text-gray-700" v-if="getWidget(item.widgetId)?.title">{{ getWidget(item.widgetId)?.title }}</div>

              <div class="flex-1 overflow-hidden">
                <ChartRenderer
                    v-if="getWidget(item.widgetId)?.type === 'chart'"
                    :data="getWidgetData(getWidget(item.widgetId))"
                    :config="getWidget(item.widgetId)?.chartConfig || {}"
                />

                <el-table
                    v-else-if="getWidget(item.widgetId)?.type === 'table'"
                    :data="getWidgetData(getWidget(item.widgetId))"
                    border
                    stripe
                    style="width: 100%"
                    height="100%"
                >
                  <DynamicTableColumn
                      v-for="(col, index) in getTableColumns(getWidget(item.widgetId))"
                      :key="index"
                      :col="col"
                  />
                </el-table>
              </div>
            </div>
          </div>
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
import DynamicTableColumn from '../../components/DynamicTableColumn.vue'
import ChartRenderer from '../../components/ChartRenderer.vue'

import * as XLSX from 'xlsx'

const route = useRoute()
const loading = ref(true)
const queryLoading = ref(false)
const error = ref('')
const reportData = shallowRef<any>(null)
const searchParams = ref<any>({})
const parameters = ref<any[]>([])

const exportExcel = () => {
  const data = getActualReportData()
  if (!data || data.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }

  const worksheet = XLSX.utils.json_to_sheet(data)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, '报表数据')
  XLSX.writeFile(workbook, `${reportData.value?.name || '报表'}.xlsx`)
}

const exportPdf = () => {
  window.print()
}

const parsedConfig = computed(() => {
  try {
    if (reportData.value?.visualizationConfig) {
      return JSON.parse(reportData.value.visualizationConfig)
    }
  } catch (e) {
    // ignore
  }
  return { widgets: [] }
})

const parsedLayout = computed(() => {
  try {
    if (reportData.value?.layoutConfig) {
      return JSON.parse(reportData.value.layoutConfig)
    }
  } catch (e) {
    // ignore
  }
  return { layout: 'vertical', items: [] }
})

const layoutItems = computed(() => {
  const layout = parsedLayout.value
  if (layout && layout.items && layout.items.length > 0) {
    return layout.items
  }
  // Fallback to rendering all widgets if no layout is defined
  const config = parsedConfig.value
  if (config && config.widgets) {
    return config.widgets.map((w: any) => ({ widgetId: w.id, height: 400 }))
  }
  return []
})

const getWidget = (widgetId: string) => {
  const config = parsedConfig.value
  if (config && config.widgets) {
    return config.widgets.find((w: any) => w.id === widgetId)
  }
  return null
}

const getActualReportData = () => {
  if (!reportData.value) return []
  if (Array.isArray(reportData.value)) return reportData.value
  if (reportData.value.data && Array.isArray(reportData.value.data)) return reportData.value.data
  return []
}

const getWidgetData = (widget: any) => {
  const data = getActualReportData()
  if (!data) return []
  if (widget && widget.dataFilter && widget.dataFilter.field) {
    return data.filter((item: any) => item[widget.dataFilter.field] == widget.dataFilter.value)
  }
  return data
}

const getTableColumns = (widget: any) => {
  if (widget && widget.tableConfig && widget.tableConfig.columns && widget.tableConfig.columns.length > 0) {
    return widget.tableConfig.columns
  }
  const data = getWidgetData(widget)
  if (data && data.length > 0) {
    return Object.keys(data[0]).map(key => ({ prop: key, label: key }))
  }
  return []
}

const visibleParameters = computed(() => {
  return parameters.value.filter(p => p.visible !== false)
})

const parseOptions = (optionsStr: string) => {
  if (!optionsStr) return []
  return optionsStr.split(',').map(item => {
    const [value, label] = item.split(':')
    return { value: value || item, label: label || value || item }
  })
}

const loadReport = async (isQuery = false) => {
  const id = route.params.id
  if (!id) {
    error.value = '报表 ID 不能为空'
    loading.value = false
    return
  }

  if (isQuery === true) {
    queryLoading.value = true
  } else {
    loading.value = true
  }
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
    queryLoading.value = false
  }
}

onMounted(() => {
  loadReport(false)
})
</script>

<style scoped>
@media print {
  .hide-on-print {
    display: none !important;
  }
  .h-screen {
    height: auto !important;
  }
  .overflow-hidden {
    overflow: visible !important;
  }
  .overflow-auto {
    overflow: visible !important;
  }
  body, .bg-gray-50 {
    background-color: white !important;
  }
}
</style>
