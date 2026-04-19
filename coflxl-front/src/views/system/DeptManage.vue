<template>
  <div class="h-full bg-white flex flex-col p-4 rounded-lg shadow-sm">
    <div class="flex justify-between items-center mb-4">
      <h2 class="text-lg font-medium">部门组织架构管理</h2>
      <el-button type="primary" @click="handleAddRoot">新增顶级部门</el-button>
    </div>

    <el-table
        v-loading="loading"
        :data="tableData"
        row-key="id"
        border
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        class="flex-1 overflow-auto"
    >
      <el-table-column prop="name" label="部门名称" min-width="200" />
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column prop="sortNo" label="排序权重" width="100" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleAddSub(row)">新增子部门</el-button>
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 弹窗 -->
    <el-dialog
        :title="dialogTitle"
        v-model="dialogVisible"
        width="500px"
        @closed="resetForm"
    >
      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
      >
        <el-form-item label="父级部门" prop="parentId">
          <el-tree-select
              v-model="form.parentId"
              :data="[{ id: null, name: '作为顶级部门' }, ...tableData]"
              :props="{ label: 'name', value: 'id', children: 'children' }"
              placeholder="请选择父级部门"
              clearable
              check-strictly
              style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="显示排序" prop="sortNo">
          <el-input-number v-model="form.sortNo" :min="1" controls-position="right" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const formRef = ref()

const form = ref<any>({
  id: null,
  parentId: null,
  name: '',
  sortNo: 1
})

const rules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/sys/dept/tree')
    tableData.value = res || []
  } finally {
    loading.value = false
  }
}

const handleAddRoot = () => {
  dialogTitle.value = '新增顶级部门'
  form.value = { id: null, parentId: null, name: '', sortNo: 1 }
  dialogVisible.value = true
}

const handleAddSub = (row: any) => {
  dialogTitle.value = '新增部门'
  form.value = { id: null, parentId: row.id, name: '', sortNo: 1 }
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '修改部门'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该部门？如果有子部门将无法抛弃!', '警告', { type: 'warning' }).then(async () => {
    await request.post(`/admin/sys/dept/delete/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitting.value = true
      try {
        await request.post('/admin/sys/dept/save', Object.assign({}, form.value, { children: undefined }))
        ElMessage.success('保存成功')
        dialogVisible.value = false
        fetchData()
      } finally {
        submitting.value = false
      }
    }
  })
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

onMounted(() => {
  fetchData()
})
</script>
