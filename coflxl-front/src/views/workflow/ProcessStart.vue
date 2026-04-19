<template>
  <div class="h-full bg-white flex flex-col p-4 rounded-lg shadow-sm">
    <ProTable ref="proTableRef" :columns="columns" :request-api="requestApi" :init-params="initParams">

      <template #search="{ search, reset }">
        <el-form :inline="true" label-width="80px">
          <el-form-item label="业务类型">
            <el-input v-model="searchParams.formName" placeholder="如: 请假单" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search(searchParams)">查询</el-button>
            <el-button @click="resetSearch(reset)">重置</el-button>
          </el-form-item>
        </el-form>
      </template>

      <template #toolbar-left>
        <h2 class="text-lg font-medium">发起流程与流转记录</h2>
      </template>
      <template #toolbar-right>
        <el-button type="primary" @click="openDialog">发起新流程</el-button>
      </template>

      <template #status="{ row }">
        <el-tag v-if="row.status === 'RUNNING'" type="warning">流转中</el-tag>
        <el-tag v-else-if="row.status === 'COMPLETED'" type="success">已批准结束</el-tag>
        <el-tag v-else-if="row.status === 'REJECTED'" type="danger">已驳回</el-tag>
        <el-tag v-else type="info">{{ row.status }}</el-tag>
      </template>

      <template #createTime="{ row }">
        {{ new Date(row.createTime).toLocaleString() }}
      </template>

      <template #action="{ row }">
        <el-button link type="primary" @click="viewProcess(row)">查看执行图</el-button>
        <el-button link type="info" @click="viewAudit(row)">审批轨迹</el-button>
      </template>
    </ProTable>

    <!-- 发起流程 Dialog -->
    <el-dialog title="发起流程单" v-model="dialogVisible" width="550px" @closed="closeDialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="所属流程" prop="defId">
          <el-select v-model="form.defId" placeholder="挂靠流程引擎模板" style="width: 100%">
            <el-option
                v-for="def in activeDefs"
                :key="def.id"
                :label="def.name + (def.typeCode ? (' - [' + def.typeCode + ']') : '')"
                :value="def.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="表单类型" prop="formId">
          <el-select v-model="form.formId" placeholder="选择业务数据表单" style="width: 100%" @change="onFormChange">
            <el-option
                v-for="f in formDefs"
                :key="f.id"
                :label="f.name"
                :value="f.id"
            />
          </el-select>
        </el-form-item>

        <!-- Dynamic Form Fields -->
        <div v-if="activeSchema.length > 0" class="mt-4 p-4 bg-gray-50 rounded-md">
          <h3 class="text-sm font-medium text-gray-500 mb-4 pb-2 border-b">动态业务字段</h3>
          <el-form-item
              v-for="field in activeSchema"
              :key="field.field"
              :label="field.label"
              :prop="'dynamicData.' + field.field"
              :rules="{ required: true, message: '此项必填', trigger: 'blur' }"
          >
            <el-input v-if="field.type === 'text'" v-model="form.dynamicData[field.field]" :placeholder="'请输入' + field.label" />
            <el-input-number v-else-if="field.type === 'number'" v-model="form.dynamicData[field.field]" controls-position="right" class="w-full" />
            <el-date-picker v-else-if="field.type === 'date'" v-model="form.dynamicData[field.field]" type="date" value-format="YYYY-MM-DD" class="w-full !flex" />
            <el-input v-else v-model="form.dynamicData[field.field]" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRequest" :loading="submitting">确认发起</el-button>
      </template>
    </el-dialog>

    <!-- 流转图 Dialog -->
    <el-dialog title="流程在线轨迹图" v-model="mapVisible" width="800px" destroy-on-close>
      <div class="h-[500px] border border-gray-200 rounded relative" v-loading="mapLoading">
        <div id="bpmn-canvas" class="w-full h-full"></div>
      </div>
    </el-dialog>

    <!-- 审批历史 Dialog -->
    <el-dialog title="全链路审批轨迹" v-model="auditVisible" width="600px">
      <el-timeline v-if="auditLogs.length > 0">
        <el-timeline-item
            v-for="(activity, index) in auditLogs"
            :key="index"
            :timestamp="new Date(activity.createTime).toLocaleString()"
            :type="activity.action === 'APPROVE' ? 'success' : (activity.action === 'REJECT' ? 'danger' : 'primary')"
        >
          <div class="font-medium text-gray-800">{{ activity.taskName || '系统任务' }} <el-tag size="small" class="ml-2">{{ activity.action === 'APPROVE' ? '已同意' : (activity.action === 'REJECT' ? '被驳回' : activity.action) }}</el-tag></div>
          <div class="text-sm text-gray-500 mt-1">处理人: {{ activity.assignee || '无' }}</div>
          <div class="text-sm text-gray-700 bg-gray-50 p-2 mt-2 rounded border border-gray-100" v-if="activity.comment">
            意见: {{ activity.comment }}
          </div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无历史流转记录"></el-empty>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import ProTable from '../../components/ProTable.vue'
import BpmnViewer from 'bpmn-js/lib/NavigatedViewer'

// 忽略原生高亮，我们使用自己的 css
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'

const proTableRef = ref()

