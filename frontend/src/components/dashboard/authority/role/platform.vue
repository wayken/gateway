<template>
  <div class="a-authority-role platform inline-flex-c-n-n">
    <div class="head inline-flex-r-c-b">
      <div class="head-left"></div>
      <div class="head-right">
        <a-button type="primary" :icon="Plus" @click="isRoleAddView = true">
          {{ $t('authority.add-role') }}
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
          <el-table-column prop="name" :label="$t('authority.rolename')" width="auto" min-width="320">
            <template #default="{ row }">
              <span class="name">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="permission" :label="$t('authority.role-permission')" width="380">
            <template #default="{ row }">
              <div class="permission"
                :class="{
                  'is-admin': row.is_admin
                }"
              >
                <el-icon><UserFilled /></el-icon>
                {{ row.is_admin ? $t('authority.is-admin') : $t('authority.is-user') }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="permission" :label="$t('authority.create-time')" width="380">
            <template #default="{ row }">
              {{ datetimeftfn(row.date) }}
            </template>
          </el-table-column>
          <el-table-column fixed="right" :label="$t('extension.operation')" width="320">
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
  <!-- 添加角色弹窗 -->
  <a-role-add :visible="isRoleAddView"
    @close="isRoleAddView = false"
    @change="handleLoad"
  />
  <!-- 编辑角色弹窗 -->
  <a-role-edit :visible="isRoleEditView" :id="loadEditData.id?.toString()"
    @close="isRoleEditView = false"
    @change="handleLoad"
  />
</template>

<script setup lang="ts">
import {
  Plus,
  Edit,
  Delete,
  UserFilled
} from '@element-plus/icons-vue'
import { useRequest } from '@/hooks/useRequest'
import { datetimeftfn } from '@/hooks/useDataFormatter'
import ARoleAdd from './add/platform.vue'
import ARoleEdit from './edit/platform.vue'

const dataList = ref<any[]>([])
const isRoleAddView = ref(false)
const isRoleEditView = ref(false)
const loadEditData = ref<any>({})

const { ioload, progression } = useRequest()
onMounted(() => {
  handleLoad()
})

const handleLoad = () => {
  ioload('database', 'loadRoleList', null).then((result) => {
    dataList.value = result
  })
}
const handleEdit = (data: any) => {
  loadEditData.value = data
  isRoleEditView.value = true
}
const handleDelete = (data: any) => {
  const params = {
    id: data.id.toString()
  }
  ioload('database', 'xhrRoleRemove', params).then(() => {
    handleLoad()
  })
}
</script>
