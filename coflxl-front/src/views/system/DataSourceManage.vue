<template>
  <div class="bg-white p-6 rounded-lg shadow-sm h-full flex flex-col">
    <ProTable v-if="isReady" ref="proTable" :columns="columns" :requestApi="getDataSources">
      <template #search="{ search, reset }">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="所属系统">
            <el-select v-model="searchForm.systemCode" placeholder="请选择系统" clearable style="width: 200px">
              <el-option v-for="sys in systemList" :key="sys.systemCode" :label="sys.systemName" :value="sys.systemCode" />
            </el-select>
          </el-form-item>
          <el-form-item label="数据源编码">
            <el-input v-model="searchForm.code" placeholder="请输入编码" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item label="数据源名称">
            <el-input v-model="searchForm.name" placeholder="请输入名称" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="search(searchForm)">查询</el-button>
            <el-button @click="resetForm(reset)">重置</el-button>
          </el-form-item>
        </el-form>
      </template>

      <template #toolbar-left>
        <h2 class="text-lg font-medium">数据源管理</h2>
      </template>
      <template #toolbar-right>
        <el-button type="primary" @click="openDrawer()">新增数据源</el-button>
      </template>

      <template #systemSlot="{ row }">
        {{ systemList.find(s => s.systemCode === row.systemCode)?.systemName || row.systemCode }}
      </template>
      <template #actionSlot="{ row }">
        <el-button link type="primary" size="small" @click="openDrawer(row)">编辑</el-button>
        <el-popconfirm title="确定删除该数据源吗？" @confirm="deleteDataSource(row.code)">
          <template #reference>
            <el-button link type="danger" size="small">删除</el-button>
          </template>
        </el-popconfirm>
      </template>
    </ProTable>

    <el-drawer v-model="drawerVisible" :title="isEdit ? '编辑数据源' : '新增数据源'" size="40%">
      <el-form :model="form" label-width="120px" class="mt-4">
        <el-form-item label="所属系统" required>
          <el-select v-model="form.systemCode" class="w-full" placeholder="请选择所属系统">
            <el-option v-for="sys in systemList" :key="sys.systemCode" :label="sys.systemName" :value="sys.systemCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据源编码" required>
          <el-input v-model="form.code" :disabled="isEdit" placeholder="例如: target_db" />
        </el-form-item>
        <el-form-item label="数据源名称" required>
          <el-input v-model="form.name" placeholder="例如: 目标业务库" />
        </el-form-item>
        <el-form-item label="数据库类型">
          <el-select v-model="form.dbType" class="w-full">
            <el-option label="MySQL" value="mysql" />
            <el-option label="PostgreSQL" value="postgresql" />
            <el-option label="Oracle" value="oracle" />
          </el-select>
        </el-form-item>
        <el-form-item label="JDBC URL" required>
          <el-input v-model="form.jdbcUrl" type="textarea" :rows="3" placeholder="jdbc:mysql://localhost:3306/db_name" />
        </el-form-item>
        <el-form-item label="驱动类名">
          <el-input v-model="form.driverClassName" placeholder="com.mysql.cj.jdbc.Driver" />
        </el-form-item>
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="form.passwordEncrypted" type="password" show-password />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDataSource" :loading="saving">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
import ProTable from '../../components/ProTable.vue'

const proTable = ref()
const drawerVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const isReady = ref(false)
const systemList = ref<any[]>([])

const searchForm = ref({ code: '', name: '', systemCode: '' })

const columns = [
  { prop: 'systemCode', label: '所属系统', width: 150, slotName: 'systemSlot' },
  { prop: 'code', label: '数据源编码', width: 150 },
  { prop: 'name', label: '数据源名称', width: 180 },
  { prop: 'dbType', label: '数据库类型', width: 120 },
  { prop: 'jdbcUrl', label: 'JDBC URL', showOverflowTooltip: true },
  { prop: 'username', label: '用户名', width: 120 },
  { label: '操作', width: 150, slotName: 'actionSlot' }
]

const form = ref<any>({
  id: undefined, code: '', name: '', dbType: 'mysql', jdbcUrl: '', driverClassName: 'com.mysql.cj.jdbc.Driver', username: '', passwordEncrypted: '', remark: '', systemCode: ''
})

onMounted(async () => {
  const res: any = await request.get('/admin/system/list')
  systemList.value = res || []
  isReady.value = true
})

const getDataSources = (params: any) => request.get('/admin/data-source/page', { params })

const resetForm = (resetFn: Function) => {
  searchForm.value = { code: '', name: '', systemCode: '' }
  resetFn()
}

const openDrawer = (row?: any) => {
  if (row) {
    isEdit.value = true
    form.value = { ...row }
  } else {
    isEdit.value = false
    form.value = { id: undefined, code: '', name: '', dbType: 'mysql', jdbcUrl: '', driverClassName: 'com.mysql.cj.jdbc.Driver', username: '', passwordEncrypted: '', remark: '', systemCode: '' }
  }
  drawerVisible.value = true
}

const saveDataSource = async () => {
  if (!form.value.code || !form.value.jdbcUrl || !form.value.username) {
    ElMessage.warning('请填写必填项')
    return
  }
  saving.value = true
  try {
    await request.post('/admin/data-source/save', form.value)
    ElMessage.success('保存成功')
    drawerVisible.value = false
    proTable.value.refresh()
  } finally {
    saving.value = false
  }
}

const deleteDataSource = async (code: string) => {
  await request.post(`/admin/data-source/delete/${code}`)
  ElMessage.success('删除成功')
  proTable.value.refresh()
}
</script>
