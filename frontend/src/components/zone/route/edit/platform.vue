<template>
  <div class="a-route--edit a-zone-common-form platform inline-flex-c-n-n">
    <div class="head inline-flex-r-c-n">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: `/zone/route/${$route.params.id}` }">
          {{ $t('menu.route') }}
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          {{ $t('route.edit-route') }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="content" v-loading="progression.batchLoading">
      <el-form ref="formRef" label-position="right" label-width="120px"
        :model="loadFormData"
        :rules="loadFormRules"
      >
        <!-- 基础配置 -->
        <div class="card">
          <div class="title">
            <el-icon><Setting /></el-icon>
            <div class="name">{{ $t('upstream.basic-config') }}</div>
          </div>
          <el-form-item :label="$t('route.route-type')" prop="type">
            <el-select v-model="loadFormData.type" :teleported="false">
              <el-option v-for="data in routeTypeList" :key="data.value"
                :label="$t('route.route-type-' + data.name)"
                :value="data.value"
              >
                <div class="type inline-flex-r-c-n">
                  <el-icon><component :is="data.icon"></component></el-icon>
                  <div>{{ $t('route.route-type-' + data.name) }}</div>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('extension.name')" prop="name">
            <el-input ref="nameRef" v-model="loadFormData.name"></el-input>
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
          <el-form-item :label="$t('setting.auth')" prop="authId">
            <el-select v-model="loadFormData.authId" v-loading="progression.loading">
              <el-option v-for="(data, key) in loadAuthList" :key="key" :value="data.id" :label="data.name">
                <span>{{ data.name }}</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('extension.remark')" prop="remark">
            <el-input type="textarea" v-model="loadFormData.remark" :rows="2"></el-input>
          </el-form-item>
        </div>
        <!-- 上游配置 -->
        <div class="card">
          <div class="title">
            <el-icon><Promotion /></el-icon>
            <div class="name">{{ $t('route.upstream-config') }}</div>
          </div>
          <el-form-item :label="$t('route.select-upstream')" prop="upstreamId">
            <el-select v-model="loadFormData.upstreamId" v-loading="progression.loading"
              @change="handleUpstreamChange"
            >
              <el-option v-for="(data, key) in loadUpstreamList" :key="key" :value="data.id"
                :label="data.name"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('upstream.upstream-algorithm')" prop="upstream.algorithm"
            :rules="{
              required: true,
              trigger: 'change'
            }"
          >
            <el-select v-model="loadFormData.upstream.algorithm" :disabled="!isUpstreamManual">
              <el-option v-for="(data, key) in upstreamAlgorithmList" :key="key" :value="data.value"
                :label="$t('upstream.algorithm.' + data.name)"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('upstream.upstream-type')" prop="upstream.type"
            :rules="{
              required: true,
              trigger: 'change'
            }"
          >
            <el-select v-model="loadFormData.upstream.type" :disabled="!isUpstreamManual">
              <el-option v-for="(data, key) in upstreamTypeList" :key="key" :value="data.value"
                :label="$t('upstream.type.' + data.name)"
              />
            </el-select>
          </el-form-item>
          <template v-if="loadFormData.upstream.type === 'echo'">
            <el-form-item :label="$t('upstream.echo-response')"
              :rules="{
                required: true,
                trigger: 'change'
              }"
            >
              <el-row :gutter="20" style="width: 100%;">
                <el-col :span="18">
                  <el-select v-model="loadFormData.upstream.contentType" :disabled="!isUpstreamManual"
                    :placeholder="$t('upstream.echo-content-type')"
                  >
                    <el-option v-for="(data, key) in upstreamContentTypeList" :key="key" :value="data.value"
                      :label="$t('upstream.response.' + data.name)"
                    />
                  </el-select>
                </el-col>
                <el-col :span="6">
                  <el-input-number v-model="loadFormData.upstream.httpStatus" :min="0" controls-position="right"
                    :disabled="!isUpstreamManual"
                    :placeholder="$t('upstream.echo-status')"
                  />
                </el-col>
              </el-row>
            </el-form-item>
            <el-form-item :label="$t('upstream.echo-content')" :prop="'upstream.content'"
              :rules="{
                required: true,
                trigger: 'change',
                message: $t('extension.please-input-placeholder') + $t('upstream.echo-content')
              }"
            >
              <el-input type="textarea" v-model="loadFormData.upstream.content" :rows="2"
                :disabled="!isUpstreamManual"
              ></el-input>
            </el-form-item>
          </template>
          <!-- 节点转发 -->
          <template v-if="loadFormData.upstream.type === 'node'">
            <el-form-item v-for="(data, index) in loadFormData.upstream.nodes" :key="index" class="node"
              :label="$t('upstream.node')"
              :disabled="!isUpstreamManual"
              :prop="'upstream.nodes.' + index"
              :rules="{
                required: true,
                trigger: 'change',
                validator: handleUpstreamNodeValidator
              }"
            >
              <el-row :gutter="20" style="width: 100%;" class="node">
                <el-col :span="10">
                  <el-input v-model="data.host" :placeholder="$t('upstream.host')" :disabled="!isUpstreamManual"></el-input>
                </el-col>
                <el-col :span="5">
                  <el-input v-model="data.port" :placeholder="$t('upstream.port')" :disabled="!isUpstreamManual"></el-input>
                </el-col>
                <el-col :span="4">
                  <el-input v-model="data.weight" :placeholder="$t('upstream.weight')" :disabled="!isUpstreamManual"></el-input>
                </el-col>
                <el-col :span="4">
                  <el-select v-model="data.schema" :disabled="!isUpstreamManual">
                    <el-option v-for="data in upstreamSchemaList" :key="data.value"
                      :label="data.name"
                      :value="data.value"
                    />
                  </el-select>
                </el-col>
                <el-col :span="1">
                  <el-icon v-if="loadFormData.upstream.nodes.length > 1 && isUpstreamManual" class="icon"
                    @click="handleUpstreamNodeRemove(index)"
                  >
                    <Delete />
                  </el-icon>
                </el-col>
              </el-row>
            </el-form-item>
            <el-form-item v-if="isUpstreamManual">
              <a-button :icon="Plus" style="width: 100%;" @click="handleUpstreamNodeAdd">
                {{ $t('upstream.add-node') }}
              </a-button>
            </el-form-item>
          </template>
          <template v-if="loadFormData.upstream.type === 'index'">
            <el-form-item :label="$t('upstream.index-path')" prop="upstream.static.path"
              :rules="{
                required: true,
                trigger: 'change',
                message: $t('extension.please-input-placeholder') + $t('upstream.index-path')
              }"
            >
              <el-input v-model="loadFormData.upstream.static.path" :disabled="!isUpstreamManual" placeholder="/user/local/html"></el-input>
            </el-form-item>
            <el-form-item :label="$t('upstream.default-index-page')" prop="upstream.static.index">
              <el-input v-model="loadFormData.upstream.static.index" :disabled="!isUpstreamManual" placeholder="index.html"></el-input>
            </el-form-item>
            <el-form-item :label="$t('upstream.try-file-uri')" prop="upstream.static.fallback">
              <el-input v-model="loadFormData.upstream.static.fallback" :disabled="!isUpstreamManual" placeholder="index.html"></el-input>
            </el-form-item>
          </template>
          <el-form-item v-if="loadFormData.upstream.type === 'dns'" :label="$t('upstream.dns-domain')" :prop="'upstream.nodes.domain'"
            :rules="{
              required: true,
              trigger: 'change',
              message: $t('extension.please-input-placeholder') + $t('upstream.dns-domain')
            }"
          >
            <el-input v-model="loadFormData.upstream.domain" :disabled="!isUpstreamManual" placeholder="http://www.xxx.com"></el-input>
          </el-form-item>
          <template v-if="loadFormData.upstream.type === 'service'">
            <el-form-item :label="$t('upstream.service.service-type')">
              <el-select v-model="loadFormData.upstream.service.type" :disabled="!isUpstreamManual">
                <el-option v-for="(data, key) in upstreamServiceTypeList" :key="key" :value="data.value" :label="data.name" />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('upstream.service.service-name')" prop="upstream.service.name"
              :rules="{
                required: true,
                trigger: 'change',
                message: $t('extension.please-input-placeholder') + $t('upstream.service.service-name')
              }"
            >
              <el-input v-model="loadFormData.upstream.service.name" :disabled="!isUpstreamManual"></el-input>
            </el-form-item>
            <el-form-item :label="$t('upstream.service.service-server')" prop="upstream.service.server"
              :rules="{
                required: true,
                trigger: 'change',
                message: $t('extension.please-input-placeholder') + $t('upstream.service.service-server')
              }"
            >
              <el-input v-model="loadFormData.upstream.service.server" placeholder="127.0.0.1:2181,127.0.0.1:2182" :disabled="!isUpstreamManual"></el-input>
            </el-form-item>
            <el-form-item prop="upstream.service.path"
              :label="loadFormData.upstream.service.type === 'nacos' ? $t('upstream.service.service-groupname') : $t('upstream.service.service-path')"
              :rules="{
                required: true,
                trigger: 'change',
                message: $t('extension.please-input-placeholder') + (loadFormData.upstream.service.type === 'nacos' ? $t('upstream.service.service-groupname') : $t('upstream.service.service-path'))
              }"
            >
              <el-input v-model="loadFormData.upstream.service.path" placeholder="/service/providers" :disabled="!isUpstreamManual"></el-input>
            </el-form-item>
          </template>
          <template v-if="loadFormData.upstream.type === 'ai'">
            <el-form-item :label="$t('upstream.ai.provider')" prop="upstream.ai.provider"
              :rules="{
                required: true,
                trigger: 'change',
                message: $t('extension.please-select-placeholder') + $t('upstream.ai.provider')
              }"
            >
              <el-select v-model="loadFormData.upstream.ai.provider" v-loading="progression.loading" :teleported="false" :disabled="!isUpstreamManual"
                @change="handleProviderChange"
              >
                <el-option v-for="(data, index) in loadProviderList" :key="index" :value="data.id" :label="$t('provider.' + data.name)">
                  <div class="provider inline-flex-r-c-n">
                    <div class="image">
                      <img :src="loadProviderIcon(data.name)" />
                    </div>
                    <div class="name">{{ $t('provider.' + data.name) }}</div>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('upstream.ai.model')" prop="upstream.ai.parameters.model"
              :rules="{
                required: true,
                trigger: 'blur',
                message: $t('extension.please-select-placeholder') + $t('upstream.ai.model')
              }"
            >
              <el-select v-model="loadFormData.upstream.ai.parameters.model" :disabled="!isUpstreamManual">
                <el-option v-for="(data, index) in loadProviderModelList" :key="index" :value="data" :label="data" />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('upstream.ai.key')" prop="upstream.ai.parameters.keys">
              <el-select v-model="loadFormData.upstream.ai.parameters.keys" multiple collapse-tags collapse-tags-tooltip :max-collapse-tags="12" :disabled="!isUpstreamManual">
                <el-option v-for="data in loadProviderKeyList" :key="data" :label="data" :value="data" />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('upstream.ai.stream')" prop="upstream.ai.parameters.stream">
              <el-switch v-model="loadFormData.upstream.ai.parameters.stream" :disabled="!isUpstreamManual" />
            </el-form-item>
            <el-form-item :label="$t('upstream.ai.temperature')" prop="upstream.ai.parameters.temperature">
              <el-slider v-model="loadFormData.upstream.ai.parameters.temperature" show-input :step="0.01" :min="0" :max="2" :disabled="!isUpstreamManual" />
            </el-form-item>
            <el-form-item :label="$t('upstream.ai.top-p')" prop="upstream.ai.parameters.topP">
              <el-slider v-model="loadFormData.upstream.ai.parameters.topP" show-input :step="0.01" :min="0" :max="1" :disabled="!isUpstreamManual" />
            </el-form-item>
            <el-form-item :label="$t('upstream.ai.system-prompt')" prop="upstream.ai.parameters.systemPrompt">
              <el-input type="textarea" v-model="loadFormData.upstream.ai.parameters.systemPrompt" :rows="2" :disabled="!isUpstreamManual"></el-input>
            </el-form-item>
            <el-form-item :label="$t('upstream.ai.user-prompt')" prop="upstream.ai.parameters.userPrompt">
              <el-input type="textarea" v-model="loadFormData.upstream.ai.parameters.userPrompt" :rows="2" :disabled="!isUpstreamManual"></el-input>
            </el-form-item>
          </template>
          <template v-if="isUpstreamSupportHealthCheck(loadFormData.upstream.type)">
            <el-form-item :label="$t('upstream.proactive-health-check')">
              <el-switch v-model="loadFormData.upstream.healthy.proactive" />
            </el-form-item>
            <el-form-item :label="$t('upstream.passive-health-check')">
              <el-switch v-model="loadFormData.upstream.healthy.passive" />
            </el-form-item>
          </template>
          <template v-if="isUpstreamSupportWebSocket(loadFormData.upstream.type)">
            <el-form-item :label="$t('upstream.websocket-proxy')">
              <el-switch v-model="loadFormData.upstream.websocket" :disabled="!isUpstreamManual" />
            </el-form-item>
          </template>
        </div>
        <!-- 匹配条件 -->
        <div class="card">
          <div class="title">
            <el-icon><Link /></el-icon>
            <div class="name">{{ $t('route.matched-condition') }}</div>
          </div>
          <el-form-item :label="$t('route.route-path')" prop="path">
            <el-input v-model="loadFormData.path"></el-input>
          </el-form-item>
          <el-form-item :label="$t('route.http-method')" prop="methods">
            <el-select v-model="loadFormData.methods" multiple collapse-tags collapse-tags-tooltip :max-collapse-tags="12">
              <el-option v-for="data in loadHttpMethodList" :key="data.value"
                :label="data.label"
                :value="data.value"
              />
            </el-select>
          </el-form-item>
        </div>
        <!-- 高级匹配 -->
        <div class="card">
          <div class="title">
            <el-icon><Tickets /></el-icon>
            <div class="name">{{ $t('route.advanced-matched') }}</div>
          </div>
          <a-rule-mvel ref="ruleMvelRef" />
        </div>
      </el-form>
    </div>
    <div class="footer inline-flex-r-c-e">
      <el-button type="primary" :icon="Document" :loading="progression.sending"
        @click="handleSubmit"
      >
        {{ $t('common.save') }}
      </el-button>
      <el-button @click="handleBack">
        {{ $t('common.cancel') }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Plus,
  Delete,
  Setting,
  Document,
  Link,
  Warning,
  Promotion,
  Tickets
} from '@element-plus/icons-vue'
import type { FormRules } from 'element-plus'
import {
  routeTypeList
} from '@/hooks/page/useRoute'
import {
  upstreamTypeList,
  upstreamSchemaList,
  upstreamAlgorithmList,
  upstreamContentTypeList,
  upstreamServiceTypeList,
  isUpstreamSupportHealthCheck,
  isUpstreamSupportWebSocket
} from '@/hooks/page/useUpstream'
import { useRequest } from '@/hooks/useRequest'
import { loadProviderIcon } from '@/config/data/model'
import ARuleMvel from '@/components/common/rule/mvel.vue'

