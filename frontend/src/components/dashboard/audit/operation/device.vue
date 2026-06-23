<template>
  <div class="a-audit-operation device inline-flex-c-n-n">
    <a-nodata v-if="dataList.length === 0"
      :loading="false"
      :success="true"
    />
  </div>
</template>

<script setup lang="ts">
const dataList = ref<any[]>([])
</script>
