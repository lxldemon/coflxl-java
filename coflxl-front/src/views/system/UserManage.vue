<template>
  <div class="bg-white p-6 rounded-lg shadow-sm h-full flex flex-col">
    <ProTable ref="proTable" :columns="columns" :requestApi="getUsers">
      <template #search="{ search, reset }">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="用户名">
            <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search(searchForm)">查询</el-button>
            <el-button @click="resetForm(reset)">重置</el-button>
          </el-form-item>
        </el-form>
      </template>

      <template #toolbar-left>
        <h2 class="text-lg font-medium">用户管理</h2>
      </template>

      <template #toolbar-right>
        <el-button v-permission="'sys:user:export'" type="success" @click="exportExcel" plain>导出 Excel</el-button>
        <el-button v-permission="'sys:user:add'" type="primary" @click="handleAdd">新增用户</el-button>
      </template>

      <template #statusSlot="{ row }">
        <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status === 'ACTIVE' ? '正常' : '禁用' }}</el-tag>
      </template>

      <template #actionSlot="{ row }">
        <el-button v-permission="'sys:user:update'" type="primary" link @click="handleEdit(row)">编辑</el-button>
        <el-button v-permission="'sys:user:assign_role'" type="primary" link @click="handleAssignRole(row)">分配角色</el-button>
        <el-button v-permission="'sys:user:delete'" type="danger" link @click="handleDelete(row)">删除</el-button>
      </template>
    </ProTable>

    <el-dialog :title="dialogType === 'add' ? '新增用户' : '编辑用户'" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码(不修改请留空)" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="ACTIVE">正常</el-radio>
            <el-radio label="INACTIVE">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" title="分配角色" width="400px">
      <el-form>
        <el-form-item label="选择角色">
          <el-select v-model="selectedRoleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option
                v-for="item in allRoles"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRoles">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import ProTable from '../../components/ProTable.vue'
import * as XLSX from 'xlsx'

const proTable = ref()
const searchForm = ref({ username: '' })

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'username', label: '用户名' },
  { prop: 'nickname', label: '昵称' },
  { prop: 'status', label: '状态', slotName: 'statusSlot' },
  { prop: 'action', label: '操作', width: 220, slotName: 'actionSlot' }
]

const exportExcel = async () => {
  try {
    const res: any = await request.get('/admin/sys/user/page', { params: { pageNo: 1, pageSize: 10000, ...searchForm.value } })
    if (!res || !res.rows || res.rows.length === 0) {
      ElMessage.warning('暂无数据可导出')
      return
    }
    const exportData = res.rows.map((item: any) => ({
      'ID': item.id,
      '用户名': item.username,
      '昵称': item.nickname,
      '状态': item.status === 'ACTIVE' ? '正常' : '禁用',
      '创建时间': item.createdAt
    }))

    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '用户列表')
    XLSX.writeFile(workbook, `用户列表_${new Date().getTime()}.xlsx`)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const getUsers = (params: any) => request.get('/admin/sys/user/page', { params })

const resetForm = (resetFn: Function) => {
  searchForm.value = { username: '' }
  resetFn()
}

// User CRUD
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref()
const form = ref({
  id: null,
  username: '',
  nickname: '',
  password: '',
  status: 'ACTIVE'
})
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

const handleAdd = () => {
  dialogType.value = 'add'
  form.value = { id: null, username: '', nickname: '', password: '', status: 'ACTIVE' }
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  form.value = { ...row, password: '' }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      await request.post('/admin/sys/user/save', form.value)
      ElMessage.success('保存成功')
      dialogVisible.value = false
      proTable.value.refresh()
    }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该用户吗?', '提示', { type: 'warning' }).then(async () => {
    await request.post(`/admin/sys/user/delete/${row.id}`)
    ElMessage.success('删除成功')
    proTable.value.refresh()
  }).catch(() => {})
}

// Role Assignment
const roleDialogVisible = ref(false)
const currentUser = ref<any>(null)
const selectedRoleIds = ref<number[]>([])
const allRoles = ref<any[]>([])

const fetchRoles = async () => {
  const res = await request.get('/admin/sys/role/list')
  allRoles.value = res as any
}

const handleAssignRole = async (row: any) => {
  currentUser.value = row
  const res: any = await request.get('/admin/sys/user/roles', { params: { userId: row.id } })
  selectedRoleIds.value = res.map((r: any) => r.roleId)
  roleDialogVisible.value = true
}

const submitRoles = async () => {
  if (!currentUser.value) return
  await request.post('/admin/sys/user/assignRoles', {
    userId: currentUser.value.id,
    roleIds: selectedRoleIds.value
  })
  ElMessage.success('角色分配成功')
  roleDialogVisible.value = false
}

onMounted(() => {
  fetchRoles()
})
</script>
