<template>
  <div class="a-zone-rewrite-request device inline-flex-c-n-n">
    <div class="head inline-flex-r-c-b">
      <div class="head--left"></div>
      <div class="head--right">
        <a-button :icon="Plus" @click="handleAdd">
          {{ $t('protection.rule.add-rule') }}
        </a-button>
      </div>
    </div>
    <div class="content inline-flex-c-n-n">
      <div class="card inline-flex-r-c-n" v-for="(data, index) in dataList" :key="index">
        <a-svg-icon icon-class="shield" />
        <div class="source">
          <div class="infomation inline-flex-r-c-b">
            <div class="name">{{ data.name }}</div>
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
  Plus
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
  ioload('zone', 'loadRewriteRequestRuleList', params).then((result) => {
    dataList.value = result
  })
}

const handleAdd = () => {
}
</script>
