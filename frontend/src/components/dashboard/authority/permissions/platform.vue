<template>
  <div class="a-authority-permissions platform inline-flex-c-n-n">
    <div class="head inline-flex-r-c-b">
      <div class="head-left"></div>
      <div class="head-right">
        <a-button type="link" :icon="Sort" @click="handleTableOpenCollapse">
          {{ $t('authority.open-collapse') }}
        </a-button>
      </div>
    </div>
    <div class="content inline-flex-c-n-n">
      <div class="table">
        <el-table v-if="isTableRefresh" ref="tableRef" :data="permissions" row-key="name" :default-expand-all="isTableExpandAll">
          <el-table-column prop="name" :label="$t('authority.menu-name')" width="auto" min-width="320">
            <template #default="{ row }">
              <span class="name">{{ row.name ? $t('permission.' + row.name) : '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="permission" :label="$t('authority.role-permission')" width="380">
            <template #default="{ row }">
              {{ row.permission || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="$t('extension.status')" width="420">
            <template #default="{ row }">
              <div class="status is-enable" v-if="row.status !== 1">
                <el-icon><Check /></el-icon>
                <div class="name">{{ $t('authority.status-normal') }}</div>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Sort,
  Check
} from '@element-plus/icons-vue'
import { ElTable } from 'element-plus'
import permissions from '@/config/permissions.json'

const isTableRefresh = ref(true)
const isTableExpandAll = ref(false)
const tableRef = ref<InstanceType<typeof ElTable>>()
// 点击展现/折叠表格
const handleTableOpenCollapse = () => {
  isTableRefresh.value = false
  isTableExpandAll.value = !isTableExpandAll.value
  nextTick(() => {
    isTableRefresh.value = true
  })
}
</script>
