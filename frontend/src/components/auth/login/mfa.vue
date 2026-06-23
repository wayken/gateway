<template>
  <div class="mfa"
    :class="[isMobile() ? 'device' : 'platform']"
  >
    <el-form ref="formRef" size="large" :model="loadFormData" :rules="loadFormRules" :disabled="progression.sending"
      @keyup.enter="handleSubmit"
    >
      <div class="logo inline-flex-r-c-n">
        <img :src="logoSvg" />
        <div class="name">{{ $t('portal.title') }}</div>
      </div>
      <el-row>
        <el-col :span="isMobile() || !params.qrcode ? 24 : 14">
          <div class="title">{{ $t('login.twofa-auth') }}</div>
          <el-form-item>
            <el-select model-value="onetime">
              <el-option key="onetime" value="onetime"
                :label="$t('login.onetime-password')"
              />
            </el-select>
          </el-form-item>
          <el-form-item prop="code">
            <el-input :prefix-icon="Lock" ref="codeInputRef" v-model="loadFormData.code"
              :placeholder="$t('login.dynamic-code-placeholder')"
            >
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="isMobile() ? 24 : 10">
          <div class="code inline-flex-c-c-c" v-if="params.qrcode">
            <img :src="params.qrcode" />
            <div class="description">
              {{ $t('login.scan-qrcode') }}
            </div>
          </div>
        </el-col>
      </el-row>
      <el-form-item>
        <el-button class="confirm" type="primary" :icon="Promotion"
          :loading="progression.sending"
          @click="handleSubmit"
        >
          {{ $t('login.next-step') }}
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
  Lock,
  Promotion
} from '@element-plus/icons-vue'
import { isMobile } from '@/hooks/useDevice'
import type { FormRules } from 'element-plus'
import logoSvg from '@/assets/logo.svg'
import Constant from '@/config/constant'
import { setCookie } from '@/utils/cookie'
import { useRequest } from '@/hooks/useRequest'

const props = defineProps({
  params: {
    type: Object,
    default: () => ({})
  }
})

const loadFormData = reactive<any>({
  code: ''
})

const i18n = useI18n()
const loadFormRules = reactive<FormRules<any>>({
  code: [
    {
      required: true,
      trigger: 'change',
      message: i18n.t('login.dynamic-code-placeholder')
    }
  ]
})

const formRef = ref()
const codeInputRef = ref()
const router = useRouter()
const { iopost, progression } = useRequest()

onMounted(() => {
  codeInputRef.value.focus()
})

const handleSubmit = () => {
  formRef.value.validate((valid: boolean) => {
    if (!valid) {
      return
    }
    const params = {
      acct: props.params.acct,
      pwd: props.params.pwd,
      code: loadFormData.code
    }
    iopost('session', 'xhrSessionMfa', params).then((result) => {
      // 会话登录成功，回调相关URL并传递TOKEN供平台设置cookie
      // 获取当前域名，写入cookie
      const domain = window.location.host.split(':')[0]
      setCookie(Constant.X_Auth_Token, result.token, domain)
      router.push({ path: '/' })
    })
  })
}
</script>
