<template>
  <div class="a-authority-user platform inline-flex-c-n-n">
    <div class="head inline-flex-r-c-b">
      <div class="head-left"></div>
      <div class="head-right">
        <a-button type="primary" :icon="Plus" @click="isUserAddView = true">
          {{ $t('authority.add-user') }}
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
          <el-table-column property="name" :label="$t('authority.username')" width="auto" min-width="180"></el-table-column>
          <el-table-column property="acct" :label="$t('authority.account')" width="220"></el-table-column>
          <el-table-column property="date" :label="$t('authority.create-time')" width="220">
            <template #default="{ row }">
              {{ dateminuteftfn(row.date) }}
            </template>
          </el-table-column>
          <el-table-column fixed="right" :label="$t('extension.operation')" width="320">
            <template #default="{ row }">
              <el-button link type="primary" :icon="Edit" @click="handleEdit(row, false)">
                {{ $t('common.edit') }}
              </el-button>
              <el-button link type="success" :icon="Edit" @click="handleEdit(row, true)">
                {{ $t('authority.reset-password') }}
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
  <!-- 添加用户弹窗 -->
  <a-user-add :visible="isUserAddView"
    @close="isUserAddView = false"
    @change="handleLoad"
  />
  <!-- 编辑用户弹窗 -->
  <a-user-edit :visible="isUserEditView" :id="loadEditData.id?.toString()" :reset="isResetPasswordMode"
    @close="isUserEditView = false"
    @change="handleLoad"
  />
</template>

<script setup lang="ts">
import {
  Edit,
  Plus,
  Delete
} from '@element-plus/icons-vue'
import { useRequest } from '@/hooks/useRequest'
import { dateminuteftfn } from '@/hooks/useDataFormatter'
import AUserAdd from './add/platform.vue'
import AUserEdit from './edit/platform.vue'

const dataList = ref<any[]>([])
const isUserAddView = ref(false)
const isUserEditView = ref(false)
const loadEditData = ref<any>({})
const isResetPasswordMode = ref(false)

const { ioload, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const handleLoad = () => {
  ioload('database', 'loadUserList', null).then((result) => {
    dataList.value = result
  })
}
const handleEdit = (data: any, reset: boolean) => {
  loadEditData.value = data
  isResetPasswordMode.value = reset
  isUserEditView.value = true
}
const handleDelete = (data: any) => {
  const params = {
    id: data.id.toString()
  }
  ioload('database', 'xhrUserRemove', params).then(() => {
    handleLoad()
  })
}
</script>
