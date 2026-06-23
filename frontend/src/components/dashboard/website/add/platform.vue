<template>
  <el-dialog draggable width="740px" append-to-body class="a-website--add platform"
    :title="$t('website.add-zone')"
    :model-value="visible"
    @opened="handleOpen"
    @close="handleClose"
  >
    <el-form ref="formRef" label-position="right" label-width="120px"
      :model="loadFormData"
      :rules="loadFormRules"
      @keyup.enter="handleSubmit"
    >
      <el-form-item :label="$t('website.zone-name')" prop="name">
        <template #label>
          <div class="label inline-flex-r-c-n">
            {{ $t('website.zone-name') }}
            <el-tooltip placement="top" effect="dark" :content="$t('website.tips.name')">
              <el-icon><Warning /></el-icon>
            </el-tooltip>
          </div>
        </template>
        <el-input ref="nameRef" v-model="loadFormData.name"></el-input>
      </el-form-item>
      <el-form-item :label="$t('website.matched-zone')" prop="match">
        <template #label>
          <div class="label inline-flex-r-c-n">
            {{ $t('website.matched-zone') }}
            <el-tooltip placement="top" effect="dark" :content="$t('website.tips.match')">
              <el-icon><Warning /></el-icon>
            </el-tooltip>
          </div>
        </template>
        <el-input v-model="loadFormData.match" placeholder="www.sample.com"></el-input>
      </el-form-item>
      <el-form-item :label="$t('extension.status')" prop="status">
        <template #label>
          <div class="label inline-flex-r-c-n">
            {{ $t('extension.status') }}
            <el-tooltip placement="top" effect="dark" :content="$t('website.tips.status')">
              <el-icon><Warning /></el-icon>
            </el-tooltip>
          </div>
        </template>
        <el-switch v-model="loadFormData.status" inline-prompt style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
          :active-icon="Check"
          :inactive-icon="Close"
        />
      </el-form-item>
      <el-form-item :label="$t('extension.remark')" prop="number">
        <el-input type="textarea" v-model="loadFormData.number" :rows="4"></el-input>
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
  Check,
  Close,
  Warning
} from '@element-plus/icons-vue'
import type { FormRules } from 'element-plus'
import { useRequest } from '@/hooks/useRequest'
import { isDomain } from '@/utils/validator'

defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const i18n = useI18n()
const formRef = ref()
const nameRef = ref()
const loadFormData = reactive<any>({
  name: '',
  match: '',
  status: 0,
  remark: ''
})
const loadFormRules = reactive<FormRules<any>>({
  name: [
    { trigger: 'blur', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('website.zone-name') }
  ],
  match: [
    {
      trigger: 'blur',
      required: true,
      validator: (rule: any, value: any, callback: any) => {
        // 通过,分隔的域名
        const domains = value.split(',')
        for (const domain of domains) {
          if (!isDomain(domain.trim())) {
            return callback(new Error())
          }
        }
        callback()
      },
      message: i18n.t('extension.please-input-placeholder') + i18n.t('website.matched-zone')
    }
  ]
})

const { iopost, progression } = useRequest()

const handleEmit = defineEmits(['change', 'close'])
const handleOpen = () => {
  nameRef.value.focus()
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
    const domains = loadFormData.match.trim().split(',')
    for (let i = 0; i < domains.length; i++) {
      domains[i] = domains[i].trim()
    }
    const params: any = {
      name: loadFormData.name.trim(),
      match: domains,
      status: loadFormData.status ? 1 : 0,
      remark: loadFormData.remark.trim()
    }
    iopost('zone', 'xhrZoneAdd', params, {
      onMessage: true
    }).then((result) => {
      params.id = result.id
      handleEmit('change', params)
      handleClose()
    })
  })
}
</script>
