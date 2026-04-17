<template>
  <div class="bg-white p-6 rounded-lg shadow-sm h-full flex flex-col">
    <div class="flex justify-between items-center mb-4">
      <h2 class="text-lg font-medium">菜单管理</h2>
      <div>
        <el-button type="primary" @click="handleAdd">新增菜单</el-button>
        <el-button type="primary" @click="fetchMenus">刷新列表</el-button>
      </div>
    </div>

    <el-table
        :data="menus"
        style="width: 100%"
        row-key="id"
        border
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column prop="name" label="菜单名称" sortable width="200" />
      <el-table-column prop="icon" label="图标" width="100">
        <template #default="{ row }">
          <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="sortNo" label="排序" width="80" />
      <el-table-column prop="path" label="路由地址" show-overflow-tooltip />
      <el-table-column prop="visibleFlag" label="是否可见" width="100">
        <template #default="{ row }">
          <el-switch v-model="row.visibleFlag" disabled />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleAddSubMenu(row)">新增</el-button>
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="dialogType === 'add' ? '新增菜单' : '编辑菜单'" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="上级菜单">
          <el-tree-select
              v-model="form.parentId"
              :data="[{ id: null, name: '主目录 (一级菜单)' }, ...menus]"
              :props="{ label: 'name', value: 'id', children: 'children' }"
              placeholder="请选择上级菜单"
              check-strictly
              style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路由地址" prop="path">
          <el-input v-model="form.path" placeholder="请输入路由地址" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-select
              v-model="form.icon"
              placeholder="请选择图标"
              filterable
              clearable
              style="width: 100%"
          >
            <template #prefix v-if="form.icon">
              <el-icon><component :is="Icons[form.icon]" /></el-icon>
            </template>
            <el-option
                v-for="item in iconOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            >
              <div class="flex items-center space-x-2">
                <el-icon><component :is="Icons[item.value]" /></el-icon>
                <span>{{ item.label }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sortNo">
          <el-input-number v-model="form.sortNo" :min="1" />
        </el-form-item>
        <el-form-item label="是否可见" prop="visibleFlag">
          <el-switch v-model="form.visibleFlag" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as Icons from '@element-plus/icons-vue'
import request from '../../utils/request'

const menus = ref<any[]>([])
const iconOptions = Object.keys(Icons).map(key => ({ label: key, value: key }))

const fetchMenus = async () => {
  const res = await request.get('/admin/sys/menu/tree')
  menus.value = res as any
}

// Menu CRUD
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref()
const form = ref({
  id: null,
  parentId: null,
  name: '',
  path: '',
  icon: '',
  sortNo: 1,
  visibleFlag: true,
  keepAliveFlag: false
})
const rules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }]
}

const handleAdd = () => {
  dialogType.value = 'add'
  form.value = {
    id: null, parentId: null, name: '', path: '',
    icon: '', sortNo: 1, visibleFlag: true, keepAliveFlag: false
  }
  dialogVisible.value = true
}

const handleAddSubMenu = (row: any) => {
  dialogType.value = 'add'
  form.value = {
    id: null, parentId: row.id, name: '', path: '',
    icon: '', sortNo: 1, visibleFlag: true, keepAliveFlag: false
  }
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
      await request.post('/admin/sys/menu/save', form.value)
      ElMessage.success('保存成功')
      dialogVisible.value = false
      fetchMenus()
    }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该菜单及其子菜单吗?', '提示', { type: 'warning' }).then(async () => {
    await request.post(`/admin/sys/menu/delete/${row.id}`)
    ElMessage.success('删除成功')
    fetchMenus()
  }).catch(() => {})
}

onMounted(() => {
  fetchMenus()
})
</script>
