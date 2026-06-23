<template>
  <div class="a-authority-role device">
    <div class="card inline-flex-r-c-n" v-for="(data, index) in dataList" :key="index">
      <div class="avatar">
        <el-icon><Document /></el-icon>
      </div>
      <div class="source">
        <div class="name">{{ data.name }}</div>
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
  Document
} from '@element-plus/icons-vue'
import { useRequest } from '@/hooks/useRequest'

const dataList = ref<any[]>([])

const { ioload, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const handleLoad = () => {
  ioload('database', 'loadRoleList', null).then((result) => {
    dataList.value = result
  })
}
</script>
