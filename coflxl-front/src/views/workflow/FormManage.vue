<template>
  <div class="h-full bg-white flex flex-col p-4 rounded-lg shadow-sm">
    <ProTable ref="proTableRef" :columns="columns" :request-api="requestApi" :show-pagination="false">
      <template #toolbar-left>
        <h2 class="text-lg font-medium">发起流程表单(JSON)管理</h2>
      </template>
      <template #toolbar-right>
        <el-button type="primary" @click="openDialog()">新建表单</el-button>
      </template>

      <template #action="{ row }">
        <el-button link type="primary" @click="openDialog(row)">编辑设计</el-button>
        <el-popconfirm title="确定要删除该表单吗?" @confirm="handleDelete(row)">
          <template #reference>
            <el-button link type="danger">删除</el-button>
          </template>
        </el-popconfirm>
      </template>
    </ProTable>

    <!-- Drawer for Editing Schema JSON -->
    <el-drawer :title="isEdit ? '编辑流程表单' : '新增流程表单'" v-model="dialogVisible" size="700px" direction="rtl" destroy-on-close>
      <div class="flex flex-col h-full -mx-4 -my-4 p-4">
        <div class="flex-1 overflow-visible pr-2 pb-4 flex flex-col">
          <el-form :model="form" ref="formRef" :rules="rules" label-width="100px" label-position="top">
            <el-form-item label="表单名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入表单名称" />
            </el-form-item>
          </el-form>
          <div class="flex-1 flex flex-col min-h-0 border border-gray-200 mt-2">
            <div class="p-2 border-b border-gray-200 bg-gray-50 text-sm font-medium text-gray-700 flex justify-between">
              <span>界面结构设计 (JSON Schema)</span>
              <el-button link type="primary" size="small" @click="addTemplate">插入范例字段</el-button>
            </div>
            <div class="flex-1 relative">
              <MonacoEditor
                  v-model="form.schemaJson"
                  language="json"
                  height="100%"
                  theme="vs-light"
              />
            </div>
          </div>
        </div>
        <div class="pt-4 border-t flex justify-end shrink-0 gap-3">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveForm" :loading="saving">保存配置</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import ProTable from '../../components/ProTable.vue'
import MonacoEditor from '../../components/MonacoEditor.vue'

const proTableRef = ref()
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref()

const form = ref({
  id: null as number | null,
  name: '',
  schemaJson: '[\n  {\n    "label": "申请人",\n    "field": "applicant",\n    "type": "text"\n  }\n]'
})

const rules = {
  name: [{ required: true, message: '请输入表单名称', trigger: 'blur' }]
}

const columns = [
  { prop: 'id', label: 'ID', width: 120 },
  { prop: 'name', label: '业务单名称', width: 250 },
  { prop: 'schemaJson', label: 'JSON Schema (预览)', showOverflowTooltip: true },
  { label: '操作', width: 150, fixed: 'right', slotName: 'action' }
]

const requestApi = async () => {
  return await request.get('/admin/wf/form/list')
}

const openDialog = (row?: any) => {
  dialogVisible.value = true
  isEdit.value = !!row
  if (row) {
    form.value = {
      id: row.id,
      name: row.name,
      schemaJson: row.schemaJson || '[]'
    }
  } else {
    form.value = {
      id: null,
      name: '',
      schemaJson: '[\n  {\n    "label": "申请人",\n    "field": "applicant",\n    "type": "text"\n  }\n]'
    }
  }
}

const addTemplate = () => {
  const tpl = JSON.stringify([
    { "label": "请假类型", "field": "leaveType", "type": "text" },
    { "label": "请假天数", "field": "days", "type": "number" },
    { "label": "开始日期", "field": "startDate", "type": "date" }
  ], null, 2)
  form.value.schemaJson = tpl
}

const saveForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        // Validate JSON
        JSON.parse(form.value.schemaJson)
      } catch (err) {
        ElMessage.error('JSON 格式不合法!')
        return
      }

      saving.value = true
      try {
        await request.post('/admin/wf/form/save', form.value)
        ElMessage.success('保存成功')
        dialogVisible.value = false
        proTableRef.value?.refresh()
      } finally {
        saving.value = false
      }
    }
  })
}

const handleDelete = async (row: any) => {
  try {
    await request.post(`/admin/wf/form/delete/${row.id}`)
    ElMessage.success('删除成功')
    proTableRef.value?.refresh()
  } catch(e) {}
}
</script>

<style scoped>
</style>