<template>
  <div class="a-authority inline-flex-r-n-n">
    <el-drawer v-if="isMobile()" direction="ltr" :size="228"
      :with-header="false"
      :model-value="!isCollapsed"
      @close="handleCollapsed(true)"
    >
      <a-menu @click="handleCollapsed(true)"></a-menu>
    </el-drawer>
    <a-menu v-else></a-menu>
    <div class="content inline-flex-c-n-n">
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
        </template>
      </a-head>
      <router-view></router-view>
    </div>
  </div>
</template>

<script setup lang="ts">
import { isMobile } from '@/hooks/useDevice'
import AHead from '@/components/common/head/index.vue'
import AMenu from '@/components/dashboard/authority/menu.vue'

const isCollapsed = ref(false)

const handleCollapsed = (value: boolean) => {
  isCollapsed.value = value
}
</script>