const loadFormData = reactive<any>({
  name: '',
  methods: [],
  path: '',
  priority: 0,
  authId: '',
  remark: '',
  upstreamId: '',
  upstream: {
    ai: {
      provider: '',
      parameters: {
        model: '',
        keys: [],
        stream: false,
        temperature: 0.7,
        topP: 1,
        systemPrompt: '',
        userPrompt: ''
      }
    },
    websocket: false
  },
  service: {
  }
})
const i18n = useI18n()
const loadFormRules = reactive<FormRules<any>>({
  name: [
    { trigger: 'change', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('extension.name') }
  ],
  upstreamId: [
    { trigger: 'change', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('route.route-upstream') }
  ],
  path: [
    { trigger: 'change', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('route.route-path') }
  ]
})
const loadHttpMethodList = [
  { label: 'GET', value: 'GET' },
  { label: 'POST', value: 'POST' },
  { label: 'PUT', value: 'PUT' },
  { label: 'DELETE', value: 'DELETE' },
  { label: 'PATCH', value: 'PATCH' },
  { label: 'OPTIONS', value: 'OPTIONS' },
  { label: 'HEAD', value: 'HEAD' },
  { label: 'TRACE', value: 'TRACE' },
  { label: 'CONNECT', value: 'CONNECT' }
]
const loadUpstreamList = ref<any[]>([
  {
    id: 'manual',
    name: i18n.t('route.input-upstream-manual'),
    algorithm: 'roundrobin',
    type: 'node',
    content: '',
    static: {
      path: '',
      index: '',
      fallback: ''
    },
    nodes: [
      {
        host: '',
        port: 80,
        weight: 1,
        schema: 'http'
      }
    ],
    service: {
      type: 'nacos',
      name: '',
      server: '',
      path: ''
    },
    ai: {
      provider: '',
      parameters: {
        model: '',
        keys: [],
        stream: false,
        temperature: 0.7,
        topP: 1,
        systemPrompt: '',
        userPrompt: ''
      }
    },
    healthy: {
      type: 'SocketPing',
      passive: false,
      proactive: false
    },
    websocket: false,
    domain: '',
    contentType: 'text/html',
    httpStatus: 200
  }
])
const loadAuthList = ref<any[]>([
  {
    id: '',
    name: i18n.t('setting.no-auth')
  }
])
const loadProviderList = ref<any[]>([])
const loadProviderModelList = computed(() => {
  if (!loadFormData.upstream.ai.provider) {
    return []
  }
  const matchedProvider = loadProviderList.value.find(data => data.id === loadFormData.upstream.ai.provider)
  if (!matchedProvider) {
    return []
  }
  return matchedProvider.models
})
const loadProviderKeyList = computed(() => { 
  if (!loadFormData.upstream.ai.provider) {
    return []
  }
  const matchedProvider = loadProviderList.value.find(data => data.id === loadFormData.upstream.ai.provider)
  if (!matchedProvider) {
    return []
  }
  const keys = matchedProvider.keys
  if (!keys) {
    return []
  }
  return keys.filter((data: any) => data.status === 1).map((data: any) => data.key)
})

