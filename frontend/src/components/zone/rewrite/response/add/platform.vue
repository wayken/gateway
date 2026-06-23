<template>
  <div class="a-zone-rewrite-response--add a-zone-common-form platform inline-flex-c-n-n">
    <div class="head inline-flex-r-c-n">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: `/zone/rewrite/${$route.params.id}` }">
          {{ $t('menu.rewrite') }}
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: `/zone/rewrite/response/${$route.params.id}` }">
          {{ $t('submenu.rewrite.response') }}
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
          <a-rule-mvel ref="ruleMvelRef"/>
        </div>
        <div class="card">
          <div class="title">
            <el-icon><Promotion /></el-icon>
            <div class="name">{{ $t('protection.rule.rule-then') }}</div>
          </div>
          <a-rewrite-action ref="rewriteActionRef"></a-rewrite-action>
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
import ARewriteAction from '@/components/zone/rewrite/action/index.vue'

const loadRouteList = ref<any[]>([])
const loadFormData = reactive<any>({
  name: '',
  routes: [],
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
const rewriteActionRef = ref<InstanceType<typeof ARewriteAction>>()

onMounted(() => {
  nameRef.value.focus()
  handleLoad()
})

const router = useRouter()
const { ioload, iopost, progression } = useRequest()
const handleLoad = () => {
  const params = {
    zone: router.currentRoute.value.params.id
  }
  ioload('zone', 'loadRouteList', params).then((result) => {
    loadRouteList.value = result
  })
}

const handleSubmit = async (deploy: boolean) => {
  let validate = await ruleMvelRef.value?.handleFormValidate()
  if (!validate) {
    return
  }
  validate = await rewriteActionRef.value?.handleFormValidate()
  if (!validate) {
    return
  }
  formRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false
    }
    const params: any = {
      zone: router.currentRoute.value.params.id,
      name: loadFormData.name,
      status: deploy ? 1 : 0,
      routes: loadFormData.routes,
      rule: ruleMvelRef.value?.handleExpreassionRead(),
      action: rewriteActionRef.value?.handleDataListRead(),
      remark: loadFormData.remark
    }
    iopost('zone', 'xhrRewriteResponseAdd', params, {
      onMessage: true
    }).then(() => {
      const zone = router.currentRoute.value.params.id
      router.push({ path: `/zone/rewrite/response/${zone}` })
    })
  })
}
const handleBack = () => {
  router.back()
}
</script>
