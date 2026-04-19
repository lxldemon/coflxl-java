<template>
  <div class="h-full bg-white p-6 rounded-lg shadow-sm flex flex-col">
    <div class="flex justify-between items-center mb-4">
      <h2 class="text-lg font-medium">流程定义管理</h2>
      <el-button type="primary" @click="handleCreate">新建流程</el-button>
    </div>

    <el-table :data="tableData" border style="width: 100%" class="flex-1">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="流程名称" />
      <el-table-column prop="typeCode" label="业务类型编码" width="150" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
            {{ row.status === 'ACTIVE' ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">
          {{ new Date(row.createdAt).toLocaleString() }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
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
      </el-table-column>
    </el-table>

    <div class="mt-4 flex justify-end">
      <el-pagination
          v-model:current-page="page.pageNo"
          v-model:page-size="page.pageSize"
          :total="page.recordCount"
          layout="total, prev, pager, next"
          @current-change="loadData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const router = useRouter()
const tableData = ref([])
const page = ref({
  pageNo: 1,
  pageSize: 10,
  recordCount: 0
})

const loadData = async () => {
  const res: any = await request.get('/admin/wf/def/list', {
    params: { pageNo: page.value.pageNo, pageSize: page.value.pageSize }
  })
  tableData.value = res.rows || []
  page.value.recordCount = res.recordCount || 0
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
    loadData()
  }).catch(() => {})
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该流程定义吗? 删除后无法恢复', '警告', { type: 'error' }).then(async () => {
    await request.post(`/admin/wf/def/delete/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>