const nameRef = ref()
const formRef = ref()
const ruleMvelRef = ref<InstanceType<typeof ARuleMvel>>()
const router = useRouter()
const { ioload, iopost, progression } = useRequest()

onMounted(() => {
  nameRef.value.focus()
  handleLoad()
})

const isUpstreamManual = computed(() => loadFormData.upstreamId === 'manual')
const handleLoad = async () => {
  const route = router.currentRoute.value
  // 1. 加载路由数据
  let params: any = {
    zone: route.params.id,
    id: route.params.rid
  }
  progression.batchLoading = true
  let result = await ioload('zone', 'loadRouteInfo', params)
  Object.keys(result).forEach((key) => {
    // 如果上游ID为手动配置，则将上游配置信息合并到上游配置中
    if (key === 'upstream_id') {
      const upstreamId = result[key]
      loadFormData.upstreamId = upstreamId
      if (upstreamId === 'manual') {
        const manualUpstream = loadUpstreamList.value.find((data: any) => data.id === upstreamId)
        Object.keys(manualUpstream).forEach((manualKey) => {
          if (result['upstream'][manualKey] === undefined) {
            return
          }
          manualUpstream[manualKey] = result['upstream'][manualKey]
        })
      }
      return
    } else if (key === 'auth_id') {
      loadFormData.authId = result[key] || ''
      return
    }
    loadFormData[key] = result[key]
  })
  ruleMvelRef.value?.handleExpressionLoad(loadFormData.rule)
  // 2. 加载上游列表
  params = {
    zone: route.params.id
  }
  result = await ioload('zone', 'loadUpstreamList', params)
  result.forEach((data: any) => {
    loadUpstreamList.value.push(data)
  })
  handleUpstreamChange(loadFormData.upstreamId)
  // 3. 加载鉴权列表
  result = await ioload('auth', 'loadAuthList', null)
  result.forEach((data: any) => {
    loadAuthList.value.push(data)
  })
  // 4. 加载AI服务列表
  result = await ioload('provider', 'loadProviderList', null)
  result.forEach((data: any) => {
    loadProviderList.value.push(data)
  })
  progression.batchLoading = false
}
const handleUpstreamChange = (value: string) => {
  const upstream = loadUpstreamList.value.find((item: any) => item.id === value)
  if (upstream) {
    if (value === 'manual' && loadFormData.upstream.type) {
      // 编辑模式下保留已加载的upstream数据
      Object.keys(upstream).forEach((key) => {
        if (loadFormData.upstream[key] === undefined) {
          loadFormData.upstream[key] = upstream[key]
        }
      })
    } else {
      loadFormData.upstream = upstream
    }
    if (loadFormData.upstream.websocket === undefined) {
      loadFormData.upstream.websocket = false
    }
  }
}
const handleUpstreamNodeAdd = () => {
  loadFormData.upstream.nodes.push({
    host: '',
    port: 80,
    weight: 1,
    schema: 'http'
  })
}
const handleUpstreamNodeValidator = (rule: any, value: any, callback: any) => {
  if (value.host === '') {
    callback(new Error(i18n.t('extension.please-input-placeholder') + i18n.t('upstream.host')))
  } else if (value.port === '') {
    callback(new Error(i18n.t('extension.please-input-placeholder') + i18n.t('upstream.port')))
  } else if (value.weight === '') {
    callback(new Error(i18n.t('extension.please-input-placeholder') + i18n.t('upstream.weight')))
  } else {
    callback()
  }
}
const handleUpstreamNodeRemove = (index: number) => {
  loadFormData.upstream.nodes.splice(index, 1)
}
const handleProviderChange = () => {
  const modelList = unref(loadProviderModelList)
  loadFormData.upstream.ai.parameters.model = modelList.length > 0 ? modelList[0] : ''
  loadFormData.upstream.ai.parameters.keys = []
}
const handleSubmit = async () => {
  const validate = await ruleMvelRef.value?.handleFormValidate()
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
      type: loadFormData.type,
      name: loadFormData.name,
      path: loadFormData.path,
      priority: loadFormData.priority,
      status: loadFormData.status,
      methods: loadFormData.methods,
      rule: ruleMvelRef.value?.handleExpreassionRead(),
      remark: loadFormData.remark
    }
    if (loadFormData.authId) {
      params.authId = loadFormData.authId
    }
    params.upstreamId = loadFormData.upstreamId
    if (loadFormData.upstreamId === 'manual') {
      const upstreamType = loadFormData.upstream.type
      params.upstream = {
        type: upstreamType,
        algorithm: loadFormData.upstream.algorithm
      }
      if (upstreamType === 'echo') {
        params.upstream.contentType = loadFormData.upstream.contentType
        params.upstream.code = loadFormData.upstream.httpStatus
        params.upstream.content = loadFormData.upstream.content
      } else if (upstreamType === 'index') {
        params.upstream.static = {
          path: loadFormData.upstream.static.path,
          index: loadFormData.upstream.static.index || '',
          fallback: loadFormData.upstream.static.fallback || ''
        }
      } else if (upstreamType === 'node') {
        params.upstream.nodes = loadFormData.upstream.nodes
        params.upstream.healthy = {
          type: loadFormData.upstream.healthy.type,
          proactive: loadFormData.upstream.healthy.proactive,
          passive: loadFormData.upstream.healthy.passive
        }
        params.upstream.websocket = loadFormData.upstream.websocket
      } else if (upstreamType === 'service') {
        params.upstream.service = {
          type: loadFormData.upstream.service.type,
          name: loadFormData.upstream.service.name,
          server: loadFormData.upstream.service.server,
          path: loadFormData.upstream.service.path
        }
        params.upstream.websocket = loadFormData.upstream.websocket
      } else if (upstreamType === 'dns') {
        params.upstream.domain = loadFormData.upstream.domain
        params.upstream.websocket = loadFormData.upstream.websocket
      } else if (upstreamType === 'ai') {
        params.upstream.ai = {
          provider: loadFormData.upstream.ai.provider,
          parameters: {
            model: loadFormData.upstream.ai.parameters.model,
            keys: loadFormData.upstream.ai.parameters.keys || [],
            stream: loadFormData.upstream.ai.parameters.stream,
            temperature: loadFormData.upstream.ai.parameters.temperature,
            topP: loadFormData.upstream.ai.parameters.topP,
            systemPrompt: loadFormData.upstream.ai.parameters.systemPrompt,
            userPrompt: loadFormData.upstream.ai.parameters.userPrompt
          }
        }
      }
    }
    iopost('zone', 'xhrRouteUpdate', params).then(() => {
      router.push({ path: `/zone/route/${route.params.id}` })
    })
  })
}
const handleBack = () => {
  router.back()
}
</script>
