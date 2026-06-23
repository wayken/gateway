<template>
  <div class="a-route platform inline-flex-c-n-n">
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
          {{ $t('route.add-route') }}
        </a-button>
      </div>
    </div>
    <div class="content inline-flex-c-n-n">
      <div class="table">
        <el-table :data="dataList">
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="infomation">
                <div class="field inline-flex-r-c-n" v-if="row.type">
                  <div class="label">{{ $t('route.route-type') }}：</div>
                  <div class="value">{{ $t('route.route-type-' + row.type) }}</div>
                </div>
                <div class="field inline-flex-r-c-n">
                  <div class="label">{{ $t('upstream.upstream-name') }}：</div>
                  <div class="value">{{ row.upstream?.name || $t('route.input-upstream-manual') }}</div>
                </div>
                <div class="field inline-flex-r-c-n">
                  <div class="label">{{ $t('extension.priority') }}：</div>
                  <div class="value">{{ row.priority || 0 }}</div>
                </div>
                <div class="field inline-flex-r-c-n">
                  <div class="label">{{ $t('upstream.upstream-algorithm') }}：</div>
                  <div class="value">
                    {{ row.upstream?.algorithm ? $t('upstream.algorithm.' + row.upstream?.algorithm) : '-' }}
                  </div>
                </div>
                <div v-if="row.auth_id" class="field inline-flex-r-c-n">
                  <div class="label">{{ $t('setting.auth') }}：</div>
                  <div class="value">{{ row.auth_id }}</div>
                </div>
                <div class="field inline-flex-r-c-n" v-if="row.type === 'restful'">
                  <div class="label">{{ $t('upstream.upstream-type') }}：</div>
                  <div class="value inline-flex-r-c-n upstream"
                    :style="{
                      backgroundColor: loadUpstreamTypeInfo(row.upstream?.type)?.color
                    }"
                  >
                    <el-icon>
                      <component :is="loadUpstreamTypeInfo(row.upstream?.type)?.icon"></component>
                    </el-icon>
                    <div class="name">
                      {{ row.upstream?.type ? $t('upstream.type.' + row.upstream?.type) : '-' }}
                    </div>
                  </div>
                </div>
                <template v-if="row.upstream?.type === 'echo'">
                  <div class="field inline-flex-r-c-n">
                    <div class="label">{{ $t('upstream.echo-content-type') }}：</div>
                    <div class="value">
                      {{ row.upstream?.contentType || '-' }}
                    </div>
                  </div>
                  <div class="field inline-flex-c-n-n">
                    <div class="label">{{ $t('upstream.echo-content') }}：</div>
                    <div class="value echo">
                      {{ row.upstream?.content || '-' }}
                    </div>
                  </div>
                </template>
                <template v-if="row.upstream?.type === 'index'">
                  <div class="field inline-flex-r-c-n">
                    <div class="label">{{ $t('upstream.index-path') }}：</div>
                    <div class="value">
                      {{ row.upstream?.static.path || '-' }}
                    </div>
                  </div>
                  <div class="field inline-flex-r-c-n">
                    <div class="label">{{ $t('upstream.default-index-page') }}：</div>
                    <div class="value">
                      {{ row.upstream?.static.index || '-' }}
                    </div>
                  </div>
                  <div class="field inline-flex-r-c-n">
                    <div class="label">{{ $t('upstream.try-file-uri') }}：</div>
                    <div class="value">
                      {{ row.upstream?.static.fallback || '-' }}
                    </div>
                  </div>
                </template>
                <template v-if="row.upstream?.type === 'node'">
                  <div class="field inline-flex-r-c-n">
                    <div class="label">{{ $t('upstream.node') }}：</div>
                    <div class="value inline-flex-r-c-n node">
                      <div v-for="(data, index) in row.upstream?.nodes" :key="index">
                        {{ data.schema + '://' + data.host + ':' + data.port }}
                      </div>
                    </div>
                  </div>
                </template>
                <template v-if="row.type === 'ai'">
                <div class="field inline-flex-r-c-n">
                  <div class="label">{{ $t('route.ai-provider') }}：</div>
                  <div class="value inline-flex-r-c-n provider">
                    <div class="image">
                      <img :src="loadProviderIcon(handleProviderMatched(row.upstream.provider).name)" />
                    </div>
                    <div class="name">{{ $t('provider.' + handleProviderMatched(row.upstream.provider).name) }}</div>
                  </div>
                </div>
                <div class="field inline-flex-r-c-n">
                  <div class="label">{{ $t('route.ai-model') }}：</div>
                  <div class="value inline-flex-r-c-n">
                    {{ row.upstream?.parameters.model }}
                  </div>
                </div>
                </template>
                <div class="field inline-flex-r-c-n">
                  <div class="label">{{ $t('route.http-method') }}：</div>
                  <div class="value">{{ row.methods }}</div>
                </div>
                <div class="field inline-flex-r-n-n">
                  <div class="label">{{ $t('route.matched-condition') }}：</div>
                  <div class="value">
                    {{ row.rule || '-' }}
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column width="48">
            <template #default="{ $index }">
              <div class="index">{{ $index + 1 }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="id" :label="$t('route.route-id')" width="220">
            <template #default="{ row }">
              <div class="id inline-pointer" @click="handleView(row)">{{ row.id }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="name" :label="$t('route.route-name')" width="auto" min-width="180">
            <template #default="{ row }">
              <div class="name inline-pointer" @click="handleView(row)">{{ row.name || '-' }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="path" :label="$t('route.route-path')" width="220">
            <template #default="{ row }">
              <div class="path">{{ row.path }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="type" :label="$t('route.route-type')" width="180">
            <template #default="{ row }">
              <div class="type" v-if="row.type"
                :style="{
                  backgroundColor: loadRouteTypeInfo(row.type)?.color
                }"
              >{{ $t('route.route-type-' + row.type) }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="priority" :label="$t('extension.priority')" width="140">
            <template #default="{ row }">
              <div class="priority">{{ row.priority || 0 }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="$t('extension.status')" width="180">
            <template #default="{ row }">
              <div class="status is-actived" v-if="row.status === 1">
                <el-icon><Check /></el-icon>
                <div class="name">{{ $t('route.route-status-deploy') }}</div>
              </div>
              <div class="status is-holded" v-if="row.status === 0">
                <el-icon><WarnTriangleFilled /></el-icon>
                <div class="name">{{ $t('route.route-status-undeploy') }}</div>
              </div>
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
              <el-popconfirm v-if="row.status === 0" :title="$t('route.tips.enable-route')" width="280"
                @confirm="handleStatusUpdate(row, 1)"
              >
                <template #reference>
                  <el-button link type="success" :icon="Check">
                    {{ $t('extension.enable') }}
                  </el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm v-if="row.status === 1" :title="$t('route.tips.disable-route')" width="280"
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
              <el-button link type="primary" :icon="Download"
                @click="handleExport(row)"
              >
                {{ $t('extension.export') }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <a-nodata v-if="dataList.length === 0"
        :loading="progression.batchLoading"
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
  Edit,
  Plus,
  Check,
  Delete,
  Search,
  Download,
  WarnTriangleFilled
} from '@element-plus/icons-vue'
import { useFileDownload } from '@/utils/dom'
import { useRequest } from '@/hooks/useRequest'
import { loadRouteTypeInfo } from '@/hooks/page/useRoute'
import { loadProviderIcon } from '@/config/data/model'
import { loadUpstreamTypeInfo } from '@/hooks/page/useUpstream'
import AMonaco from '@/components/common/monaco/platform.vue'

const dataList = ref<any[]>([])
const loadUpstreamList = ref<any[]>([])
const loadProviderList = ref<any[]>([])
const isMonacoView = ref(false)
const loadActivedJsonData = ref<string>('')

const { ioload, iopost, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const router = useRouter()
const handleLoad = async () => {
  progression.batchLoading = true
  const route = router.currentRoute.value
  try {
    // 1. 加载路由数据
    let params = {
      zone: route.params.id
    }
    let result = await ioload('zone', 'loadRouteList', params)
    dataList.value = result
    // 2. 加载上游列表，将上游数据合并到路由数据中
    params = {
      zone: route.params.id
    }
    result = await ioload('zone', 'loadUpstreamList', params)
    loadUpstreamList.value = result
    dataList.value.forEach((data: any) => {
      const matched = result.find((upstream: any) => upstream.id === data.upstream_id)
      if (matched) {
        data.upstream = matched
      }
    })
    // 3. 加载AI服务商列表，将数据合并到路由数据中
    loadProviderList.value = await ioload('provider', 'loadProviderList', null)
  } catch (error) {
    progression.success = false
  } finally {
    progression.batchLoading = false
  }
}
const handleProviderMatched = (id: string) => { 
  return loadProviderList.value.find((data: any) => data.id === id)
}
const handleView = (data: any) => {
  // 深度拷贝，避免修改原始数据
  const dataClone = JSON.parse(JSON.stringify(data))
  // 如果上游不是手动配置，则删除上游数据
  if (dataClone['upstream_id'] !== 'manual') {
    delete dataClone['upstream']
  }
  loadActivedJsonData.value = JSON.stringify(dataClone, null, 2)
  isMonacoView.value = true
}
const handleEdit = (data: any) => {
  const zone = router.currentRoute.value.params.id
  const id = data.id
  router.push({ path: `/zone/route/edit/${zone}/${id}` })
}
const handleAdd = () => {
  const zone = router.currentRoute.value.params.id
  router.push({ path: `/zone/route/add/${zone}` })
}
const handleDelete = (data: any) => {
  const params = {
    id: data.id,
    zone: router.currentRoute.value.params.id
  }
  iopost('zone', 'xhrRouteRemove', params).then(() => {
    handleLoad()
  })
}
const handleDataUpdate = (data: any) => {
  data.zone = router.currentRoute.value.params.id
  data.upstreamId = data.upstream_id
  iopost('zone', 'xhrRouteUpdate', data, {
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
  params.upstreamId = data.upstream_id
  iopost('zone', 'xhrRouteUpdate', params, {
    onMessage: true
  }).then(() => {
    handleLoad()
  })
}
const handleExport = (data: any) => {
  const dataClone = JSON.parse(JSON.stringify(data))
  if (dataClone['upstream_id'] !== 'manual') {
    delete dataClone['upstream']
  }
  const dataJson = JSON.stringify(dataClone, null, 2)
  const date = new Date()
  const formattedDate = date.toISOString().replace(/[-:T]/g, '').slice(0, 14)
  useFileDownload(dataJson, `Route_${data.id}_${formattedDate}.json`)
}
</script>
