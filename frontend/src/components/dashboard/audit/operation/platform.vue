<template>
  <div class="a-audit-operation platform inline-flex-c-n-n">
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
          <el-table-column property="module_id" label="ID" width="220"></el-table-column>
          <el-table-column property="name" :label="$t('audit.operation-user')" width="180"></el-table-column>
          <el-table-column :label="$t('audit.operation-type')" width="220">
            <template #default="{ row }">
              <div class="type  inline-flex-r-c-n"
                :style="{
                  backgroundColor: loadOperationTypeInfo(row.operation_type)?.color
                }"
              >
                <el-icon>
                  <component :is="loadOperationTypeInfo(row.operation_type)?.icon"></component>
                </el-icon>
                <div class="name" v-if="row.module">
                  {{ $t('audit.operation-type-' + loadOperationTypeInfo(row.operation_type).name) + $t('audit.module.' + row.module) }}
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column property="ip" :label="$t('audit.operation-location')" width="180"></el-table-column>
          <el-table-column prop="user_agent" :label="$t('audit.operation-device')" width="auto" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.user_agent }}
            </template>
          </el-table-column>
          <el-table-column prop="date" :label="$t('audit.operation-time')" width="220">
            <template #default="{ row }">
              {{ datetimeftfn(row.date) }}
            </template>
          </el-table-column>
          <el-table-column fixed="right" :label="$t('extension.operation')" width="220">
            <template #default="{ row }">
              <el-button link type="success" :icon="View" @click="handleDataView(row)">
                {{ $t('extension.view') }}
              </el-button>
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
  <!-- 数据编辑器 -->
  <a-monaco v-model:visible="isMonacoView" :read-only="true" :value="loadActivedJsonData" />
</template>

<script setup lang="ts">
import {
  View,
  Search
} from '@element-plus/icons-vue'
import { useRequest } from '@/hooks/useRequest'
import { datetimeftfn } from '@/hooks/useDataFormatter'
import { loadOperationTypeInfo } from '@/hooks/page/useAudit'
import AMonaco from '@/components/common/monaco/platform.vue'

const dataList = ref<any[]>([])
const pagination = reactive({
  index: 0,
  size: 10,
  count: 0
})
const isMonacoView = ref(false)
const loadActivedJsonData = ref<string>('')

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
  dataList.value = await ioload('audit', 'loadOperationLogList', params)
}
const handleCountLoad = async () => {
  const result = await ioload('audit', 'loadOperationLogCount', null)
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
// 查看数据详情
const handleDataView = (data: any) => {
  isMonacoView.value = true
  const content = JSON.parse(data.content || '{}')
  loadActivedJsonData.value = JSON.stringify(content, null, 2)
}
</script>
