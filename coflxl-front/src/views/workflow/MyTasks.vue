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

    <!-- 审批办理 Dialog -->
    <el-dialog title="办理任务" v-model="approveVisible" width="600px">
      <div v-if="currentTask">
        <el-descriptions title="申请详情" :column="2" border class="mb-4">
          <el-descriptions-item label="申请人">{{ currentTask.starterName }}</el-descriptions-item>
          <el-descriptions-item label="所属表单">{{ currentTask.formName }}</el-descriptions-item>
        </el-descriptions>

        <!-- 动态数据展示 -->
        <h3 class="text-sm font-medium text-gray-500 mb-2">业务表单数据</h3>
        <div class="bg-gray-50 p-4 rounded border mb-6">
          <el-row :gutter="20">
            <el-col :span="12" v-for="(value, key) in parseDynamicData(currentTask.dataContent)" :key="key" class="mb-2">
              <span class="text-gray-500 mr-2">{{ key }}:</span>
              <span class="font-medium">{{ value }}</span>
            </el-col>
          </el-row>
        </div>

        <el-form label-width="80px">
          <el-form-item label="审批意见">
            <el-input v-model="approveComment" type="textarea" :rows="3" placeholder="请输入办理/驳回意见..." />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="danger" @click="submitTask('REJECT')">驳回终止</el-button>
        <el-button type="success" @click="submitTask('APPROVE')">同意流转</el-button>
      </template>
    </el-dialog>
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
  { prop: 'taskId', label: '任务编号', width: 90 },
  { prop: 'formName', label: '业务类型', width: 140 },
  { prop: 'starterName', label: '申请人', width: 100 },
  { prop: 'taskName', label: '当前节点', width: 150, slotName: 'taskName' },
  { prop: 'taskTime', label: '到达时间', width: 180, slotName: 'taskTime' },
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

const parseDynamicData = (jsonStr: string) => {
  try {
    return JSON.parse(jsonStr)
  } catch (e) {
    return { '解析失败': '该数据不是标准的 JSON 格式' }
  }
}

const openApproveDialog = (row: any) => {
  currentTask.value = row
  approveComment.value = ''
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
