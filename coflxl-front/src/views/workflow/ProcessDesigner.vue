<template>
  <div class="h-full bg-white flex flex-col p-4 rounded-lg shadow-sm relative">
    <div class="flex justify-between items-center mb-4">
      <h2 class="text-lg font-medium">流程设计器 <span class="text-xs text-gray-500 ml-2">(请点击画布右上方的 💾 按钮保存流程)</span></h2>
    </div>

    <div class="flex-1 relative border rounded overflow-hidden flex bg-gray-50">
      <iframe
          v-if="dataReady"
          ref="myFrame"
          src="https://nayacco.github.io/workflow-bpmn-modeler/cdn/0.2.8/"
          id="myFrame"
          frameborder="0"
          class="w-full h-full"
          @load="onIframeLoad"
      ></iframe>
      <div v-else class="w-full h-full flex items-center justify-center text-gray-400">
        正在加载流程数据...
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import request from '../../utils/request'

const route = useRoute()
const myFrame = ref<HTMLIFrameElement | null>(null)

const users = ref<any[]>([])
const groups = ref<any[]>([]) // 1. 定义变量
const forms = ref<any[]>([])

const initialXml = ref('')
const dataReady = ref(false)

const loadUsers = async () => {
  const res: any = await request.get('/admin/wf/users')
  // 转为 iframe 所需格式
  users.value = (res || []).map((u: any) => ({
    name: `${u.nickname}(${u.username})`,
    id: String(u.id)
  }))
}

// 2. 加载逻辑
const loadRoles = async () => {
  const res: any = await request.get('/admin/wf/roles')
  groups.value = res || []
}

const loadForms = async () => {
  const res: any = await request.get('/admin/wf/form/list')
  forms.value = (res || []).map((f: any) => ({
    name: `${f.name}`,
    id: String(f.id)
  }))
}


const onIframeLoad = () => {
  if (!myFrame.value) return
  let postMsg = {
    xml: initialXml.value, // 后端查询到的xml，新建则为空串
    users: JSON.parse(JSON.stringify(users.value)), // 👈 深度拷贝去除 Vue Proxy 特性，防止 postMessage 克隆报错
    groups: JSON.parse(JSON.stringify(groups.value)), // 使用动态加载的数据
    categorys: JSON.parse(JSON.stringify(forms.value.length ? forms.value : [{ name: "基础通用", id: "GENERAL" }])),
    isView: false
  }
  // 给 iframe 发送初始化消息
  myFrame.value.contentWindow?.postMessage(JSON.parse(JSON.stringify(postMsg)), "*")
}

const handleMessage = async (event: MessageEvent) => {
  // 只处理包含 xml 数据的合法消息返回，过滤掉其他的插件/系统级别消息
  if (event.data && event.data.xml) {
    const { xml, process } = event.data

    const finalName = process?.name || '未命名流程定义'
    const finalTypeCode = process?.category || 'GENERAL'

    try {
      await request.post('/admin/wf/def/save', {
        id: route.query.id ? Number(route.query.id) : null,
        name: finalName,
        typeCode: finalTypeCode,
        xmlData: xml
      })
      ElMessage.success('流程流转图保存并部署成功！')
    } catch(e) {
      ElMessage.error('流程部署失败')
    }
  }
}

onMounted(async () => {
  await loadUsers()
  await loadRoles() // 记得调用
  await loadForms()

  if (route.query.id) {
    const res: any = await request.get(`/admin/wf/def/detail/${route.query.id}`)
    if (res && res.xmlData) {
      initialXml.value = res.xmlData
    }
  } else {
    initialXml.value = ''
  }

  // 数据加载完毕，放行 iframe 渲染
  dataReady.value = true

  // 接收 iframe 发来的消息
  window.addEventListener("message", handleMessage)
})

onBeforeUnmount(() => {
  window.removeEventListener("message", handleMessage)
})
</script>

<style scoped>
/* 避免覆盖其他页面的样式 */
::v-deep(.bjs-powered-by) {
  display: none !important;
}
</style>
