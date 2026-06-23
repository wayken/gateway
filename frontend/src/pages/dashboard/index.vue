<template>
  <div class="a-dashboard inline-flex-r-n-n">
    <el-drawer v-if="isMobile()" v-model="isMenuOpened" direction="ltr"
      :size="88"
      :with-header="false"
    >
      <a-menu @click="handleCollapse(false)"></a-menu>
    </el-drawer>
    <a-menu v-else></a-menu>
    <div class="page inline-flex-c-n-n">
      <router-view />
    </div>
  </div>
  <a-touch :value="isMenuOpened" @click="handleCollapse"></a-touch>
</template>

<script setup lang="ts">
import { isMobile } from '@/hooks/useDevice'
import AMenu from '@/components/dashboard/menu.vue'
import ATouch from '@/components/dashboard/touch.vue'

const isMenuOpened = ref(false)

const handleCollapse = (value: boolean) => {
  isMenuOpened.value = value
}
</script>
