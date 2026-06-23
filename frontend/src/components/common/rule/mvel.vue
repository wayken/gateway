<template>
  <div class="a-rule-mvel">
    <el-form ref="formRef" label-position="right"
      :model="loadRuleList"
    >
      <div v-for="(data, index) in loadRuleList" :key="index">
        <el-form-item :prop="'' + index"
          :rules="{
            required: true,
            trigger: 'change',
            validator: handleRuleValidator
          }"
        >
        <el-row :gutter="12">
          <el-col :span="4">
            <div class="field">
              <div v-if="!isRuleMultiple(index)" class="name">{{ $t('rule.mvel.field-label') }}</div>
              <el-select v-model="data.field" :placeholder="$t('rule.mvel.field-label')" :teleported="false"
                @change="handleFieldUpdate(data)"
              >
                <el-option v-for="field in loadRuleFieldList" :key="field.value"
                  :label="$t('rule.mvel.field.' + field.name)"
                  :value="field.value"
                />
              </el-select>
            </div>
          </el-col>
          <el-col v-if="isFieldMultiple(data.field)" :span="5">
            <div class="field">
              <div v-if="!isRuleMultiple(index)" class="name">{{ $t('rule.mvel.field-key') }}</div>
              <el-input v-model="data.key" :placeholder="$t('rule.mvel.field-key')" />
            </div>
          </el-col>
          <el-col :span="5">
            <div class="field">
              <div v-if="!isRuleMultiple(index)" class="name">{{ $t('rule.mvel.operators') }}</div>
              <el-select v-model="data.operator" :placeholder="$t('rule.mvel.operators')" :teleported="false"
                @change="handleOperatorUpdate($event, data)"
              >
                <el-option v-for="operator in getFieldOperatorList(data.field)" :key="operator.value"
                  :label="$t('rule.mvel.operator.' + operator.name)"
                  :value="operator.value"
                />
              </el-select>
            </div>
          </el-col>
          <el-col :span="isFieldMultiple(data.field) ? 5 : 10">
            <div class="field">
              <div v-if="!isRuleMultiple(index)" class="name">{{ $t('rule.mvel.field-value') }}</div>
              <el-select v-if="loadFieldValueSelect" v-model="data.value" :teleported="false">
                <el-option v-for="option in loadFieldValueSelectOptions[loadFieldValueSelect]" :key="option.value"
                  :label="option.name"
                  :value="option.name"
                />
              </el-select>
              <el-select v-else-if="isFieldSelect(data.field)" v-model="data.value" :teleported="false" filterable>
                <el-option v-for="option in getSelectOptions(data.field)" :key="option.value"
                  :label="option.name"
                  :value="option.value"
                />
              </el-select>
              <el-input v-else-if="!isOperatorNumberic(data.operator)" v-model="data.value" :placeholder="$t('rule.mvel.field-value')" />
              <el-input-number v-else v-model="data.value" />
            </div>
          </el-col>
          <el-col :span="5">
            <div class="field">
              <div v-if="!isRuleMultiple(index)" class="name"></div>
              <div class="logical">
                <el-button class="button" type="primary" @click="handleAddRule(data, index, true)">
                  {{ $t('rule.mvel.logical.&&') }}
                </el-button>
                <el-button class="button" type="primary" @click="handleAddRule(data, index, false)">
                  {{ $t('rule.mvel.logical.||') }}
                </el-button>
                <el-button v-if="loadRuleList.length > 0" type="danger" :icon="Delete" circle
                  @click="handleRemoveRule(index)"  
                />
              </div>
            </div>
          </el-col>
        </el-row>
        </el-form-item>
        <div class="divider" v-if="data.logical && index !== loadRuleList.length - 1">
          <div class="name">{{ $t('rule.mvel.logical.' + data.logical) }}</div>
        </div>
      </div>
    </el-form>
    <div v-if="loadRuleList.length > 0" class="expression">
      <div class="expression-header">
        <div class="name">{{ $t('rule.mvel.expression') }}</div>
        <el-button v-if="!isEditingExpression" type="primary" link @click="handleEditExpression">编辑表达式</el-button>
      </div>
      <div v-if="!isEditingExpression" class="preview">{{ loadPreviewExression }}</div>
      <div v-else class="preview-edit">
        <el-input v-model="editExpressionText" type="textarea" :autosize="{ minRows: 2 }" />
        <div class="preview-edit-actions">
          <el-button type="primary" size="small" @click="handleSaveExpression">{{ $t('common.save') }}</el-button>
          <el-button size="small" @click="isEditingExpression = false">{{ $t('common.cancel') }}</el-button>
        </div>
      </div>
    </div>
    <div v-if="loadRuleList.length === 0" class="nodata">
      <a-button :icon="Plus" @click="handleAddRule({ field: 'http.path', operator: '==', value: '' }, 0, true)">
        {{ $t('rule.mvel.add-expression') }}
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Plus,
  Delete
} from '@element-plus/icons-vue'
import {
  Rule,
  isFieldMultiple,
  isFieldSelect,
  getFieldOperatorList,
  getSelectOptions,
  isOperatorNumberic,
  loadRuleFieldList
} from './rule'
import { useRequest } from '@/hooks/useRequest'
import { isIp, isIpCidr } from '@/utils/validator'
import { generateExpression, parseExpression } from './parser'

