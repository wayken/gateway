<template>
  <div class="sequence inline-flex-c-n-n">
    <div class="head inline-flex-r-c-b">
      {{ $t('sequence.traffic-sequence') }}
      <el-icon class="close" @click="handleClose">
        <Close />
      </el-icon>
    </div>
    <div class="content" v-loading="progression.loading">
      <div class="http inline-flex-c-c-c">
        <div class="wrapper inline-flex-r-c-c">
          <el-icon><Monitor /></el-icon>
          <div class="name">{{ $t('sequence.http-request') }}</div>
        </div>
        <div class="direction">
          <el-icon><Bottom /></el-icon>
        </div>
      </div>
      <div class="card" v-for="(data, index) in loadSequenceList" :key="index">
        <div class="wrapper" @click="handleRuleClick(data)">
          <div class="rule inline-flex-r-c-n">
            <a-svg-icon :icon-class="data.icon" />
            <div class="name">{{ $t('sequence.' + data.name) }}</div>
            <div v-if="infomation[data.name]" class="infomation">
              <i18n-t keypath="sequence.rule-count-info" tag="div" scope="global">
                <template v-slot:placeholder>
                  {{ infomation[data.name] }}
                </template>
              </i18n-t>
              </div>
          </div>
          <div class="description">
            {{ $t('sequence.' + data.name + '-description') }}
          </div>
          </div>
        <div class="hr inline-flex-r-c-c" v-if="index !== loadSequenceList.length - 1"></div>
      </div>
      <div class="http inline-flex-c-c-c">
        <div class="direction">
          <el-icon><Bottom /></el-icon>
        </div>
        <div class="wrapper inline-flex-r-c-c">
          <el-icon><UserFilled /></el-icon>
          <div class="name">{{ $t('sequence.end-user') }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Close,
  Bottom,
  Monitor,
  UserFilled
} from '@element-plus/icons-vue'
import { useRequest } from '@/hooks/useRequest'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const loadSequenceList = [
  {
    name: 'route',
    icon: 'route',
    path: '/zone/route'
  },
  {
    name: 'upstream',
    icon: 'palette',
    path: '/zone/upstream'
  },
  {
    name: 'redirect',
    icon: 'rewrite',
    path: '/zone/rewrite/redirect'
  },
  {
    name: 'waf',
    icon: 'shield',
    path: '/zone/protection/rule'
  },
  {
    name: 'limit',
    icon: 'shield',
    path: '/zone/protection/limit'
  },
  {
    name: 'rewrite-request',
    icon: 'rewrite',
    path: '/zone/rewrite/request'
  },
  {
    name: 'cache',
    icon: 'database',
    path: '/zone/cache/rule'
  },
  {
    name: 'rewrite-response',
    icon: 'rewrite',
    path: '/zone/rewrite/response'
  }
]
const infomation = ref<any>({})

onMounted(() => {
  handleLoad()
})

const router = useRouter()
const { ioload, progression } = useRequest()
const handleLoad = () => {
  const route = router.currentRoute.value
  const zoneId = route.params.id
  const params = {
    zone: zoneId
  }
  ioload('zone', 'loadZoneSequenceInfo', params).then((result) => {
    infomation.value = result
  })
}
const handleEmit = defineEmits(['close'])
const handleClose = () => {
  handleEmit('close')
}
const handleRuleClick = (data: any) => {
  if (data.path) {
    const route = router.currentRoute.value
    const id = route.params.id
    const path = `${data.path}/${id}`
    router.push(path)
  }
}

watch(() => props.visible, (value) => {
  if (value) {
    handleLoad()
  }
})
</script>
