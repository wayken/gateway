<template>
  <div class="a-website device inline-flex-c-n-n">
    <div class="head inline-flex-r-c-b">
      <div class="head--left"></div>
      <div class="head--right">
        <a-button :icon="Plus">
          {{ $t('website.add-zone') }}
        </a-button>
      </div>
    </div>
    <div class="content inline-flex-c-n-n">
      <div class="card inline-flex-r-c-n" v-for="(data, index) in dataList" :key="index"
        @click="handleClick(data)"
      >
        <a-svg-icon icon-class="laptop" />
        <div class="source">
          <div class="infomation inline-flex-r-c-b">
            <div class="name" v-if="!isGlobalZone(data.id)">{{ data.name }}</div>
            <div class="name" v-else>
              {{ $t('website.global-zone') }}
            </div>
            <div class="status is-actived" v-if="data.status === 1">
              <el-icon><Check /></el-icon>
              <div class="name">{{ $t('website.zone-status-actived') }}</div>
            </div>
            <div class="status is-holded" v-if="data.status === 0">
              <el-icon><WarnTriangleFilled /></el-icon>
              <div class="name">{{ $t('website.zone-status-hold') }}</div>
            </div>
          </div>
          <div class="metadata inline-flex-r-c-b">
            <div class="remark">{{ data.remark || '-' }}</div>
            <el-button link type="primary">
              {{ $t('common.edit') }}
            </el-button>
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
  Check,
  WarnTriangleFilled
} from '@element-plus/icons-vue'
import { useRequest } from '@/hooks/useRequest'
import { isGlobalZone } from '@/hooks/page/useZone'

const dataList = ref<any[]>([])

const { ioload, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const router = useRouter()
const handleLoad = () => {
  ioload('zone', 'loadZoneList', null).then((result) => {
    dataList.value = result
  })
}
const handleClick= (data: any) => {
  router.push(`/zone/${data.id}`)
}
</script>
