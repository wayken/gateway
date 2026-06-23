<template>
  <div class="a-setting-certificate platform inline-flex-c-n-n">
    <div class="head inline-flex-r-c-b">
      <div class="head-left"></div>
      <div class="head-right">
        <a-button type="primary" :icon="Plus" @click="isAddView = true">
          {{ $t('setting.add-certificate') }}
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
          <el-table-column property="domain" :label="$t('setting.bind-domain')" width="auto" min-width="180"></el-table-column>
          <el-table-column property="issuser" :label="$t('setting.issuser')" width="220"></el-table-column>
          <el-table-column prop="period" :label="$t('setting.period')" width="340">
            <template #default="{ row }">
              {{ datetimeftfn(row.before) }} - {{ datetimeftfn(row.after) }}
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
  <!-- 添加证书弹窗 -->
  <a-certificate-add :visible="isAddView"
    @close="isAddView = false"
    @change="handleLoad"
  />
  <!-- 编辑证书弹窗 -->
  <a-certificate-edit :visible="isEditView" :id="loadEditData.id?.toString()"
    @close="isEditView = false"
    @change="handleLoad"
  />
</template>

<script setup lang="ts">
import {
  Plus,
  Delete
} from '@element-plus/icons-vue'
import { useRequest } from '@/hooks/useRequest'
import { datetimeftfn } from '@/hooks/useDataFormatter'
import ACertificateAdd from './add/platform.vue'
import ACertificateEdit from './edit/platform.vue'

const dataList = ref<any[]>([])
const isAddView = ref(false)
const isEditView = ref(false)
const loadEditData = ref<any>({})

const { ioload, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const handleLoad = () => {
  ioload('certificate', 'loadCertificateList', null).then((result) => {
    dataList.value = result
  })
}

const handleEdit = (data: any) => {
  loadEditData.value = data
  isEditView.value = true
}
const handleDelete = (data: any) => {
  const params = {
    id: data.id.toString()
  }
  ioload('certificate', 'xhrCertificateRemove', params).then(() => {
    handleLoad()
  })
}
</script>
