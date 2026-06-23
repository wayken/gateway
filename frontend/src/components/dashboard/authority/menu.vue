<template>
  <div class="a-authority-menu">
    <div class="head">
      <a-svg-icon icon-class="shield-user" />
      <span>{{ $t('menu.authority') }}</span>
    </div>
    <ul>
      <li v-for="(data, index) in loadMenuList" :key="index" v-permit="data.permissions"
        :class="{
          'is-actived': isPathMatched(data)
        }"
        @click="handleClick(data)"
      >
        <el-icon :style="{ color: data.color }">
          <component :is="data.icon"></component>
        </el-icon>
        <div class="text">{{ $t('submenu.authority.' + data.name) }}</div>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import {
  Lock,
  Document,
  Avatar
} from '@element-plus/icons-vue'

const loadMenuList = [
  {
    name: 'user',
    path: '/authority/user',
    color: '#ffc107',
    icon: markRaw(Avatar),
    permissions: ['system:authority:user:list']
  },
  {
    name: 'role',
    path: '/authority/role',
    color: '#fc587b',
    icon: markRaw(Document),
    permissions: ['system:authority:role:list']
  },
  {
    name: 'permissions',
    path: '/authority/permissions',
    color: '#00b3db',
    icon: markRaw(Lock),
    permissions: ['system:authority:permissions:list']
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
