<template>
  <div class="a-setting-certificate device inline-flex-c-n-n">
    <div class="head inline-flex-r-c-b">
      <div class="head-left"></div>
      <div class="head-right">
        <a-button type="primary" :icon="Plus">
          {{ $t('setting.add-certificate') }}
        </a-button>
      </div>
    </div>
    <div class="content">
      <div v-for="(data, index) in dataList" :key="index">
        <div class="source">
          <div class="domain inline-flex-r-c-n">
            <el-icon :size="24"><Document /></el-icon>
            {{ data.domain }}
          </div>
          <div class="metadata inflex-text-ellipsis">
            {{ data.issuser || '-' }}
          </div>
        </div>
      </div>
      <a-nodata v-if="dataList.length === 0"
        :loading="progression.loading"
        :success="progression.success"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Plus,
  Document
} from '@element-plus/icons-vue'
import { useRequest } from '@/hooks/useRequest'

const dataList = ref<any[]>([])

const { ioload, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const handleLoad = () => {
  ioload('certificate', 'loadCertificateList', null).then((result) => {
    dataList.value = result
  })
}
</script>
