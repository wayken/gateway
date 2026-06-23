<template>
  <div class="a-drawer"
    :class="{
      'is-closed': closed,
      'is-mobile': isMobile() || isTablet(),
      'is-dragging': dragging
    }"
    :style="{
      width: width + 'px'
    }"
  >
    <div class="wrapper"
    :style="{
        width: width + 'px'
      }"
    >
      <slot></slot>
      <div class="icon" @click="handleDrawerSwitch">
        <el-icon><DArrowLeft /></el-icon>
      </div>
      <div class="flexible" v-if="flexible" @mousedown="handleResize"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { isMobile, isTablet } from '@/hooks/useDevice'
import {
  DArrowLeft
} from '@element-plus/icons-vue'

defineOptions({
  name: 'ADivider'
})

defineProps({
  flexible: {
    type: Boolean,
    default: false
  },
  closed: {
    type: Boolean,
    default: false
  }
})

const width = ref(288)
const dragging = ref(false)

const handleEmit = defineEmits(['switch'])
const handleDrawerSwitch = () => {
  handleEmit('switch')
}
const handleResize = (event: MouseEvent) => {
  const drawerWidth = width.value
  dragging.value = true
  const move = (moveEvent: MouseEvent) => {
    if (moveEvent.x > event.x) {
      const resizeWidth = drawerWidth + (moveEvent.x - event.x)
      if (resizeWidth <= 480) {
        width.value = resizeWidth
      }
    } else {
      const resizeWidth = drawerWidth - (event.x - moveEvent.x)
      if (resizeWidth >= 200) {
        width.value = resizeWidth
      }
    }
  }
  const up = () => {
    dragging.value = false
    document.removeEventListener('mousemove', move)
    document.removeEventListener('mouseup', up)
  }
  document.addEventListener('mousemove', move)
  document.addEventListener('mouseup', up)
}
</script>
