<template>
  <div class="p-6">
    <ProTable
        ref="proTable"
        :request-api="getChampionDetailsList"
        :columns="columns"
    >
      <template #search="{ search, reset }">
        <el-form :inline="true" :model="searchForm" class="flex flex-wrap gap-2">
          <el-form-item label="create">
            <el-input v-model="searchForm.create" placeholder="请输入create" clearable style="width: 100px" />
          </el-form-item>
          <el-form-item label="id">
            <el-input v-model="searchForm.heroid" placeholder="请输入id" clearable style="width: 100px" />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable style="width: 100px" />
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="searchForm.title" placeholder="请输入标题" clearable style="width: 100px" />
          </el-form-item>
          <el-form-item label="角色">
            <el-input v-model="searchForm.roles" placeholder="请输入角色" clearable style="width: 100px" />
          </el-form-item>
          <el-form-item label="名称">
            <el-input v-model="searchForm.lane" placeholder="请输入名称" clearable style="width: 100px" />
          </el-form-item>
          <el-form-item label="介绍">
            <el-input v-model="searchForm.intro" placeholder="请输入介绍" clearable style="width: 100px" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="searchForm.description" placeholder="请输入描述" clearable style="width: 100px" />
          </el-form-item>
          <el-form-item label="技能">
            <el-input v-model="searchForm.skillsJson" placeholder="请输入技能" clearable style="width: 100px" />
          </el-form-item>
          <el-form-item label="url">
            <el-input v-model="searchForm.avatarUrl" placeholder="请输入url" clearable style="width: 100px" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="search(searchForm)">查询</el-button>
            <el-button @click="resetForm(reset)">重置</el-button>
          </el-form-item>
        </el-form>
      </template>
      <template #toolbar-left>
        <h2 class="text-lg font-medium">ChampionDetails</h2>
      </template>
      <template #toolbar-right>
        <el-button type="primary" @click="openDialog()">新增</el-button>
      </template>
      <template #actionSlot="scope">
        <el-button type="primary" link size="small" @click="openDialog(scope.row)">编辑</el-button>
        <el-popconfirm title="确定删除吗？" @confirm="handleDelete(scope.row)">
          <template #reference>
            <el-button type="danger" link size="small">删除</el-button>
          </template>
        </el-popconfirm>
      </template>

    </ProTable>

    <el-dialog :title="isEdit ? '编辑' : '新增'" v-model="dialogVisible" width="600px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="create">
          <el-input v-model="formData.create" placeholder="请输入create" />
        </el-form-item>
        <el-form-item label="id">
          <el-input v-model="formData.heroid" placeholder="请输入id" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="formData.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="formData.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="角色">
          <el-input v-model="formData.roles" placeholder="请输入角色" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="formData.lane" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="介绍">
          <el-input v-model="formData.intro" placeholder="请输入介绍" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="技能">
          <el-input v-model="formData.skillsJson" placeholder="请输入技能" />
        </el-form-item>
        <el-form-item label="url">
          <el-input v-model="formData.avatarUrl" placeholder="请输入url" />
        </el-form-item>

      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
import ProTable from '../../components/ProTable.vue'

const proTable = ref()
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formData = ref<any>({})
const searchForm = ref<any>({})

const columns = [
  { prop: 'heroid', label: 'id' },
  { prop: 'name', label: '姓名' },
  { prop: 'title', label: '标题' },
  { prop: 'roles', label: '角色' },
  { prop: 'lane', label: '名称' },
  { prop: 'intro', label: '介绍' },
  { prop: 'description', label: '描述' },
  { prop: 'avatarUrl', label: 'url' },

  { prop: 'actionSlot', label: '操作', width: 150, fixed: 'right', slotName: 'actionSlot' }]

const resetForm = (reset: any) => {
  searchForm.value = {}
  reset()
}

const getChampionDetailsList = (params: any) => {
  // ProTable 默认传 current 和 size，需要转换为 pageNo 和 pageSize
  const queryParams = {
    ...params,
    pageNo: params.current || params.pageNo || 1,
    pageSize: params.size || params.pageSize || 10
  }
  return request.get('/admin/champion-details/page', { params: queryParams }).then((res: any) => {
    // 适配 ProTable 的返回格式
    return {
      rows: res.rows || [],
      recordCount: res.recordCount || 0
    }
  })
}

const openDialog = (row?: any) => {
  isEdit.value = !!row
  formData.value = row ? { ...row } : {}
  dialogVisible.value = true
}

const handleSubmit = async () => {
  saving.value = true
  try {
    await request.post('/admin/champion-details/save', formData.value)
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    proTable.value?.refresh()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row: any) => {
  try {
    await request.post(`/admin/champion-details/delete/\${row.id}`)
    ElMessage.success('删除成功')
    proTable.value?.refresh()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}
</script>
