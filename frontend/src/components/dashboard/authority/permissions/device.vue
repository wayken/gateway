<template>
  <div class="a-authority-permissions device inline-flex-c-n-n">
    <div class="content inline-flex-c-n-n">
      <el-tree ref="menuRef" class="permissions" node-key="name"
        :data="permissions"
      >
        <template #default="{ data }">
          <div class="node">
            <span>{{  $t('permission.' + data.name) }}</span>
          </div>
        </template>
      </el-tree>
    </div>
  </div>
</template>

<script setup lang="ts">
import permissions from '@/config/permissions.json'
</script>
