<template>
  <div class="a-service device">
    <a-nodata v-if="dataList.length === 0"
      :loading="progression.loading"
      :success="progression.success"
    />
  </div>
</template>

<script setup lang="ts">
import { useRequest } from '@/hooks/useRequest'

const dataList = ref<any[]>([])

const { ioload, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const handleLoad = () => {
  ioload('service', 'loadServiceList', null).then((result) => {
    dataList.value = result
  })
}
</script>
