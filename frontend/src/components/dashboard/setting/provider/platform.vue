<template>
  <div class="a-setting-provider platform inline-flex-r-n-n">
    <div class="source inline-flex-c-n-n">
      <div class="wrapper inline-flex-c-n-n">
        <div class="provider" v-for="(data, index) in dataList" :key="index"
          :class="{
            'is-actived': index === loadActivedMenuIndex
          }"
          @click="handleClick(data, index)"
        >
          <template v-if="data.system">
            <div class="image">
              <img :src="loadProviderIcon(data.name)" />
            </div>
            <div class="name">
              {{ $t('provider.' + data.name) }}
            </div>
          </template>
          <template v-else>
            <el-avatar>{{ data.name }}</el-avatar>
            <div class="name"> {{ data.name }}</div>
          </template>
        </div>
      </div>
      <div class="add inline-flex-r-c-c">
        <a-button :icon="Plus"
          @click="isAddProviderView = true"
        >
          {{ $t('setting.provider.add-provider') }}
        </a-button>
      </div>
    </div>
    <!-- 模型商详情数据 -->
    <div class="context inline-flex-c-n-n">
      <a-context v-if="loadActivedData" :infomation="loadActivedData"
        @update="handleUpdate"
      ></a-context>
    </div>
    <a-nodata v-if="dataList.length === 0"
      :loading="progression.loading"
      :success="progression.success"
    />
  </div>
  <!-- 添加提供商弹窗 -->
  <a-add-provider :visible="isAddProviderView"
    @close="isAddProviderView = false"
    @add="handleProviderAdd"
  ></a-add-provider>
</template>

<script setup lang="ts">
import {
  Plus
} from '@element-plus/icons-vue'
import { useRequest } from '@/hooks/useRequest'
import { loadProviderIcon } from '@/config/data/model'
import AContext from './platform/context.vue'
import AAddProvider from './platform/provider.vue'

const dataList = ref<any[]>([])
const loadActivedMenuIndex = ref(0)
const isAddProviderView = ref(false)
const loadActivedData = computed(() => {
  return unref(dataList)[loadActivedMenuIndex.value]
})

const { ioload, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const handleLoad = () => {
  ioload('provider', 'loadProviderList', null).then((result) => {
    dataList.value = result
  })
}
const handleClick = (data: any, index: number) => {
  loadActivedMenuIndex.value = index
}
const handleProviderAdd = (data: any) => {
  dataList.value.push(data)
  loadActivedMenuIndex.value = dataList.value.length - 1
}
const handleUpdate = (infomation: any) => {
  ioload('provider', 'xhrProviderUpdate', infomation, {
    onMessage: true
  }).then(() => {
    dataList.value[loadActivedMenuIndex.value] = infomation
  })
}
</script>
