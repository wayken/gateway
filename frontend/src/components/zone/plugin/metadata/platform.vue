<template>
  <el-drawer :model-value="visible" direction="rtl" size="720" class="a-metadata platform"
    @opened="handleOpen"
    @close="handleClose"
  >
    <template #header>
      <div class="head">{{ $t('plugin.plugin-metadata') }}</div>
    </template>
    <div class="content inline-flex-c-n-n" v-loading="isMonacoLoading">
      <div ref="monacoRef" class="monaco"></div>
    </div>
    <template #footer>
      <el-button @click="handleClose">
        {{ $t('common.cancel') }}
      </el-button>
      <el-button type="primary" :icon="Promotion" @click="handleSubmit">
        {{ $t('common.confirm') }}
      </el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import {
  Promotion
} from '@element-plus/icons-vue'
import '@/components/common/monaco/worker'
import * as monaco from 'monaco-editor'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  name: {
    type: String,
    default: ''
  },
  value: {
    type: String,
    default: ''
  }
})

const monacoRef = ref<HTMLElement | null>(null)
let monacoInstance: monaco.editor.IStandaloneCodeEditor | null = null
const isMonacoLoading = ref(true)
const isMonacoUpdated = ref(false)

const handleEmit = defineEmits(['update:visible', 'change'])
const handleOpen = () => {
  monacoInstance = monaco.editor.create(<HTMLElement> monacoRef.value, {
    value: props.value,
    language: 'json',
    theme: 'vs-dark',
    automaticLayout: true // 自动布局，窗口大小变化时调整编辑器大小
  })
  isMonacoLoading.value = false
  monacoInstance.onDidChangeModelContent(() => {
    isMonacoUpdated.value = true
  })
}
const handleClose = () => {
  isMonacoLoading.value = true
  handleEmit('update:visible', false)
  if (monacoInstance) {
    monacoInstance.dispose()
  }
}
const handleSubmit = () => {
  if (monacoInstance && isMonacoUpdated.value) {
    let value
    try {
      value = {
        name: props.name,
        metadata: JSON.parse(monacoInstance.getValue())
      }
    } catch (error) {
      return
    }
    handleEmit('change', value)
  }
  handleClose()
}
</script>
