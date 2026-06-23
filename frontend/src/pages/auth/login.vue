<template>
  <div class="a-auth-login inline-flex-c-n-n">
    <div class="header">
      <el-dropdown size="large" :teleported="false"
        @command="handleLangSwitch"
      >
      <span class="el-dropdown-link">
        {{ loadCurrentLocalLang }}<el-icon><ArrowRight /></el-icon>
      </span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item v-for="(data, key) in langList" :key="key"
            :command="data.key"
          >
            {{ data.description }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
      </el-dropdown>
    </div>
    <a-signin v-if="!isMfaRequired" @next="handleMfaAuth"></a-signin>
    <a-mfa v-else :params="loadMfaParams"></a-mfa>
  </div>
</template>

<script setup lang="ts">
import {
  ArrowRight
} from '@element-plus/icons-vue'
import langList from '@/locale/lang.json'
import { setLocalLang } from '@/utils/locale'
import ASignin from '@/components/auth/login/signin.vue'
import AMfa from '@/components/auth/login/mfa.vue'

const isMfaRequired = ref(false)
const loadMfaParams = ref<any>({})

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
const handleMfaAuth = (data: any) => {
  loadMfaParams.value = data
  isMfaRequired.value = true
}
</script>
