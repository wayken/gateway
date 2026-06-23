<template>
  <el-dialog draggable width="720px" append-to-body class="a-setting-ips--add platform"
    :title="$t('setting.add-ip')"
    :model-value="visible"
    @opened="handleOpen"
    @close="handleClose"
  >
    <el-form ref="formRef" label-position="right" label-width="88px"
      :model="loadFormData"
      :rules="loadFormRules"
      @keyup.enter="handleSubmit"
    >
      <el-form-item :label="$t('extension.name')" prop="name">
        <el-input ref="nameInputRef" v-model="loadFormData.name" />
      </el-form-item>
      <el-form-item :label="$t('extension.remark')" prop="remark">
        <el-input v-model="loadFormData.remark" />
      </el-form-item>
      <el-form-item :label="$t('extension.content')" prop="content">
        <el-input type="textarea" :rows="8" v-model="loadFormData.content"
          :placeholder="$t('setting.add-ip-content-placeholder')"
        />
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
import { isIpCidr } from '@/utils/validator'

defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const i18n = useI18n()
const formRef = ref()
const nameInputRef = ref()
const loadFormData = reactive<any>({
  name: '',
  remark: '',
  content: ''
})
const loadFormRules = reactive<FormRules<any>>({
  name: [
    { trigger: 'change', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('extension.name') }
  ],
  content: [
    {
      trigger: 'blur',
      required: true,
      validator: (rule: any, value: any, callback: any) => {
        // 验证IP地址，value以换行符分隔
        const ipList = value.split('\n')
        for (const ip of ipList) {
          if (ip && !isIpCidr(ip)) {
            return callback(new Error(i18n.t('setting.ip-invalid-format')))
          }
        }
        callback()
      }
    }
  ]
})

const { iopost, progression } = useRequest()

const handleEmit = defineEmits(['change', 'close'])
const handleOpen = () => {
  nameInputRef.value.focus()
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
      name: loadFormData.name.trim(),
      cidrs: loadFormData.content.split('\n').map((ip: string) => ip.trim())
    }
    if (loadFormData.remark) {
      params.remark = loadFormData.remark.trim()
    }
    // 将content以换行符分隔的IP地址转为数组
    params.ips = loadFormData.content.split('\n').map((ip: string) => ip.trim())
    iopost('ip', 'xhrIpAdd', params, {
      onMessage: true
    }).then((result) => {
      params.id = result.id
      handleEmit('change', params)
      handleClose()
    })
  })
}
</script>
