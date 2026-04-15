<template>
  <div class="bg-white p-6 rounded-lg shadow-sm h-full flex flex-col">
    <ProTable ref="proTable" :columns="columns" :requestApi="getInstances">
      <template #search="{ search, reset }">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="实例名称">
            <el-input v-model="searchForm.name" placeholder="请输入名称" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search(searchForm)">查询</el-button>
            <el-button @click="resetForm(reset)">重置</el-button>
          </el-form-item>
        </el-form>
      </template>

      <template #toolbar-left>
        <h2 class="text-lg font-medium">报表实例管理</h2>
      </template>
      <template #toolbar-right>
        <el-button type="primary" @click="openDialog()">创建实例</el-button>
      </template>

      <template #statusSlot="{ row }">
        <el-tag :type="row.publishStatus === 'PUBLISHED' ? 'success' : (row.publishStatus === 'OFFLINE' ? 'danger' : 'warning')">{{ row.publishStatus }}</el-tag>
      </template>

      <template #actionSlot="{ row }">
        <el-button link type="primary" size="small" @click="openDialog(row)">配置</el-button>
        <el-button link type="primary" size="small" @click="previewReport(row)">预览</el-button>
        <el-button link type="success" size="small" v-if="row.publishStatus !== 'PUBLISHED'" @click="changeStatus(row, 'publish')">发布</el-button>
        <el-button link type="warning" size="small" v-else @click="changeStatus(row, 'offline')">下线</el-button>
        <el-button link type="info" size="small" v-if="row.publishStatus === 'PUBLISHED'" @click="copyLink(row)">复制链接</el-button>
        <el-popconfirm title="确定删除吗？" @confirm="handleDelete(row)">
          <template #reference>
            <el-button link type="danger" size="small">删除</el-button>
          </template>
        </el-popconfirm>
      </template>
    </ProTable>

    <el-dialog :title="isEdit ? '编辑实例' : '创建实例'" v-model="dialogVisible" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="实例名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="报表模板" required>
          <el-select v-model="form.definitionId" class="w-full" @change="handleTemplateChange">
            <el-option v-for="tpl in templates" :key="tpl.id" :label="tpl.name" :value="tpl.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据源" required>
          <el-select v-model="form.dataSourceCode" class="w-full">
            <el-option v-for="ds in dataSources" :key="ds.code" :label="ds.code" :value="ds.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="参数配置">
          <div class="border border-gray-200 rounded p-2 w-full bg-gray-50">
            <div v-if="currentTemplateParams.length === 0" class="text-gray-400 text-sm">该模板无参数</div>
            <div v-for="param in currentTemplateParams" :key="param.name" class="mb-2 flex items-center">
              <span class="w-24 text-sm text-gray-600">{{ param.name }}:</span>
              <el-input v-model="form.actualParams[param.name]" size="small" class="flex-1" :placeholder="param.type" />
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveInstance" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
import ProTable from '../../components/ProTable.vue'

const proTable = ref()
const searchForm = ref({ name: '' })
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)

const templates = ref<any[]>([])
const dataSources = ref<any[]>([])
const currentTemplateParams = ref<any[]>([])

const form = ref<any>({
  id: undefined,
  name: '',
  description: '',
  definitionId: undefined,
  dataSourceCode: '',
  actualParams: {}
})

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'name', label: '实例名称', width: 200 },
  { prop: 'description', label: '描述', width: 250 },
  { prop: 'dataSourceCode', label: '数据源', width: 150 },
  { prop: 'publishStatus', label: '状态', width: 100, slotName: 'statusSlot' },
  { prop: 'createdAt', label: '创建时间', width: 180 },
  { label: '操作', width: 250, slotName: 'actionSlot', fixed: 'right' }
]

const getInstances = (params: any) => request.get('/admin/report/instance/page', { params })

const resetForm = (resetFn: Function) => {
  searchForm.value = { name: '' }
  resetFn()
}

const fetchDataSources = async () => {
  dataSources.value = await request.get('/admin/data-source/list')
}

const fetchTemplates = async () => {
  templates.value = await request.get('/admin/report/template/list')
}

const handleTemplateChange = (val: number) => {
  const tpl = templates.value.find(t => t.id === val)
  if (tpl) {
    if (tpl.dataSourceCode) {
      form.value.dataSourceCode = tpl.dataSourceCode
    }
    if (tpl.parametersJson) {
      try {
        const parsed = JSON.parse(tpl.parametersJson)
        currentTemplateParams.value = Array.isArray(parsed) ? parsed : []
        // 初始化参数
        const newParams: any = {}
        currentTemplateParams.value.forEach(p => {
          newParams[p.name] = p.defaultValue || ''
        })
        form.value.actualParams = newParams
      } catch (e) {
        console.error('Failed to parse parametersJson:', e)
        currentTemplateParams.value = []
      }
    } else {
      currentTemplateParams.value = []
    }
  } else {
    currentTemplateParams.value = []
  }
}

const openDialog = async (row?: any) => {
  if (row) {
    isEdit.value = true
    const detail: any = await request.get(`/admin/report/instance/detail/${row.id}`)

    let parsedActualParams = {}
    if (detail.actualParametersJson) {
      try {
        parsedActualParams = JSON.parse(detail.actualParametersJson)
      } catch (e) {
        console.error('Failed to parse actualParametersJson:', e)
      }
    }

    form.value = { ...detail, actualParams: parsedActualParams }
    handleTemplateChange(detail.definitionId)
    // 恢复实际参数
    form.value.actualParams = parsedActualParams
  } else {
    isEdit.value = false
    form.value = { id: undefined, name: '', description: '', definitionId: undefined, dataSourceCode: '', actualParams: {} }
    currentTemplateParams.value = []
  }
  dialogVisible.value = true
}

const saveInstance = async () => {
  saving.value = true
  try {
    const payload = {
      ...form.value,
      actualParametersJson: JSON.stringify(form.value.actualParams)
    }
    await request.post('/admin/report/instance/save', payload)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    proTable.value.refresh()
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row: any) => {
  try {
    await request.post(`/admin/report/instance/delete/${row.id}`)
    ElMessage.success('删除成功')
    proTable.value.refresh()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const changeStatus = async (row: any, action: 'publish' | 'offline') => {
  await request.post(`/admin/report/instance/${action}/${row.id}`)
  ElMessage.success('操作成功')
  proTable.value.refresh()
}

const previewReport = (row: any) => {
  window.open(`/report/view/${row.id}`, '_blank')
}

const copyLink = (row: any) => {
  const url = `${window.location.origin}/report/view/${row.id}`
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制: ' + url)
  })
}

onMounted(() => {
  fetchDataSources()
  fetchTemplates()
})
</script>
