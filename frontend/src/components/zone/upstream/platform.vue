<template>
  <div class="a-upstream platform inline-flex-c-n-n">
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
          {{ $t('upstream.add-upstream') }}
        </a-button>
      </div>
    </div>
    <div class="content inline-flex-c-n-n">
      <div class="table">
        <el-table :data="dataList">
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="infomation">
                <div class="field inline-flex-r-c-n">
                  <div class="label">{{ $t('upstream.upstream-algorithm') }}：</div>
                  <div class="value">{{ row.algorithm ? $t('upstream.algorithm.' + row.algorithm) : '-' }}</div>
                </div>
                <template v-if="row.type === 'echo'">
                  <div class="field inline-flex-r-c-n">
                    <div class="label">{{ $t('upstream.echo-content-type') }}：</div>
                    <div class="value">
                      {{ row.contentType || '-' }}
                    </div>
                  </div>
                  <div class="field inline-flex-c-n-n">
                    <div class="label">{{ $t('upstream.echo-content') }}：</div>
                    <div class="value echo">
                      {{ row.content || '-' }}
                    </div>
                  </div>
                </template>
                <template v-else-if="row.type === 'index'">
                  <div class="field inline-flex-r-c-n">
                    <div class="label">{{ $t('upstream.index-path') }}：</div>
                    <div class="value">
                      {{ row.static.path || '-' }}
                    </div>
                  </div>
                  <div class="field inline-flex-r-c-n">
                    <div class="label">{{ $t('upstream.default-index-page') }}：</div>
                    <div class="value">
                      {{ row.static.index || '-' }}
                    </div>
                  </div>
                  <div class="field inline-flex-r-c-n">
                    <div class="label">{{ $t('upstream.try-file-uri') }}：</div>
                    <div class="value">
                      {{ row.static.fallback || '-' }}
                    </div>
                  </div>
                </template>
                <template v-else-if="row.type === 'node'">
                  <div class="field inline-flex-r-c-n">
                    <div class="label">{{ $t('upstream.node') }}：</div>
                    <div class="value inline-flex-r-c-n node">
                      <div v-for="(data, index) in row.nodes" :key="index">
                        {{ data.schema + '://' + data.host + ':' + data.port }}
                      </div>
                    </div>
                  </div>
                </template>
                <template v-else-if="row.type === 'service'">
                  <div class="field inline-flex-r-c-n">
                    <div class="label">{{ $t('upstream.service.service-type') }}：</div>
                    <div class="value inline-flex-r-c-n">
                      {{ row.service.type || '-' }}
                    </div>
                  </div>
                  <div class="field inline-flex-r-c-n">
                    <div class="label">{{ $t('upstream.service.service-name') }}：</div>
                    <div class="value inline-flex-r-c-n">
                      {{ row.service.name || '-' }}
                    </div>
                  </div>
                  <div class="field inline-flex-r-c-n">
                    <div class="label">{{ $t('upstream.service.service-path') }}：</div>
                    <div class="value inline-flex-r-c-n">
                      {{ row.service.path || '-' }}
                    </div>
                  </div>
                  <div class="field inline-flex-r-c-n">
                    <div class="label">{{ $t('upstream.service.service-server') }}：</div>
                    <div class="value inline-flex-r-c-n">
                      {{ row.service.server || '-' }}
                    </div>
                  </div>
                </template>
                <template v-if="row.type === 'ai'">
                  <div class="field inline-flex-r-c-n">
                    <div class="label">{{ $t('upstream.ai.provider') }}：</div>
                    <div class="value inline-flex-r-c-n provider">
                      <div class="image">
                        <img :src="loadProviderIcon(handleProviderMatched(row.ai.provider).name)" />
                      </div>
                      <div class="name">{{ $t('provider.' + handleProviderMatched(row.ai.provider).name) }}</div>
                    </div>
                  </div>
                  <div class="field inline-flex-r-c-n">
                    <div class="label">{{ $t('upstream.ai.model') }}：</div>
                    <div class="value inline-flex-r-c-n">
                      {{ row?.ai.parameters.model }}
                    </div>
                  </div>
                </template>
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
          <el-table-column prop="name" :label="$t('upstream.upstream-name')" width="auto" min-width="180">
            <template #default="{ row }">
              <div class="upstream inline-flex-r-c-n" @click="handleView(row)">
                <a-svg-icon icon-class="palette" />
                <div class="name">{{ row.name || '-' }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="type" :label="$t('upstream.upstream-type')" width="220">
            <template #default="{ row }">
              <div class="type inline-flex-r-c-n"
                :style="{
                  backgroundColor: loadUpstreamTypeInfo(row.type)?.color
                }"
              >
                <el-icon>
                  <component :is="loadUpstreamTypeInfo(row.type)?.icon"></component>
                </el-icon>
                <div class="name" v-if="row.type">
                  {{ $t('upstream.upstream-type-' + row.type) }}
                </div>
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
              <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">
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
    @change="handleUpdate"
  />
</template>

<script setup lang="ts">
import {
  Plus,
  Edit,
  Delete,
  Search,
  Download
} from '@element-plus/icons-vue'
import { useFileDownload } from '@/utils/dom'
import { useRequest } from '@/hooks/useRequest'
import { loadProviderIcon } from '@/config/data/model'
import { loadUpstreamTypeInfo } from '@/hooks/page/useUpstream'
import AMonaco from '@/components/common/monaco/platform.vue'

const dataList = ref<any[]>([])
const isMonacoView = ref(false)
const loadActivedJsonData = ref<string>('')
const loadProviderList = ref<any[]>([])

const { ioload, iopost, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const router = useRouter()
const handleLoad = async () => {
  const params = {
    zone: router.currentRoute.value.params.id
  }
  progression.batchLoading = true
  try {
    // 1. 加载上游列表
    dataList.value = await ioload('zone', 'loadUpstreamList', params)
    // 2. 加载AI服务商列表
    loadProviderList.value = await ioload('provider', 'loadProviderList', null)
  } catch (error) {
    progression.success = false
  } finally {
    progression.batchLoading = false
  }
}
const handleView = (data: any) => {
  // 深度拷贝，避免修改原始数据
  const dataClone = JSON.parse(JSON.stringify(data))
  loadActivedJsonData.value = JSON.stringify(dataClone, null, 2)
  isMonacoView.value = true
}
const handleAdd = () => {
  const zone = router.currentRoute.value.params.id
  router.push({ path: `/zone/upstream/add/${zone}` })
}
const handleEdit = (data: any) => {
  const zone = router.currentRoute.value.params.id
  const id = data.id
  router.push({ path: `/zone/upstream/edit/${zone}/${id}` })
}
const handleDelete = (data: any) => {
  const params = {
    id: data.id,
    zone: router.currentRoute.value.params.id
  }
  iopost('zone', 'xhrUpstreamRemove', params).then(() => {
    handleLoad()
  })
}
const handleUpdate = (data: any) => {
  data.zone = router.currentRoute.value.params.id
  data.parameters = {}
  if (data.type === 'echo') {
    data.parameters.content = data.content
  } else if (data.type === 'index') {
    data.parameters.path = data.path
  } else if (data.type === 'node') {
    data.parameters.nodes = data.nodes
  }
  iopost('zone', 'xhrUpstreamUpdate', data, {
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
  useFileDownload(dataJson, `Upstream_${data.id}_${formattedDate}.json`)
}
const handleProviderMatched = (id: string) => { 
  return loadProviderList.value.find((data: any) => data.id === id)
}
</script>
