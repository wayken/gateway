<template>
  <el-dialog draggable width="580px" append-to-body class="a-authority-user--edit platform"
    :title="$t('common.edit')"
    :model-value="visible"
    @opened="handleOpen"
    @close="handleClose"
  >
    <el-form ref="formRef" label-position="right" label-width="88px"
      :model="loadFormData"
      :rules="loadFormRules"
      @keyup.enter="handleSubmit"
    >
      <el-form-item :label="$t('authority.account')" prop="acct">
        <el-input :disabled="reset" :prefix-icon="Avatar" ref="accountInputRef" v-model="loadFormData.acct" />
      </el-form-item>
      <el-form-item :label="$t('authority.username')" prop="name">
        <el-input :disabled="reset" :prefix-icon="UserFilled" v-model="loadFormData.name" />
      </el-form-item>
      <el-form-item v-if="!reset" :label="$t('authority.is-twofa')" prop="twofa">
        <el-switch v-model="loadFormData.twofa" />
      </el-form-item>
      <el-form-item v-if="reset" :label="$t('authority.password')" prop="password">
        <el-input show-password :prefix-icon="Lock" ref="passwordRef" v-model="loadFormData.password">
        </el-input>
      </el-form-item>
      <el-form-item v-if="reset" :label="$t('authority.confirm-password')" prop="password2">
        <el-input show-password :prefix-icon="Lock" v-model="loadFormData.password2">
        </el-input>
      </el-form-item>
      <el-form-item :label="$t('authority.role')" prop="roles">
        <el-select v-model="loadFormData.roles" multiple>
          <el-option v-for="(data, index) in loadRoleList" :key="index"
            :label="data.name"
            :value="data.id.toString()"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="footer">
        <el-button @click="handleClose">
          {{ $t('common.cancel') }}
        </el-button>
        <el-button type="primary" :loading="progression.sending"
          @click="handleSubmit"
        >
          {{ $t('common.confirm') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import {
  Lock,
  Avatar,
  UserFilled
} from '@element-plus/icons-vue'
import md5 from 'md5'
import type { FormRules } from 'element-plus'
import { useRequest } from '@/hooks/useRequest'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  id: {
    type: String,
    default: ''
  },
  reset: {
    type: Boolean,
    default: false
  }
})

const i18n = useI18n()
const formRef = ref()
const accountInputRef = ref()
const passwordRef = ref()
const loadRoleList = ref<any[]>([])
const loadFormData = reactive<any>({
  acct: '',
  name: '',
  twofa: false,
  roles: [],
  password: '',
  password2: ''
})
const loadFormRules = reactive<FormRules<any>>({
  acct: [
    { trigger: 'blur', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('authority.account') }
  ],
  name: [
    { trigger: 'blur', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('authority.username') }
  ],
  password: [
    { trigger: 'blur', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('authority.password') }
  ],
  password2: [
    {
      trigger: 'blur',
      required: true,
      validator: (rule: any, value: any, callback: any) => {
        if (value !== loadFormData.password) {
          callback(new Error(i18n.t('authority.password-unmatch')))
        } else {
          callback()
        }
      }
    }
  ],
  roles: [
    { trigger: 'change', required: true, message: i18n.t('extension.please-select-placeholder') + i18n.t('authority.role') }
  ]
})

const { ioload, iopost, progression } = useRequest()

const handleLoad = () => {
  const params = {
    id: props.id
  }
  ioload('database', 'loadUserInfo', params).then((result) => {
    Object.keys(result).forEach((key) => {
      // 如果key为id，需要转换成字符串
      if (key === 'id') {
        loadFormData[key] = result[key].toString()
        return
      }
      if (key === 'twofa_scheme') {
        loadFormData['twofa'] = true
        return
      } else if (key === 'role_ids') {
        loadFormData['roles'] = result[key].map((data: any) => data.toString())
        return
      }
      loadFormData[key] = result[key]
    })
  })
  ioload('database', 'loadRoleList', null).then((result) => {
    loadRoleList.value = result
  })
}
const handleEmit = defineEmits(['change', 'close'])
const handleOpen = () => {
  if (props.reset) {
    passwordRef.value.focus()
  } else {
    accountInputRef.value.focus()
  }
}
const handleClose = () => {
  formRef.value.resetFields()
  handleEmit('close')
}
const handleSubmit = () => {
  formRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false
    }
    const params: any = {
      id: props.id,
      acct: loadFormData.acct.trim(),
      name: loadFormData.name.trim(),
      roles: loadFormData.roles,
      twofa: loadFormData.twofa
    }
    if (props.reset) {
      params.pwd = md5(loadFormData.password)
    }
    iopost('database', 'xhrUserUpdate', params, {
      onMessage: true
    }).then(() => {
      handleEmit('change', params)
      handleClose()
    })
  })
}

watch(() => props.visible, (value) => {
  if (value) {
    handleLoad()
  }
})
</script>
