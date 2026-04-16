<template>
  <div class="bg-white p-6 rounded-lg shadow-sm h-full flex flex-col">
    <ProTable ref="proTable" :columns="columns" :requestApi="getCallLogs">
      <template #search="{ search, reset }">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="接口编码">
            <el-input v-model="searchForm.apiCode" placeholder="请输入编码" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search(searchForm)">查询</el-button>
            <el-button @click="resetForm(reset)">重置</el-button>
          </el-form-item>
        </el-form>
      </template>

      <template #toolbar-left>
        <h2 class="text-lg font-medium">调用日志</h2>
      </template>

      <template #successSlot="{ row }">
        <el-tag :type="row.successFlag ? 'success' : 'danger'">
          {{ row.successFlag ? '成功' : '失败' }}
        </el-tag>
      </template>

      <template #costSlot="{ row }">
        <span>{{ row.costMs }} ms</span>
      </template>

      <template #timeSlot="{ row }">
        <span>{{ formatTime(row.createdAt) }}</span>
      </template>
    </ProTable>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import request from '../../utils/request.ts'
import ProTable from '../../components/ProTable.vue'
import { formatTime } from '../../utils/format.ts'

const proTable = ref()
const searchForm = ref({ apiCode: '' })

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'apiCode', label: '接口编码', width: 150 },
  { prop: 'requestParams', label: '请求参数', showOverflowTooltip: true },
  { prop: 'responseCode', label: '响应码', width: 100 },
  { prop: 'responseMessage', label: '响应信息', showOverflowTooltip: true },
  { prop: 'costMs', label: '耗时', width: 100, slotName: 'costSlot' },
  { prop: 'successFlag', label: '状态', width: 100, slotName: 'successSlot' },
  { prop: 'clientIp', label: '客户端IP', width: 150 },
  { prop: 'createdAt', label: '调用时间', width: 180, slotName: 'timeSlot' }
]

const getCallLogs = (params: any) => request.get('/admin/log/page', { params })

const resetForm = (resetFn: Function) => {
  searchForm.value = { apiCode: '' }
  resetFn()
}
</script>
