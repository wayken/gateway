<template>
  <a-device v-if="isMobile()"></a-device>
  <a-platform v-else></a-platform>
</template>

<script setup lang="ts">
import { isMobile } from '@/hooks/useDevice'
import ADevice from '@/components/zone/cache/rule/device.vue'
import APlatform from '@/components/zone/cache/rule/platform.vue'
</script>
