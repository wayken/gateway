<template>
  <div class="a-zone-protection-rule--edit a-zone-common-form platform inline-flex-c-n-n">
    <div class="head inline-flex-r-c-n">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: `/zone/protection/${$route.params.id}` }">
          {{ $t('menu.protection') }}
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: `/zone/protection/rule/${$route.params.id}` }">
          {{ $t('submenu.protection.rule') }}
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          {{ $t('protection.rule.edit-rule') }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="content">
      <el-form ref="formRef" label-position="right" label-width="120px"
        :model="loadFormData"
        :rules="loadFormRules"
      >
        <div class="card">
          <div class="title">
            <el-icon><Setting /></el-icon>
            <div class="name">{{ $t('protection.rule.basic-config') }}</div>
          </div>
          <el-form-item :label="$t('protection.rule.rule-name')" prop="name">
            <el-input ref="nameRef" v-model="loadFormData.name"></el-input>
          </el-form-item>
          <el-form-item :label="$t('route.matched-route')" prop="routes" v-loading="progression.loading">
            <el-select v-model="loadFormData.routes" multiple collapse-tags :teleported="false" collapse-tags-tooltip :max-collapse-tags="10"
              :placeholder="$t('route.select-matched-route-placeholder')"
            >
              <el-option v-for="data in loadRouteList" :key="data.value"
                :label="data.name"
                :value="data.id"
              >
                <div class="route inline-flex-r-c-n">
                  <a-svg-icon icon-class="route" />
                  <div class="name">{{ data.name || '-' }}</div>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('extension.priority')" prop="priority">
            <template #label>
              <div class="label inline-flex-r-c-n">
                {{ $t('extension.priority') }}
                <el-tooltip placement="top" effect="dark" :content="$t('protection.rule.tips.priority')">
                  <el-icon><Warning /></el-icon>
                </el-tooltip>
              </div>
            </template>
            <el-input-number v-model="loadFormData.priority" />
          </el-form-item>
          <el-form-item :label="$t('extension.remark')" prop="remark">
            <el-input type="textarea" v-model="loadFormData.remark" :rows="2"></el-input>
          </el-form-item>
        </div>
        <div class="card">
          <div class="title">
            <el-icon><Tickets /></el-icon>
            <div class="name">{{ $t('protection.rule.rule-if') }}</div>
          </div>
          <a-rule-mvel ref="ruleMvelRef"/>
        </div>
        <div class="card">
          <div class="title">
            <el-icon><Promotion /></el-icon>
            <div class="name">{{ $t('protection.rule.rule-then') }}</div>
          </div>
          <a-rule-action ref="ruleActionRef" />
        </div>
      </el-form>
    </div>
    <div class="footer inline-flex-r-c-e">
      <el-button type="primary" :icon="Document" :loading="progression.loading"
        @click="handleSubmit(false)"
      >
        {{ $t('common.save') }}
      </el-button>
      <el-button type="success" :icon="Promotion" :loading="progression.loading"
        @click="handleSubmit(true)"
      >
        {{ $t('extension.save-and-deploy') }}
      </el-button>
      <el-button @click="handleBack">
        {{ $t('common.cancel') }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Tickets,
  Setting,
  Warning,
  Document,
  Promotion
} from '@element-plus/icons-vue'
import type { FormRules } from 'element-plus'
import { useRequest } from '@/hooks/useRequest'
import ARuleMvel from '@/components/common/rule/mvel.vue'
import ARuleAction from '@/components/common/rule/action.vue'

const loadRouteList = ref<any[]>([])
const loadFormData = reactive<any>({
  name: '',
  routes: [],
  priority: 0,
  remark: '',
  action: {
  }
})
const i18n = useI18n()
const loadFormRules = reactive<FormRules<any>>({
  name: [
    { trigger: 'change', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('protection.rule.rule-name') }
  ]
})
const nameRef = ref()
const formRef = ref()
const ruleMvelRef = ref<InstanceType<typeof ARuleMvel>>()
const ruleActionRef = ref<InstanceType<typeof ARuleAction>>()

onMounted(() => {
  nameRef.value.focus()
  handleLoad()
})

const router = useRouter()
const { ioload, iopost, progression } = useRequest()
const handleLoad = async () => {
  const route = router.currentRoute.value
  // 1. 加载路由列表
  let params: any = {
    zone: route.params.id
  }
  let result = await ioload('zone', 'loadRouteList', params)
  loadRouteList.value = result
  // 2. 加载规则数据
  params = {
    zone: route.params.id,
    id: route.params.rid
  }
  result = await ioload('zone', 'loadWafRuleInfo', params)
  Object.keys(result).forEach((key) => {
    loadFormData[key] = result[key]
  })
  ruleMvelRef.value?.handleExpressionLoad(loadFormData.rule)
  ruleActionRef.value?.handleActionLoad(loadFormData.action)
}

const handleSubmit = async (deploy: boolean) => {
  let validate = await ruleMvelRef.value?.handleFormValidate()
  if (!validate) {
    return
  }
  validate = await ruleActionRef.value?.handleFormValidate()
  if (!validate) {
    return
  }
  formRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false
    }
    const route = router.currentRoute.value
    const params: any = {
      id: route.params.rid,
      zone: route.params.id,
      name: loadFormData.name,
      status: deploy ? 1 : 0,
      routes: loadFormData.routes,
      priority: loadFormData.priority,
      rule: ruleMvelRef.value?.handleExpreassionRead(),
      action: ruleActionRef.value?.handleActionRead(),
      remark: loadFormData.remark
    }
    iopost('zone', 'xhrWafRuleUpdate', params, {
      onMessage: true
    }).then(() => {
      const zone = route.params.id
      router.push({ path: `/zone/protection/rule/${zone}` })
    })
  })
}
const handleBack = () => {
  router.back()
}
</script>
