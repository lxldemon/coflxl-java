<template>
  <div class="space-y-6">
    <!-- Hero Section -->
    <div class="bg-gradient-to-r from-blue-600 to-indigo-700 rounded-xl shadow-lg p-8 text-white">
      <h1 class="text-3xl font-bold mb-4">接口在线管理平台-开发者指南</h1>
      <p class="text-blue-100 text-lg max-w-3xl leading-relaxed">
        欢迎使用 Coflxl API Platform。这是一个基于数据驱动的动态 API 生成与管理平台。
        您可以通过编写 SQL 语句，快速发布为标准的 HTTP API 接口，无需编写任何 Java 代码。
      </p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Left Column: API Invocation -->
      <div class="space-y-6">
        <el-card class="border-0 shadow-sm" body-class="p-6">
          <template #header>
            <div class="flex items-center font-semibold text-gray-800 text-lg">
              <el-icon class="mr-2 text-blue-500"><Connection /></el-icon>
              快速调用指南 (Quick Start)
            </div>
          </template>
          <div class="space-y-4">
            <p class="text-gray-600 text-sm">当在“接口管理”中将一个接口状态设置为 <el-tag type="success" size="small">已发布 (PUBLISHED)</el-tag> 后，外部系统即可调用该接口。</p>

            <div class="bg-gray-50 p-4 rounded-md border border-gray-100">
              <div class="flex items-center mb-2">
                <span class="bg-green-500 text-white text-xs font-bold px-2 py-1 rounded mr-3">POST</span>
                <code class="text-gray-800 font-mono text-sm">/api/open/{apiCode}</code>
              </div>
              <p class="text-xs text-gray-500 mt-2">* {apiCode}: 在接口管理中定义的唯一接口编码（例如：getUserInfo）。</p>
            </div>

            <div>
              <h4 class="font-medium text-gray-700 mb-2 text-sm">请求头 (Headers)</h4>
              <div class="bg-gray-800 text-gray-200 p-3 rounded-md font-mono text-sm overflow-x-auto">
                Content-Type: application/json<br>
                <!-- Authorization: Bearer {token} -->
              </div>
            </div>
          </div>
        </el-card>

        <el-card class="border-0 shadow-sm" body-class="p-6">
          <template #header>
            <div class="flex items-center font-semibold text-gray-800 text-lg">
              <el-icon class="mr-2 text-green-500"><DocumentChecked /></el-icon>
              标准响应结构 (Response)
            </div>
          </template>
          <div class="space-y-4">
            <p class="text-gray-600 text-sm">接口统一返回标准的 JSON 结构：</p>
            <div class="bg-gray-800 text-green-400 p-4 rounded-md font-mono text-sm overflow-x-auto">
<pre><code>{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "user_name": "张三",
      "age": 25
    }
  ]
}</code></pre>
            </div>
            <p class="text-xs text-gray-500">注：如果配置的操作类型为 PAGE 分页查询，data 字段将包含 rows 和 recordCount 等分页属性。</p>
          </div>
        </el-card>
      </div>

      <!-- Right Column: Parameter Injection & Modules -->
      <div class="space-y-6">
        <el-card class="border-0 shadow-sm" body-class="p-6">
          <template #header>
            <div class="flex items-center font-semibold text-gray-800 text-lg">
              <el-icon class="mr-2 text-purple-500"><Key /></el-icon>
              动态参数注入说明
            </div>
          </template>
          <div class="space-y-4">
            <p class="text-gray-600 text-sm">
              请求体为 JSON 格式，键值对需与“参数定义”中配置的参数名保持一致。平台会自动将 JSON 中的参数注入到 SQL 语句中。
            </p>

            <div class="bg-blue-50 border-l-4 border-blue-400 p-3 mb-4">
              <p class="text-sm text-blue-800 font-mono">SQL 定义示例：</p>
              <code class="text-sm text-blue-900 block mt-1">select * from users where 1=1 #[and user_code = :userCode]</code>
            </div>

            <div>
              <h4 class="font-medium text-gray-700 mb-2 text-sm">请求体示例 (Body)</h4>
              <div class="bg-gray-800 text-yellow-300 p-4 rounded-md font-mono text-sm overflow-x-auto">
<pre><code>{
  "userCode": "U1001",
  "pageNo": 1,
  "pageSize": 10
}</code></pre>
              </div>
            </div>
            <p class="text-xs text-gray-500 mt-2">
              * 平台支持 SqlToy 动态 SQL 语法。使用 <code>#[...]</code> 包裹的条件，当传入参数为空时，该条件会自动被忽略，避免拼接 <code>1=1</code> 的繁琐操作。
            </p>
          </div>
        </el-card>

        <el-card class="border-0 shadow-sm" body-class="p-6">
          <template #header>
            <div class="flex items-center font-semibold text-gray-800 text-lg">
              <el-icon class="mr-2 text-orange-500"><Grid /></el-icon>
              平台模块概览
            </div>
          </template>
          <div class="grid grid-cols-2 gap-4">
            <div class="p-4 border border-gray-100 rounded-lg bg-gray-50 hover:shadow-md transition-shadow cursor-pointer" @click="$router.push('/system-manage')">
              <div class="flex items-center mb-2">
                <el-icon class="text-blue-500 text-xl mr-2"><Platform /></el-icon>
                <h3 class="font-medium text-gray-800">系统管理</h3>
              </div>
              <p class="text-xs text-gray-500">管理接入平台的各个业务系统，方便分类检索和维护。</p>
            </div>
            <div class="p-4 border border-gray-100 rounded-lg bg-gray-50 hover:shadow-md transition-shadow cursor-pointer" @click="$router.push('/data-source-manage')">
              <div class="flex items-center mb-2">
                <el-icon class="text-green-500 text-xl mr-2"><Coin /></el-icon>
                <h3 class="font-medium text-gray-800">数据源管理</h3>
              </div>
              <p class="text-xs text-gray-500">动态配置多种类型的数据库连接 (MySQL, Oracle 等)。</p>
            </div>
            <div class="p-4 border border-gray-100 rounded-lg bg-gray-50 hover:shadow-md transition-shadow cursor-pointer" @click="$router.push('/api-manage')">
              <div class="flex items-center mb-2">
                <el-icon class="text-purple-500 text-xl mr-2"><Setting /></el-icon>
                <h3 class="font-medium text-gray-800">接口管理</h3>
              </div>
              <p class="text-xs text-gray-500">定义 API 基础信息、编写 SQL 语句并定义入参规则。</p>
            </div>
            <div class="p-4 border border-gray-100 rounded-lg bg-gray-50 hover:shadow-md transition-shadow cursor-pointer" @click="$router.push('/sql-workbench')">
              <div class="flex items-center mb-2">
                <el-icon class="text-orange-500 text-xl mr-2"><Monitor /></el-icon>
                <h3 class="font-medium text-gray-800">SQL 工作台</h3>
              </div>
              <p class="text-xs text-gray-500">提供强力支持的 SQL 编写环境，在线测试和调试接口。</p>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Connection, DocumentChecked, Key, Grid, Platform, Coin, Setting, Monitor } from '@element-plus/icons-vue'
</script>

<style scoped>
pre {
  margin: 0;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
}
</style>
