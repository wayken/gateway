<template>
  <div class="a-rewrite-redirct-pattern">
    <el-form ref="formRef" label-position="right"
      :model="loadFormData"
      :rules="loadFormRules"
    >
        <el-row :gutter="12">
          <el-col :span="17">
            <el-form-item prop="redirectUrl">
              <div class="field">
                <div class="name">{{ $t('redirect.url-name') }}</div>
                <el-input v-model="loadFormData.redirectUrl" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item prop="redirectStatus">
              <div class="field" v-if="isFieldMultiple(loadFormData.type)">
                <div class="name">{{ $t('redirect.url-status') }}</div>
                <el-select v-model="loadFormData.redirectStatus" :teleported="false">
                  <el-option v-for="data in loadStatusList" :key="data"
                    :label="data"
                    :value="data"
                  />
                </el-select>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import type { FormRules } from 'element-plus'

defineProps({
  value: {
    type: Array<any>,
    default: () => []
  }
})

const loadFormData = ref<any>({
  redirectUrl: '',
  redirectStatus: 301
})
const loadStatusList = [
  301, 302, 303, 307, 308
]
const i18n = useI18n()
const loadFormRules = reactive<FormRules<any>>({
  redirectUrl: [
    { trigger: 'blur', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('redirect.url-name') }
  ]
})

const formRef = ref()
// 判断字段是否支持值
const isFieldMultiple = (field: string) => {
  return field !== 'remove'
}
const handleValueLoad = (value: any) => {
  loadFormData.value = value
}
const handleDataRead = () => {
  return loadFormData.value
}
const handleFormValidate = () => {
  return new Promise<boolean>((resolve) => {
    formRef.value.validate((valid: boolean) => {
      if (!valid) {
        resolve(false)
      }
      resolve(true)
    })
  })
}
const handleFormReset = () => {
  formRef.value.resetFields()
}

defineExpose({
  handleValueLoad,
  handleDataRead,
  handleFormValidate,
  handleFormReset
})
</script>
