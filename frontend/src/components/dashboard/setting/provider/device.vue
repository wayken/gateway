<template>
  <div class="a-setting-provider device inline-flex-c-n-n">
    <div class="card inline-flex-r-c-n" v-for="(data, index) in dataList" :key="index">
      <div class="image">
        <img :src="loadProviderIcon(data.name)" />
      </div>
      <div class="wrapper">
        <div class="source">
          {{ $t('provider.' + data.name) }}
        </div>
        <div class="metadata">
          {{ data.url }}
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
import { useRequest } from '@/hooks/useRequest'
import { loadProviderIcon } from '@/config/data/model'

const dataList = ref<any[]>([])

const { ioload, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const handleLoad = () => {
  ioload('provider', 'loadProviderList', null).then((result) => {
    dataList.value = result
  })
}
</script>
