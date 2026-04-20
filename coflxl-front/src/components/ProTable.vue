<template>
  <div class="pro-table flex flex-col h-full">
    <!-- Search Area -->
    <div class="search-area mb-4 p-4 bg-gray-50 rounded-md border border-gray-100 shrink-0" v-if="$slots.search">
      <slot name="search" :search="search" :reset="reset"></slot>
    </div>

    <!-- Toolbar Area -->
    <div class="toolbar-area flex justify-between items-center mb-4 shrink-0">
      <div class="toolbar-left">
        <slot name="toolbar-left"></slot>
      </div>
      <div class="toolbar-right">
        <slot name="toolbar-right"></slot>
      </div>
    </div>

    <!-- Table Area -->
    <div class="table-area flex-1 overflow-hidden">
      <el-table :data="tableData" border style="width: 100%" height="100%" v-loading="loading">
        <template v-for="col in columns" :key="col.prop || col.label">
          <el-table-column v-bind="col">
            <template #default="scope" v-if="col.slotName">
              <slot :name="col.slotName" :row="scope.row" :$index="scope.$index"></slot>
            </template>
          </el-table-column>
        </template>
      </el-table>
    </div>

    <!-- Pagination Area -->
    <div class="pagination-area mt-4 flex justify-end shrink-0" v-if="showPagination">
      <el-pagination
          v-model:current-page="pageParams.pageNo"
          v-model:page-size="pageParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'

const props = defineProps({
  columns: { type: Array as () => any[], required: true },
  requestApi: { type: Function, required: true },
  showPagination: { type: Boolean, default: true },
  initParams: { type: Object, default: () => ({}) }
})

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageParams = reactive({ pageNo: 1, pageSize: 10 })
const searchParams = ref({})

const fetchData = async () => {
  loading.value = true
  try {
    const params = props.showPagination
        ? { ...pageParams, ...props.initParams, ...searchParams.value }
        : { ...props.initParams, ...searchParams.value }

    const res: any = await props.requestApi(params)
    if (props.showPagination) {
      tableData.value = res.rows || []
      total.value = res.recordCount || 0
    } else {
      tableData.value = res || []
    }
  } catch (error) {
    console.error('Fetch data error:', error)
  } finally {
    loading.value = false
  }
}

const search = (params: any) => {
  searchParams.value = { ...params }
  pageParams.pageNo = 1
  fetchData()
}

const reset = () => {
  searchParams.value = {}
  pageParams.pageNo = 1
  fetchData()
}

const handleSizeChange = () => {
  pageParams.pageNo = 1
  fetchData()
}

const handleCurrentChange = () => {
  fetchData()
}

onMounted(() => {
  fetchData()
})

defineExpose({ refresh: fetchData, search, reset })
</script>

<style scoped>
.search-area .el-form-item {
  margin-bottom: 0;
}
</style>
