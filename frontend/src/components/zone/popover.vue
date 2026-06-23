<template>
  <div class="a-menu--popover"
    :class="{
      'is-show': visible
    }"
    :style="{
      'top': position + 'px'
    }"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  >
    <ul>
      <li v-for="(menu, index) in menus" :key="index"
        :class="{
          'is-actived': isPathMatched(menu)
        }"
        @click="handleMenuClick(menu)"
      >
        {{ $t(menu.meta.lang) }}
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
defineProps({
  visible: {
    type: Boolean,
    default: true
  },
  position: {
    type: Number,
    default: 0
  },
  menus: {
    type: Array<any>,
    default() {
      return []
    }
  }
})

const router = useRouter()
const isPathMatched = (menu: any) => {
  const path = menu.path
  const route = router.currentRoute.value
  if (route.path === path) {
    return true
  }
  return route.matched[1] && route.matched[1].path === path
}
const handleEmit = defineEmits(['update:visible'])
const handleMouseEnter = () => {
  handleEmit('update:visible', true)
}
const handleMouseLeave = () => {
  handleEmit('update:visible', false)
}
const handleMenuClick = (menu: any) => {
  const path = menu.path
  const route = router.currentRoute.value
  if (route.path !== path) {
    router.push({ path })
    handleEmit('update:visible', false)
  }
}
</script>
