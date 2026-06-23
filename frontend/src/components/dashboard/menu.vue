<template>
  <div class="menu inline-flex-c-n-n">
    <div class="logo inline-flex-r-c-c">
      <img :src="logoSvg" />
    </div>
    <div class="guide inline-flex-c-c-n">
      <ul>
        <li v-for="(data, index) in loadMenuList" :key="index" v-permit="data.permissions"
          :class="{
            'is-actived': isPathMatched(data)
          }"
          @click="handleClick(data)"
        >
          <a-svg-icon :icon-class="data.icon"
            :style="{
              color: data.color
            }"
          />
          <div class="text">{{ $t('menu.' + data.name) }}</div>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import logoSvg from '@/assets/logo.svg'

const loadMenuList = [
  {
    name: 'website',
    path: '/website',
    icon: 'home',
    color: '#409efc',
    permissions: ['system:website:list']
  },
  {
    name: 'tool',
    path: '/tool',
    icon: 'widget2',
    color: '#34c724',
    permissions: ['system:tool:list']
  },
  {
    name: 'authority',
    path: '/authority',
    icon: 'shield-user',
    color: '#e4433e',
    permissions: ['system:authority:list']
  },
  {
    name: 'audit',
    path: '/audit',
    icon: 'history',
    color: '#9884ff',
    permissions: ['system:audit:list']
  },
  {
    name: 'service',
    path: '/service',
    icon: 'service',
    color: '#0fc6c2',
    permissions: ['system:service:list']
  },
  {
    name: 'setting',
    path: '/setting',
    icon: 'settings',
    color: '#fc587b',
    permissions: ['system:setting:list']
  }
]

const router = useRouter()
const isPathMatched = (menu: any) => {
  const path = menu.path
  const route = router.currentRoute.value
  if (route.meta.root) {
    return route.meta.root === menu.name
  }
  if (route.path === path) {
    return true
  }
  return false
}
const handleEmit = defineEmits(['click'])
const handleClick = (data: any) => {
  router.push(data.path)
  handleEmit('click', data.path)
}
</script>
