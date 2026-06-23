<template>
  <div class="a-head inline-flex-r-c-b">
    <div class="a-head--left inline-flex-r-c-n">
      <slot name="head"></slot>
    </div>
    <div class="a-head--right inline-flex-r-c-n">
      <div v-if="!isMobile()" class="icon">
        <a-svg-icon icon-class="info" size="22px" />
      </div>
      <el-dropdown v-if="!isMobile()" trigger="click" size="large" :teleported="false"
        @command="handleAppearanceSwitch"
      >
        <div class="icon">
          <a-svg-icon icon-class="widget" size="22px" />
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="light">
              <a-svg-icon icon-class="sun" size="20px" style="margin-right: 8px;"/>
              <div class="name">{{ $t('head.mode-light') }}</div>
            </el-dropdown-item>
            <el-dropdown-item command="dark">
              <a-svg-icon icon-class="moon" size="20px"  style="margin-right: 8px;" />
              <div class="name">{{ $t('head.mode-dark') }}</div>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <a-divider v-if="!isMobile()" direction="vertical" :height="22"></a-divider>
      <el-select v-if="!isMobile()" :model-value="loadCurrentLocalLang" style="width: 152px"
        @change="handleLangSwitch"
      >
        <el-option v-for="(data, key) in langList" :key="key"
          :value="data.key"
          :label="data.description"
        />
      </el-select>
      <el-dropdown trigger="click" size="large" :teleported="false"
        @visible-change="handleAccountDropdown"
      >
        <div class="account inline-flex-r-c-n"
          :class="{
            'is-actived': isAccountDropdown
          }"
        >
          <el-avatar :size="32" :icon="UserFilled" :src="account.avatar" />
          <div class="name">{{ account.name || '-'}}</div>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>
              <el-icon><Setting /></el-icon>
              <span>{{ $t('head.account-setting') }}</span>
            </el-dropdown-item>
            <el-dropdown-item @click="handleXhrLogout">
              <el-icon><SwitchButton /></el-icon>
              <span>{{ $t('head.logout') }}</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  UserFilled,
  ArrowDown,
  Setting,
  SwitchButton
} from '@element-plus/icons-vue'
import Mitter from '@/utils/mitt'
import langList from '@/locale/lang.json'
import Constant from '@/config/constant'
import {
  getCookie,
  removeCookie
} from '@/utils/cookie'
import { isMobile } from '@/hooks/useDevice'
import { useRequest } from '@/hooks/useRequest'
import { setLocalLang } from '@/utils/locale'
import { useAuthStore } from '@/store/modules/auth'

const isAccountDropdown = ref(false)
const {
  account
} = storeToRefs(useAuthStore())
const { iopost } = useRequest()

// 变换外观
const handleAppearanceSwitch = (payload: any) => {
  Mitter.emit('mitt-appearance-switch', payload === 'dark')
}
// 切换多语言
const i18n = useI18n()
const loadCurrentLocalLang = computed(() => {
  for (const data of langList) {
    if (data.key === i18n.locale.value) {
      return data.description
    }
  }
  return langList[0].description
})
const handleLangSwitch = (locale: string) => {
  i18n.locale.value = locale
  setLocalLang(locale)
}
// 账号下拉功能
const handleAccountDropdown = (value: boolean) => {
  isAccountDropdown.value = value
}
const handleXhrLogout = () => {
  const authToken = getCookie(Constant.X_Auth_Token)
  const token = decodeURIComponent(authToken)
  const params = {
    token: token
  }
  iopost('session', 'xhrSessionLogout', params).finally(() => {
    const domain = window.location.host.split(':')[0]
    removeCookie(Constant.X_Auth_Token, domain)
    window.location.href = import.meta.env.VITE_LOGIN_URL
  })
}
</script>