const searchParams = reactive({
  formName: ''
})

const initParams = {}

const columns = [
  { prop: 'businessId', label: '业务单号', width: 280 },
  { prop: 'formName', label: '表单名称', width: 140 },
  { prop: 'createTime', label: '申请时间', width: 180, slotName: 'createTime' },
  { prop: 'status', label: '实例状态', width: 120, slotName: 'status' },
  { prop: 'action', label: '追踪 / 日志', minWidth: 200, slotName: 'action' }
]

const requestApi = async (params: any) => {
  return await request.get('/admin/wf/process/my', { params })
}

const resetSearch = (resetFn: any) => {
  searchParams.formName = ''
  resetFn()
}


const dialogVisible = ref(false)
const submitting = ref(false)
const activeDefs = ref<any[]>([])
const formDefs = ref<any[]>([])

const formRef = ref()
const form = ref<any>({
  defId: null,
  formId: null,
  dynamicData: {}
})
const activeSchema = ref<any[]>([])

const rules = {
  defId: [{ required: true, message: '请选择流程模型', trigger: 'change' }],
  formId: [{ required: true, message: '请选择业务表单结构', trigger: 'change' }]
}

const loadActiveDefs = async () => {
  const res = await request.get('/admin/wf/def/active')
  activeDefs.value = (res || []) as any
  const res2 = await request.get('/admin/wf/form/list')
  formDefs.value = (res2 || []) as any
}

const openDialog = () => {
  dialogVisible.value = true
  loadActiveDefs()
  form.value = { defId: null, formId: null, dynamicData: {} }
  activeSchema.value = []
}

const onFormChange = (val: string) => {
  form.value.dynamicData = {}
  const match = formDefs.value.find(f => f.id === val)
  if (match && match.schemaJson) {
    try {
      activeSchema.value = JSON.parse(match.schemaJson)
    } catch(e) {
      activeSchema.value = []
    }
  } else {
    activeSchema.value = []
  }
}

const closeDialog = () => {
  form.value = { defId: null, formId: null, dynamicData: {} }
  activeSchema.value = []
}

const submitRequest = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitting.value = true
      try {
        const payload = {
          defId: form.value.defId,
          formId: form.value.formId,
          dataContent: form.value.dynamicData
        }
        await request.post('/admin/wf/process/start', payload)
        ElMessage.success('流转发起成功！表单已进入流转中心！')
        dialogVisible.value = false
        proTableRef.value?.refresh()
      } finally {
        submitting.value = false
      }
    }
  })
}

// ============== 审计追踪 ==============
const auditVisible = ref(false)
const auditLogs = ref<any[]>([])

const viewAudit = async (row: any) => {
  auditLogs.value = []
  auditVisible.value = true
  const res = await request.get('/admin/wf/audit/history', { params: { businessId: row.businessId } })
  auditLogs.value = res || []
}

// ============== 流程流转高亮 ==============
const mapVisible = ref(false)
const mapLoading = ref(false)
let viewer: any = null

const viewProcess = async (row: any) => {
  mapVisible.value = true
  mapLoading.value = true
  try {
    const res = await request.get('/admin/wf/process/viewer', { params: { procInsId: row.procInsId } });
    await nextTick()
    if (viewer) {
      viewer.destroy()
    }
    const canvas = document.getElementById('bpmn-canvas')
    viewer = new BpmnViewer({ container: canvas! })
    await viewer.importXML(res.xml)

    // 自适应并且加入高亮
    const canvasObj = viewer.get('canvas')
    canvasObj.zoom('fit-viewport')

    const overlays = viewer.get('overlays')
    const elementRegistry = viewer.get('elementRegistry')

        // 给活动节点加发光动画 (用红色代替)
    ;(res.activeNodes || []).forEach((id: string) => {
      if (elementRegistry.get(id)) {
        overlays.add(id, 'note', {
          position: { bottom: 0, right: 0 },
          html: `<div style="background:#52c41a;color:#fff;font-size:10px;padding:2px 4px;border-radius:4px;white-space:nowrap;">当前停留节点</div>`
        })
        canvasObj.addMarker(id, 'highlight-active')
      }
    })

    // 给历史节点打标记
    ;(res.completedNodes || []).forEach((id: string) => {
      if (elementRegistry.get(id)) {
        canvasObj.addMarker(id, 'highlight-completed')
      }
    })
  } catch(e) {
    ElMessage.error("渲染出错了")
  } finally {
    mapLoading.value = false
  }
}
</script>

<style>
/* 流程高亮全局样式 */
.highlight-active .djs-visual > :nth-child(1) {
  stroke: #52c41a !important; /* 绿色发光 */
  stroke-width: 3px !important;
  fill: #f6ffed !important;
}
.highlight-completed .djs-visual > :nth-child(1) {
  stroke: #1890ff !important;
  stroke-width: 2px !important;
  fill: #e6f7ff !important;
}
.highlight-active .djs-connection > path {
  stroke: #52c41a !important;
  stroke-width: 2px !important;
  animation: dash 1.5s linear infinite;
  stroke-dasharray: 4, 4;
}

@keyframes dash {
  from { stroke-dashoffset: 8; }
  to { stroke-dashoffset: 0; }
}
</style>
