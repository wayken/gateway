<template>
  <div class="zones inline-flex-c-n-n">
    <div class="placeholder inline-flex-r-c-n">
      <el-icon><SuccessFilled /></el-icon>
      {{ $t('head.zone-search-placeholder') }}
    </div>
    <div class="search inline-flex-r-c-n">
      <el-input v-model="search" clearable>
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary">
        {{ $t('common.search') }}
      </el-button>
    </div>
    <div class="source">
      <div class="zone inline-flex-r-c-n" v-for="(data, key) in dataList" :key="key"
        :class="{
          'is-actived': isZoneActived(data)
        }"
        @click="handleClick(data)"
      >
        <div class="icon">
          <a-svg-icon icon-class="laptop" />
        </div>
        <div class="metadata">
          <div class="name" v-if="isGlobalZone(data.id)">
            {{ $t('website.global-zone') }}
          </div>
          <div class="name" v-else>{{ data.name }}</div>
          <div class="status is-actived" v-if="data.status === 1">
            <el-icon><Check /></el-icon>
            <div class="name">{{ $t('website.zone-status-actived') }}</div>
          </div>
          <div class="status is-holded" v-if="data.status === 0">
            <el-icon><WarnTriangleFilled /></el-icon>
            <div class="name">{{ $t('website.zone-status-hold') }}</div>
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
  Check,
  Search,
  SuccessFilled,
  WarnTriangleFilled
} from '@element-plus/icons-vue'
import { useRequest } from '@/hooks/useRequest'
import { isGlobalZone } from '@/hooks/page/useZone'

const search = ref('')
const dataList = ref<any[]>([])
const router = useRouter()
const { ioload, progression } = useRequest()

onMounted(() => {
  handleLoad()
})

const isZoneActived = (data: any) => {
  const route = router.currentRoute.value
  return route.params.id === data.id
}
const handleLoad = () => {
  ioload('zone', 'loadZoneList', null).then((result) => {
    dataList.value = result
  })
}
const handleEmit = defineEmits(['switch'])
const handleClick = (data: any) => {
  handleEmit('switch', data)
}
</script>
