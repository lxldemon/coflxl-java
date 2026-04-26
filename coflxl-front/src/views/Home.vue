<template>
  <div class="space-y-6">
    <section class="theme-card rounded-xl p-6 shadow-sm lg:p-7">
      <div class="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
        <div class="max-w-2xl">
          <p class="text-sm font-medium text-blue-700">平台概览</p>
          <h1 class="mt-2 text-2xl font-semibold theme-title lg:text-3xl">接口在线管理平台</h1>
          <p class="mt-3 text-sm leading-7 theme-text">
            平台面向企业级接口管理场景，统一支撑 API 发布、数据源配置、参数注入和 SQL 调试，帮助团队更稳定地完成数据服务交付。
          </p>
        </div>
        <div class="grid w-full gap-4 sm:grid-cols-2 xl:w-[520px]">
          <div class="theme-subcard rounded-lg p-4">
            <div class="text-xs font-medium theme-muted">核心模块</div>
            <div class="mt-2 text-2xl font-semibold theme-title">4</div>
            <div class="mt-1 text-xs theme-muted">覆盖系统、数据源、接口与调试能力</div>
          </div>
          <div class="theme-subcard rounded-lg p-4">
            <div class="text-xs font-medium theme-muted">接口发布</div>
            <div class="mt-2 text-2xl font-semibold theme-title">标准化</div>
            <div class="mt-1 text-xs theme-muted">统一请求结构与响应规范</div>
          </div>
          <div class="theme-subcard rounded-lg p-4">
            <div class="text-xs font-medium theme-muted">数据源管理</div>
            <div class="mt-2 text-2xl font-semibold theme-title">集中维护</div>
            <div class="mt-1 text-xs theme-muted">支持多业务系统接入与统一配置</div>
          </div>
          <div class="theme-subcard rounded-lg p-4">
            <div class="text-xs font-medium theme-muted">在线调试</div>
            <div class="mt-2 text-2xl font-semibold theme-title">SQL 工作台</div>
            <div class="mt-1 text-xs theme-muted">缩短接口配置到验证的联调周期</div>
          </div>
        </div>
      </div>
    </section>

    <div class="grid grid-cols-1 gap-6 xl:grid-cols-[1.25fr_0.75fr]">
      <div class="space-y-6">
        <section class="theme-card rounded-xl p-6 shadow-sm">
          <div class="mb-4 flex items-center justify-between">
            <div>
              <h2 class="text-lg font-semibold theme-title">快捷入口</h2>
              <p class="mt-1 text-sm theme-muted">常用模块可从此快速进入。</p>
            </div>
          </div>
          <div class="grid gap-4 sm:grid-cols-2">
            <div class="cursor-pointer theme-subcard rounded-lg p-4 transition-colors hover:border-blue-300 hover:bg-blue-50" @click="$router.push('/system-manage')">
              <div class="flex items-center gap-3">
                <el-icon class="text-lg text-blue-600"><Platform /></el-icon>
                <h3 class="font-medium theme-title">系统管理</h3>
              </div>
              <p class="mt-2 text-sm leading-6 theme-muted">管理接入平台的业务系统，便于分类维护与检索。</p>
            </div>
            <div class="cursor-pointer theme-subcard rounded-lg p-4 transition-colors hover:border-blue-300 hover:bg-blue-50" @click="$router.push('/data-source-manage')">
              <div class="flex items-center gap-3">
                <el-icon class="text-lg text-emerald-600"><Coin /></el-icon>
                <h3 class="font-medium theme-title">数据源管理</h3>
              </div>
              <p class="mt-2 text-sm leading-6 theme-muted">集中配置多种数据库连接，支撑不同业务系统接入。</p>
            </div>
            <div class="cursor-pointer theme-subcard rounded-lg p-4 transition-colors hover:border-blue-300 hover:bg-blue-50" @click="$router.push('/api-manage')">
              <div class="flex items-center gap-3">
                <el-icon class="text-lg text-violet-600"><Setting /></el-icon>
                <h3 class="font-medium theme-title">接口管理</h3>
              </div>
              <p class="mt-2 text-sm leading-6 theme-muted">维护接口定义、参数规则与发布状态。</p>
            </div>
            <div class="cursor-pointer theme-subcard rounded-lg p-4 transition-colors hover:border-blue-300 hover:bg-blue-50" @click="$router.push('/sql-workbench')">
              <div class="flex items-center gap-3">
                <el-icon class="text-lg text-amber-600"><Monitor /></el-icon>
                <h3 class="font-medium theme-title">SQL 工作台</h3>
              </div>
              <p class="mt-2 text-sm leading-6 theme-muted">在线调试 SQL 与接口效果，提升联调效率。</p>
            </div>
          </div>
        </section>

        <section class="theme-card rounded-xl p-6 shadow-sm">
          <div class="mb-5">
            <h2 class="text-lg font-semibold theme-title">接口说明</h2>
            <p class="mt-1 text-sm theme-muted">保留常用调用规则，便于快速查阅。</p>
          </div>

          <div class="grid gap-5 lg:grid-cols-2">
            <div class="theme-subcard rounded-lg p-4">
              <div class="flex items-center text-base font-medium theme-title">
                <el-icon class="mr-2 text-blue-600"><Connection /></el-icon>
                快速调用
              </div>
              <p class="mt-3 text-sm leading-6 theme-text">
                当接口状态设置为 <el-tag type="success" size="small">已发布</el-tag> 后，外部系统即可通过统一地址进行调用。
              </p>
              <div class="theme-code-block mt-4 rounded-md px-4 py-3 font-mono text-sm">
                POST /api/open/{apiCode}
              </div>
              <div class="mt-3 text-xs leading-5 theme-muted">请求头默认使用 `Content-Type: application/json`，按标准 JSON 请求体传参。</div>
            </div>

            <div class="theme-subcard rounded-lg p-4">
              <div class="flex items-center text-base font-medium theme-title">
                <el-icon class="mr-2 text-purple-600"><Key /></el-icon>
                动态参数
              </div>
              <p class="mt-3 text-sm leading-6 theme-text">
                请求体中的键名需要与参数定义保持一致，平台会自动完成 SQL 参数注入。
              </p>
              <div class="theme-info-block mt-4 rounded-md px-4 py-3 text-sm">
                select * from users where 1=1 #[and user_code = :userCode]
              </div>
              <div class="mt-3 text-xs leading-5 theme-muted">使用 `#[...]` 包裹动态条件，参数为空时条件会自动忽略。</div>
            </div>

            <div class="theme-subcard rounded-lg p-4 lg:col-span-2">
              <div class="flex items-center text-base font-medium theme-title">
                <el-icon class="mr-2 text-emerald-600"><DocumentChecked /></el-icon>
                标准响应结构
              </div>
              <p class="mt-3 text-sm leading-6 theme-text">平台接口统一返回标准 JSON 结构，便于调用方统一处理。</p>
              <div class="theme-code-block mt-4 overflow-x-auto rounded-md p-4 text-sm">
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
              <div class="mt-3 text-xs leading-5 theme-muted">分页查询场景下，`data` 中会返回 `rows`、`recordCount` 等分页字段。</div>
            </div>
          </div>
        </section>
      </div>

      <div class="space-y-6">
        <section class="theme-card rounded-xl p-6 shadow-sm">
          <h2 class="text-lg font-semibold theme-title">平台简介</h2>
          <p class="mt-3 text-sm leading-7 theme-text">
            Coflxl API Platform 以数据驱动方式组织接口能力，支持通过 SQL 快速构建标准 HTTP API，适用于内部系统集成、数据服务开放和统一接口管理。
          </p>
          <div class="mt-5 space-y-3 text-sm theme-text">
            <div class="theme-subcard flex items-start gap-3 rounded-md px-4 py-3">
              <span class="mt-2 h-1.5 w-1.5 shrink-0 rounded-full bg-blue-500"></span>
              <span>统一接口配置、发布与调用规范，降低协作成本。</span>
            </div>
            <div class="theme-subcard flex items-start gap-3 rounded-md px-4 py-3">
              <span class="mt-2 h-1.5 w-1.5 shrink-0 rounded-full bg-blue-500"></span>
              <span>支持多数据源接入与动态参数注入，增强配置灵活性。</span>
            </div>
            <div class="theme-subcard flex items-start gap-3 rounded-md px-4 py-3">
              <span class="mt-2 h-1.5 w-1.5 shrink-0 rounded-full bg-blue-500"></span>
              <span>结合 SQL 工作台在线验证能力，缩短接口配置到交付的闭环时间。</span>
            </div>
          </div>
        </section>

        <section class="theme-card rounded-xl p-6 shadow-sm">
          <h2 class="text-lg font-semibold theme-title">使用建议</h2>
          <div class="mt-4 space-y-4 text-sm theme-text">
            <div class="theme-info-block rounded-md border-l-4 border-blue-500 px-4 py-3">
              优先在“接口管理”中完成接口定义与参数配置，再进入 SQL 工作台调试查询逻辑。
            </div>
            <div class="theme-info-block rounded-md border-l-4 border-emerald-500 px-4 py-3">
              接口对外调用前，请确认状态已发布，并校验请求体字段名称与参数定义保持一致。
            </div>
            <div class="theme-info-block rounded-md border-l-4 border-amber-500 px-4 py-3">
              如涉及分页查询，请在调用方预留 `rows` 与 `recordCount` 等分页字段的处理逻辑。
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Connection, DocumentChecked, Key, Platform, Coin, Setting, Monitor } from '@element-plus/icons-vue'
</script>

<style scoped>
pre {
  margin: 0;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
}
</style>
