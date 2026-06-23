<template>
  <a-device v-if="isMobile()"></a-device>
  <a-platform v-else></a-platform>
</template>

<script setup lang="ts">
import { isMobile } from '@/hooks/useDevice'
import ADevice from '@/components/zone/protection/rule/device.vue'
import APlatform from '@/components/zone/protection/rule/platform.vue'
</script>
