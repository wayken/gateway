<template>
  <el-dialog draggable width="720px" append-to-body class="a-setting-certificate--edit platform"
    :title="$t('setting.edit-certificate')"
    :model-value="visible"
    @opened="handleOpen"
    @close="handleClose"
  >
    <el-form ref="formRef" label-position="right" label-width="88px"
      :model="loadFormData"
      :rules="loadFormRules"
      @keyup.enter="handleSubmit"
    >
      <el-form-item :label="$t('setting.bind-domain')" prop="domain">
        <el-input ref="domainInputRef" v-model="loadFormData.domain" />
      </el-form-item>
      <el-form-item :label="$t('setting.certificate')" prop="certData">
        <el-input type="textarea" :rows="8" v-model="loadFormData.certData" />
      </el-form-item>
      <el-form-item :label="$t('setting.secret-key')" prop="keyData">
        <el-input type="textarea" :rows="8" v-model="loadFormData.keyData" />
      </el-form-item>
      <el-form-item :label="$t('extension.remark')" prop="remark">
        <el-input type="textarea" :rows="2" v-model="loadFormData.remark" />
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
import type { FormRules } from 'element-plus'
import { useRequest } from '@/hooks/useRequest'
import { isDomain } from '@/utils/validator'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  id: {
    type: String,
    default: ''
  }
})

const i18n = useI18n()
const formRef = ref()
const domainInputRef = ref()
const loadFormData = reactive<any>({
  domain: '',
  certData: '',
  keyData: '',
  remark: ''
})
const loadFormRules = reactive<FormRules<any>>({
  domain: [
    {
      trigger: 'blur',
      required: true,
      validator: (rule: any, value: any, callback: any) => {
        // 通过,分隔的域名
        if (!isDomain(value.trim())) {
          return callback(new Error())
        }
        callback()
      },
      message: i18n.t('extension.please-input-placeholder') + i18n.t('setting.bind-domain')
    }
  ],
  certData: [
    { trigger: 'change', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('setting.certificate') }
  ],
  keyData: [
    { trigger: 'change', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('setting.secret-key') }
  ]
})

const { ioload, iopost, progression } = useRequest()
const handleLoad = () => {
  const params = {
    id: props.id
  }
  ioload('certificate', 'loadCertificateInfo', params).then((result) => {
    Object.keys(result).forEach((key) => {
      loadFormData[key] = result[key]
    })
  })
}

const handleEmit = defineEmits(['change', 'close'])
const handleOpen = () => {
  domainInputRef.value.focus()
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
      id: loadFormData.id,
      domain: loadFormData.domain.trim(),
      certData: loadFormData.certData.trim(),
      keyData: loadFormData.keyData.trim()
    }
    if (loadFormData.remark) {
      params.remark = loadFormData.remark.trim()
    }
    iopost('certificate', 'xhrCertificateUpdate', params, {
      onMessage: true
    }).then((result) => {
      params.id = result.id
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
