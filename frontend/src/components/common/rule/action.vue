<template>
  <div class="a-rule-action">
    <el-form ref="formRef" :model="infomation" label-position="top" :rules="loadFormRules">
      <div class="title">{{ $t('rule.action.using-operation') }}</div>
      <el-row :gutter="12">
        <el-col :span="6">
          <el-form-item :label="$t('rule.action.select-operation')" prop="name">
            <el-select v-model="infomation.type">
              <el-option v-for="(data, index) in loadActionList" :key="index"
                :label="$t('rule.action.type.' + data.name)"
                :value="data.value"
              >
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <template v-if="infomation.type === 'block'">
          <el-col :span="6">
            <el-form-item :label="$t('rule.action.response-type')" prop="response">
              <el-select v-model="infomation.response">
                <el-option v-for="(data, index) in loadResponseTypeList" :key="index"
                  :label="$t('rule.action.response.' + data.name)"
                  :value="data.value"
                >
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item :label="$t('rule.action.response-code')" prop="code">
              <el-input v-model="infomation.code" />
            </el-form-item>
          </el-col>
        </template>
      </el-row>
      <el-form-item v-if="infomation.type === 'block' && infomation.response !== 'system'" prop="content"
        :label="$t('rule.action.response-content')"
      >
        <el-input type="textarea" v-model="infomation.content" :rows="4" />
      </el-form-item>
      <template v-if="blockable">
        <div class="title">{{ $t('rule.action.using-behaviour') }}</div>
        <div class="behaviour inline-flex-r-s-n">
          <el-col :span="12">
            <el-radio v-model="infomation.behaviour" label="block">
              <i18n-t keypath="rule.action.block-rate" tag="span" scope="global">
                <template v-slot:placeholder>
                  {{ $t('rule.action.type.' + infomation.type) }}
                </template>
              </i18n-t>
            </el-radio>
            <div class="description">
              <i18n-t keypath="rule.action.block-rate-description" tag="div" scope="global">
                <template v-slot:placeholder>
                  {{ $t('rule.action.type.' + infomation.type) }}
                </template>
              </i18n-t>
            </div>
            <div class="duration inline-flex-r-c-n">
              <el-input-number v-model="infomation.duration" :min=0 :disabled="infomation.behaviour !== 'block'" />
              <el-select v-model="infomation.unit" :disabled="infomation.behaviour !== 'block'">
                <el-option v-for="(data, index) in loadUnitList" :key="index"
                  :label="$t('rule.action.unit.' + data)"
                  :value="data"
                >
                </el-option>
              </el-select>
            </div>
            <el-radio v-model="infomation.behaviour" label="limit">
              {{ $t('rule.action.limit-rate') }}
            </el-radio>
            <div class="description">
              <i18n-t keypath="rule.action.limit-rate-description" tag="div" scope="global">
                <template v-slot:placeholder>
                  {{ $t('rule.action.type.' + infomation.type) }}
                </template>
              </i18n-t>
            </div>
          </el-col>
          <el-col :span="8">
            <img v-if="infomation.behaviour === 'block'" src="@/assets/rule/block.svg" />
            <img v-if="infomation.behaviour === 'limit'" src="@/assets/rule/limit.svg" />
          </el-col>
        </div>
      </template>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import {
  Lock,
  Check,
  Compass,
  Document,
  Message,
  Avatar
} from '@element-plus/icons-vue'
import type { FormRules } from 'element-plus'

const props = defineProps({
  // 是否使用阻断行为
  // 用于控制当触发规则时网关进行阻断操作，在阻断时间内不再触发规则（如限流规则）
  blockable: {
    type: Boolean,
    default: false
  },
  // 默认响应码
  defaultCode: {
    type: Number,
    default: 429
  }
})

const infomation = ref<any>({
  type: 'block',            // 操作类型，详见：loadActionList
  response: 'system',       // 响应类型，详见：loadResponseTypeList
  code: props.defaultCode,  // 响应状态码
  content: '',              // 响应内容，当 response 不为 `系统默认` 时有效
  behaviour: 'block',       // 行为类型，block: 阻断, limit: 限流
  duration: 300,            // 阻断时间，在阻断行为下不再触发规则
  unit: 'second'            // 阻断时间单位
})
const loadActionList = [
  {
    name: 'block',
    value: 'block',
    color: '#f56c6c',
    icon: markRaw(Lock)
  },
  {
    name: 'skip',
    value: 'skip',
    color: '#67c23a',
    icon: markRaw(Check)
  },
  {
    name: 'log',
    value: 'log',
    color: '#ff8801',
    icon: markRaw(Document)
  },
  {
    name: 'cookie_challenge',
    value: 'cookie_challenge',
    color: '#5e34ba',
    icon: markRaw(Compass)
  },
  {
    name: 'js_challenge',
    value: 'js_challenge',
    color: '#409eff',
    icon: markRaw(Message)
  },
  {
    name: 'interactive_challenge',
    value: 'interactive_challenge',
    color: '#5b65e1',
    icon: markRaw(Avatar)
  }
]
const loadResponseTypeList = [
  {
    name: 'system',
    value: 'system'
  },
  {
    name: 'text',
    value: 'text/plain'
  },
  {
    name: 'html',
    value: 'text/html'
  },
  {
    name: 'json',
    value: 'application/json'
  },
  {
    name: 'xml',
    value: 'application/xml'
  }
]
const loadUnitList = ['second', 'minute', 'hour', 'day']
const i18n = useI18n()
const loadFormRules = reactive<FormRules<any>>({
  type: [
    { trigger: 'change', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('rule.action.select-operation') }
  ],
  response: [
    { trigger: 'change', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('rule.action.response-type') }
  ],
  code: [
    { trigger: 'change', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('rule.action.response-code') }
  ],
  content: [
    { trigger: 'change', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('rule.action.response-content') }
  ]
})

const formRef = ref()
const handleActionLoad = (value: any) => {
  Object.assign(infomation.value, value)
}
const handleActionRead = () => {
  const data = infomation.value
  const params: any = {
    type: data.type
  }
  if (data.type === 'block') {
    params.code = data.code
    if (data.response !== 'system') {
      params.response = data.response
      params.content = data.content
    }
  }
  if (props.blockable) {
    params.behaviour = data.behaviour
    params.duration = data.duration
    params.unit = data.unit
  }
  return params
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

defineExpose({
  handleActionLoad,
  handleActionRead,
  handleFormValidate
})
</script>
