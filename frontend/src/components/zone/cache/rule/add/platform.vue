<template>
  <div class="a-zone-cache-rule--add a-zone-common-form platform inline-flex-c-n-n">
    <div class="head inline-flex-r-c-n">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: `/zone/cache/${$route.params.id}` }">
          {{ $t('menu.cache') }}
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: `/zone/cache/rule/${$route.params.id}` }">
          {{ $t('submenu.cache.rule') }}
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
          <el-form ref="actionFormRef" label-position="top"
            :model="loadFormData.action"
          >
            <el-form-item prop="type">
              <el-radio-group v-model="loadFormData.action.type">
                <el-radio label="cache-force" size="large">
                  {{ $t('rule.action.type.cache-force') }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('rule.action.input-time-to-live')" prop="ttl">
              <el-input-number v-model="loadFormData.action.ttl" :min="1" />
            </el-form-item>
          </el-form>
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

const loadRouteList = ref<any[]>([])
const loadFormData = reactive<any>({
  name: '',
  routes: [],
  priority: 0,
  remark: '',
  action: {
    type: 'cache-force',
    ttl: 3600
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
  const validate = await ruleMvelRef.value?.handleFormValidate()
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
      priority: loadFormData.priority,
      rule: ruleMvelRef.value?.handleExpreassionRead(),
      action: loadFormData.action,
      remark: loadFormData.remark
    }
    iopost('zone', 'xhrCacheRuleAdd', params, {
      onMessage: true
    }).then(() => {
      const zone = router.currentRoute.value.params.id
      router.push({ path: `/zone/cache/rule/${zone}` })
    })
  })
}
const handleBack = () => {
  router.back()
}
</script>
