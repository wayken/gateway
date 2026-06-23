<template>
  <div class="a-zone-rewrite-request platform inline-flex-c-n-n">
    <div class="head inline-flex-r-c-b">
      <div class="head--left inline-flex-r-c-n">
        <el-input>
          <template #prepend>
            <el-button :icon="Search" />
          </template>
          <template #append>
            <el-button>
              {{ $t('common.search') }}
            </el-button>
          </template>
        </el-input>
      </div>
      <div class="head--right">
        <a-button :icon="Plus" @click="handleAdd">
          {{ $t('protection.rule.add-rule') }}
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
          <el-table-column prop="status" :label="$t('extension.status')" width="220">
            <template #default="{ row }">
              <div class="status is-enable" v-if="row.status === 1">
                <el-icon><Check /></el-icon>
                <div class="name">{{ $t('extension.enable') }}</div>
              </div>
              <div class="status is-disable" v-if="row.status === 0">
                <el-icon><WarnTriangleFilled /></el-icon>
                <div class="name">{{ $t('extension.disable') }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="id" :label="$t('protection.rule.rule-id')" width="220">
            <template #default="{ row }">
              <div class="id inline-pointer" @click="handleView(row)">{{ row.id }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="name" :label="$t('protection.rule.rule-name')">
            <template #default="{ row }">
              <div class="name inline-pointer" @click="handleView(row)">{{ row.name || '-' }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="remark" :label="$t('extension.remark')" width="280">
            <template #default="{ row }">
              {{ row.remark || '-' }}
            </template>
          </el-table-column>
          <el-table-column fixed="right" :label="$t('extension.operation')" width="280">
            <template #default="{ row }">
              <el-button link type="primary" :icon="Edit"
                @click="handleEdit(row)"
              >
                {{ $t('common.edit') }}
              </el-button>
              <el-popconfirm v-if="row.status === 0" :title="$t('protection.rule.tips.enable-rule')" width="280"
                @confirm="handleStatusUpdate(row, 1)"
              >
                <template #reference>
                  <el-button link type="success" :icon="Check">
                    {{ $t('extension.enable') }}
                  </el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm v-if="row.status === 1" :title="$t('protection.rule.tips.disable-rule')" width="280"
                @confirm="handleStatusUpdate(row, 0)"
              >
                <template #reference>
                  <el-button link type="danger" :icon="WarnTriangleFilled">
                    {{ $t('extension.disable') }}
                  </el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm :title="$t('extension.delete-item-tips')" width="258"
                @confirm="handleDelete(row)"
              >
                <template #reference>
                  <el-button link type="danger" :icon="Delete">
                    {{ $t('common.delete') }}
                  </el-button>
                </template>
              </el-popconfirm>
              <el-button link type="success" :icon="Download"
                @click="handleExport(row)"
              >
                {{ $t('extension.export') }}
              </el-button>
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
  <!-- 数据编辑器 -->
  <a-monaco v-model:visible="isMonacoView" :value="loadActivedJsonData"
    @change="handleDataUpdate"
  />
</template>

<script setup lang="ts">
import {
  Plus,
  Edit,
  Check,
  Delete,
  Search,
  Download,
  WarnTriangleFilled
} from '@element-plus/icons-vue'
import { useFileDownload } from '@/utils/dom'
import { useRequest } from '@/hooks/useRequest'
import AMonaco from '@/components/common/monaco/platform.vue'

const dataList = ref<any[]>([])
const isMonacoView = ref(false)
const loadActivedJsonData = ref<string>('')

const { ioload, iopost, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const router = useRouter()
const handleLoad = () => {
  const params = {
    zone: router.currentRoute.value.params.id
  }
  ioload('zone', 'loadRewriteRequestRuleList', params).then((result) => {
    dataList.value = result
  })
}

const handleView = (data: any) => {
  const dataClone = JSON.parse(JSON.stringify(data))
  loadActivedJsonData.value = JSON.stringify(dataClone, null, 2)
  isMonacoView.value = true
}
const handleAdd = () => {
  const zone = router.currentRoute.value.params.id
  router.push({ path: `/zone/rewrite/Request/add/${zone}` })
}
const handleEdit = (data: any) => {
  const zone = router.currentRoute.value.params.id
  const id = data.id
  router.push({ path: `/zone/rewrite/Request/edit/${zone}/${id}` })
}
const handleDelete = (data: any) => {
  const params = {
    id: data.id,
    zone: router.currentRoute.value.params.id
  }
  iopost('zone', 'xhrRewriteRequestRemove', params).then(() => {
    handleLoad()
  })
}
const handleDataUpdate = (data: any) => {
  data.zone = router.currentRoute.value.params.id
  iopost('zone', 'xhrRewriteRequestUpdate', data, {
    onMessage: true
  }).then(() => {
    handleLoad()
  })
}
const handleStatusUpdate = (data: any, status: number) => {
  const route = router.currentRoute.value
  const params = { ...data }
  params.zone = route.params.id
  params.status = status
  iopost('zone', 'xhrRewriteRequestUpdate', params, {
    onMessage: true
  }).then(() => {
    handleLoad()
  })
}
const handleExport = (data: any) => {
  const dataClone = JSON.parse(JSON.stringify(data))
  const dataJson = JSON.stringify(dataClone, null, 2)
  const date = new Date()
  const formattedDate = date.toISOString().replace(/[-:T]/g, '').slice(0, 14)
  useFileDownload(dataJson, `Request_${data.id}_${formattedDate}.json`)
}
</script>
