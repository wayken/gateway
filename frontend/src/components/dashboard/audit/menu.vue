<template>
  <div class="a-audit-menu">
    <div class="head">
      <a-svg-icon icon-class="history" />
      <span>{{ $t('menu.audit') }}</span>
    </div>
    <ul>
      <li v-for="(data, index) in loadMenuList" :key="index"
        :class="{
          'is-actived': isPathMatched(data)
        }"
        @click="handleClick(data)"
      >
        <el-icon :style="{ color: data.color }">
          <component :is="data.icon"></component>
        </el-icon>
        <div class="text">{{ $t('submenu.audit.' + data.name) }}</div>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import {
  Document,
  Avatar
} from '@element-plus/icons-vue'

const loadMenuList = [
  {
    name: 'session',
    path: '/audit/session',
    color: '#ffc107',
    icon: markRaw(Avatar)
  },
  {
    name: 'operation',
    path: '/audit/operation',
    color: '#fc587b',
    icon: markRaw(Document)
  }
]

const router = useRouter()
const handleEmit = defineEmits(['click'])
const isPathMatched = (menu: any) => {
  const path = menu.path
  const route = router.currentRoute.value
  if (route.path === path) {
    return true
  }
  return false
}
const handleClick = (data: any) => {
  router.push(data.path)
  handleEmit('click', data.path)
}
</script>
