<template>
  <div class="a-setting-ips platform inline-flex-c-n-n">
    <div class="head inline-flex-r-c-b">
      <div class="head-left"></div>
      <div class="head-right">
        <a-button type="primary" :icon="Plus" @click="isIpAddView = true">
          {{ $t('setting.add-ip') }}
        </a-button>
      </div>
    </div>
    <div class="content inline-flex-c-n-n">
      <div class="table">
        <el-table :data="dataList">
          <el-table-column width="48">
            <template #default="{ $index }">
              <div class="index">{{ $index + 1 }}</div>
            </template>
          </el-table-column>
          <el-table-column property="id" label="ID" width="280"></el-table-column>
          <el-table-column property="name" :label="$t('extension.name')" width="auto" min-width="180"></el-table-column>
          <el-table-column prop="content" :label="$t('extension.content')" width="320">
            <template #default="{ row }">
              {{ loadCidrList(row) }}
              <i18n-t keypath="setting.ip-content-description" tag="span" scope="global">
                <template v-slot:placeholder>
                  {{ row.cidrs.length }}
                </template>
              </i18n-t>
            </template>
          </el-table-column>
          <el-table-column prop="remark" :label="$t('extension.remark')" width="280">
            <template #default="{ row }">
              <div class="inline-color-grey">{{ row.remark || '-' }}</div>
            </template>
          </el-table-column>
          <el-table-column fixed="right" :label="$t('extension.operation')" width="280">
            <template #default="{ row }">
              <el-button link type="primary"
                @click="handleEdit(row)"
              >
                {{ $t('common.edit') }}
              </el-button>
              <el-popconfirm :title="$t('extension.delete-item-tips')" width="258"
                @confirm="handleDelete(row)"
              >
                <template #reference>
                  <el-button link type="danger" :icon="Delete">
                    {{ $t('common.delete') }}
                  </el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <a-nodata v-if="dataList.length === 0"
        :loading="progression.loading"
        :success="progression.success"
      />
    </div>
  </div>
  <!-- 添加IP组弹窗 -->
  <a-ip-add :visible="isIpAddView"
    @close="isIpAddView = false"
    @change="handleLoad"
  />
  <!-- 编辑IP组弹窗 -->
  <a-ip-edit :visible="isIpEditView" :id="loadEditData.id?.toString()"
    @close="isIpEditView = false"
    @change="handleLoad"
  />
</template>

<script setup lang="ts">
import {
  Plus,
  Delete
} from '@element-plus/icons-vue'
import { useRequest } from '@/hooks/useRequest'
import AIpAdd from './add/platform.vue'
import AIpEdit from './edit/platform.vue'

const dataList = ref<any[]>([])
const isIpAddView = ref(false)
const isIpEditView = ref(false)
const loadEditData = ref<any>({})

const loadCidrList = computed(() => (data: any) => {
  // 获取CIDR列表前1个IP
  if (data.cidrs.length > 0) {
    return data.cidrs[0]
  }
  return '-'
})

const { ioload, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const handleLoad = () => {
  ioload('ip', 'loadIpList', null).then((result) => {
    dataList.value = result
  })
}
const handleEdit = (data: any) => {
  loadEditData.value = data
  isIpEditView.value = true
}
const handleDelete = (data: any) => {
  const params = {
    id: data.id.toString()
  }
  ioload('ip', 'xhrIpRemove', params).then(() => {
    handleLoad()
  })
}
</script>
