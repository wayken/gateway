<template>
  <a-device v-if="isMobile()"></a-device>
  <a-platform v-else></a-platform>
</template>

<script setup lang="ts">
import { isMobile } from '@/hooks/useDevice'
import ADevice from '@/components/dashboard/setting/provider/device.vue'
import APlatform from '@/components/dashboard/setting/provider/platform.vue'
</script>
