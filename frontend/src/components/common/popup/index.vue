<template>
  <div class="a-popup" v-if="isShow"
    :style="{
      position: fixed ? 'fixed' : 'absolute'
    }"
  >
    <div class="a-popup__mask" v-if="masked"
      :class="{
        'is-animate': isAnimate
      }"
      @click="handleClose(true)"
    ></div>
    <div class="a-popup__wrapper"
      :class="['is-' + direction, {
        'is-animate': isAnimate
      }]"
      :style="loadPopupStyle"
    >
      <div class="a-popup__wrapper-box">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: 'APopup'
})

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  // 弹窗方向，left/right/center/top/bottom
  direction: {
    type: String,
    default: 'bottom'
  },
  fixed: {
    type: Boolean,
    default: true
  },
  // 是否显示遮罩
  masked: {
    type: Boolean,
    default: true
  },
  width: {
    type: [String, Number]
  },
  height: {
    type: [String, Number]
  }
})

const isShow = ref(false)
const isAnimate = ref(false)
const loadPopupStyle = computed(() => {
  const style: any = {}
  if (props.direction === 'left' || props.direction === 'right') {
    style.width = typeof props.width === 'number' ? `${props.width}px` : props.width
  } else if (props.direction === 'top' || props.direction === 'bottom') {
    style.height = typeof props.height === 'number' ? `${props.height}px` : props.height
  }
  return style
})

onMounted(() => {
  // 添加ESC快捷键监听，重置表单输入状态
  document.addEventListener('keyup', handleEscKeyListen)
})
onBeforeUnmount(() => {
  document.removeEventListener('keyup', handleEscKeyListen)
})

const handleEmit = defineEmits(['open', 'close'])
const handleOpen = () => {
  isShow.value = true
  window.setTimeout(() => {
    isAnimate.value = true
    handleEmit('open')
  }, 100)
}
const handleClose = (isEmit: boolean) => {
  if (isEmit) {
    // 如果是点击遮罩则只是触发close事件，
    // 父组件需要手动设置visiable为false
    handleEmit('close')
  } else {
    // 如果是监听到visiable为false则自动隐藏当前组件
    isAnimate.value = false
    setTimeout(() => {
      isShow.value = false
      handleEmit('close')
    }, 300)
  }
}
// ESC快捷键监听，重置表单输入状态
const handleEscKeyListen = (event: KeyboardEvent) => {
  const keyName = event.code.toUpperCase()
  if ('ESCAPE' === keyName) {
    handleClose(true)
  }
}

watch(() => props.visible, (value) => {
  if (value) {
    handleOpen()
  } else {
    if (isAnimate.value) {
      handleClose(false)
    }
  }
})
</script>
