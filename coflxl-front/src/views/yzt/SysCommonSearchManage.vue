<template>
  <div class="p-6">
    <ProTable
        ref="proTable"
        :request-api="getSysCommonSearchList"
        :columns="columns"
    >
      <template #search="{ search, reset }">
        <el-form :inline="true" :model="searchForm" class="flex flex-wrap gap-2">
          <el-form-item label="sqlId">
            <el-input v-model="searchForm.sqlId" placeholder="请输入sqlId" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item label="SQL描述">
            <el-input v-model="searchForm.sqlDesc" placeholder="请输入SQL描述" clearable style="width: 200px" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="search(searchForm)">查询</el-button>
            <el-button @click="resetForm(reset)">重置</el-button>
          </el-form-item>
        </el-form>
      </template>
      <template #toolbar-left>
        <h2 class="text-lg font-medium">SQL公共查询</h2>
      </template>
      <template #toolbar-right>
        <el-button type="primary" @click="openDialog()">新增</el-button>
      </template>
      <template #actionSlot="scope">
        <el-button type="primary" link size="small" @click="openDialog(scope.row)">编辑</el-button>
        <el-popconfirm title="确定删除吗？" @confirm="handleDelete(scope.row)">
          <template #reference>
            <el-button type="danger" link size="small">删除</el-button>
          </template>
        </el-popconfirm>
      </template>
      <template #timeSlot="{ row }">
        <span>{{ formatTime(row.createTime) }}</span>
      </template>

      <!-- 新增：SQL内容展示插槽 -->
      <template #sqlContentSlot="{ row }">
        <el-button
            type="primary"
            link
            size="small"
            @click="handleViewSql(row)"
        >
          &lt;/&gt;
        </el-button>
      </template>

    </ProTable>

    <el-drawer :title="isEdit ? '编辑' : '新增'" v-model="dialogVisible" size="700px" direction="rtl">
      <div class="flex flex-col h-full -mx-4 -my-4 p-4">
        <div class="flex-1 overflow-auto pr-2 pb-4">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="sqlId:">
          <el-input v-model="formData.sqlId" placeholder="请输入sqlId"  :disabled="true" />
        </el-form-item>
        <el-form-item label="sql描述:">
          <el-input v-model="formData.sqlDesc" placeholder="请输入sqlDesc" />
        </el-form-item>
        <el-form-item label="sql内容:">
          <MonacoEditor
              v-model="formData.sqlContent"
              language="sql"
              height="400px"
              theme="vs-dark"
              :readOnly="false"
          />
        </el-form-item>


      </el-form>

        </div>
        <div class="pt-4 border-t flex justify-end shrink-0 gap-3">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="saving">确定</el-button>
        </div>
      </div>
    </el-drawer>

    <el-dialog
        v-model="sqlViewVisible"
        title="SQL内容"
        width="900px"
        destroy-on-close
    >
      <MonacoEditor
          v-model="currentSqlContent"
          language="sql"
          height="600px"
          theme="vs-dark"
          :readOnly="true"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
import ProTable from '../../components/ProTable.vue'
import { formatTime } from '../../utils/format.ts'
import MonacoEditor from "@/components/MonacoEditor.vue";
const proTable = ref()
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formData = ref<any>({})
const searchForm = ref<any>({})

const sqlViewVisible = ref(false)
const currentSqlContent = ref('')

const handleViewSql = (row: any) => {
  currentSqlContent.value = row.sqlContent || ''
  sqlViewVisible.value = true
}

const columns = [
  { prop: 'sqlId', label: 'sqlId' },
  { prop: 'sqlDesc', label: 'sql描述', width:500},
  { prop: 'sqlContent', label: 'SQL内容',align: 'center', width: 120,    slotName: 'sqlContentSlot'  },
  { prop: 'createTime', label: 'SQL创建时间', slotName: 'timeSlot' },
  { prop: 'updateTime', label: 'SQL更新时间', slotName: 'timeSlot' },
  { prop: 'actionSlot', label: '操作', width: 150, fixed: 'right', slotName: 'actionSlot' }
]

const resetForm = (reset: any) => {
  searchForm.value = {}
  reset()
}

const getSysCommonSearchList = (params: any) => {
  // ProTable 默认传 current 和 size，需要转换为 pageNo 和 pageSize
  const queryParams = {
    ...params,
    pageNo: params.current || params.pageNo || 1,
    pageSize: params.size || params.pageSize || 10
  }
  return request.get('/admin/SYS-COMMON-SEARCH/page', { params: queryParams }).then((res: any) => {
    // 适配 ProTable 的返回格式
    return {
      rows: res.rows || [],
      recordCount: res.recordCount || 0
    }
  })
}

const openDialog = (row?: any) => {
  isEdit.value = !!row
  formData.value = row ? { ...row } : {}
  dialogVisible.value = true
}

const handleSubmit = async () => {
  saving.value = true
  try {
    await request.post('/admin/SYS-COMMON-SEARCH/save', formData.value)
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    proTable.value?.refresh()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row: any) => {
  try {
    await request.post(`/admin/SYS-COMMON-SEARCH/delete/${row.sqlId}`)
    ElMessage.success('删除成功')
    proTable.value?.refresh()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}
</script>
