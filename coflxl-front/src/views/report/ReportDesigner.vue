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

            <el-divider>
              可视化配置 (JSON)
              <el-popover placement="right" title="配置说明" :width="500" trigger="hover">
                <template #reference>
                  <el-icon class="ml-1 cursor-pointer text-blue-500"><QuestionFilled /></el-icon>
                </template>
                <div class="text-sm text-gray-600 space-y-2">
                  <p><strong>1. 默认表格（取SQL字段）</strong><br/>不配置 <code>columns</code> 或保持为空，系统将自动读取 SQL 结果的字段作为表头。</p>
                  <div><strong>2. 多级表头</strong><br/>在 <code>columns</code> 中使用 <code>children</code> 嵌套。例如：<br/>
                    <pre class="bg-gray-100 p-2 rounded text-xs">"columns": [
  {
    "label": "基础信息",
    "children": [
      { "label": "姓名", "prop": "name" },
      { "label": "年龄", "prop": "age" }
    ]
  }
]</pre>
                  </div>
                  <div><strong>3. 组件化配置 (Widgets)</strong><br/>
                    使用 <code>widgets</code> 数组定义多个组件，支持 <code>table</code> 和 <code>chart</code>。<br/>
                    <strong>多数据集方案：</strong>如果需要不同组件显示不同数据，可在 SQL 中使用 <code>UNION ALL</code> 合并数据并增加类型列，然后在组件中配置 <code>dataFilter</code> 进行过滤。<br/>
                    <pre class="bg-gray-100 p-2 rounded text-xs">{
  "widgets": [
    {
      "id": "chart1",
      "type": "chart",
      "title": "图表分析",
      "dataFilter": { "field": "data_type", "value": "sales" },
      "chartConfig": {
        "chartType": "bar",
        "xAxis": "category_field",
        "yAxis": ["value_field1"],
        "seriesNames": ["销售额"]
      }
    },
    {
      "id": "table1",
      "type": "table",
      "title": "明细数据",
      "dataFilter": { "field": "data_type", "value": "detail" },
      "tableConfig": { "columns": [] }
    }
  ]
}</pre>
                  </div>
                </div>
              </el-popover>
              <el-button link type="primary" size="small" class="ml-2" @click="formatJson('visualization')">格式化</el-button>
              <el-button link type="success" size="small" class="ml-1" @click="applyJsonToVisual">从JSON回填引导</el-button>
            </el-divider>
            <div class="mb-3 p-3 bg-blue-50 border border-blue-100 rounded-md space-y-3">
              <div class="flex items-center justify-between">
                <div class="text-sm font-medium text-blue-700">可视化引导配置（推荐）</div>
                <el-button type="primary" plain size="small" @click="addVisualWidget">添加组件</el-button>
              </div>
              <div v-if="visualWidgets.length === 0" class="text-xs text-gray-500">暂无组件，点击“添加组件”开始配置</div>
              <div
                v-for="(widget, index) in visualWidgets"
                :key="`widget-${index}`"
                class="p-3 bg-white border border-gray-200 rounded space-y-2"
              >
                <div class="flex gap-2 items-center">
                  <el-input v-model="widget.id" size="small" placeholder="组件ID，如 chart1 / table1" />
                  <el-select v-model="widget.type" size="small" style="width: 100px">
                    <el-option label="表格" value="table" />
                    <el-option label="图表" value="chart" />
                  </el-select>
                  <el-button link type="danger" size="small" @click="removeVisualWidget(index)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
                <el-input v-model="widget.title" size="small" placeholder="组件标题（可选）" />
                <div class="grid grid-cols-2 gap-2">
                  <el-input v-model="widget.filterField" size="small" placeholder="过滤字段（可选），如 data_type" />
                  <el-input v-model="widget.filterValue" size="small" placeholder="过滤值（可选），如 detail" />
                </div>
                <div v-if="widget.type === 'table'" class="space-y-1">
                  <el-input
                    v-model="widget.tableColumnsText"
                    type="textarea"
                    :rows="3"
                    resize="none"
                    placeholder="表格字段配置（每行一个）：label:prop，例如 姓名:name"
                  />
                  <div class="text-xs text-gray-500">不填时自动使用 SQL 返回字段；填写后按配置顺序展示。</div>
                </div>
                <div v-if="widget.type === 'chart'" class="grid grid-cols-2 gap-2">
                  <el-select v-model="widget.chartType" size="small" placeholder="图表类型">
                    <el-option label="柱状图" value="bar" />
                    <el-option label="折线图" value="line" />
                    <el-option label="饼图" value="pie" />
                  </el-select>
                  <el-input v-model="widget.xAxis" size="small" placeholder="X轴字段，如 month" />
                  <el-input v-model="widget.yAxisText" size="small" placeholder="Y轴字段，逗号分隔" />
                  <el-input v-model="widget.seriesNamesText" size="small" placeholder="系列名称，逗号分隔" />
                </div>
              </div>
            </div>
            <div class="h-[200px] border border-gray-200 rounded-md overflow-hidden mb-4">
              <MonacoEditor v-model="form.visualizationConfigJson" language="json" height="100%" theme="vs" />
            </div>

            <el-divider>
              布局配置 (JSON)
              <el-popover placement="right" title="布局说明" :width="500" trigger="hover">
                <template #reference>
                  <el-icon class="ml-1 cursor-pointer text-blue-500"><QuestionFilled /></el-icon>
                </template>
                <div class="text-sm text-gray-600 space-y-2">
                  <div><strong>栅格布局 (Grid)</strong><br/>
                    基于 12 列栅格系统。<code>span: 12</code> 为占满整行，<code>span: 6</code> 为占一半。<br/>
                    <strong>示例：上面两个图表，下面一个表格</strong>
                    <pre class="bg-gray-100 p-2 rounded text-xs">{
  "layout": "grid",
  "items": [
    { "widgetId": "chart1", "span": 6, "height": 300 },
    { "widgetId": "chart2", "span": 6, "height": 300 },
    { "widgetId": "table1", "span": 12, "height": 500 }
  ]
}</pre>
                  </div>
                </div>
              </el-popover>
              <el-button link type="primary" size="small" class="ml-2" @click="formatJson('layout')">格式化</el-button>
            </el-divider>
            <div class="mb-3 p-3 bg-green-50 border border-green-100 rounded-md space-y-3">
              <div class="flex items-center justify-between">
                <div class="text-sm font-medium text-green-700">布局引导配置（Grid）</div>
                <el-button type="success" plain size="small" @click="addLayoutItem">添加布局项</el-button>
              </div>
              <div v-if="visualLayoutItems.length === 0" class="text-xs text-gray-500">暂无布局项，点击“添加布局项”开始配置</div>
              <div
                v-for="(item, index) in visualLayoutItems"
                :key="`layout-${index}`"
                class="p-3 bg-white border border-gray-200 rounded"
              >
                <div class="grid grid-cols-3 gap-2 items-center">
                  <el-select v-model="item.widgetId" size="small" placeholder="选择组件ID">
                    <el-option v-for="w in visualWidgets" :key="w.id || 'empty'" :label="w.id || '(未命名组件)'" :value="w.id" />
                  </el-select>
                  <el-input-number v-model="item.span" :min="1" :max="12" size="small" controls-position="right" />
                  <el-input-number v-model="item.height" :min="120" :max="1000" :step="20" size="small" controls-position="right" />
                </div>
                <div class="flex justify-end mt-1">
                  <el-button link type="danger" size="small" @click="removeLayoutItem(index)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
            <div class="h-[150px] border border-gray-200 rounded-md overflow-hidden">
              <MonacoEditor v-model="form.layoutConfigJson" language="json" height="100%" theme="vs" />
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
        <div v-if="previewData || previewLoading" class="h-[400px] border-t border-gray-200 flex flex-col" v-loading="previewLoading">
          <div class="px-4 py-2 bg-gray-50 border-b border-gray-200 flex justify-between items-center">
            <span class="text-sm font-medium text-gray-700">数据预览 (前 50 条)</span>
            <el-button link @click="previewData = null"><el-icon><Close /></el-icon></el-button>
          </div>
          <div class="flex-1 p-4 overflow-auto bg-white">
            <div v-if="layoutItems.length === 0" class="text-gray-500 text-center py-8">请配置可视化组件和布局</div>
            <div class="grid grid-cols-12 gap-4">
              <div v-for="item in layoutItems" :key="item.widgetId"
                   class="bg-white rounded-lg border border-gray-200 p-4 flex flex-col"
                   :style="{ gridColumn: `span ${item.span || 12}`, height: item.height ? item.height + 'px' : '300px' }">
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
                      size="small"
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
  </div>
