<template>
  <div class="a-touch" ref="touchRef" v-show="isMobile()"
    @click="handleCollapse"
    @touchstart.stop="handleTouchStart"
    @touchmove.stop.prevent="handleTouchMove"
  >
    <div
      :class="{
        'is-show': value
      }"
    >
      <span></span>
      <span></span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { isMobile } from '@/hooks/useDevice'

const props = defineProps({
  value: {
    type: Boolean,
    default: false
  }
})

const loadCoordinate = ref({
  client: {
    x: 0,
    y: 0
  },
  element: {
    top: 0,
    left: 0
  }
})
const touchRef = ref()
const handleEmit = defineEmits(['click'])
const handleCollapse = () => {
  handleEmit('click', !props.value)
}
const handleTouchStart = (event: any) => {
  const target = event.targetTouches[0]
  // 记录点击的坐标
  loadCoordinate.value.client = {
    x: target.clientX,
    y: target.clientY
  }
  // 记录需要移动的元素坐标
  loadCoordinate.value.element = {
    top: touchRef.value.offsetTop,
    left: touchRef.value.offsetLeft
  }
}
const handleTouchMove = (event: any) => {
  let element = event.targetTouches[0]
  // 根据初始 client 位置计算移动距离(元素移动位置=元素初始位置+光标移动后的位置-光标点击时的初始位置)
  let x = loadCoordinate.value.element.left + (element.clientX - loadCoordinate.value.client.x)
  let y = loadCoordinate.value.element.top + (element.clientY - loadCoordinate.value.client.y)
  // 限制可移动距离，不超出可视区域
  const touch = touchRef.value
  x = x <= 0 ? 0 : x >= innerWidth - touch.offsetWidth ? innerWidth - touch.offsetWidth : x
  y = y <= 0 ? 0 : y >= innerHeight - touch.offsetHeight ? innerHeight - touch.offsetHeight : y
  // 移动当前元素
  touch.style.left = x + 'px'
  touch.style.top = y + 'px'
}
</script>
