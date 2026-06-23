<template>
  <div class="a-route device inline-flex-c-n-n">
    <div class="head inline-flex-r-c-b">
      <div class="head--left"></div>
      <div class="head--right">
        <a-button :icon="Plus">
          {{ $t('route.add-route') }}
        </a-button>
      </div>
    </div>
    <div class="content inline-flex-c-n-n">
      <div class="card inline-flex-r-c-n" v-for="(data, index) in dataList" :key="index">
        <a-svg-icon icon-class="route" />
        <div class="source">
          <div class="infomation inline-flex-r-c-b">
            <div class="name inline-text-ellipsis">
              {{ data.id + ':' + (data.name || '-') }}
            </div>
            <div class="status is-actived" v-if="data.status === 1">
              <el-icon><Check /></el-icon>
              <div class="name">{{ $t('route.route-status-deploy') }}</div>
            </div>
            <div class="status is-holded" v-if="data.status === 0">
              <el-icon><WarnTriangleFilled /></el-icon>
              <div class="name">{{ $t('route.route-status-undeploy') }}</div>
            </div>
          </div>
          <div class="path inline-flex-r-c-n">
            <div class="name">{{ $t('route.route-path') }}：</div>
            <div class="value">{{ data.path }}</div>
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

const dataList = ref<any[]>([])

const { ioload, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const router = useRouter()
const handleLoad = () => {
  const params = {
    zone: router.currentRoute.value.params.id
  }
  ioload('zone', 'loadRouteList', params).then((result) => {
    dataList.value = result
  })
}
</script>
