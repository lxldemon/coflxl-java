<template>
  <div class="theme-card h-full rounded-lg p-6 shadow-sm flex flex-col">
    <ProTable
        ref="proTable"
        :request-api="getOperLogs"
        :columns="columns"
    >
      <template #search="{ search, reset }">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="模块标题">
            <el-input v-model="searchForm.title" placeholder="如 用户管理" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item label="操作人">
            <el-input v-model="searchForm.operName" placeholder="操作人" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search(searchForm)">查询</el-button>
            <el-button @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
      </template>

      <template #toolbar-left>
        <h2 class="theme-title text-lg font-medium">操作日志</h2>
      </template>

      <template #status="{ row }">
        <el-tag :type="row.status === 1 ? 'success' : 'danger'">
          {{ row.status === 1 ? '成功' : '失败' }}
        </el-tag>
      </template>

      <template #operTime="{ row }">
        {{ new Date(row.operTime).toLocaleString() }}
      </template>

      <template #action="{ row }">
        <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
      </template>
    </ProTable>

    <el-drawer title="日志详情" v-model="drawerVisible" size="800px">
      <el-form label-width="100px" class="pr-6">
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="模块标题：">{{ detailRow.title }}</el-form-item>
          <el-form-item label="业务类型：">{{ detailRow.businessType }}</el-form-item>
          <el-form-item label="请求方式：">{{ detailRow.requestMethod }}</el-form-item>
          <el-form-item label="操作方法：" class="col-span-2">{{ detailRow.method }}</el-form-item>
          <el-form-item label="操作人员：">{{ detailRow.operName }}</el-form-item>
          <el-form-item label="操作IP：">{{ detailRow.operIp }}</el-form-item>
          <el-form-item label="请求地址：" class="col-span-2">{{ detailRow.operUrl }}</el-form-item>
          <el-form-item label="异常信息：" v-if="detailRow.status === 0" class="col-span-2">
            <el-alert :title="detailRow.errorMsg" type="error" :closable="false" />
          </el-form-item>
          <el-form-item label="操作时间：">{{ detailRow.operTime ? new Date(detailRow.operTime).toLocaleString() : '' }}</el-form-item>
        </div>

        <el-form-item label="操作参数：" class="mt-4">
          <MonacoEditor v-model="detailRow.formattedParam" language="json" read-only height="250px" />
        </el-form-item>
        <el-form-item label="返回参数：" class="mt-4">
          <MonacoEditor v-model="detailRow.formattedResult" language="json" read-only height="250px" />
        </el-form-item>
      </el-form>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import ProTable from '../../components/ProTable.vue'
import MonacoEditor from '../../components/MonacoEditor.vue'
import request from '../../utils/request'

const proTable = ref()
const searchForm = reactive({
  title: '',
  operName: ''
})

const drawerVisible = ref(false)
const detailRow = ref<any>({})

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'title', label: '模块标题' },
  { prop: 'businessType', label: '业务类型', width: 120 },
  { prop: 'requestMethod', label: '请求方式', width: 100 },
  { prop: 'operName', label: '操作人', width: 120 },
  { prop: 'operIp', label: 'IP地址', width: 140 },
  { prop: 'status', label: '操作状态', width: 100, slotName: 'status' },
  { prop: 'operTime', label: '操作时间', width: 180, slotName: 'operTime' },
  { label: '操作', width: 100, fixed: 'right', slotName: 'action' }
]

const getOperLogs = async (params: any) => {
  return await request.get('/admin/sys/operLog/page', { params })
}

const formatJson = (val: string) => {
  if (!val) return ''
  try {
    return JSON.stringify(JSON.parse(val), null, 2)
  } catch (e) {
    return val
  }
}

const viewDetail = (row: any) => {
  detailRow.value = {
    ...row,
    formattedParam: formatJson(row.operParam),
    formattedResult: formatJson(row.jsonResult)
  }
  drawerVisible.value = true
}
</script>