const props = defineProps({
  expression: {
    type: String,
    default: ''
  }
})

const loadRuleList = ref<Rule[]>([])
// 自定义值下拉框，如源IP中的在列表中会自动加载IP组列表数据
const loadFieldValueSelect = ref<any>(null)
const FIELD_VALUE_IP_SRC = 'ip.src'
const loadFieldValueSelectOptions = ref<{ [key: string]: any }>({})
const i18n = useI18n()
const formRef = ref()
const { ioload } = useRequest()
const isEditingExpression = ref(false)
const editExpressionText = ref('')
const loadPreviewExression = computed(() => {
  return generateExpression(loadRuleList.value)
})

onMounted(() => {
  handleExpressionLoad(props.expression)
})

// 如果是第一个规则，则显示字段，否则不显示
const isRuleMultiple = (index: number) => {
  return index > 0
}
const handleExpressionLoad = (expression: string) => {
  loadRuleList.value = parseExpression(expression)
}
const handleAddRule = (data: any, index: number, isLogical: boolean) => {
  const logic = isLogical ? '&&' : '||'
  // 如果index是最后一个，直接添加，否则插入到index后面，并且index+1的logical为上一个的logical
  if (index === loadRuleList.value.length - 1) {
    loadRuleList.value.push({
      field: 'http.path',
      operator: '==',
      value: ''
    })
  } else {
    loadRuleList.value.splice(index + 1, 0, {
      field: 'http.path',
      operator: '==',
      value: ''
    })
    const next = loadRuleList.value[index + 1]
    if (next) {
      next.logical = data.logical
    }
  }
  data.logical = logic
}
const handleRemoveRule = (index: number) => {
  loadRuleList.value.splice(index, 1)
}
const handleRuleValidator = (rule: any, value: any, callback: any) => {
  if (value.field === '') {
    callback(new Error(i18n.t('extension.please-select-placeholder') + i18n.t('rule.mvel.field-label')))
  } else if (value.key === '') {
    callback(new Error(i18n.t('extension.please-input-placeholder') + i18n.t('rule.mvel.field-key')))
  } else if (value.operator === '') {
    callback(new Error(i18n.t('extension.please-select-placeholder') + i18n.t('rule.mvel.operators')))
  } else if (value.value === '') {
    callback(new Error(i18n.t('extension.please-input-placeholder') + i18n.t('rule.mvel.field-value')))
  } else {
    if (value.field === 'http.ip.src') {
      if (value.operator === '.eq' || value.operator === '.ne') {
        if (!isIp(value.value)) {
          callback(new Error(i18n.t('setting.ip-invalid-format')))
        }
      }
      if (value.operator === '.in' || value.operator === '.notIn') {
        if (!isIpCidr(value.value)) {
          callback(new Error(i18n.t('setting.ip-invalid-format')))
        }
      }
    }
    callback()
  }
}
const handleFieldUpdate = (data: Rule) => {
  if (isFieldMultiple(data.field)) {
    data.key = ''
  }
  data.operator = ''
  data.value = isOperatorNumberic(data.operator) ? 0 : ''
}
const handleOperatorUpdate = (value: string, data: any) => {
  if (value === '.inList' || value === '.notInList') {
    const isFieldValueOptionsExist = loadFieldValueSelectOptions.value[FIELD_VALUE_IP_SRC]
    if (isFieldValueOptionsExist) {
      data.value = loadFieldValueSelectOptions.value[FIELD_VALUE_IP_SRC][0].name
      loadFieldValueSelect.value = FIELD_VALUE_IP_SRC
      return
    }
    ioload('ip', 'loadIpList', null).then((result) => {
      loadFieldValueSelectOptions.value[FIELD_VALUE_IP_SRC] = result
      data.value = loadFieldValueSelectOptions.value[FIELD_VALUE_IP_SRC][0].name
      loadFieldValueSelect.value = FIELD_VALUE_IP_SRC
    })
    return
  }
  loadFieldValueSelect.value = null
}

const handleEditExpression = () => {
  editExpressionText.value = loadPreviewExression.value
  isEditingExpression.value = true
}
const handleSaveExpression = () => {
  loadRuleList.value = parseExpression(editExpressionText.value)
  isEditingExpression.value = false
}

const handleExpreassionRead = () => {
  return loadPreviewExression.value
}
const handleFormValidate = () => {
  return new Promise<boolean>((resolve) => {
    if (loadRuleList.value.length === 0) {
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
  handleExpressionLoad,
  handleExpreassionRead,
  handleFormValidate,
  handleFormReset
})
</script>
