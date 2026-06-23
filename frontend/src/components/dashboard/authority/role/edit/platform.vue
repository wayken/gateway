<template>
  <el-dialog draggable width="580px" append-to-body class="a-authority-role--edit platform"
    :title="$t('common.edit')"
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

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  id: {
    type: String,
    default: ''
  }
})

const i18n = useI18n()
const formRef = ref()
const nameInputRef = ref()
const loadFormData = reactive<any>({
  name: '',
  remark: '',
  isAdmin: false,
  permissions: []
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

const { ioload, iopost, progression } = useRequest()

const handleLoad = () => {
  const params = {
    id: props.id
  }
  ioload('database', 'loadRoleInfo', params).then((result) => {
    const permissionList: string[] = []
    Object.keys(result).forEach((key) => {
      // 如果key为id，需要转换成字符串
      if (key === 'id') {
        loadFormData[key] = result[key].toString()
        return
      }
      if (key === 'permissions') {
        permissionList.push(...result[key])
        const keys: string[] = []
        handleLoadRoleTreeKeys(permissions, permissionList, keys)
        keys.forEach((key) => {
          nextTick(() => {
            menuRef.value?.setChecked(key, true, false)
          })
        })
      } else if (key === 'is_admin') {
        loadFormData.isAdmin = result[key] === 1
      }
      loadFormData[key] = result[key]
    })
  })
}
const handleLoadRoleTreeKeys = (permissions: any[], dataList: string[], keys: string[]) => {
  // permissions数据结构是树形结构，包含多级children节点
  // 其中存在permission，需要和遍历并和permissionList匹配获取permissions里面的name，并添加到数组中，需要递归处理
  permissions.forEach((data) => {
    if (data.permission && dataList.includes(data.permission)) {
      keys.push(data.name)
    }
    if (data.children && data.children.length > 0) {
      handleLoadRoleTreeKeys(data.children, dataList, keys)
    }
  })
}
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
      id: props.id,
      name: loadFormData.name.trim(),
      remark: loadFormData.password
    }
    if (loadFormData.isAdmin) {
      params.admin = loadFormData.isAdmin
    } else {
      params.admin = false
      params.permissions = permissions
    }
    iopost('database', 'xhrRoleUpdate', params, {
      onMessage: true
    }).then((result) => {
      params.id = result.id
      handleEmit('change', params)
      handleClose()
    })
  })
}

watch(() => props.visible, (value) => {
  if (value) {
    handleLoad()
  }
})
</script>
