<template>
  <div class="bg-white p-6 rounded-lg shadow-sm h-full flex flex-col">
    <ProTable ref="proTable" :columns="columns" :requestApi="getRoles">
      <template #search="{ search, reset }">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="角色名">
            <el-input v-model="searchForm.name" placeholder="请输入角色名" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search(searchForm)">查询</el-button>
            <el-button @click="resetForm(reset)">重置</el-button>
          </el-form-item>
        </el-form>
      </template>

      <template #toolbar-left>
        <h2 class="text-lg font-medium">角色管理</h2>
      </template>

      <template #toolbar-right>
        <el-button type="primary" @click="handleAdd">新增角色</el-button>
      </template>

      <template #statusSlot="{ row }">
        <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status === 'ACTIVE' ? '正常' : '禁用' }}</el-tag>
      </template>

      <template #actionSlot="{ row }">
        <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
        <el-button type="primary" link @click="handleAssignMenu(row)">分配菜单</el-button>
        <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
      </template>
    </ProTable>

    <el-dialog :title="dialogType === 'add' ? '新增角色' : '编辑角色'" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入角色编码" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
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

    <el-dialog v-model="menuDialogVisible" title="分配菜单" width="500px">
      <div style="height: 400px; overflow-y: auto;">
        <el-tree
            ref="menuTree"
            :data="allMenus"
            show-checkbox
            node-key="id"
            :props="{ label: 'name', children: 'children' }"
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="menuDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitMenus">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import ProTable from '../../components/ProTable.vue'

const proTable = ref()
const searchForm = ref({ name: '' })

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'code', label: '角色编码' },
  { prop: 'name', label: '角色名称' },
  { prop: 'status', label: '状态', slotName: 'statusSlot' },
  { prop: 'action', label: '操作', width: 220, slotName: 'actionSlot' }
]

const getRoles = (params: any) => request.get('/admin/sys/role/page', { params })

const resetForm = (resetFn: Function) => {
  searchForm.value = { name: '' }
  resetFn()
}

// Role CRUD
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref()
const form = ref({
  id: null,
  code: '',
  name: '',
  status: 'ACTIVE'
})
const rules = {
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const handleAdd = () => {
  dialogType.value = 'add'
  form.value = { id: null, code: '', name: '', status: 'ACTIVE' }
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  form.value = { ...row }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      await request.post('/admin/sys/role/save', form.value)
      ElMessage.success('保存成功')
      dialogVisible.value = false
      proTable.value.refresh()
    }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该角色吗?', '提示', { type: 'warning' }).then(async () => {
    await request.post(`/admin/sys/role/delete/${row.id}`)
    ElMessage.success('删除成功')
    proTable.value.refresh()
  }).catch(() => {})
}

// Menu Assignment
const menuDialogVisible = ref(false)
const currentRole = ref<any>(null)
const allMenus = ref<any[]>([])
const menuTree = ref()

const fetchMenus = async () => {
  const res = await request.get('/admin/sys/menu/tree')
  allMenus.value = res as any
}

const handleAssignMenu = async (row: any) => {
  currentRole.value = row
  const res: any = await request.get('/admin/sys/role/menus', { params: { roleId: row.id } })
  const menuIds = res.map((m: any) => m.menuId)

  menuDialogVisible.value = true
  await nextTick()
  // 必须只勾选叶子节点，否则父节点半选状态会导致全部子节点被强制选中
  // 这里简化处理，直接setCheckedKeys
  menuTree.value?.setCheckedKeys(menuIds)
}

const submitMenus = async () => {
  if (!currentRole.value) return
  // 获取全选和半选的节点
  const checkedKeys = menuTree.value?.getCheckedKeys() || []
  const halfCheckedKeys = menuTree.value?.getHalfCheckedKeys() || []
  const allSelectedKeys = [...checkedKeys, ...halfCheckedKeys]

  await request.post('/admin/sys/role/assignMenus', {
    roleId: currentRole.value.id,
    menuIds: allSelectedKeys
  })
  ElMessage.success('菜单分配成功')
  menuDialogVisible.value = false
}

onMounted(() => {
  fetchMenus()
})
</script>
