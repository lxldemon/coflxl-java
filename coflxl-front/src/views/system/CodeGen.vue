<template>
  <div class="p-6 h-full flex flex-col">
    <div class="flex justify-between items-center mb-4">
      <h2 class="text-lg font-medium">代码生成器</h2>
      <el-button type="primary" @click="handleGenerate" :loading="generating">生成代码</el-button>
    </div>

    <div class="flex-1 flex gap-4 min-h-0">
      <!-- 左侧：DDL输入区 -->
      <div class="w-1/3 flex flex-col border border-gray-200 rounded-md bg-white">
        <div class="p-4 border-b border-gray-200">
          <el-form :model="configForm" label-width="60px" size="small">
            <el-form-item label="包名">
              <el-input v-model="configForm.packageName" placeholder="例如: com.coflxl.api" />
            </el-form-item>
            <el-form-item label="作者" class="mb-0">
              <el-input v-model="configForm.author" placeholder="例如: coflxl" />
            </el-form-item>
          </el-form>
        </div>
        <div class="p-2 border-b border-gray-200 bg-gray-50 text-sm font-medium text-gray-700">
          输入 DDL (CREATE TABLE)
        </div>
        <div class="flex-1">
          <MonacoEditor
              v-model="ddlInput"
              language="sql"
              height="100%"
              theme="vs-light"
          />
        </div>
      </div>

      <!-- 右侧：生成代码预览区 -->
      <div class="w-2/3 flex flex-col border border-gray-200 rounded-md bg-white">
        <el-tabs v-model="activeTab" class="flex-1 flex flex-col" type="border-card">
          <el-tab-pane
              v-for="(content, fileName) in generatedCode"
              :key="fileName"
              :label="fileName"
              :name="fileName"
              class="h-full"
          >
            <div class="h-full flex flex-col relative">
              <el-button
                  class="absolute top-2 right-4 z-10"
                  size="small"
                  @click="copyCode(content)"
              >
                复制代码
              </el-button>
              <MonacoEditor
                  :modelValue="content"
                  :language="getLanguage(fileName)"
                  height="100%"
                  theme="vs-light"
                  :readOnly="true"
              />
            </div>
          </el-tab-pane>

          <div v-if="Object.keys(generatedCode).length === 0" class="flex items-center justify-center h-full text-gray-400">
            暂无生成的代码，请在左侧输入DDL并点击生成
          </div>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import MonacoEditor from '../../components/MonacoEditor.vue'

const ddlInput = ref(`CREATE TABLE \`user_info\` (
\`id\` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
\`username\` varchar(50) NOT NULL COMMENT '用户名',
\`age\` int(11) DEFAULT NULL COMMENT '年龄',
\`created_at\` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
PRIMARY KEY (\`id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';`)

const generating = ref(false)
const generatedCode = ref<Record<string, string>>({})
const activeTab = ref('')

const configForm = ref({
  packageName: 'com.coflxl.api',
  author: 'coflxl'
})

const handleGenerate = async () => {
  if (!ddlInput.value.trim()) {
    ElMessage.warning('请输入DDL语句')
    return
  }

  generating.value = true
  try {
    const res = await request.post('/admin/codegen/generate', {
      ddl: ddlInput.value,
      packageName: configForm.value.packageName || 'com.coflxl.api',
      author: configForm.value.author || 'coflxl'
    })

    generatedCode.value = res || {}
    // 默认选中第一个tab
    const keys = Object.keys(generatedCode.value)
    if (keys.length > 0) {
      activeTab.value = keys[0]
    }
    ElMessage.success('代码生成成功')
  } catch (e: any) {
    ElMessage.error(e.message || '生成失败，请检查DDL格式')
  } finally {
    generating.value = false
  }
}

const getLanguage = (fileName: string) => {
  if (fileName.endsWith('.java')) return 'java'
  if (fileName.endsWith('.vue')) return 'html'
  if (fileName.endsWith('.xml')) return 'xml'
  return 'plaintext'
}

const copyCode = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('复制成功')
  } catch (err) {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped>
:deep(.el-tabs__content) {
  flex: 1;
  padding: 0;
  height: 0; /* 必须设置，否则无法撑开内部滚动 */
}
:deep(.el-tab-pane) {
  height: 100%;
}
</style>
