<template>
  <div class="a-audit-session platform inline-flex-c-n-n">
    <div class="head inline-flex-r-c-b">
      <div class="head-left"></div>
      <div class="head-right">
        <a-button type="primary" :icon="Search">
          {{ $t('common.search') }}
        </a-button>
      </div>
    </div>
    <div class="content inline-flex-c-n-n">
      <div class="table">
        <el-table :data="dataList" stripe>
          <el-table-column width="48">
            <template #default="{ $index }">
              <div class="index">{{ $index + 1 }}</div>
            </template>
          </el-table-column>
          <el-table-column property="id" label="ID" width="280"></el-table-column>
          <el-table-column property="name" :label="$t('audit.session-user')" width="180"></el-table-column>
          <el-table-column property="ip" :label="$t('audit.session-location')" width="180"></el-table-column>
          <el-table-column prop="user_agent" :label="$t('audit.session-device')" width="auto" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.user_agent }}
            </template>
          </el-table-column>
          <el-table-column prop="date" :label="$t('audit.session-time')" width="280">
            <template #default="{ row }">
              {{ datetimeftfn(row.date) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination">
        <div class="pagination__total">
          {{ $t('pagination.total', { total: pagination.count }) }}
        </div>
        <div class="pagination__content">
          <el-pagination layout="prev, pager, next, jumper"
            :page-size="pagination.size"
            :total="pagination.count"
            @current-change="handlePageClick"
          >
          </el-pagination>
        </div>
      </div>
      <a-nodata v-if="dataList.length === 0"
        :loading="progression.batchLoading"
        :success="progression.success"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Search
} from '@element-plus/icons-vue'
import { useRequest } from '@/hooks/useRequest'
import { datetimeftfn } from '@/hooks/useDataFormatter'

const dataList = ref<any[]>([])
const pagination = reactive({
  index: 0,
  size: 10,
  count: 0
})

const { ioload, progression } = useRequest()
onMounted(async () => {
  try {
    progression.batchLoading = true
    await handleDataLoad()
    await handleCountLoad()
  } finally {
    progression.batchLoading = false
  }
})

const handleDataLoad = async (page = 0) => {
  const params = {
    start: 0,
    limit: pagination.size
  }
  // 有分页参数则代表是属于点击分页加载数据
  if (page) {
    dataList.value = []
    params.start = (page - 1) * pagination.size
  }
  dataList.value = await ioload('audit', 'loadSessionLogList', params)
}
const handleCountLoad = async () => {
  const result = await ioload('audit', 'loadSessionLogCount', null)
  pagination.count = result.count || 0
}
// 分页点击事件
const handlePageClick = async (page: number) => {
  progression.batchLoading = true
  try {
    await handleDataLoad(page)
  } finally {
    progression.batchLoading = false
  }
}
</script>
