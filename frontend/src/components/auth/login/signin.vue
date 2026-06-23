<template>
  <div class="signin"
    :class="[isMobile() ? 'device' : 'platform']"
  >
    <el-form ref="formRef" size="large" :model="loadFormData" :rules="loadFormRules" :disabled="progression.sending"
      @keyup.enter="handleSubmit"
    >
      <div class="logo inline-flex-r-c-n">
        <img :src="logoSvg" />
        <div class="name">{{ $t('portal.title') }}</div>
      </div>
      <div class="title">{{ $t('login.signin') }}</div>
      <el-form-item prop="acccount">
        <el-input :prefix-icon="Avatar" ref="acccountInputRef" v-model="loadFormData.acccount"
          :placeholder="$t('login.acccount-placeholder')"
        >
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input show-password :prefix-icon="Lock" v-model="loadFormData.password"
          :placeholder="$t('login.password-placeholder')"
        >
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button class="confirm" type="primary" :icon="Promotion"
          :loading="progression.sending"
          @click="handleSubmit"
        >
          {{ $t('login.signin') }}
        </el-button>
      </el-form-item>
      <div class="copyright">
        2024 - 2025 Powered by <a href="https://www.teambeit.com" target="_blank">Teambeit</a>.
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import {
  Avatar,
  Lock,
  Promotion
} from '@element-plus/icons-vue'
import md5 from 'md5'
import logoSvg from '@/assets/logo.svg'
import Constant from '@/config/constant'
import type { FormRules } from 'element-plus'
import { setCookie } from '@/utils/cookie'
import { isMobile } from '@/hooks/useDevice'
import { useRequest } from '@/hooks/useRequest'

const loadFormData = reactive<any>({
  acccount: '',
  password: ''
})

const i18n = useI18n()
const loadFormRules = reactive<FormRules<any>>({
  acccount: [
    {
      required: true,
      message: i18n.t('login.acccount-placeholder')
    }
  ],
  password: [
    {
      required: true,
      message: i18n.t('login.password-placeholder')
    }
  ]
})

const formRef = ref()
const acccountInputRef = ref()
const router = useRouter()
const { iopost, progression } = useRequest()

onMounted(() => {
  acccountInputRef.value.focus()
})

const handleEmit = defineEmits(['next'])
const handleSubmit = () => {
  formRef.value.validate((valid: boolean) => {
    if (!valid) {
      return
    }
    const params = {
      acct: loadFormData.acccount,
      pwd: md5(loadFormData.password)
    }
    iopost('session', 'xhrSessionLogin', params).then((result) => {
      if (result.twofa) {
        // 二次验证
        handleEmit('next', {
          acct: params.acct,
          pwd: params.pwd,
          twofa: result.twofa,
          qrcode: result.qrcode
        })
        return
      }
      // 会话登录成功，回调相关URL并传递TOKEN供平台设置cookie
      // 获取当前域名，写入cookie
      const domain = window.location.host.split(':')[0]
      setCookie(Constant.X_Auth_Token, result.token, domain)
      router.push({ path: '/' })
    })
  })
}
</script>
