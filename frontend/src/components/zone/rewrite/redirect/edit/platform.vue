<template>
  <div class="a-zone-rewrite-redirect--edit a-zone-common-form platform inline-flex-c-n-n">
    <div class="head inline-flex-r-c-n">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: `/zone/rewrite/${$route.params.id}` }">
          {{ $t('menu.rewrite') }}
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: `/zone/rewrite/redirect/${$route.params.id}` }">
          {{ $t('submenu.rewrite.redirect') }}
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          {{ $t('protection.rule.add-rule') }}
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
          <el-form-item :label="$t('extension.remark')" prop="number">
            <el-input type="textarea" v-model="loadFormData.number" :rows="2"></el-input>
          </el-form-item>
        </div>
        <div class="card">
          <div class="title">
            <el-icon><Tickets /></el-icon>
            <div class="name">{{ $t('protection.rule.rule-if') }}</div>
          </div>
          <el-radio-group v-model="loadFormData.mode">
            <el-radio label="pattern" size="large">
              {{ $t('redirect.mode-pattern') }}
            </el-radio>
            <el-radio label="mvel" size="large">
              {{ $t('redirect.mode-mvel') }}
            </el-radio>
          </el-radio-group>
          <el-form-item :label="$t('redirect.request-url')" prop="url" v-show="loadFormData.mode === 'pattern'">
            <el-input type="input" v-model="loadFormData.url"></el-input>
          </el-form-item>
          <a-rule-mvel ref="ruleMvelRef" v-show="loadFormData.mode === 'mvel'" />
        </div>
        <div class="card">
          <div class="title">
            <el-icon><Promotion /></el-icon>
            <div class="name">{{ $t('protection.rule.rule-then') }}</div>
          </div>
          <a-redirect-mvel v-show="loadFormData.mode === 'mvel'" ref="redirectMvelRef" />
          <a-redirect-pattern v-show="loadFormData.mode === 'pattern'" ref="redirectPatternRef" />
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
  Document,
  Promotion
} from '@element-plus/icons-vue'
import type { FormRules } from 'element-plus'
import { useRequest } from '@/hooks/useRequest'
import ARuleMvel from '@/components/common/rule/mvel.vue'
import ARedirectMvel from '@/components/zone/rewrite/redirect/action/mvel.vue'
import ARedirectPattern from '@/components/zone/rewrite/redirect/action/pattern.vue'

const loadRouteList = ref<any[]>([])
const loadFormData = reactive<any>({
  name: '',
  routes: [],
  url: '',
  mode: 'pattern',
  remark: '',
  action: []
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
const redirectMvelRef = ref<InstanceType<typeof ARedirectMvel>>()
const redirectPatternRef = ref<InstanceType<typeof ARedirectPattern>>()

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
  result = await ioload('zone', 'loadRewriteRedirectRuleInfo', params)
  Object.keys(result).forEach((key) => {
    loadFormData[key] = result[key]
  })
  ruleMvelRef.value?.handleExpressionLoad(loadFormData.rule)
  if (loadFormData.mode === 'pattern') {
    redirectPatternRef.value?.handleValueLoad(loadFormData.action)
  } else {
    redirectMvelRef.value?.handleValueLoad(loadFormData.action)
  }
}

const handleSubmit = async (deploy: boolean) => {
  formRef.value.validate(async (valid: boolean) => {
    if (!valid) {
      return false
    }
    const mode = loadFormData.mode
    if (mode === 'mvel') {
      let validate = await ruleMvelRef.value?.handleFormValidate()
      if (!validate) {
        return false
      }
      validate = await redirectMvelRef.value?.handleFormValidate()
      if (!validate) {
        return false
      }
    } else {
      const validate = await redirectPatternRef.value?.handleFormValidate()
      if (!validate) {
        return false
      }
    }
    const route = router.currentRoute.value
    const params: any = {
      zone: router.currentRoute.value.params.id,
      id: route.params.rid,
      name: loadFormData.name,
      status: deploy ? 1 : 0,
      routes: loadFormData.routes,
      mode: mode,
      remark: loadFormData.remark
    }
    if (mode === 'mvel') {
      params.rule = ruleMvelRef.value?.handleExpreassionRead()
      params.action = redirectMvelRef.value?.handleDataRead()
    } else {
      params.rule = loadFormData.url
      params.action = redirectPatternRef.value?.handleDataRead()
    }
    iopost('zone', 'xhrRewriteRedirectUpdate', params, {
      onMessage: true
    }).then(() => {
      const zone = route.params.id
      router.push({ path: `/zone/rewrite/redirect/${zone}` })
    })
  })
}
const handleBack = () => {
  router.back()
}
</script>
