<template>
  <div class="bg-white p-6 rounded-lg shadow-sm h-full flex flex-col">
    <ProTable ref="proTable" :columns="columns" :requestApi="getTemplates">
      <template #search="{ search, reset }">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="模板名称">
            <el-input v-model="searchForm.name" placeholder="请输入名称" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search(searchForm)">查询</el-button>
            <el-button @click="resetForm(reset)">重置</el-button>
          </el-form-item>
        </el-form>
      </template>

      <template #toolbar-left>
        <h2 class="text-lg font-medium">报表模板管理</h2>
      </template>
      <template #toolbar-right>
        <el-button type="primary" @click="goToDesigner()">新建模板</el-button>
      </template>

      <template #actionSlot="{ row }">
        <el-button link type="primary" size="small" @click="goToDesigner(row)">设计 / 编辑</el-button>
        <el-popconfirm title="确定删除吗？" @confirm="handleDelete(row)">
          <template #reference>
            <el-button link type="danger" size="small">删除</el-button>
          </template>
        </el-popconfirm>
      </template>
    </ProTable>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
import ProTable from '../../components/ProTable.vue'

const router = useRouter()
const proTable = ref()
const searchForm = ref({ name: '' })

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'name', label: '模板名称', width: 200 },
  { prop: 'description', label: '描述', width: 300 },
  { prop: 'category', label: '分类', width: 150 },
  { prop: 'createdAt', label: '创建时间', width: 180 },
  { label: '操作', width: 200, slotName: 'actionSlot', fixed: 'right' }
]

const getTemplates = (params: any) => request.get('/admin/report/template/page', { params })

const resetForm = (resetFn: Function) => {
  searchForm.value = { name: '' }
  resetFn()
}

const goToDesigner = (row?: any) => {
  if (row) {
    router.push({ path: '/report-designer', query: { id: row.id } })
  } else {
    router.push({ path: '/report-designer' })
  }
}

const handleDelete = async (row: any) => {
  try {
    await request.post(`/admin/report/template/delete/${row.id}`)
    ElMessage.success('删除成功')
    proTable.value.refresh()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}
</script>
