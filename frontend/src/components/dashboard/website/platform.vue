<template>
  <div class="a-website platform inline-flex-c-n-n">
    <div class="head inline-flex-r-c-b">
      <div class="head--left">
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
        <a-button :icon="Plus" @click="isWebsiteAddView = true">
          {{ $t('website.add-zone') }}
        </a-button>
      </div>
    </div>
    <div class="content inline-flex-c-n-n">
      <div class="table">
        <el-table :data="dataList">
          <el-table-column prop="zone" :label="$t('website.zone-id')" width="220">
            <template #default="{ row }">
              <div class="id inline-flex-r-c-n" @click="handleClick(row)">
                <div v-if="isGlobalZone(row.id)" class="name">-</div>
                <div v-else class="name">{{ row.id }}</div>
                <el-tooltip effect="dark" placement="top"
                  :content="$t('extension.openin-new-window')"
                >
                  <el-icon @click.stop="handleWindowOpen(row)"><Promotion /></el-icon>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="name" :label="$t('website.zone-name')" width="auto" min-width="180">
            <template #default="{ row }">
              <div class="zone inline-flex-r-c-n"
                @click="handleClick(row)"
              >
                <a-svg-icon icon-class="laptop" />
                <div class="name" v-if="isGlobalZone(row.id)">
                  {{ $t('website.global-zone') }}
                </div>
                <div class="name" v-else>{{ row.name }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="match" :label="$t('website.matched-zone')" width="420">
            <template #default="{ row }">
              <div class="match">
                <div v-if="isGlobalZone(row.id)">*</div>
                <div v-else v-for="(data, key) in row.match" :key="key">{{ data }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="$t('extension.status')" width="220">
            <template #default="{ row }">
              <div class="status is-actived" v-if="row.status === 1">
                <el-icon><Check /></el-icon>
                <div class="name">{{ $t('website.zone-status-actived') }}</div>
              </div>
              <div class="status is-holded" v-if="row.status === 0">
                <el-icon><WarnTriangleFilled /></el-icon>
                <div class="name">{{ $t('website.zone-status-hold') }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="remark" :label="$t('extension.remark')" width="280">
            <template #default="{ row }">
              <div class="remark inline-color-grey">{{ row.remark || '-' }}</div>
            </template>
          </el-table-column>
          <el-table-column fixed="right" :label="$t('extension.operation')" width="280">
            <template #default="{ row }">
              <el-button v-if="!isGlobalZone(row.id)" link type="primary" :icon="Edit"
                @click="handleEdit(row)"
              >
                {{ $t('common.edit') }}
              </el-button>
              <el-popconfirm v-if="row.status === 0" :title="$t('website.tips.enable-zone')" width="280"
                @confirm="handleStatusUpdate(row, 1)"
              >
                <template #reference>
                  <el-button link type="success" :icon="Check">
                    {{ $t('website.zone-status-actived') }}
                  </el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm v-if="row.status === 1" :title="$t('website.tips.disable-zone')" width="280"
                @confirm="handleStatusUpdate(row, 0)"
              >
                <template #reference>
                  <el-button link type="danger" :icon="WarnTriangleFilled">
                    {{ $t('website.zone-status-hold') }}
                  </el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm v-if="!isGlobalZone(row.id)" :title="$t('website.tips.delete-zone')" width="420"
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
  <!-- 添加站点弹窗 -->
  <a-website-add :visible="isWebsiteAddView"
    @close="isWebsiteAddView = false"
    @change="handleAdded"
  />
  <!-- 编辑站点弹窗 -->
  <a-website-edit :visible="isWebsiteEditView"
    :zoneId="loadEditData.id"
    @change="handleLoad"
    @close="isWebsiteEditView = false"
  />
</template>

<script setup lang="ts">
import {
  Plus,
  Check,
  Search,
  Edit,
  Delete,
  Promotion,
  WarnTriangleFilled
} from '@element-plus/icons-vue'
import { useRequest } from '@/hooks/useRequest'
import { isGlobalZone } from '@/hooks/page/useZone'
import AWebsiteAdd from './add/platform.vue'
import AWebsiteEdit from './edit/platform.vue'

const dataList = ref<any[]>([])
const isWebsiteAddView = ref(false)
const isWebsiteEditView = ref(false)
const loadEditData = ref<any>({})

const { ioload, iopost, progression } = useRequest()
onMounted(() => {
  handleLoad()
})


const router = useRouter()
const handleLoad = () => {
  ioload('zone', 'loadZoneList', null).then((result) => {
    dataList.value = result
  })
}
const handleClick= (data: any) => {
  router.push(`/zone/${data.id}`)
}
const handleEdit = (data: any) => {
  loadEditData.value = data
  isWebsiteEditView.value = true
}
const handleWindowOpen = (data: any) => {
  const path = `/zone/${data.id}`
  let routeData = router.resolve({
    path: <string> path
  })
  window.open(routeData.href, '_blank')
}
const handleAdded = (data: any) => {
  dataList.value.splice(1, 0, data)
}
const handleStatusUpdate = (data: any, status: number) => {
  const params = { ...data }
  params.zone = data.id
  params.status = status
  iopost('zone', 'xhrZoneUpdate', params).then(() => {
    handleLoad()
  })
}
const handleDelete = (data: any) => {
  const params = {
    zone: data.id
  }
  ioload('zone', 'xhrZoneRemove', params).then(() => {
    handleLoad()
  })
}
</script>
