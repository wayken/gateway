<template>
  <el-dialog draggable width="580px" append-to-body class="a-authority-role--add platform"
    :title="$t('authority.add-role')"
    :model-value="visible"
    @opened="handleOpen"
    @close="handleClose"
  >
    <el-form ref="formRef" label-position="right" label-width="88px"
      :model="loadFormData"
      :rules="loadFormRules"
      @submit.prevent="handleSubmit"
    >
      <el-form-item :label="$t('authority.rolename')" prop="name">
        <el-input :prefix-icon="Avatar" ref="nameInputRef" v-model="loadFormData.name" />
      </el-form-item>
      <el-form-item :label="$t('extension.remark')" prop="remark">
        <el-input type="textarea" :prefix-icon="UserFilled" v-model="loadFormData.remark" />
      </el-form-item>
      <el-form-item :label="$t('authority.super-admin')" prop="remark">
        <el-switch v-model="loadFormData.isAdmin" />
      </el-form-item>
      <el-form-item v-if="!loadFormData.isAdmin" :label="$t('authority.menu-permission')" prop="permission">
        <el-checkbox v-model="isMenuExpand"
          @change="handlePermissionTreeExpand"
        >
          {{ $t('authority.open-collapse') }}
        </el-checkbox>
        <el-checkbox v-model="isMenuNodeAll"
          @change="handlePermissionTreeNodeAll"
        >
          {{ $t('authority.menu-node-all') }}
        </el-checkbox>
        <el-checkbox v-model="isMenuCheckStrictly">
          {{ $t('authority.menu-check-strictly') }}
        </el-checkbox>
        <el-tree ref="menuRef" class="permissions" show-checkbox node-key="name"
          :data="permissions"
          :check-strictly="!isMenuCheckStrictly"
        >
          <template #default="{ data }">
            <div class="node">
              <span>{{  $t('permission.' + data.name) }}</span>
            </div>
          </template>
        </el-tree>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="footer">
        <el-button @click="handleClose">
          {{ $t('common.cancel') }}
        </el-button>
        <el-button type="primary" :loading="progression.sending"
          @click="handleSubmit"
        >
          {{ $t('common.confirm') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import {
  Avatar,
  UserFilled
} from '@element-plus/icons-vue'
import type { ElTree, FormRules } from 'element-plus'
import { useRequest } from '@/hooks/useRequest'
import permissions from '@/config/permissions.json'

defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const i18n = useI18n()
const formRef = ref()
const nameInputRef = ref()
const loadFormData = reactive<any>({
  name: '',
  remark: '',
  isAdmin: false,
  permission: []
})
const loadFormRules = reactive<FormRules<any>>({
  name: [
    { trigger: 'blur', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('authority.rolename') }
  ]
})
const isMenuExpand = ref(false)
const isMenuNodeAll = ref(false)
const isMenuCheckStrictly = ref(true)
const menuRef = ref<InstanceType<typeof ElTree>>()

const { iopost, progression } = useRequest()

const handleEmit = defineEmits(['change', 'close'])
const handleOpen = () => {
  nameInputRef.value.focus()
}
const handleClose = () => {
  formRef.value.resetFields()
  menuRef.value?.setCheckedKeys([])
  handlePermissionTreeExpand(false)
  handleEmit('close')
}
// 树权限（展开/折叠）
function handlePermissionTreeExpand(value: boolean) {
  const dataList = permissions
  const instance = menuRef.value as InstanceType<typeof ElTree>
  if (instance) {
    for (let i = 0, length = dataList.length; i < length; i++) {
      instance.store.nodesMap[dataList[i].name].expanded = value
    }
  }
}
// 树权限（全选/全不选）
function handlePermissionTreeNodeAll(value: boolean) {
  const dataList = permissions as any[]
  const instance = menuRef.value as InstanceType<typeof ElTree>
  instance.setCheckedNodes(value ? dataList : [])
}
// 提交表单
const handleSubmit = () => {
  formRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false
    }
    // 目前被选中的菜单节点
    let nodes = menuRef.value?.getCheckedNodes() as any[]
    const permissions: string[] = []
    if (nodes) {
      nodes.forEach((node: any) => {
        if (node.permission) {
          permissions.push(node.permission)
        }
      })
    }
    const params: any = {
      name: loadFormData.name.trim(),
      remark: loadFormData.password
    }
    if (loadFormData.isAdmin) {
      params.admin = loadFormData.isAdmin
    } else {
      params.admin = false
      params.permissions = permissions
    }
    iopost('database', 'xhrRoleAdd', params, {
      onMessage: true
    }).then((result) => {
      params.id = result.id
      handleEmit('change', params)
      handleClose()
    })
  })
}
</script>
