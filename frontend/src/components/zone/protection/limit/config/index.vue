<template>
  <div class="a-zone-protection-limit-config">
    <el-form ref="formRef" label-position="right"
      :model="infomation"
    >
      <div class="title">{{ $t('protection.limit.characteristics') }}</div>
      <el-form-item v-for="(data, index) in infomation.characteristics" :key="index" :prop="'characteristics.' + index"
        :rules="{
          required: true,
          trigger: 'change',
          validator: handleValidator
        }"
      >
        <el-row :gutter="12">
          <el-col :span="8" >
            <el-select v-model="data.key" :teleported="false">
              <el-option v-for="field in loadCharacteristicList" :key="field.key"
                :label="$t('protection.limit.field.' + field.name)"
                :value="field.key"
              />
            </el-select>
          </el-col>
          <el-col :span="12" v-if="isFieldMultiple(data.key)">
            <el-input v-model="data.value" />
          </el-col>
          <el-col :span="4" >
            <div class="operation inline-flex-r-c-n">
              <el-button :icon="Plus"
                @click="handleAddCharacteristic(index)"
              ></el-button>
              <el-icon class="close" v-if="infomation.characteristics.length > 1"
                @click="handleRemoveCharacteristic(index)"
              ><Close /></el-icon>
            </div>
          </el-col>
        </el-row>
      </el-form-item>
      <div class="title">{{ $t('protection.limit.rate-exceeds') }}</div>
      <el-form-item>
        <el-row :gutter="12">
          <el-col :span="8" >
            <el-input-number v-model="infomation.burst" :min="1" />
          </el-col>
          <el-col :span="4">
            <el-select v-model="infomation.burstUnit" :teleported="false">
              <el-option v-for="unit in loadUnitList" :key="unit.value" :value="unit.value"
                :label="$t('protection.limit.unit.' + unit.name)"
              />
            </el-select>
          </el-col>
        </el-row>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import {
  Plus,
  Close
} from '@element-plus/icons-vue'
const infomation = ref<any>(
  {
    characteristics: [
      {
        key: 'http.ip'
      }
    ],
    burst: 10,
    burstUnit: 'second'
  }
)
const loadCharacteristicList = [
  {
    name: 'ip',
    key: 'http.ip'
  },
  {
    name: 'host',
    key: 'http.host'
  },
  {
    name: 'path',
    key: 'http.path'
  },
  {
    name: 'header',
    key: 'http.header',
    value: '',
    multiple: true
  },
  {
    name: 'cookie',
    key: 'http.cookie',
    value: '',
    multiple: true
  },
  {
    name: 'custom',
    key: 'custom',
    multiple: true,
    value: ''
  }
]
const loadUnitList = [
  {
    name: 'second',
    value: 'second'
  },
  {
    name: 'minute',
    value: 'minute'
  },
  {
    name: 'hour',
    value: 'hour'
  }
]
const formRef = ref()
const i18n = useI18n()

const isFieldMultiple = (field: string) => {
  return loadCharacteristicList.find(item => item.key === field)?.multiple
}
const handleValidator = (rule: any, value: any, callback: any) => {
  const key = value.key
  if (isFieldMultiple(key) && !value.value) {
    callback(new Error(i18n.t('extension.please-input-placeholder') + i18n.t('rule.mvel.field-value')))
  } else {
    callback()
  }
}
const handleAddCharacteristic = (index: number) => {
  infomation.value.characteristics.splice(index + 1, 0, {
    name: 'ip'
  })
}
const handleRemoveCharacteristic = (index: number) => {
  infomation.value.characteristics.splice(index, 1)
}
const handleFormValidate = () => {
  return new Promise<boolean>((resolve) => {
    if (infomation.value.characteristics.length === 0) {
      resolve(false)
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
const handleConfigRead = () => {
  const characteristics = infomation.value.characteristics.map((data: any) => {
    if (data.multiple) {
      return {
        key: data.key,
        value: `${data.key}[${data.value}]`
      }
    }
    return {
      key: data.key,
      value: data.value
    }
  })
  return {
    characteristics,
    burst: infomation.value.burst,
    burstUnit: infomation.value.burstUnit
  }
}
const handleConfigLoad = (data: any) => {
  infomation.value = data
}

defineExpose({
  handleFormValidate,
  handleConfigRead,
  handleConfigLoad
})
</script>
