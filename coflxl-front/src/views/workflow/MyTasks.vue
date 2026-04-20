<template>
  <div class="h-full bg-white flex flex-col p-4 rounded-lg shadow-sm">
    <ProTable ref="proTableRef" :columns="columns" :request-api="requestApi" :init-params="initParams">
      <template #search="{ search, reset }">
        <el-form :inline="true" label-width="80px">
          <el-form-item label="任务名称">
            <el-input v-model="searchParams.taskName" placeholder="节点/任务名称模糊查询" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search(searchParams)">查询</el-button>
            <el-button @click="resetSearch(reset)">重置</el-button>
          </el-form-item>
        </el-form>
      </template>

      <template #toolbar-left>
        <h2 class="text-lg font-medium">我的待办审批</h2>
      </template>
      <template #toolbar-right>
        <el-button type="primary" plain @click="refreshTable">刷新列表</el-button>
      </template>

      <template #taskName="{ row }">
        <strong>{{ row.taskName }}</strong>
      </template>

      <template #taskTime="{ row }">
        {{ new Date(row.taskTime).toLocaleString() }}
      </template>

      <template #action="{ row }">
        <el-button type="primary" link @click="openApproveDialog(row)">办理</el-button>
      </template>
    </ProTable>

    <!-- 审批办理 Drawer -->
    <el-drawer title="办理任务" v-model="approveVisible" size="650px" direction="rtl">
      <div v-if="currentTask" class="flex flex-col h-full -mx-4 -my-4 p-4">
        <div class="flex-1 overflow-auto pr-2 pb-4">
          <el-descriptions title="申请详情" :column="2" border class="mb-6">
            <el-descriptions-item label="申请人">{{ currentTask.starterName }}</el-descriptions-item>
            <el-descriptions-item label="所属表单">{{ currentTask.formName }}</el-descriptions-item>
          </el-descriptions>

          <!-- 动态数据展示 -->
          <h3 class="text-sm font-bold text-gray-800 mb-3 border-l-4 border-blue-500 pl-2">业务表单数据</h3>
          <div class="bg-gray-50 p-4 rounded border mb-6 text-sm">
            <el-row :gutter="20">
              <el-col :span="24" sm="12" v-for="(value, key) in parseDynamicData(currentTask.dataContent, currentTask.schemaJson)" :key="key" class="mb-3">
                <span class="text-gray-500 mr-2">{{ key }}:</span>
                <span class="font-medium text-gray-900">{{ value }}</span>
              </el-col>
            </el-row>
          </div>

          <h3 class="text-sm font-bold text-gray-800 mb-3 border-l-4 border-blue-500 pl-2">审批意见</h3>
          <el-form label-width="0" class="mb-6">
            <el-form-item>
              <el-input v-model="approveComment" type="textarea" :rows="3" placeholder="请输入办理/驳回意见..." resize="none" />
            </el-form-item>
          </el-form>

          <h3 class="text-sm font-bold text-gray-800 mb-3 border-l-4 border-blue-500 pl-2">审批轨迹</h3>
          <div class="bg-white p-4 rounded border mb-2">
            <el-timeline v-if="auditLogs.length > 0" class="mt-2 text-sm">
              <el-timeline-item
                  v-for="(activity, index) in auditLogs"
                  :key="index"
                  :timestamp="new Date(activity.createTime).toLocaleString()"
                  :type="activity.action === 'APPROVE' ? 'success' : (activity.action === 'REJECT' ? 'danger' : 'info')"
              >
                <div class="text-sm">
                  <strong class="text-gray-800">{{ activity.taskName }}</strong> <span class="text-gray-500">- 办理人: </span> {{ activity.assignee }}
                  <div class="mt-2 text-gray-600 bg-gray-50 p-2 rounded w-full border border-gray-100" v-if="activity.comment">
                    {{ activity.action === 'APPROVE' ? '【同意】' : (activity.action === 'REJECT' ? '【驳回】' : '') }}{{ activity.comment }}
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无历史流转记录" :image-size="60"></el-empty>
          </div>
        </div>

        <div class="pt-4 border-t flex justify-end shrink-0 gap-3">
          <el-button @click="approveVisible = false">取消</el-button>
          <el-button type="danger" @click="submitTask('REJECT')">驳回终止</el-button>
          <el-button type="success" @click="submitTask('APPROVE')">同意流转</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import ProTable from '../../components/ProTable.vue'

const proTableRef = ref()

const searchParams = reactive({
  taskName: ''
})

const initParams = {}

const columns = [
  { prop: 'taskId', label: '任务编号', minWidth: 280 },
  { prop: 'formName', label: '业务类型', minWidth: 140 },
  { prop: 'starterName', label: '申请人', minWidth: 120 },
  { prop: 'taskName', label: '当前节点', minWidth: 160, slotName: 'taskName' },
  { prop: 'taskTime', label: '到达时间', minWidth: 180, slotName: 'taskTime' },
  { label: '操作', width: 120, fixed: 'right', slotName: 'action' }
]

const requestApi = async (params: any) => {
  return await request.get('/admin/wf/task/my', { params })
}

const refreshTable = () => {
  proTableRef.value?.refresh()
}

const resetSearch = (resetFn: any) => {
  searchParams.taskName = ''
  resetFn()
}


// ============== 办理流程 ==============
const approveVisible = ref(false)
const currentTask = ref<any>(null)
const approveComment = ref('')
const auditLogs = ref<any[]>([])

const parseDynamicData = (jsonStr: string, schemaStr?: string) => {
  try {
    const data = JSON.parse(jsonStr || '{}')
    let schema = []
    if (schemaStr) {
      try {
        schema = JSON.parse(schemaStr)
      } catch (e) {}
    }

    // 如果存在 schema，尝试将 field 转换为汉字 label
    if (schema && schema.length > 0) {
      const result: Record<string, any> = {}
      for (const [key, value] of Object.entries(data)) {
        const matchedField = schema.find((s: any) => s.field === key)
        const label = matchedField ? matchedField.label : key
        result[label] = value
      }
      return result
    }
    return data
  } catch (e) {
    return { '解析失败': '该数据不是标准的 JSON 格式' }
  }
}

const openApproveDialog = async (row: any) => {
  currentTask.value = row
  approveComment.value = ''

  auditLogs.value = []
  try {
    const res: any = await request.get('/admin/wf/audit/history', { params: { businessId: row.businessId } })
    auditLogs.value = res || []
  } catch(e) {}

  approveVisible.value = true
}

const submitTask = async (outcome: string) => {
  if (outcome === 'REJECT' && !approveComment.value) {
    ElMessage.warning('驳回时请填写审批意见')
    return
  }

  await request.post('/admin/wf/task/complete', {
    taskId: currentTask.value.taskId,
    outcome: outcome,
    comment: approveComment.value || (outcome === 'APPROVE' ? '同意' : '驳回')
  })

  ElMessage.success(outcome === 'APPROVE' ? '已同意流转' : '流程已被驳回终止')
  approveVisible.value = false
  refreshTable()
}
</script>
