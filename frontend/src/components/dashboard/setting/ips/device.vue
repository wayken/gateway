<template>
  <div class="a-setting-ips device">
    <div class="head inline-flex-r-c-b">
      <div class="head-left"></div>
      <div class="head-right">
        <a-button type="primary" :icon="Plus">
          {{ $t('setting.add-ip') }}
        </a-button>
      </div>
    </div>
    <div class="content inline-flex-c-n-n">
      <div v-for="(data, index) in dataList" :key="index">
        <div class="source">
          {{ loadCidrList(data) }}
          <i18n-t keypath="setting.ip-content-description" tag="span" scope="global">
            <template v-slot:placeholder>
              {{ data.cidrs.length }}
            </template>
          </i18n-t>
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

const loadCidrList = computed(() => (data: any) => {
  // 获取CIDR列表前1个IP
  if (data.cidrs.length > 0) {
    return data.cidrs[0]
  }
  return '-'
})

const { ioload, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const handleLoad = () => {
  ioload('ip', 'loadIpList', null).then((result) => {
    dataList.value = result
  })
}
</script>
