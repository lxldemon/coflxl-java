<template>
  <div class="min-h-screen bg-white text-gray-900 font-sans selection:bg-blue-100 selection:text-blue-900">
    <!-- 1. 极简毛玻璃导航栏 (Header) -->
    <header class="fixed top-0 w-full z-50 transition-all duration-300 backdrop-blur-md bg-white/70 border-b border-gray-100">
      <nav class="max-w-[1200px] mx-auto px-6 h-12 flex justify-between items-center text-[12px] font-medium tracking-tight">
        <div class="text-lg font-semibold tracking-tighter hover:text-blue-600 transition-colors cursor-pointer">COFLXL</div>
        <div class="hidden md:flex space-x-8 text-gray-600/90">
          <a href="#" class="hover:text-black transition-colors">首页</a>
          <a href="#" class="hover:text-black transition-colors">产品功能</a>
          <a href="#" class="hover:text-black transition-colors">解决方案</a>
          <a href="#" class="hover:text-black transition-colors">价格</a>
          <a href="#" class="hover:text-black transition-colors">联系我们</a>
        </div>
        <div class="bg-black text-white px-3 py-1 rounded-full text-[11px] font-semibold hover:bg-gray-800 transition-all cursor-pointer">
          立即体验
        </div>
      </nav>
    </header>

    <main class="pt-12">
      <!-- 2. 沉浸式核心价值区 (Hero Section) -->
      <section class="relative overflow-hidden pt-24 pb-32 md:pt-40 md:pb-48 text-center bg-white">
        <div class="container mx-auto px-6 max-w-5xl relative z-10">
          <h1 class="text-5xl md:text-7xl font-bold tracking-tighter mb-6 bg-clip-text text-transparent bg-gradient-to-b from-gray-900 to-gray-600 leading-tight">
            以 SQL 之名，<br class="hidden md:block"/>重塑您的 API 开发。
          </h1>
          <p class="text-xl md:text-2xl text-gray-500 font-medium mb-12 max-w-2xl mx-auto tracking-tight leading-relaxed">
            coflxl 低代码平台：告别繁琐编码，用数据思维快速构建、发布及管理企业级接口。
          </p>
          <div class="flex flex-col sm:flex-row items-center justify-center gap-6">
            <button class="w-full sm:w-auto bg-blue-600 text-white font-semibold py-4 px-10 rounded-full text-lg hover:bg-blue-700 transition-all shadow-lg shadow-blue-200">
              免费开始
            </button>
            <button class="w-full sm:w-auto group flex items-center justify-center text-blue-600 font-semibold text-lg hover:underline transition-all">
              查看演示视频
              <span class="ml-2 group-hover:translate-x-1 transition-transform">→</span>
            </button>
          </div>
        </div>
        <!-- 装饰性渐变背景 -->
        <div class="absolute top-0 left-1/2 -translate-x-1/2 w-full h-full pointer-events-none opacity-20 bg-[radial-gradient(circle_at_center,_var(--tw-gradient-stops))] from-blue-200 via-transparent to-transparent"></div>
      </section>

      <!-- 3. 产品特写区 (Bento Style Overview) -->
      <section class="py-24 bg-gray-50">
        <div class="container mx-auto px-6 max-w-6xl">
          <div class="text-center mb-16">
            <h2 class="text-3xl md:text-5xl font-bold tracking-tight mb-4">什么是 coflxl？</h2>
            <p class="text-gray-500 text-lg max-w-2xl mx-auto">它是专为开发者打造的创新型低代码开发平台，让复杂的后台逻辑回归简单的 SQL 定义。</p>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-8 items-center">
            <div class="order-2 md:order-1">
              <div class="bg-white rounded-[32px] p-2 shadow-2xl shadow-gray-200 overflow-hidden group">
                <img src="/marketing/接口管理.png" alt="接口管理" class="rounded-[28px] w-full h-auto group-hover:scale-105 transition-transform duration-700">
              </div>
            </div>
            <div class="order-1 md:order-2 space-y-6">
              <h3 class="text-4xl font-bold tracking-tight">轻松管理您的 API。</h3>
              <p class="text-lg text-gray-500 leading-relaxed font-medium">
                通过极其纯净的直观界面，您可以轻松完成 API 的创建、版本控制及流量监控。coflxl 提供的全生命周期管理，让团队协作像空气一样自然。
              </p>
              <ul class="space-y-3 text-gray-600">
                <li class="flex items-center"><span class="text-blue-600 mr-2">✓</span> 实时日志追踪</li>
                <li class="flex items-center"><span class="text-blue-600 mr-2">✓</span> 多环境一键切换</li>
                <li class="flex items-center"><span class="text-blue-600 mr-2">✓</span> 自动化文档生成</li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      <!-- 4. 优势模块 (Key Features Bento Grid) -->
      <section class="py-24 bg-white">
        <div class="container mx-auto px-6 max-w-6xl">
          <h2 class="text-3xl md:text-5xl font-bold text-center mb-20 tracking-tight">强大，而不失优雅。</h2>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <!-- SQL 驱动 - 大卡片 -->
            <div class="md:col-span-2 bg-gray-50 rounded-[32px] p-10 flex flex-col justify-between hover:bg-gray-100 transition-colors group cursor-default">
              <div class="max-w-md">
                <h4 class="text-2xl font-bold mb-4">SQL 驱动接口</h4>
                <p class="text-gray-500 text-lg font-medium leading-relaxed">
                  消除厚重的后端逻辑。直接利用 SQL 语句、存储过程定义 RESTful API。数据即接口，逻辑即服务。
                </p>
              </div>
              <div class="mt-8 border border-gray-200 rounded-xl bg-white p-4 font-mono text-sm text-blue-600 self-start shadow-sm group-hover:shadow-md transition-shadow">
                SELECT * FROM api_data WHERE id = :id;
              </div>
            </div>

            <!-- 可视化管理 -->
            <div class="bg-gray-50 rounded-[32px] p-10 hover:bg-gray-100 transition-colors">
              <h4 class="text-2xl font-bold mb-4 text-green-600">可视化管理</h4>
              <p class="text-gray-500 font-medium">配置化而非编码，将学习曲线降至冰点。快速迭代，实时见效。</p>
              <div class="mt-8 overflow-hidden rounded-xl border border-gray-200 shadow-sm">
                <img src="/marketing/数据连接.png" alt="数据连接" class="w-full h-auto grayscale hover:grayscale-0 transition-all duration-500">
              </div>
            </div>

            <!-- 扩展性 -->
            <div class="bg-gray-50 rounded-[32px] p-10 hover:bg-gray-100 transition-colors">
              <h4 class="text-2xl font-bold mb-4 text-purple-600">强大的扩展性</h4>
              <p class="text-gray-500 font-medium">适配多种主流数据库，灵活的参数映射与转换机制，应对各种复杂业务挑战。</p>
            </div>

            <!-- 安全控制 - 大卡片 -->
            <div class="md:col-span-2 bg-gray-900 text-white rounded-[32px] p-10 flex items-center justify-between group overflow-hidden relative">
              <div class="max-w-sm relative z-10">
                <h4 class="text-2xl font-bold mb-4">企业级安全标准</h4>
                <p class="text-gray-400 font-medium">精细化的 API 权限管理与动态流控，全方位护航您的企业数字资产。</p>
              </div>
              <div class="text-6xl group-hover:scale-110 transition-transform duration-500 opacity-20">🛡️</div>
            </div>
          </div>
        </div>
      </section>

      <!-- 5. 代码生成亮点 (Code Gen) -->
      <section class="py-24 bg-gray-50 relative overflow-hidden">
        <div class="container mx-auto px-6 max-w-6xl">
          <div class="flex flex-col md:flex-row items-center gap-16">
            <div class="md:w-1/2">
              <h2 class="text-4xl md:text-5xl font-bold tracking-tight mb-8">智能生成，<br/>分秒必争。</h2>
              <p class="text-lg text-gray-500 mb-8 font-medium">
                内置智能代码生成引擎，根据接口定义自动产出前后端交互代码。这不仅仅是效率，这是对开发秩序的尊重。
              </p>
              <div class="space-y-4">
                <div class="flex items-center text-gray-700 font-medium bg-white/50 p-4 rounded-2xl border border-gray-100">
                  <span class="w-8 h-8 flex items-center justify-center bg-blue-100 text-blue-600 rounded-full mr-4 text-sm font-bold">1</span>
                  一键生成 CRUD 基础逻辑
                </div>
                <div class="flex items-center text-gray-700 font-medium bg-white/50 p-4 rounded-2xl border border-gray-100">
                  <span class="w-8 h-8 flex items-center justify-center bg-blue-100 text-blue-600 rounded-full mr-4 text-sm font-bold">2</span>
                  参数自动校验与类型推导
                </div>
                <div class="flex items-center text-gray-700 font-medium bg-white/50 p-4 rounded-2xl border border-gray-100">
                  <span class="w-8 h-8 flex items-center justify-center bg-blue-100 text-blue-600 rounded-full mr-4 text-sm font-bold">3</span>
                  自由定制的多语言模板
                </div>
              </div>
            </div>
            <div class="md:w-1/2">
              <div class="bg-white p-2 rounded-[40px] shadow-2xl shadow-gray-200">
                <img src="/marketing/代码生成.png" alt="代码生成" class="rounded-[32px] w-full h-auto">
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 6. 定价方案 (Pricing - Minimalist Card) -->
      <section class="py-32 bg-white">
        <div class="container mx-auto px-6 max-w-4xl text-center">
          <h2 class="text-4xl md:text-6xl font-extrabold tracking-tighter mb-20">灵活的合作模式。</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
            <div class="p-12 rounded-[40px] border border-gray-100 bg-gray-50 hover:border-blue-200 transition-all group">
              <h3 class="text-2xl font-bold mb-4">平台订阅 / 授权</h3>
              <p class="text-gray-500 font-medium mb-8">适合快速成长的研发团队，享受持续的版本演进与即时技术支持。</p>
              <button class="bg-black text-white font-semibold py-3 px-8 rounded-full hover:bg-gray-800 transition-colors">咨询定价</button>
            </div>
            <div class="p-12 rounded-[40px] border border-gray-100 bg-gray-50 hover:border-blue-200 transition-all group">
              <h3 class="text-2xl font-bold mb-4">商业源码授权</h3>
              <p class="text-gray-500 font-medium mb-8">专为追求自主可控、私有部署的企业级客户设计。完整的源码生态掌控权。</p>
              <button class="bg-black text-white font-semibold py-3 px-8 rounded-full hover:bg-gray-800 transition-colors">获取详情</button>
            </div>
          </div>
        </div>
      </section>

      <!-- 7. 联系流 (Contact Form & QR) -->
      <section class="py-24 bg-gray-50">
        <div class="container mx-auto px-6 max-w-5xl">
          <div class="flex flex-col md:flex-row gap-20">
            <div class="md:w-1/3 space-y-12">
              <div>
                <h2 class="text-3xl font-bold mb-4">即刻连接。</h2>
                <p class="text-gray-500 font-medium">我们的专家将为您提供专属的解决方案建议。</p>
              </div>
              <div class="flex gap-8">
                <div class="text-center group">
                  <div class="w-32 h-32 bg-white p-2 rounded-2xl shadow-sm group-hover:shadow-md transition-shadow">
                    <img src="/marketing/wechat_qr.jpg" alt="微信" class="w-full h-full rounded-lg">
                  </div>
                  <p class="text-[12px] text-gray-500 mt-2 font-semibold tracking-wider uppercase">WeChat</p>
                </div>
                <div class="text-center group">
                  <div class="w-32 h-32 bg-white p-2 rounded-2xl shadow-sm group-hover:shadow-md transition-shadow">
                     <img src="/marketing/QQ.jpg" alt="QQ" class="w-full h-full rounded-lg">
                  </div>
                  <p class="text-[12px] text-gray-500 mt-2 font-semibold tracking-wider uppercase">QQ</p>
                </div>
              </div>
            </div>
            <div class="md:w-2/3">
              <form class="bg-white p-10 rounded-[40px] shadow-2xl shadow-gray-200/50 space-y-6">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div class="space-y-2">
                    <label class="text-[12px] font-bold text-gray-400 uppercase tracking-widest ml-1">您的姓名</label>
                    <input type="text" placeholder="刘先生/女士" class="w-full px-5 py-3 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-blue-100 transition-all outline-none font-medium">
                  </div>
                  <div class="space-y-2">
                    <label class="text-[12px] font-bold text-gray-400 uppercase tracking-widest ml-1">公司全称</label>
                    <input type="text" placeholder="XX 科技股份有限公司" class="w-full px-5 py-3 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-blue-100 transition-all outline-none font-medium">
                  </div>
                </div>
                <div class="space-y-2">
                  <label class="text-[12px] font-bold text-gray-400 uppercase tracking-widest ml-1">电子邮箱</label>
                  <input type="email" placeholder="example@company.com" class="w-full px-5 py-3 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-blue-100 transition-all outline-none font-medium">
                </div>
                <div class="space-y-2">
                  <label class="text-[12px] font-bold text-gray-400 uppercase tracking-widest ml-1">具体需求</label>
                  <textarea rows="4" placeholder="请简述您的业务场景..." class="w-full px-5 py-3 bg-gray-50 border-none rounded-xl focus:ring-2 focus:ring-blue-100 transition-all outline-none font-medium resize-none"></textarea>
                </div>
                <button type="submit" class="w-full bg-blue-600 text-white font-bold py-4 rounded-2xl hover:bg-blue-700 transition-all">发送申请</button>
                <p class="text-[11px] text-gray-400 text-center font-medium">我们致力于保护您的隐私，您的信息仅用于此次商业交流。</p>
              </form>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 8. 极简页脚 (Footer) -->
    <footer class="bg-white py-12 border-t border-gray-100">
      <div class="container mx-auto px-6 max-w-6xl">
        <div class="flex flex-col md:flex-row justify-between items-center text-gray-400 font-medium text-sm gap-6">
          <div class="text-gray-900 font-bold text-lg tracking-tighter">coflxl</div>
          <div>&copy; 2026 coflxl. 精选技术，赋能未来。</div>
          <div class="flex space-x-6">
            <a href="#" class="hover:text-gray-900 transition-colors">隐私政策</a>
            <a href="#" class="hover:text-gray-900 transition-colors">服务条款</a>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
// 脚本逻辑保持不变
</script>

<style scoped>
/* 移除冗余样式，全部由 Tailwind 处理 */
.min-h-screen {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>