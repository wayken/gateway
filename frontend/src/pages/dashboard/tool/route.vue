<template>
  <a-head></a-head>
  <a-device v-if="isMobile()"></a-device>
  <a-platform v-else></a-platform>
</template>

<script setup lang="ts">
import AHead from '@/components/common/head/index.vue'
import { isMobile } from '@/hooks/useDevice'
import ADevice from '@/components/dashboard/tool/route/device.vue'
import APlatform from '@/components/dashboard/tool/route/platform.vue'
</script>
