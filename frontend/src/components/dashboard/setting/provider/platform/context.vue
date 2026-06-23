<template>
  <div class="head inline-flex-r-c-b">
      <div class="head--left inline-flex-r-c-n">
        <div class="name">
          <template v-if="infomation.system">{{ $t('provider.' + infomation.name) }}</template>
          <template v-else>{{ infomation.name }}</template>
        </div>
        <el-icon><Promotion /></el-icon>
      </div>
      <div class="head--right inline-flex-r-c-n">
        <el-icon v-if="!infomation.system"
          @click="isEditProviderView = true"
        ><Edit /></el-icon>
      </div>
  </div>
  <div class="option inline-flex-r-c-n">
    <div class="key">{{ $t('setting.provider.api-url') }}</div>
    <div class="value">
      <el-input v-model="infomation.url">
        <template #append>
          <el-button @click="handleUpdate">
            {{ $t('common.save') }}
          </el-button>
        </template>
      </el-input>
    </div>
  </div>
  <div class="main inline-flex-c-n-n">
    <div class="modules inline-flex-r-c-n">
      <div class="module inline-flex-r-c-n"
        :class="{
          'is-actived': loadActiveMenuIndex === 0
        }"
        @click="loadActiveMenuIndex = 0"
      >
        {{ $t('setting.provider.model') }}
      </div>
      <div class="module inline-flex-r-c-n"
        :class="{
          'is-actived': loadActiveMenuIndex === 1
        }"
        @click="loadActiveMenuIndex = 1"
      >
        {{ $t('setting.provider.api-key') }}
      </div>
      <a-button :icon="Plus" v-if="loadActiveMenuIndex === 0"
        @click="handleAddModel"
      >
        {{ $t('setting.provider.add-model') }}
      </a-button>
      <a-button :icon="Plus" v-if="loadActiveMenuIndex === 1"
        @click="handleAddApiKey"
      >
        {{ $t('setting.provider.add-api-key') }}
      </a-button>
    </div>
    <div class="content" v-if="loadActiveMenuIndex === 0">
      <div class="model inline-flex-r-c-n" v-for="(data, index) in infomation.models" :key="index">
        <div class="image">
          <img :src="loadProviderIcon(infomation.name)" />
        </div>
        <div class="name">{{ data }}</div>
        <div class="operation">
          <el-popconfirm placement="top" :width="218" trigger="click" style="padding: 12px;" v-if="infomation.models.length > 1"
            :title="$t('setting.provider.delete-model-tips')"
            @confirm="handleModelRemove(data)"
          >
            <template #reference>
              <el-icon class="icon"><Remove /></el-icon>
            </template>
          </el-popconfirm>
        </div>
      </div>
      <a-nodata v-if="!infomation.models || infomation.models.length === 0"
        :loading="false"
        :success="true"
      />
    </div>
    <div class="content" v-if="loadActiveMenuIndex === 1">
      <div class="key inline-flex-r-c-n" v-for="(data, index) in infomation.keys" :key="index">
        <el-icon class="image"><Key /></el-icon>
        <div class="name">{{ data.key }}</div>
        <div class="operation inline-flex-r-c-n">
          <el-switch v-model="data.status" size="large" inline-prompt
            style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
            :active-value="1"
            :inactive-value="0"
            :active-text="$t('extension.enable')"
            :inactive-text="$t('extension.disable')"
          />
          <el-popconfirm placement="top" :width="218" trigger="click" style="padding: 12px;" v-if="infomation.keys.length > 1"
            :title="$t('setting.provider.delete-api-key-tips')"
            @confirm="handleApiKeyRemove(data)"
          >
            <template #reference>
              <el-icon class="icon"><Remove /></el-icon>
            </template>
          </el-popconfirm>
        </div>
      </div>
      <a-nodata v-if="!infomation.keys || infomation.keys.length === 0"
        :loading="false"
        :success="true"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Key,
  Plus,
  Edit,
  Remove,
  Promotion
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { loadProviderIcon } from '@/config/data/model'

const isEditProviderView = ref(false)
const loadActiveMenuIndex = ref(0)

const props = defineProps({
  infomation: {
    type: Object,
    default: () => ({})
  }
})

const i18n = useI18n()
const handleEmit = defineEmits(['update'])

const handleAddApiKey = () => {
  const title = i18n.t('setting.provider.add-api-key')
  const name = i18n.t('setting.provider.api-key')
  const tips = i18n.t('extension.please-input-placeholder') + name
  ElMessageBox.prompt(name, title, {
    confirmButtonText: i18n.t('common.confirm'),
    cancelButtonText: i18n.t('common.cancel'),
    inputValidator: function(value) {
      return value ? value.trim().length > 0 : false
    },
    inputErrorMessage: tips
  }).then(({ value }) => {
    handleXhrAddApiKey(value)
  }).catch(() => {
  })
}
const handleXhrAddApiKey = (value: string) => {
  if (!props.infomation.keys) {
    props.infomation.keys = []
  }
  props.infomation.keys.push({
    key: value,
    status: 1
  })
  handleEmit('update', props.infomation)
}
const handleAddModel = () => {
  const title = i18n.t('setting.provider.add-model')
  const name = i18n.t('setting.provider.model-name')
  const tips = i18n.t('extension.please-input-placeholder') + name
  ElMessageBox.prompt(name, title, {
    confirmButtonText: i18n.t('common.confirm'),
    cancelButtonText: i18n.t('common.cancel'),
    inputValidator: function(value) {
      return value ? value.trim().length > 0 : false
    },
    inputErrorMessage: tips
  }).then(({ value }) => {
    handleXhrAddModel(value)
  }).catch(() => {
  })
}
const handleXhrAddModel = (value: string) => {
  if (!props.infomation.models) {
    props.infomation.models = []
  }
  props.infomation.models.push(value)
  handleUpdate()
}
const handleModelRemove = (name: string) => {
  if (!props.infomation.models) {
    return
  }
  props.infomation.models = props.infomation.models.filter((item: string) => item !== name)
  handleUpdate()
}
const handleApiKeyRemove = (data: any) => {
  if (!props.infomation.keys) {
    return
  }
  props.infomation.keys = props.infomation.keys.filter((item: any) => item.key !== data.key)
  handleUpdate()
}
const handleUpdate = () => {
  handleEmit('update', props.infomation)
}
</script>
