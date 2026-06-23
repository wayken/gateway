<template>
  <div class="a-setting-auths platform inline-flex-c-n-n">
    <div class="head inline-flex-r-c-b">
      <div class="head-left"></div>
      <div class="head-right">
        <a-button type="primary" :icon="Plus" @click="isAuthAddView = true">
          {{ $t('setting.add-auth') }}
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
          <el-table-column prop="type" :label="$t('setting.auth-type')" width="220">
            <template #default="{ row }">
              <div class="type inline-flex-r-c-n"
                :style="{
                  backgroundColor: loadAuthTypeInfo(row.type)?.color
                }"
              >
                <el-icon>
                  <component :is="loadAuthTypeInfo(row.type)?.icon"></component>
                </el-icon>
                <div class="name" v-if="row.type">
                  {{ $t('setting.auth-type-' + row.type) }}
                </div>
              </div>
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
  <!-- 添加Auth组弹窗 -->
  <a-auth-add :visible="isAuthAddView"
    @close="isAuthAddView = false"
    @change="handleLoad"
  />
  <!-- 编辑Auth组弹窗 -->
  <a-auth-edit :visible="isAuthEditView" :id="loadEditData.id?.toString()"
    @close="isAuthEditView = false"
    @change="handleLoad"
  />
</template>

<script setup lang="ts">
import {
  Plus,
  Delete
} from '@element-plus/icons-vue'
import { useRequest } from '@/hooks/useRequest'
import { loadAuthTypeInfo } from '@/hooks/page/useAuths'
import AAuthAdd from './add/platform.vue'
import AAuthEdit from './edit/platform.vue'

const dataList = ref<any[]>([])
const isAuthAddView = ref(false)
const isAuthEditView = ref(false)
const loadEditData = ref<any>({})

const { ioload, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const handleLoad = () => {
  ioload('auth', 'loadAuthList', null).then((result) => {
    dataList.value = result
  })
}
const handleEdit = (data: any) => {
  loadEditData.value = data
  isAuthEditView.value = true
}
const handleDelete = (data: any) => {
  const params = {
    id: data.id.toString()
  }
  ioload('auth', 'xhrAuthRemove', params).then(() => {
    handleLoad()
  })
}
</script>
