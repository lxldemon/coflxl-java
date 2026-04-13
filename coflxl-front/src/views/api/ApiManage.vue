<template>
  <div class="bg-white p-6 rounded-lg shadow-sm h-full flex flex-col">
    <ProTable v-if="isReady" ref="proTable" :columns="columns" :requestApi="getApis">
      <template #search="{ search, reset }">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="所属系统">
            <el-select v-model="searchForm.systemCode" placeholder="请选择系统" clearable style="width: 200px">
              <el-option v-for="sys in systemList" :key="sys.systemCode" :label="sys.systemName" :value="sys.systemCode" />
            </el-select>
          </el-form-item>
          <el-form-item label="接口编码">
            <el-input v-model="searchForm.apiCode" placeholder="请输入编码" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item label="接口名称">
            <el-input v-model="searchForm.apiName" placeholder="请输入名称" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search(searchForm)">查询</el-button>
            <el-button @click="resetForm(reset)">重置</el-button>
          </el-form-item>
        </el-form>
      </template>

      <template #toolbar-left>
        <h2 class="text-lg font-medium">接口发布管理</h2>
      </template>
      <template #toolbar-right>
        <el-button type="primary" @click="openDrawer()">新增接口</el-button>
      </template>

      <template #systemSlot="{ row }">
        {{ systemList.find(s => s.systemCode === row.systemCode)?.systemName || row.systemCode }}
      </template>
      <template #statusSlot="{ row }">
        <el-tag :type="row.status === 'PUBLISHED' ? 'success' : (row.status === 'OFFLINE' ? 'danger' : 'warning')">{{ row.status }}</el-tag>
      </template>

      <template #actionSlot="{ row }">
        <el-button link type="primary" size="small" @click="openDrawer(row)">配置</el-button>
        <el-button link type="primary" size="small" @click="goToWorkbench(row)">调试</el-button>
        <el-button link type="success" size="small" v-if="row.status !== 'PUBLISHED'" @click="changeStatus(row.apiCode, 'publish')">发布</el-button>
        <el-button link type="warning" size="small" v-else @click="changeStatus(row.apiCode, 'offline')">下线</el-button>
      </template>
    </ProTable>

    <el-drawer v-model="drawerVisible" title="接口配置" size="50%">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基础信息" name="basic">
          <el-form :model="form" label-width="100px" class="mt-4">
            <el-form-item label="所属系统" required>
              <el-select v-model="form.systemCode" class="w-full" placeholder="请选择所属系统">
                <el-option v-for="sys in systemList" :key="sys.systemCode" :label="sys.systemName" :value="sys.systemCode" />
              </el-select>
            </el-form-item>
            <el-form-item label="接口编码"><el-input v-model="form.apiCode" :disabled="isEdit" /></el-form-item>
            <el-form-item label="接口名称"><el-input v-model="form.apiName" /></el-form-item>
            <el-form-item label="请求路径"><el-input v-model="form.apiPath" placeholder="/getUserList" /></el-form-item>
            <el-form-item label="HTTP方法">
              <el-select v-model="form.httpMethod" class="w-full">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
              </el-select>
            </el-form-item>
            <el-form-item label="操作类型">
              <el-select v-model="form.operationType" class="w-full">
                <el-option label="查询 (QUERY)" value="QUERY" />
                <el-option label="分页 (PAGE)" value="PAGE" />
                <el-option label="新增 (INSERT)" value="INSERT" />
                <el-option label="修改 (UPDATE)" value="UPDATE" />
                <el-option label="删除 (DELETE)" value="DELETE" />
                <el-option label="批量新增 (BATCH_INSERT)" value="BATCH_INSERT" />
              </el-select>
            </el-form-item>
            <el-form-item label="执行模式">
              <el-select v-model="form.executeMode" class="w-full">
                <el-option label="单条/默认 (SINGLE)" value="SINGLE" />
                <el-option label="主子表复合 (MULTI_SQL)" value="MULTI_SQL" />
              </el-select>
            </el-form-item>
            <el-form-item label="数据源">
              <el-select v-model="form.dataSourceCode" class="w-full">
                <el-option v-for="ds in dataSources" :key="ds.code" :label="ds.code" :value="ds.code" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="SQL定义" name="sql">
          <div class="mt-4 h-[450px] flex flex-col">
            <el-alert title="支持 SqlToy 动态 SQL 语法，例如: select * from table where 1=1 #[and id = :id]" type="info" show-icon class="mb-4 flex-shrink-0" />
            <div class="flex-1 border border-gray-200 rounded-md overflow-hidden">
              <MonacoEditor v-model="form.sqlText" language="sql" height="100%" theme="vs" />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="参数定义" name="params">
          <div class="mt-4">
            <el-button type="primary" plain size="small" class="mb-4" @click="addParam">添加参数</el-button>
            <el-table :data="form.params" border size="small">
              <el-table-column label="参数名" width="120">
                <template #default="scope"><el-input v-model="scope.row.paramCode" size="small" /></template>
              </el-table-column>
              <el-table-column label="中文名" width="120">
                <template #default="scope"><el-input v-model="scope.row.paramName" size="small" /></template>
              </el-table-column>
              <el-table-column label="数据类型" width="120">
                <template #default="scope">
                  <el-select v-model="scope.row.dataType" size="small">
                    <el-option label="String" value="String" />
                    <el-option label="Integer" value="Integer" />
                    <el-option label="Long" value="Long" />
                    <el-option label="Boolean" value="Boolean" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="必填" width="60">
                <template #default="scope"><el-checkbox v-model="scope.row.requiredFlag" /></template>
              </el-table-column>
              <el-table-column label="默认值">
                <template #default="scope"><el-input v-model="scope.row.defaultValue" size="small" /></template>
              </el-table-column>
              <el-table-column label="操作" width="80">
                <template #default="scope">
                  <el-button link type="danger" @click="form.params.splice(scope.$index, 1)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" @click="saveApi" :loading="saving">保存配置</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
