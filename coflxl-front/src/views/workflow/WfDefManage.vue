<template>
  <div class="h-full bg-white flex flex-col p-4 rounded-lg shadow-sm">
    <ProTable ref="proTableRef" :columns="columns" :request-api="requestApi" :init-params="initParams">
      <template #search="{ search, reset }">
        <el-form :inline="true" label-width="80px">
          <el-form-item label="流程名称">
            <el-input v-model="searchParams.name" placeholder="名称模糊查询" clearable />
          </el-form-item>
          <el-form-item label="业务类型">
            <el-input v-model="searchParams.typeCode" placeholder="如 LEAVE" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search(searchParams)">查询</el-button>
            <el-button @click="resetSearch(reset)">重置</el-button>
          </el-form-item>
        </el-form>
      </template>

      <template #toolbar-left>
        <h2 class="text-lg font-medium">流程定义管理</h2>
      </template>
      <template #toolbar-right>
        <el-button type="primary" @click="handleCreate">新建流程</el-button>
      </template>

      <template #status="{ row }">
        <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
          {{ row.status === 'ACTIVE' ? '已发布' : '草稿' }}
        </el-tag>
      </template>

      <template #createdAt="{ row }">
        {{ new Date(row.createdAt).toLocaleString() }}
      </template>

      <template #action="{ row }">
        <el-button link type="primary" @click="handleEdit(row)">设计流程</el-button>
        <el-button
            link
            :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
            @click="togglePublish(row)"
        >
          {{ row.status === 'ACTIVE' ? '下线' : '发布' }}
        </el-button>
        <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
      </template>
    </ProTable>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import ProTable from '../../components/ProTable.vue'

const router = useRouter()
const proTableRef = ref()

const searchParams = reactive({
  name: '',
  typeCode: ''
})

const initParams = {}

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'name', label: '流程名称' },
  { prop: 'typeCode', label: '业务类型编码', width: 150 },
  { prop: 'status', label: '状态', width: 100, slotName: 'status' },
  { prop: 'createdAt', label: '创建时间', width: 180, slotName: 'createdAt' },
  { label: '操作', width: 250, fixed: 'right', slotName: 'action' }
]

const requestApi = async (params: any) => {
  return await request.get('/admin/wf/def/list', { params })
}

const resetSearch = (resetFn: any) => {
  searchParams.name = ''
  searchParams.typeCode = ''
  resetFn()
}

const handleCreate = () => {
  router.push('/workflow/designer')
}

const handleEdit = (row: any) => {
  router.push({ path: '/workflow/designer', query: { id: row.id } })
}

const togglePublish = async (row: any) => {
  const targetStatus = row.status === 'ACTIVE' ? 'DRAFT' : 'ACTIVE'
  const actionName = targetStatus === 'ACTIVE' ? '发布' : '下线'

  ElMessageBox.confirm(`确定要${actionName}该流程吗?`, '提示', { type: 'warning' }).then(async () => {
    await request.post('/admin/wf/def/publish', {
      id: row.id,
      status: targetStatus
    })
    ElMessage.success(`${actionName}成功`)
    proTableRef.value?.refresh()
  }).catch(() => {})
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该流程定义吗? 删除后无法恢复', '警告', { type: 'error' }).then(async () => {
    await request.post(`/admin/wf/def/delete/${row.id}`)
    ElMessage.success('删除成功')
    proTableRef.value?.refresh()
  }).catch(() => {})
}
</script>
