<template>
  <div class="a-setting-menu">
    <div class="head">
      <a-svg-icon icon-class="settings" />
      <span>{{ $t('menu.setting') }}</span>
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
        <div class="text">{{ $t('submenu.setting.' + data.name) }}</div>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import {
  Tools,
  Collection,
  Document,
  MessageBox,
  MostlyCloudy
} from '@element-plus/icons-vue'

const loadMenuList = [
  {
    name: 'preference',
    path: '/setting/preference',
    color: '#ffc107',
    icon: markRaw(Tools)
  },
  {
    name: 'ips',
    path: '/setting/ips',
    color: '#fc587b',
    icon: markRaw(Collection)
  },
  {
    name: 'certificate',
    path: '/setting/certificate',
    color: '#ff8801',
    icon: markRaw(Document)
  },
  {
    name: 'auths',
    path: '/setting/auths',
    color: '#5e34ba',
    icon: markRaw(MessageBox)
  },
  {
    name: 'provider',
    path: '/setting/provider',
    color: '#409eff',
    icon: markRaw(MostlyCloudy)
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
