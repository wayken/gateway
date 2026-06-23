<template>
  <div class="a-rewrite-action">
    <el-form ref="formRef" label-position="right"
      :model="dataList"
    >
      <div v-for="(data, index) in dataList" :key="index">
        <el-form-item :prop="'' + index"
          :rules="{
            required: true,
            trigger: 'change',
            validator: handleValidator
          }"
        >
          <el-row :gutter="12">
            <el-col :span="5">
              <div class="field">
                <div v-if="!isActionMultiple(index)" class="name"></div>
                <el-select v-model="data.type" :teleported="false">
                  <el-option v-for="action in rewriteActionTypeList" :key="action.value"
                    :label="$t('rewrite.type-' + action.name)"
                    :value="action.value"
                  />
                </el-select>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="field">
                <div v-if="!isActionMultiple(index)" class="name">{{ $t('rewrite.header-name') }}</div>
                <el-input v-model="data.name" />
              </div>
            </el-col>
            <el-col :span="2">
              <div class="field" v-if="isFieldMultiple(data.type)">
                <div v-if="!isActionMultiple(index)" class="name"></div>
                <div class="inline-flex-r-c-c">=</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="field" v-if="isFieldMultiple(data.type)">
                <div v-if="!isActionMultiple(index)" class="name">{{ $t('rewrite.header-value') }}</div>
                <el-input v-model="data.value" />
              </div>
            </el-col>
            <el-col :span="1">
              <div class="field">
                <div v-if="!isActionMultiple(index)" class="name"></div>
                <el-button v-if="dataList.length > 1" link type="danger" @click="handleRemove(index)">
                  {{ $t('common.delete') }}
                </el-button>
              </div>
            </el-col>
          </el-row>
        </el-form-item>
      </div>
    </el-form>
    <div class="add">
      <a-button :icon="Plus" @click="handleAdd">
        {{ $t('rewrite.add-new-header') }}
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Plus
} from '@element-plus/icons-vue'
import {
  rewriteActionTypeList
} from '@/hooks/page/useRewrite'

defineProps({
  value: {
    type: Array<any>,
    default: () => []
  }
})

const dataList = ref<any[]>([
  {
    type: 'add',
    name: '',
    value: ''
  }
])

const i18n = useI18n()
const formRef = ref()
// 如果是第一个规则，则显示字段，否则不显示
const isActionMultiple = (index: number) => {
  return index > 0
}
// 判断字段是否支持值
const isFieldMultiple = (field: string) => {
  return field !== 'remove'
}
const handleValidator = (rule: any, value: any, callback: any) => {
  if (value.name === '') {
    callback(new Error(i18n.t('extension.please-input-placeholder') + i18n.t('rewrite.header-name')))
  } else if (value.value === '') {
    callback(new Error(i18n.t('extension.please-input-placeholder') + i18n.t('rewrite.header-value')))
  } else {
    callback()
  }
}
const handleValueLoad = (value: any[]) => {
  dataList.value = value
}
const handleAdd = () => {
  dataList.value.push({
    type: 'add',
    name: '',
    value: ''
  })
}
const handleRemove = (index: number) => {
  dataList.value.splice(index, 1)
}
const handleDataListRead = () => {
  return dataList.value
}
const handleFormValidate = () => {
  return new Promise<boolean>((resolve) => {
    if (dataList.value.length === 0) {
      resolve(true)
      return
    }
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
  handleDataListRead,
  handleFormValidate,
  handleFormReset
})
</script>
