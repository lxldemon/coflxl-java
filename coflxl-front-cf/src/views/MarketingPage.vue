<template>
  <div class="marketing-page">
    <!-- 1. 导航栏 (Header) -->
    <header class="bg-white shadow-md p-4 sticky top-0 z-50">
      <nav class="container mx-auto flex justify-between items-center">
        <div class="text-2xl font-bold text-blue-600">coflxl 低代码平台</div>
        <div>
          <a href="#" class="text-gray-600 hover:text-blue-600 mx-4">首页</a>
          <a href="#" class="text-gray-600 hover:text-blue-600 mx-4">产品功能</a>
          <a href="#" class="text-gray-600 hover:text-blue-600 mx-4">解决方案</a>
          <a href="#" class="text-gray-600 hover:text-blue-600 mx-4">价格</a>
          <a href="#" class="text-gray-600 hover:text-blue-600 mx-4">联系我们</a>
        </div>
      </nav>
    </header>

    <!-- 2. 核心价值区 (Hero Section) -->
    <section class="hero-section bg-blue-700 text-white py-20 bg-fixed bg-center bg-cover">
      <div class="container mx-auto text-center bg-blue-700 bg-opacity-75 p-10 rounded-lg">
        <h1 class="text-5xl font-extrabold leading-tight mb-4">coflxl 低代码平台：用 SQL 驱动您的 API 开发</h1>
        <p class="text-xl mb-8">告别繁琐编码，用数据思维快速构建和管理 API 接口</p>
        <div class="space-x-4">
          <button class="bg-white text-blue-700 font-bold py-3 px-8 rounded-full shadow-lg hover:bg-gray-100 transition duration-300">立即体验</button>
          <button class="border border-white text-white font-bold py-3 px-8 rounded-full hover:bg-blue-600 transition duration-300">查看演示视频</button>
        </div>
      </div>
    </section>

    <!-- 3. 产品介绍 (Product Overview) -->
    <section class="product-overview py-16 bg-gray-50">
      <div class="container mx-auto text-center">
        <h2 class="text-4xl font-bold text-gray-800 mb-8">什么是 coflxl 低代码平台？</h2>
        <p class="text-lg text-gray-700 max-w-3xl mx-auto mb-8">
          coflxl 是一个创新的低代码开发平台，专为开发者和实施人员设计。它打破了传统接口开发的束缚，
          允许您直接通过编写 SQL 语句来定义、发布和管理复杂的 RESTful API。
          通过 coflxl，您可以将精力聚焦于业务逻辑和数据本身，而非重复的编码工作，从而显著提升开发效率和响应速度。
        </p>
        <div class="flex flex-col md:flex-row items-center md:space-x-12 mt-8">
          <div class="md:w-1/2">
            <img src="/marketing/接口管理.png" alt="接口管理" class="mx-auto rounded-lg shadow-xl max-w-full h-auto border border-gray-200">
          </div>
          <div class="md:w-1/2 text-left mt-8 md:mt-0">
            <h3 class="text-3xl font-bold text-gray-800 mb-4">轻松管理您的 API</h3>
            <p class="text-lg text-gray-700">通过直观的界面，您可以轻松创建、编辑、发布和监控 API 接口。coflxl 提供全面的 API 生命周期管理，让您的团队协作更高效。</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 4. 核心功能亮点 (Key Features) -->
    <section class="key-features py-16 bg-white">
      <div class="container mx-auto">
        <h2 class="text-4xl font-bold text-gray-800 text-center mb-12">coflxl 的独特优势</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          <div class="feature-card p-6 bg-blue-50 rounded-lg shadow-md hover:shadow-xl transition duration-300">
            <h3 class="text-2xl font-semibold text-blue-600 mb-4">SQL 驱动接口</h3>
            <p class="text-gray-700">无需编写 Java/Python 等后端代码，直接通过 SQL 查询、存储过程或自定义 SQL 逻辑，即可定义和发布 RESTful API。</p>
          </div>
          <div class="feature-card p-6 bg-green-50 rounded-lg shadow-md hover:shadow-xl transition duration-300">
            <h3 class="text-2xl font-semibold text-green-600 mb-4">可视化管理</h3>
            <p class="text-gray-700">提供直观的界面，管理 API、数据源、权限等。实现配置化而非编码，快速迭代，降低学习成本。</p>
            <img src="/marketing/数据连接.png" alt="数据连接" class="rounded-lg shadow-md max-w-full h-auto mt-4">
          </div>
          <div class="feature-card p-6 bg-purple-50 rounded-lg shadow-md hover:shadow-xl transition duration-300">
            <h3 class="text-2xl font-semibold text-purple-600 mb-4">强大的扩展性</h3>
            <p class="text-gray-700">支持多种数据库连接、灵活的参数配置、数据校验与转换，轻松适应复杂业务场景。</p>
          </div>
          <div class="feature-card p-6 bg-red-50 rounded-lg shadow-md hover:shadow-xl transition duration-300">
            <h3 class="text-2xl font-semibold text-red-600 mb-4">安全与权限控制</h3>
            <p class="text-gray-700">提供精细化的 API 访问控制和数据权限管理，全面保障数据安全，符合企业级需求。</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 新增：代码生成模块 -->
    <section class="code-gen-section py-16 bg-gray-100">
      <div class="container mx-auto">
        <div class="flex flex-col md:flex-row items-center md:space-x-12">
          <div class="md:w-1/2 text-left">
            <h2 class="text-4xl font-bold text-gray-800 mb-6">智能代码生成，加速开发进程</h2>
            <p class="text-lg text-gray-700 mb-6">
              coflxl 低代码平台内置智能代码生成器，能够根据您定义的接口和数据模型，自动生成前端、后端或数据库相关代码。
              这不仅大大减少了重复性工作，降低了出错率，更让您的开发团队能够专注于创新和业务逻辑的实现，进一步提升效率。
            </p>
            <ul class="list-disc list-inside text-gray-700 text-lg">
              <li class="mb-2">根据接口自动生成请求参数和返回结构</li>
              <li class="mb-2">支持多语言代码模板定制</li>
              <li>一键生成 CRUD 基础代码</li>
            </ul>
          </div>
          <div class="md:w-1/2 mt-8 md:mt-0">
            <img src="/marketing/代码生成.png" alt="代码生成" class="mx-auto rounded-lg shadow-xl max-w-full h-auto border border-gray-200">
          </div>
        </div>
      </div>
    </section>

    <!-- 5. 适用场景/解决方案 (Use Cases/Solutions) -->
    <section class="use-cases py-16 bg-gray-50">
      <div class="container mx-auto">
        <h2 class="text-4xl font-bold text-gray-800 text-center mb-12">coflxl 能为您的团队带来什么？</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div class="case-card p-6 bg-white rounded-lg shadow-md hover:shadow-xl transition duration-300">
            <h3 class="text-2xl font-semibold text-gray-800 mb-4">快速原型开发</h3>
            <p class="text-gray-700">适用于需要快速验证业务想法、构建 MVP 的场景，大大缩短产品上市时间。</p>
          </div>
          <div class="case-card p-6 bg-white rounded-lg shadow-md hover:shadow-xl transition duration-300">
            <h3 class="text-2xl font-semibold text-gray-800 mb-4">传统系统接口改造</h3>
            <p class="text-gray-700">帮助传统系统快速暴露 API 接口，实现微服务化或数据共享，赋能老旧系统。</p>
          </div>
          <div class="case-card p-6 bg-white rounded-lg shadow-md hover:shadow-xl transition duration-300">
            <h3 class="text-2xl font-semibold text-gray-800 mb-4">数据中台/集成平台</h3>
            <p class="text-gray-700">作为数据集成和 API 统一出口，构建强大的数据中台，灵活响应前端和第三方系统需求。</p>
          </div>
          <div class="case-card p-6 bg-white rounded-lg shadow-md hover:shadow-xl transition duration-300">
            <h3 class="text-2xl font-semibold text-gray-800 mb-4">跨团队协作</h3>
            <p class="text-gray-700">降低前端与后端沟通成本，通过清晰的 SQL 定义接口，提升团队协作效率和开发一致性。</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 6. 定价与合作 (Pricing & Partnership) -->
    <section class="pricing-partnership py-16 bg-blue-700 text-white">
      <div class="container mx-auto text-center">
        <h2 class="text-4xl font-bold mb-12">灵活的合作模式</h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-4xl mx-auto">
          <div class="p-8 bg-blue-800 rounded-lg shadow-md">
            <h3 class="text-3xl font-semibold mb-4">平台订阅/授权</h3>
            <p class="text-lg mb-6">提供不同级别的平台使用许可，满足不同规模团队的 API 开发需求。享受持续更新和专业支持。</p>
            <button class="bg-white text-blue-700 font-bold py-3 px-8 rounded-full shadow-lg hover:bg-gray-100 transition duration-300">咨询定价方案</button>
          </div>
          <div class="p-8 bg-blue-800 rounded-lg shadow-md">
            <h3 class="text-3xl font-semibold mb-4">商业源码授权</h3>
            <p class="text-lg mb-6">获取 coflxl 平台全套源码，适合希望进行深度定制、二次开发或私有化部署的企业。</p>
            <button class="bg-white text-blue-700 font-bold py-3 px-8 rounded-full shadow-lg hover:bg-gray-100 transition duration-300">获取源码详情</button>
          </div>
        </div>
      </div>
    </section>

    <!-- 8. 获取潜在客户联系方式 (Lead Capture / Contact Form) -->
    <section class="contact-form py-16 bg-gray-50">
      <div class="container mx-auto max-w-2xl">
        <h2 class="text-4xl font-bold text-gray-800 text-center mb-8">立即与我们联系</h2>
        <p class="text-lg text-gray-700 text-center mb-8">填写下表，我们的专家将尽快与您取得联系，为您提供专属解决方案和合作细节。</p>
        <form class="bg-white p-8 rounded-lg shadow-md">
          <div class="mb-4">
            <label for="name" class="block text-gray-700 text-sm font-bold mb-2">姓名:</label>
            <input type="text" id="name" class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline" placeholder="您的姓名">
          </div>
          <div class="mb-4">
            <label for="company" class="block text-gray-700 text-sm font-bold mb-2">公司名称:</label>
            <input type="text" id="company" class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline" placeholder="您的公司">
          </div>
          <div class="mb-4">
            <label for="email" class="block text-gray-700 text-sm font-bold mb-2">邮箱:</label>
            <input type="email" id="email" class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline" placeholder="您的邮箱">
          </div>
          <div class="mb-6">
            <label for="message" class="block text-gray-700 text-sm font-bold mb-2">留言:</label>
            <textarea id="message" rows="5" class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline" placeholder="您想了解什么？"></textarea>
          </div>
          <div class="flex items-center justify-between">
            <button type="submit" class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline">提交</button>
            <p class="text-xs text-gray-600">我们承诺保护您的隐私。</p>
          </div>
        </form>
      </div>
    </section>

    <!-- 9. 页脚 (Footer) -->
    <footer class="bg-gray-800 text-white py-8">
      <div class="container mx-auto text-center">
        <div class="text-xl font-bold mb-2">coflxl</div>
        <p class="text-sm">&copy; 2026 coflxl. All rights reserved.</p>
        <div class="mt-4">
          <a href="#" class="text-gray-400 hover:text-white mx-2">隐私政策</a>
          <a href="#" class="text-gray-400 hover:text-white mx-2">服务条款</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
// 可以根据需要添加脚本，例如表单提交逻辑等
</script>

<style scoped>
/* 可以根据需要添加自定义样式 */
</style>