import ProTable from '../../components/ProTable.vue'
import MonacoEditor from '../../components/MonacoEditor.vue'

const router = useRouter()
const proTable = ref()
const drawerVisible = ref(false)
const activeTab = ref('basic')
const saving = ref(false)
const isEdit = ref(false)
const isReady = ref(false)

const dataSources = ref<any[]>([])
const systemList = ref<any[]>([])
const searchForm = ref({ apiCode: '', apiName: '', systemCode: '' })

const columns = [
  { prop: 'systemCode', label: '所属系统', width: 150, slotName: 'systemSlot' },
  { prop: 'apiCode', label: '接口编码', width: 150 },
  { prop: 'apiName', label: '接口名称', width: 180 },
  { prop: 'apiPath', label: '请求路径', width: 180 },
  { prop: 'operationType', label: '操作类型', width: 120 },
  { prop: 'status', label: '状态', width: 100, slotName: 'statusSlot' },
  { label: '操作', width: 200, slotName: 'actionSlot' }
]

const form = ref<any>({
  id: undefined, sqlId: undefined,
  apiCode: '', apiName: '', apiPath: '', httpMethod: 'GET', operationType: 'QUERY', executeMode: 'SINGLE', dataSourceCode: 'target_db', systemCode: '',
  sqlText: '',
  params: [] as any[]
})

watch(() => form.value.sqlText, (newSql) => {
  if (newSql === undefined || newSql === null) return

  const regex = /:([a-zA-Z0-9_]+)/g
  const matches = [...newSql.matchAll(regex)]
  const paramCodesInSql = Array.from(new Set(matches.map(m => m[1])))

  const existingParams = form.value.params || []
  const newParams: any[] = []

  // 保留 SQL 中存在的参数，以及分页相关的特殊参数
  existingParams.forEach((p: any) => {
    if (paramCodesInSql.includes(p.paramCode) || ['pageNo', 'pageSize'].includes(p.paramCode)) {
      newParams.push(p)
    }
  })

  // 添加 SQL 中新解析出的参数
  paramCodesInSql.forEach(code => {
    if (!newParams.find(p => p.paramCode === code)) {
      newParams.push({
        paramCode: code,
        paramName: code,
        dataType: 'String',
        requiredFlag: false,
        defaultValue: ''
      })
    }
  })

  form.value.params = newParams
})

const getApis = (params: any) => request.get('/admin/api/page', { params })

const resetForm = (resetFn: Function) => {
  searchForm.value = { apiCode: '', apiName: '', systemCode: '' }
  resetFn()
}

const fetchDataSources = async () => {
  dataSources.value = await request.get('/admin/data-source/list')
}

const fetchSystems = async () => {
  const res: any = await request.get('/admin/system/list')
  systemList.value = res || []
}

const openDrawer = async (row?: any) => {
  if (row) {
    isEdit.value = true
    const detail: any = await request.get(`/admin/api/detail/${row.apiCode}`)
    form.value = {
      ...detail.api,
      sqlId: detail.sql ? detail.sql.id : undefined,
      sqlText: detail.sql ? detail.sql.sqlText : '',
      params: detail.params || []
    }
  } else {
    isEdit.value = false
    form.value = { id: undefined, sqlId: undefined, apiCode: '', apiName: '', apiPath: '', httpMethod: 'GET', operationType: 'QUERY', executeMode: 'SINGLE', dataSourceCode: 'target_db', systemCode: '', sqlText: '', params: [] }
  }
  activeTab.value = 'basic'
  drawerVisible.value = true
}

const addParam = () => {
  form.value.params.push({ paramCode: '', paramName: '', dataType: 'String', requiredFlag: false, defaultValue: '' })
}

const saveApi = async () => {
  saving.value = true
  try {
    const payload = {
      api: {
        id: form.value.id,
        apiCode: form.value.apiCode,
        apiName: form.value.apiName,
        apiPath: form.value.apiPath,
        httpMethod: form.value.httpMethod,
        operationType: form.value.operationType,
        executeMode: form.value.executeMode,
        dataSourceCode: form.value.dataSourceCode,
        systemCode: form.value.systemCode
      },
      sql: {
        id: form.value.sqlId,
        sqlText: form.value.sqlText
      },
      params: form.value.params
    }
    await request.post('/admin/api/save', payload)
    ElMessage.success('保存成功')
    drawerVisible.value = false
    proTable.value.refresh()
  } finally {
    saving.value = false
  }
}

const changeStatus = async (apiCode: string, action: 'publish' | 'offline') => {
  await request.post(`/admin/api/${action}/${apiCode}`)
  ElMessage.success('操作成功')
  proTable.value.refresh()
}

const goToWorkbench = (row: any) => {
  router.push({ path: '/sql-workbench', query: { apiCode: row.apiCode } })
}

onMounted(async () => {
  await Promise.all([
    fetchDataSources(),
    fetchSystems()
  ])
  isReady.value = true
})
</script>
