<template>
  <div class="a-button"
    :class="[
      type ? `is-${type}` : '',
      size,
      {
        'is-link': link,
        'is-disabled': disabled
      }
    ]"
    @click="handleClick"
  >
    <el-icon v-if="icon || $slots.icon">
      <component :is="icon" />
    </el-icon>
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: 'AButton'
})

const props = defineProps({
  icon: {
    type: [String, Object]
  },
  type: {
    type: String
  },
  size: {
    type: String,
    default: 'medium'
  },
  link: {
    type: Boolean
  },
  disabled: {
    type: Boolean
  }
})

const handleEmit = defineEmits(['click'])
const handleClick = (event: Event) => {
  if (!props.disabled) {
    handleEmit('click', event)
  }
}
</script>
