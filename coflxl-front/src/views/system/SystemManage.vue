<template>
  <div class="theme-card h-full rounded-lg p-6 shadow-sm flex flex-col">
    <ProTable
        ref="proTable"
        :request-api="getSystems"
        :columns="columns"
    >
      <template #search="{ search, reset }">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="系统编码">
            <el-input v-model="searchForm.systemCode" placeholder="请输入系统编码" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item label="系统名称">
            <el-input v-model="searchForm.systemName" placeholder="请输入系统名称" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search(searchForm)">查询</el-button>
            <el-button @click="resetForm(reset)">重置</el-button>
          </el-form-item>
        </el-form>
      </template>

      <template #toolbar-left>
        <h2 class="theme-title text-lg font-medium">系统管理</h2>
      </template>
      <template #toolbar-right>
        <el-button v-permission="'sys:dict:export'" type="success" @click="exportExcel" plain>导出 Excel</el-button>
        <el-button v-permission="'sys:dict:add'" type="primary" @click="openDialog()">新增系统</el-button>
      </template>

      <template #timeSlot="{ row }">
        <span>{{ formatTime(row.createdAt) }}</span>
      </template>

      <template #actionSlot="scope">
        <el-button v-permission="'sys:dict:update'" type="primary" link size="small" @click="openDialog(scope.row)">编辑</el-button>
        <el-popconfirm title="确定删除该系统吗？" @confirm="handleDelete(scope.row)">
          <template #reference>
            <el-button v-permission="'sys:dict:delete'" type="danger" link size="small">删除</el-button>
          </template>
        </el-popconfirm>
      </template>
    </ProTable>

    <el-drawer :title="isEdit ? '编辑系统' : '新增系统'" v-model="dialogVisible" size="500px" direction="rtl">
      <div class="flex flex-col h-full -mx-4 -my-4 p-4">
        <div class="flex-1 overflow-auto pr-2 pb-4">
          <el-form :model="form" label-width="100px" label-position="top">
            <el-form-item label="系统编码" required>
              <el-input v-model="form.systemCode" :disabled="isEdit" placeholder="例如: system_csxt" />
            </el-form-item>
            <el-form-item label="系统名称" required>
              <el-input v-model="form.systemName" placeholder="例如: 财审项目" />
            </el-form-item>
            <el-form-item label="开发工程师">
              <el-input v-model="form.devEngineer" placeholder="请输入开发工程师姓名" />
            </el-form-item>
            <el-form-item label="实施工程师">
              <el-input v-model="form.ssEngineer" placeholder="请输入实施工程师姓名" />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" />
            </el-form-item>
          </el-form>
        </div>
        <div class="pt-4 border-t flex justify-end shrink-0 gap-3">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveSystem" :loading="saving">确定</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
import ProTable from '../../components/ProTable.vue'
import { formatTime } from '../../utils/format'
import * as XLSX from 'xlsx'

const proTable = ref()
const searchForm = ref({ systemCode: '', systemName: '' })
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)

const exportExcel = async () => {
  try {
    const res: any = await request.get('/admin/system/list')
    if (!res || res.length === 0) {
      ElMessage.warning('暂无数据可导出')
      return
    }
    const exportData = res.map((item: any) => ({
      '系统编码': item.systemCode,
      '系统名称': item.systemName,
      '开发工程师': item.devEngineer,
      '实施工程师': item.ssEngineer,
      '创建时间': formatTime(item.createdAt),
      '备注': item.remark
    }))

    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '系统管理列表')
    XLSX.writeFile(workbook, `系统管理_${new Date().getTime()}.xlsx`)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const columns = [
  { prop: 'systemCode', label: '系统编码', width: 150 },
  { prop: 'systemName', label: '系统名称', minWidth: 150 },
  { prop: 'devEngineer', label: '开发工程师', width: 120 },
  { prop: 'ssEngineer', label: '实施工程师', width: 120 },
  { prop: 'createdAt', label: '创建时间', width: 180, slotName: 'timeSlot' },
  { label: '操作', width: 150, slotName: 'actionSlot', fixed: 'right' }
]

const form = ref({
  id: undefined,
  systemCode: '',
  systemName: '',
  devEngineer: '',
  ssEngineer: '',
  remark: ''
})

const getSystems = (params: any) => request.get('/admin/system/page', { params })

const resetForm = (resetFn: Function) => {
  searchForm.value = { systemCode: '', systemName: '' }
  resetFn()
}

const openDialog = (row?: any) => {
  if (row) {
    isEdit.value = true
    form.value = { ...row }
  } else {
    isEdit.value = false
    form.value = { id: undefined, systemCode: '', systemName: '', devEngineer: '', ssEngineer: '', remark: '' }
  }
  dialogVisible.value = true
}

const saveSystem = async () => {
  if (!form.value.systemCode || !form.value.systemName) {
    ElMessage.warning('请填写必填项')
    return
  }
  saving.value = true
  try {
    await request.post('/admin/system/save', form.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    proTable.value.refresh()
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row: any) => {
  try {
    await request.post(`/admin/system/delete/${row.systemCode}`)
    ElMessage.success('删除成功')
    proTable.value.refresh()
  } catch (e) {
    // error handled in interceptor
  }
}
</script>