</template>

<script setup lang="ts">
import { ref, shallowRef, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Back, Delete, Close, QuestionFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import MonacoEditor from '../../components/MonacoEditor.vue'
import DynamicTableColumn from '../../components/DynamicTableColumn.vue'
import ChartRenderer from '../../components/ChartRenderer.vue'

const router = useRouter()
const route = useRoute()

const saving = ref(false)
const previewLoading = ref(false)
const dataSources = ref<any[]>([])
const previewDataSource = ref('')
const previewData = shallowRef<any>(null)

const parsedConfig = computed(() => {
  try {
    if (form.value.visualizationConfigJson) {
      return JSON.parse(form.value.visualizationConfigJson)
    }
  } catch (e) {
    // ignore
  }
  return { widgets: [] }
})

const parsedLayout = computed(() => {
  try {
    if (form.value.layoutConfigJson) {
      return JSON.parse(form.value.layoutConfigJson)
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
    return config.widgets.map((w: any) => ({ widgetId: w.id, height: 300 }))
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

const getActualPreviewData = () => {
  if (!previewData.value) return []
  if (Array.isArray(previewData.value)) return previewData.value
  if (previewData.value.data && Array.isArray(previewData.value.data)) return previewData.value.data
  return []
}

const getWidgetData = (widget: any) => {
  const data = getActualPreviewData()
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

const paramsList = ref<any[]>([])
const visualWidgets = ref<any[]>([])
const visualLayoutItems = ref<any[]>([])
const syncingFromVisual = ref(false)
const syncingFromJson = ref(false)

const form = ref({
  id: undefined,
  name: '',
  description: '',
  category: '',
  dataSourceCode: '',
  sqlQuery: 'SELECT * FROM users LIMIT 10',
  visualizationConfigJson: '{\n  "widgets": [\n    {\n      "id": "table1",\n      "type": "table",\n      "title": "明细数据",\n      "tableConfig": {\n        "columns": []\n      }\n    }\n  ]\n}',
  layoutConfigJson: '{\n  "layout": "vertical",\n  "items": [\n    { "widgetId": "table1", "height": 500 }\n  ]\n}'
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

const formatJson = (type: 'visualization' | 'layout') => {
  try {
    if (type === 'visualization') {
      const parsed = JSON.parse(form.value.visualizationConfigJson)
      form.value.visualizationConfigJson = JSON.stringify(parsed, null, 2)
    } else {
      const parsed = JSON.parse(form.value.layoutConfigJson)
      form.value.layoutConfigJson = JSON.stringify(parsed, null, 2)
    }
    ElMessage.success('格式化成功')
  } catch (e: any) {
    ElMessage.error(`JSON 格式错误: ${e.message}`)
  }
}

const serializeColumns = (columns: any[]) => {
  if (!Array.isArray(columns) || columns.length === 0) return ''
  return columns
    .map((col: any) => `${col?.label || col?.prop || ''}:${col?.prop || ''}`.trim())
    .filter(Boolean)
    .join('\n')
}

const parseColumnsText = (text: string) => text
  .split('\n')
  .map(line => line.trim())
  .filter(Boolean)
  .map(line => {
    const [left, right] = line.split(':')
    const label = right ? left?.trim() : line
    const prop = right ? right.trim() : line
    return { label: label || prop, prop }
  })
  .filter(col => col.prop)

const buildWidgetModel = (widget: any) => ({
  id: widget?.id || '',
  type: widget?.type || 'table',
  title: widget?.title || '',
  filterField: widget?.dataFilter?.field || '',
  filterValue: widget?.dataFilter?.value ?? '',
  tableColumnsText: serializeColumns(widget?.tableConfig?.columns || []),
  chartType: widget?.chartConfig?.chartType || 'bar',
  xAxis: widget?.chartConfig?.xAxis || '',
  yAxisText: Array.isArray(widget?.chartConfig?.yAxis) ? widget.chartConfig.yAxis.join(',') : '',
  seriesNamesText: Array.isArray(widget?.chartConfig?.seriesNames) ? widget.chartConfig.seriesNames.join(',') : ''
})

const isSameByJson = (a: any, b: any) => JSON.stringify(a) === JSON.stringify(b)

const syncVisualFromJson = () => {
  syncingFromJson.value = true
  try {
    const config = parsedConfig.value
    const layout = parsedLayout.value
    const nextWidgets = Array.isArray(config?.widgets) ? config.widgets.map((w: any) => buildWidgetModel(w)) : []
    const nextLayoutItems = Array.isArray(layout?.items)
      ? layout.items.map((item: any) => ({
        widgetId: item?.widgetId || '',
        span: Number(item?.span) || 12,
        height: Number(item?.height) || 300
      }))
      : []
    if (!isSameByJson(visualWidgets.value, nextWidgets)) {
      visualWidgets.value = nextWidgets
    }
    if (!isSameByJson(visualLayoutItems.value, nextLayoutItems)) {
      visualLayoutItems.value = nextLayoutItems
    }
  } finally {
    syncingFromJson.value = false
  }
}

const applyJsonToVisual = () => {
  syncVisualFromJson()
  ElMessage.success('已从 JSON 回填到引导配置')
}

const splitByComma = (text: string) => text
  .split(',')
  .map(item => item.trim())
  .filter(Boolean)

const syncJsonFromVisual = () => {
  if (syncingFromJson.value) return
  syncingFromVisual.value = true
  try {
    const nextWidgets = visualWidgets.value
      .filter((w: any) => w.id)
      .map((w: any) => {
        const dataFilter = w.filterField
          ? { field: w.filterField, value: w.filterValue }
          : undefined
        if (w.type === 'chart') {
          return {
            id: w.id,
            type: 'chart',
            title: w.title || undefined,
            dataFilter,
            chartConfig: {
              chartType: w.chartType || 'bar',
              xAxis: w.xAxis || '',
              yAxis: splitByComma(w.yAxisText || ''),
              seriesNames: splitByComma(w.seriesNamesText || '')
            }
          }
        }
        return {
          id: w.id,
          type: 'table',
          title: w.title || undefined,
          dataFilter,
          tableConfig: { columns: parseColumnsText(w.tableColumnsText || '') }
        }
      })
    const nextLayoutItems = visualLayoutItems.value
      .filter((item: any) => item.widgetId)
      .map((item: any) => ({
        widgetId: item.widgetId,
        span: Math.min(Math.max(Number(item.span) || 12, 1), 12),
        height: Math.max(Number(item.height) || 300, 120)
      }))
    const nextVisualizationJson = JSON.stringify({ widgets: nextWidgets }, null, 2)
    const nextLayoutJson = JSON.stringify({ layout: 'grid', items: nextLayoutItems }, null, 2)
    if (form.value.visualizationConfigJson !== nextVisualizationJson) {
      form.value.visualizationConfigJson = nextVisualizationJson
    }
    if (form.value.layoutConfigJson !== nextLayoutJson) {
      form.value.layoutConfigJson = nextLayoutJson
    }
  } finally {
    syncingFromVisual.value = false
  }
}

const addVisualWidget = () => {
  visualWidgets.value.push({
    id: `widget${visualWidgets.value.length + 1}`,
    type: 'table',
    title: '',
    filterField: '',
    filterValue: '',
    tableColumnsText: '',
    chartType: 'bar',
    xAxis: '',
    yAxisText: '',
    seriesNamesText: ''
  })
}

const removeVisualWidget = (index: number) => {
  const removed = visualWidgets.value[index]
  visualWidgets.value.splice(index, 1)
  if (removed?.id) {
    visualLayoutItems.value = visualLayoutItems.value.filter((item: any) => item.widgetId !== removed.id)
  }
}

const addLayoutItem = () => {
  visualLayoutItems.value.push({
    widgetId: visualWidgets.value[0]?.id || '',
    span: 12,
    height: 300
  })
}

const removeLayoutItem = (index: number) => {
  visualLayoutItems.value.splice(index, 1)
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
        visualizationConfigJson: detail.visualizationConfigJson || '{\n  "widgets": [\n    {\n      "id": "table1",\n      "type": "table",\n      "title": "明细数据",\n      "tableConfig": {\n        "columns": []\n      }\n    }\n  ]\n}',
        layoutConfigJson: detail.layoutConfigJson || '{\n  "layout": "vertical",\n  "items": [\n    { "widgetId": "table1", "height": 500 }\n  ]\n}'
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
  syncVisualFromJson()
})

watch(
  [visualWidgets, visualLayoutItems],
  () => {
    syncJsonFromVisual()
  },
  { deep: true }
)

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
