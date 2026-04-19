<template>
  <div class="h-full bg-white p-6 rounded-lg shadow-sm flex flex-col">
    <div class="flex justify-between items-center mb-4">
      <h2 class="text-lg font-medium">我的请假申请</h2>
      <el-button type="primary" @click="openDialog">发起请假</el-button>
    </div>

    <el-table :data="latestData" border style="width: 100%" class="flex-1">
      <el-table-column prop="id" label="单号" width="80" />
      <el-table-column prop="reason" label="请假事由" />
      <el-table-column prop="days" label="请假天数" width="100" />
      <el-table-column prop="status" label="审批状态" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.status === 'PENDING'" type="warning">审批中</el-tag>
          <el-tag v-else-if="row.status === 'APPROVED'" type="success">已批准</el-tag>
          <el-tag v-else-if="row.status === 'REJECTED'" type="danger">已驳回</el-tag>
          <el-tag v-else type="info">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="申请时间" width="180">
        <template #default="{ row }">
          {{ new Date(row.createdAt).toLocaleString() }}
        </template>
      </el-table-column>
    </el-table>

    <!-- Dialog -->
    <el-dialog title="发起请假" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="请假事由" prop="reason">
          <el-input v-model="form.reason" type="textarea" placeholder="详细输入请假事由" />
        </el-form-item>
        <el-form-item label="请假天数" prop="days">
          <el-input-number v-model="form.days" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="审批人" prop="assignee">
          <el-select v-model="form.assignee" placeholder="请指定直系审批主管" style="width: 100%">
            <el-option
                v-for="user in userList"
                :key="user.id"
                :label="user.nickname + ' (' + user.username + ')'"
                :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRequest">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const dialogVisible = ref(false)
const latestData = ref([])
const userList = ref<any[]>([])

const formRef = ref()
const form = ref({
  reason: '',
  days: 1,
  assignee: null
})

const rules = {
  reason: [{ required: true, message: '请输入请假事由', trigger: 'blur' }],
  assignee: [{ required: true, message: '请必须指派审批人', trigger: 'change' }]
}

const loadLeaves = async () => {
  const res = await request.get('/admin/wf/leave/my')
  latestData.value = (res || []) as any
}

const loadUsers = async () => {
  const res = await request.get('/admin/wf/users')
  userList.value = (res || []) as any
}

const openDialog = () => {
  dialogVisible.value = true
  form.value = { reason: '', days: 1, assignee: null }
}

const submitRequest = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      await request.post('/admin/wf/leave/start', form.value)
      ElMessage.success('请假发起成功，已流转至指定审批人！')
      dialogVisible.value = false
      loadLeaves()
    }
  })
}

onMounted(() => {
  loadLeaves()
  loadUsers()
})
</script>
