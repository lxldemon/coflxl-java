<template>
  <el-table-column
      v-if="!col.children || col.children.length === 0"
      :prop="col.prop"
      :label="col.label"
      :width="col.width"
      :align="col.align || 'center'"
  />
  <el-table-column
      v-else
      :label="col.label"
      :align="col.align || 'center'"
  >
    <DynamicTableColumn
        v-for="(child, index) in col.children"
        :key="index"
        :col="child"
    />
  </el-table-column>
</template>

<script setup lang="ts">
defineProps<{ col: any }>()
</script>