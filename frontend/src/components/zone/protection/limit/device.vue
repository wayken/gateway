<template>
  <div class="a-zone-protection-limit device inline-flex-c-n-n">
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
            <div class="action inline-flex-r-c-n"
              :class="['is-' + data.action.type]"
            >
              <el-icon>
                <component :is="loadTypeMapping[data.action.type]?.icon"></component>
              </el-icon>
              <div class="name" v-if="data.action.type">
                {{ $t('rule.action.type.' + data.action.type) }}
              </div>
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
  Lock,
  Avatar,
  Compass,
  Document,
  Message
} from '@element-plus/icons-vue'
import { useRequest } from '@/hooks/useRequest'

const dataList = ref<any[]>([])
const loadTypeMapping: any = {
  'block': {
    icon: markRaw(Lock)
  },
  'skip': {
    icon: markRaw(Check)
  },
  'log': {
    icon: markRaw(Document)
  },
  'cookie_challenge': {
    icon: markRaw(Compass)
  },
  'js_challenge': {
    icon: markRaw(Message)
  },
  'interactive_challenge': {
    icon: markRaw(Avatar)
  }
}

const { ioload, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const router = useRouter()
const handleLoad = () => {
  const params = {
    zone: router.currentRoute.value.params.id
  }
  ioload('zone', 'loadLimitRuleList', params).then((result) => {
    dataList.value = result
  })
}

const handleAdd = () => {
  const zone = router.currentRoute.value.params.id
  router.push({ path: `/zone/protection/limit/add/${zone}` })
}
</script>
