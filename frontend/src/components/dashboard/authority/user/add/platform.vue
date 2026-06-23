<template>
  <el-dialog draggable width="580px" append-to-body class="a-authority-user--add platform"
    :title="$t('authority.add-user')"
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
        <el-input :prefix-icon="Avatar" ref="acctInputRef" v-model="loadFormData.acct" />
      </el-form-item>
      <el-form-item :label="$t('authority.username')" prop="name">
        <el-input :prefix-icon="UserFilled" v-model="loadFormData.name" />
      </el-form-item>
      <el-form-item :label="$t('authority.password')" prop="password">
        <el-input show-password :prefix-icon="Lock" v-model="loadFormData.password">
        </el-input>
      </el-form-item>
      <el-form-item :label="$t('authority.confirm-password')" prop="password2">
        <el-input show-password :prefix-icon="Lock" v-model="loadFormData.password2">
        </el-input>
      </el-form-item>
      <el-form-item :label="$t('authority.role')" prop="role">
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

defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const i18n = useI18n()
const formRef = ref()
const acctInputRef = ref()
const loadRoleList = ref<any[]>([])
const loadFormData = reactive<any>({
  acct: '',
  name: '',
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

const handleEmit = defineEmits(['change', 'close'])
const handleOpen = () => {
  acctInputRef.value.focus()
  handleLoad()
}
const handleLoad = () => {
  ioload('database', 'loadRoleList', null).then((result) => {
    loadRoleList.value = result
  })
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
      acct: loadFormData.acct.trim(),
      name: loadFormData.name.trim(),
      roles: loadFormData.roles,
      pwd: md5(loadFormData.password)
    }
    iopost('database', 'xhrUserAdd', params, {
      onMessage: true
    }).then((result) => {
      params.id = result.id
      handleEmit('change', params)
      handleClose()
    })
  })
}
</script>
