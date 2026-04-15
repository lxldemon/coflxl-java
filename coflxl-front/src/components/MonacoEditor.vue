<template>
  <div class="monaco-editor-container" :style="{ height: height }">
    <vue-monaco-editor
        v-model:value="code"
        :language="language"
        :theme="theme"
        :options="editorOptions"
        @mount="handleMount"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { VueMonacoEditor } from '@guolao/vue-monaco-editor'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  language: {
    type: String,
    default: 'sql' // 默认 sql，也可以传 json 等
  },
  readOnly: {
    type: Boolean,
    default: false
  },
  height: {
    type: String,
    default: '400px'
  },
  theme: {
    type: String,
    default: 'vs-dark' // 默认深色主题，可选 'vs' (浅色)
  }
})

const emit = defineEmits(['update:modelValue', 'blur'])

const code = ref(props.modelValue)

watch(() => props.modelValue, (newVal) => {
  if (newVal !== code.value) {
    code.value = newVal
  }
})

watch(code, (newVal) => {
  if (newVal !== props.modelValue) {
    emit('update:modelValue', newVal)
  }
})

const handleMount = (editor: any) => {
  editor.onDidBlurEditorWidget(() => {
    emit('blur')
  })
}

const editorOptions = computed(() => ({
  readOnly: props.readOnly,
  minimap: { enabled: false }, // 关闭小地图
  automaticLayout: true, // 自动布局适应容器大小
  fontSize: 14,
  scrollBeyondLastLine: false, // 不允许滚动超过最后一行
  wordWrap: 'on', // 自动换行
  formatOnPaste: true,
  tabSize: 2
}))
</script>

<style scoped>
.monaco-editor-container {
  width: 100%;
  border-radius: 0.375rem;
  overflow: hidden;
}
</style>
