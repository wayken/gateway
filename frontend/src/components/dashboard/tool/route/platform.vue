<template>
  <div class="a-tool-route platform inline-flex-c-n-n">
    <div class="head inline-flex-r-c-n">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/tool' }">
          {{ $t('menu.tool') }}
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          {{ $t('tool.route-matcher') }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="content">
      <a-nodata
        :loading="false"
        :success="true"
        :description="$t('nodata.in-development')"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
</script>
