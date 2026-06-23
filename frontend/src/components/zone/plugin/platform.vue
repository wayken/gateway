<template>
  <div class="a-zone-plugin platform inline-flex-c-n-n">
    <div class="head inline-flex-r-c-b">
      <div class="head--left inline-flex-r-c-n">
        {{ $t('plugin.global-plugin-list') }}
      </div>
      <div class="head--right">
      </div>
    </div>
    <div class="content inline-flex-c-n-n" v-loading="progression.loading">
      <el-row :gutter="12">
        <el-col v-for="(data, index) in loadPluginList" :key="index" :xs="12" :sm="6" :md="4" :lg="6" :xl="3">
          <div class="card inline-flex-c-c-c">
            <div class="name inline-flex-c-c-c">{{ $t('plugin.plugins.' + data.name) }}</div>
            <el-icon class="icon" :style="{ color: data.color }">
              <component :is="data.icon"></component>
            </el-icon>
            <div class="foot inline-flex-r-c-c">
              <a-button :icon="Promotion" @click="handleEnable(data)">
                {{ $t('common.edit') }}
              </a-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
  <a-metadata v-model:visible="isMetadataView"
    :name="loadActivePlugin.value"
    :value="loadActivedJsonData"
    @change="handleUpdate"
  ></a-metadata>
</template>

<script setup lang="ts">
import {
  Files,
  Position,
  Promotion
} from '@element-plus/icons-vue'
import { useRequest } from '@/hooks/useRequest'
import AMetadata from './metadata/platform.vue'

const dataList = ref<any[]>([])
const loadPluginList = [
  {
    name: 'real-ip',
    value: 'RealIp',
    color: '#f56c6c',
    icon: markRaw(Files),
    metadata: {
      enable: false,
      source: 'X-Forwarded-For'
    }
  },
  {
    name: 'ip-region',
    value: 'Ip2Region',
    color: '#34c724',
    icon: markRaw(Position),
    metadata: {
      enable: false
    }
  }
]
const isMetadataView = ref(false)
const loadActivePlugin = ref<any>({})
const loadActivedJsonData = ref<string>('')

const router = useRouter()
const { ioload, iopost, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const handleLoad = () => {
  const params = {
    zone: router.currentRoute.value.params.id
  }
  ioload('plugin', 'loadPluginList', params).then((result) => {
    dataList.value = result
    dataList.value.forEach((data) => {
      const plugin = loadPluginList.find((plugin) => plugin.value === data.name)
      if (plugin) {
        plugin.metadata = data.metadata
      }
    })
  })
}
const handleEnable = (data: any) => {
  loadActivePlugin.value = data
  loadActivedJsonData.value = JSON.stringify(data.metadata, null, 2)
  isMetadataView.value = true
}
const handleUpdate = (data: any) => {
  const index = dataList.value.findIndex((item) => item.name === data.name)
  if (index !== -1) {
    dataList.value[index].metadata = data.metadata
  } else {
    dataList.value.push(data)
  }
  const params = {
    zone: router.currentRoute.value.params.id,
    plugins: dataList.value
  }
  iopost('plugin', 'xhrPluginListUpdate', params).then(() => {
    handleLoad()
  })
}
</script>
