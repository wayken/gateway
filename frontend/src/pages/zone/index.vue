<template>
  <div class="a-zone inline-flex-r-n-n">
    <el-drawer v-if="isMobile()" direction="ltr" :size="220"
      :with-header="false"
      :model-value="!isCollapsed"
      @close="handleCollapsed(true)"
    >
      <a-menu :infomation="infomation" @click="handleCollapsed(true)"></a-menu>
    </el-drawer>
    <a-menu v-else :infomation="infomation"
      @switch="handleSwitch"
      @sequence="handleSequenceView"
    ></a-menu>
    <div class="page inline-flex-c-n-n">
      <a-head>
        <template #head>
          <div v-if="isMobile()" class="icon">
            <a-svg-icon v-if="!isCollapsed" icon-class="indent-left" size="28px"
              @click="handleCollapsed(true)"
            />
            <a-svg-icon v-else icon-class="indent-right" size="28px"
              @click="handleCollapsed(false)"
            />
          </div>
          <div class="infomation inline-flex-r-c-n" v-loading="progression.loading">
            <div class="status is-actived" v-if="infomation.status === 1">
              <el-icon><Check /></el-icon>
              <div class="name">{{ $t('website.zone-status-actived') }}</div>
            </div>
            <div class="status is-holded" v-if="infomation.status === 0">
              <el-icon><WarnTriangleFilled /></el-icon>
              <div class="name">{{ $t('website.zone-status-hold') }}</div>
            </div>
            <div class="host inline-flex-r-c-n" v-if="!isMobile()">
              <div v-if="isGlobalZone(infomation.id)">*</div>
              <div v-else v-for="(data, key) in infomation.match" :key="key">
                {{ data }}
              </div>
            </div>
          </div>
        </template>
      </a-head>
      <router-view v-if="isReloadAlive"></router-view>
    </div>
    <el-drawer direction="rtl" :size="480"
      :with-header="false"
      :model-value="isZoneSwitched"
      @close="isZoneSwitched = false"
    >
      <a-zones @switch="handleRouteReload"></a-zones>
    </el-drawer>
    <el-drawer direction="rtl" :size="420"
      :modal="false"
      :with-header="false"
      :model-value="isSequenceView"
      @close="isSequenceView = false"
    >
      <a-sequence :visible="isSequenceView"
        @close="isSequenceView = false"
      ></a-sequence>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import {
  Check,
  WarnTriangleFilled
} from '@element-plus/icons-vue'
import { isMobile } from '@/hooks/useDevice'
import { useRequest } from '@/hooks/useRequest'
import AHead from '@/components/common/head/index.vue'
import AMenu from '@/components/zone/menu.vue'
import AZones from '@/components/zone/zones.vue'
import ASequence from '@/components/zone/sequence.vue'
import { isGlobalZone } from '@/hooks/page/useZone'

const isReloadAlive = ref(true)
const isCollapsed = ref(false)
const isZoneSwitched = ref(false)
const isSequenceView = ref(false)

const router = useRouter()
const infomation = ref<any>({})
const { ioload, progression } = useRequest()

// 加载当前网域信息
const handleZoneLoad = () => {
  const route = router.currentRoute.value
  const zoneId = route.params.id
  if (infomation.value.id !== zoneId) {
    infomation.value.id = zoneId as string
    const params = {
      zone: zoneId
    }
    ioload('zone', 'loadZoneInfo', params).then((result) => {
      infomation.value = result
    })
  }
}
// 站点区域切换
const handleRouteReload = (data: any) => {
  isZoneSwitched.value = false
  router.push({ path: `/zone/${data.id}` }).then(() => {
    isReloadAlive.value = false
    nextTick(() => {
      isReloadAlive.value = true
    })
  })
}
// 菜单折叠功能
const handleCollapsed = (value: boolean) => {
  isCollapsed.value = value
}
// 切换站点区域
const handleSwitch = () => {
  isZoneSwitched.value = true
}
// 查看流量序列
const handleSequenceView = () => {
  isSequenceView.value = true
}

// 监听路由变化
watch(() => router.currentRoute.value, () => {
  handleZoneLoad()
}, {
  immediate: true
})
</script>
