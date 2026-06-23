<template>
  <el-dialog draggable width="720px" append-to-body class="a-setting-auths--add platform"
    :title="$t('setting.add-auth')"
    :model-value="visible"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
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
      <el-form-item :label="$t('setting.auth-type')" prop="type">
        <el-select v-model="loadFormData.type">
          <el-option v-for="(data, index) in loadTypeList" :key="index" :value="data.value"
            :label="$t('setting.auth-type-' + data.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('setting.parameter-source')" prop="type" v-if="loadFormData.type === 'key' || loadFormData.type === 'jwt'">
        <el-input v-model="loadFormData.param">
          <template #prepend>
            <el-select v-model="loadFormData.source" style="width: 115px">
              <el-option v-for="(source, index) in loadSourceList" :key="index" :value="source"
                :label="$t('setting.parameter-source-' + source)"
              />
            </el-select>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item label="API Key" prop="credential" v-if="loadFormData.type === 'key' || loadFormData.type === 'jwt'"
        :rules="{
          required: true,
          trigger: 'change',
          message: $t('extension.please-input-placeholder') + ' API Key'
        }"
      >
        <el-input v-model="loadFormData.credential">
          <template #append>
            <el-button :icon="Promotion" @click="handleRandomKeyGenerate" />
          </template>
        </el-input>
      </el-form-item>
      <template v-if="loadFormData.type === 'server'">
      <el-form-item v-for="(data, index) in loadFormData.nodes" :key="index"
        :label="$t('setting.auth-node')"
        :prop="'nodes.' + index"
        :rules="{
          required: true,
          trigger: 'change',
          validator: handleNodeValidator
        }"
      >
        <el-row :gutter="20" style="width: 100%;">
          <el-col :span="8">
            <el-select v-model="data.schema">
              <el-option v-for="schema in loadSchemaList" :key="schema" :label="schema" :value="schema" />
            </el-select>
          </el-col>
          <el-col :span="10">
            <el-input v-model="data.host" :placeholder="$t('upstream.host')"></el-input>
          </el-col>
          <el-col :span="loadFormData.nodes.length > 1 ? 5 : 6">
            <el-input v-model="data.port" :placeholder="$t('upstream.port')"></el-input>
          </el-col>
          <el-col :span="1" v-if="loadFormData.nodes.length > 1">
            <el-icon class="icon" @click="handleNodeRemove(index)">
              <Delete />
            </el-icon>
          </el-col>
        </el-row>
      </el-form-item>
      <el-form-item>
        <a-button :icon="Plus" style="width: 100%;" @click="handleNodeAdd">
          {{ $t('upstream.add-node') }}
        </a-button>
      </el-form-item>
      </template>
      <el-form-item :label="$t('extension.remark')" prop="remark">
        <el-input type="textarea" v-model="loadFormData.remark" />
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
  Plus,
  Delete,
  Promotion
} from '@element-plus/icons-vue'
import type { FormRules } from 'element-plus'
import {
  loadTypeList,
  loadSourceList,
  loadSchemaList
} from '@/hooks/page/useAuths'
import { useRequest } from '@/hooks/useRequest'

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
  type: 'key',
  source: 'header',
  param: 'Authorization',
  credential: '',
  nodes: [
    {
      schema: 'http',
      host: '',
      port: 80
    }
  ],
  remark: ''
})
const loadFormRules = reactive<FormRules<any>>({
  name: [
    { trigger: 'change', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('extension.name') }
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
const handleNodeAdd = () => {
  loadFormData.nodes.push({
    host: '',
    port: 80,
    schema: 'http'
  })
}
const handleNodeRemove = (index: number) => {
  loadFormData.nodes.splice(index, 1)
}
const handleNodeValidator = (rule: any, value: any, callback: any) => {
  if (value.host === '') {
    callback(new Error(i18n.t('extension.please-input-placeholder') + i18n.t('upstream.host')))
  } else if (value.port === '') {
    callback(new Error(i18n.t('extension.please-input-placeholder') + i18n.t('upstream.port')))
  } else if (value.weight === '') {
    callback(new Error(i18n.t('extension.please-input-placeholder') + i18n.t('upstream.weight')))
  } else {
    callback()
  }
}
// 生成随机字符串
const handleRandomKeyGenerate = () => {
  const length = 42
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    const randomIndex = Math.floor(Math.random() * characters.length)
    result += characters.charAt(randomIndex)
  }
  loadFormData.credential = 'sk-' + result
}
const handleSubmit = () => {
  formRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false
    }
    const params: any = {
      name: loadFormData.name.trim(),
      type: loadFormData.type,
      parameters: {}
    }
    if (loadFormData.remark) {
      params.remark = loadFormData.remark.trim()
    }
    if (loadFormData.type === 'key') {
      params.parameters.credential = loadFormData.credential.trim()
      params.parameters.source = loadFormData.source
      params.parameters.param = loadFormData.param.trim()
    } else if (loadFormData.type === 'jwt') {
      params.parameters.credential = loadFormData.credential.trim()
      params.parameters.source = loadFormData.source
      params.parameters.param = loadFormData.param.trim()
    } else if (loadFormData.type === 'server') {
      params.parameters.nodes = loadFormData.nodes.map((node: any) => {
        return {
          schema: node.schema,
          host: node.host.trim(),
          port: node.port
        }
      })
    }
    iopost('auth', 'xhrAuthAdd', params, {
      onMessage: true
    }).then((result) => {
      params.id = result.id
      handleEmit('change', params)
      handleClose()
    })
  })
}
</script>
