<template>
  <a-head></a-head>
  <a-device v-if="isMobile()"></a-device>
  <a-platform v-else></a-platform>
</template>

<script setup lang="ts">
import { isMobile } from '@/hooks/useDevice'
import AHead from '@/components/common/head/index.vue'
import ADevice from '@/components/dashboard/website/device.vue'
import APlatform from '@/components/dashboard/website/platform.vue'
</script>
