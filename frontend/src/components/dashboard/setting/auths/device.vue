<template>
  <div class="a-setting-auths device">
    <div class="head inline-flex-r-c-b">
      <div class="head-left"></div>
      <div class="head-right">
        <a-button type="primary" :icon="Plus">
          {{ $t('setting.add-auth') }}
        </a-button>
      </div>
    </div>
    <div class="content inline-flex-c-n-n">
      <div v-for="(data, index) in dataList" :key="index">
        <div class="source">
          {{ data.name }}
        </div>
        <div class="metadata inflex-text-ellipsis">
          <div class="inline-color-grey">{{ data.remark || '-' }}</div>
        </div>
      </div>
    </div>
    <a-nodata v-if="dataList.length === 0"
      :loading="progression.loading"
      :success="progression.success"
    />
  </div>
</template>

<script setup lang="ts">
import {
  Plus
} from '@element-plus/icons-vue'
import { useRequest } from '@/hooks/useRequest'

const dataList = ref<any[]>([])

const { ioload, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const handleLoad = () => {
  ioload('auth', 'loadAuthList', null).then((result) => {
    dataList.value = result
  })
}
</script>
