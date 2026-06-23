<template>
  <div class="menu inline-flex-c-n-n">
    <div class="zone inline-flex-r-c-n">
      <a-svg-icon icon-class="arrow-left"
        @click="handleLinkHome"
      />
      <div class="name inline-text-ellipsis" @click="handleSwitch">
        <template v-if="!isGlobalZone(infomation.id)">
          {{ infomation.name }}
        </template>
        <template v-else>
          {{ $t('website.global-zone') }}
        </template>
      </div>
      <a-svg-icon class="collapse" icon-class="collapse" size="22px"
        @click="handleSequence"
      />
    </div>
    <div class="guide">
      <ul>
        <li v-for="(data, index) in loadMenuList" :key="index" v-permit="data.permissions"
          :class="{
            'is-actived': isPathMatched(data)
          }"
        >
          <div class="menu" @click="handleClick(data, index)">
            <a-svg-icon :icon-class="data.icon"
              :style="{
                color: data.color
              }"
            />
            <div class="text">{{ $t('menu.' + data.name) }}</div>
            <div v-if="data.children" class="arrow"
              :class="{
                'is-opened': isMenuOpened(index)
              }"
            ></div>
          </div>
          <template v-if="data.children">
            <ul
              :class="{
                'is-closed': !isMenuOpened(index)
              }"
              :style="{
                'max-height': `${48 * (<number> data.children.length)}px`
              }"
            >
              <li v-for="(submenu, subindex) in data.children" :key="subindex" v-permit="submenu.permissions"
                :class="{
                  'is-actived': isPathMatched(submenu)
                }"
                @click="handleSubMenuClick(submenu)"
              >
                <div class="name">{{ $t(submenu.lang) }}</div>
              </li>
            </ul>
          </template>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { isGlobalZone } from '@/hooks/page/useZone'

const props = defineProps({
  collapse: {
    type: Boolean,
    default: false
  },
  infomation: {
    type: Object,
    default: () => ({})
  }
})

const loadMenuList = [
  {
    name: 'route',
    path: '/zone/route',
    icon: 'route',
    color: '#409efc',
    permissions: ['website:route:list']
  },
  {
    name: 'upstream',
    path: '/zone/upstream',
    icon: 'palette',
    color: '#fc587b',
    permissions: ['website:upstream:list']
  },
  {
    name: 'protection',
    path: '/protection',
    icon: 'shield',
    color: '#22d7bb',
    permissions: ['website:protection:list'],
    children: [
      {
        name: 'rule',
        lang: 'submenu.protection.rule',
        path: '/zone/protection/rule',
        permissions: ['website:protection:rule:list']
      },
      {
        name: 'limit',
        lang: 'submenu.protection.limit',
        path: '/zone/protection/limit',
        permissions: ['website:protection:limit:list']
      },
      {
        name: 'semantics',
        lang: 'submenu.protection.semantics',
        path: '/zone/protection/semantics',
        permissions: ['website:protection:semantics:list']
      }
    ]
  },
  {
    name: 'cache',
    path: '/cache',
    icon: 'database',
    color: '#f6821f',
    permissions: ['website:cache:list'],
    children: [
      {
        name: 'rule',
        lang: 'submenu.cache.rule',
        path: '/zone/cache/rule',
        permissions: ['website:cache:rule:list']
      }
    ]
  },
  {
    name: 'rewrite',
    path: '/rewrite',
    icon: 'rewrite',
    color: '#ff8801',
    permissions: ['website:rewrite:list'],
    children: [
      {
        name: 'redirect',
        lang: 'submenu.rewrite.redirect',
        path: '/zone/rewrite/redirect',
        permissions: ['website:rewrite:redirect:list']
      },
      {
        name: 'request',
        lang: 'submenu.rewrite.request',
        path: '/zone/rewrite/request',
        permissions: ['website:rewrite:request:list']
      },
      {
        name: 'response',
        lang: 'submenu.rewrite.response',
        path: '/zone/rewrite/response',
        permissions: ['website:rewrite:response:list']
      }
    ]
  },
  {
    name: 'plugin',
    path: '/zone/plugin',
    icon: 'box',
    color: '#5e34ba',
    permissions: ['website:plugin:list']
  },
  {
    name: 'page',
    path: '/zone/page',
    icon: 'documents',
    color: '#34c724',
    permissions: ['website:page:list']
  }
]
const loadOpenedMenuList = ref<number[]>([])

onMounted(() => {
  // 当选中子菜单时自动展现菜单项
  handleMenuAutoOpen()
})

const router = useRouter()
const isPathMatched = (menu: any) => {
  const route = router.currentRoute.value
  if (route.meta.root) {
    return route.meta.root === menu.name
  }
  const id = route.params.id
  const path = `${menu.path}/${id}`
  if (route.path === path) {
    return true
  }
  return false
}
const isMenuOpened = (index: number) => {
  return loadOpenedMenuList.value.indexOf(index) !== -1
}
const handleMenuAutoOpen = () => {
  const dataList = loadMenuList
  const route = router.currentRoute.value
  for (let i = 0, len = dataList.length; i < len; i++) {
    const menu = dataList[i]
    if (!menu.children) {
      continue
    }
    // 如果是自动展开菜单则展开全部菜单，否则展开当前匹配的菜单
    if (!props.collapse) {
      loadOpenedMenuList.value.push(i)
      continue
    }
    const child = menu.children
    for (let m = 0, len2 = child.length; m < len2; m++) {
      const submenu = child[m]
      const id = route.params.id
      if (route.path === `${submenu.path}/${id}`) {
        loadOpenedMenuList.value.push(i)
        break
      }
    }
  }
}
const handleLinkHome = () => {
  router.push({ path: '/' })
}
const handleEmit = defineEmits(['click', 'switch', 'sequence'])
const handleClick = (data: any, index: number) => {
  if (data.children) {
    if (!isMenuOpened(index)) {
      loadOpenedMenuList.value.push(index)
    } else {
      const i = loadOpenedMenuList.value.indexOf(index)
      if (i !== -1) {
        loadOpenedMenuList.value.splice(i, 1)
      }
    }
  } else {
    const route = router.currentRoute.value
    const id = route.params.id
    const path = `${data.path}/${id}`
    router.push(path)
    handleEmit('click', data.path)
  }
}
const handleSubMenuClick = (data: any) => {
  const route = router.currentRoute.value
  const id = route.params.id
  const path = `${data.path}/${id}`
  router.push(path)
  handleEmit('click', path)
}
const handleSwitch = () => {
  handleEmit('switch')
}
const handleSequence = () => {
  handleEmit('sequence')
}
</script>
