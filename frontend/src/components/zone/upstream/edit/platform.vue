<template>
  <div class="a-upstream--edit a-zone-common-form platform inline-flex-c-n-n">
    <div class="head inline-flex-r-c-n">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: `/zone/upstream/${$route.params.id}` }">
          {{ $t('menu.upstream') }}
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          {{ $t('upstream.edit-upstream') }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="content">
      <el-form ref="formRef" label-position="right" label-width="120px"
        :model="loadFormData"
        :rules="loadFormRules"
      >
        <div class="card">
          <el-divider content-position="left">{{ $t('upstream.basic-config') }}</el-divider>
          <el-form-item :label="$t('extension.name')" prop="name">
            <el-input ref="nameRef" v-model="loadFormData.name"></el-input>
          </el-form-item>
          <el-form-item :label="$t('extension.remark')" prop="remark">
            <el-input type="textarea" v-model="loadFormData.remark" :rows="2"></el-input>
          </el-form-item>
          <el-form-item :label="$t('upstream.upstream-algorithm')">
            <el-select v-model="loadFormData.algorithm">
              <el-option v-for="(data, key) in upstreamAlgorithmList" :key="key" :value="data.value"
                :label="$t('upstream.algorithm.' + data.name)"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('upstream.upstream-type')">
            <el-select v-model="loadFormData.type">
              <el-option v-for="(data, key) in upstreamTypeList" :key="key" :value="data.value"
                :label="$t('upstream.type.' + data.name)"
              />
            </el-select>
          </el-form-item>
          <template v-if="loadFormData.type === 'echo'">
            <el-form-item :label="$t('upstream.echo-response')"
              :rules="{
                required: true,
                trigger: 'change'
              }"
            >
              <el-row :gutter="20" style="width: 100%;">
                <el-col :span="18">
                  <el-select v-model="loadFormData.contentType" :placeholder="$t('upstream.echo-content-type')">
                    <el-option v-for="(data, key) in upstreamContentTypeList" :key="key" :value="data.value"
                      :label="$t('upstream.response.' + data.name)"
                    />
                  </el-select>
                </el-col>
                <el-col :span="6">
                  <el-input-number v-model="loadFormData.code" :min="0" controls-position="right"
                    :placeholder="$t('upstream.echo-status')"
                  />
                </el-col>
              </el-row>
            </el-form-item>
            <el-form-item :label="$t('upstream.echo-content')" prop="content"
              :rules="{
                required: true,
                trigger: 'change',
                message: $t('extension.please-input-placeholder') + $t('upstream.echo-content')
              }"
            >
              <el-input type="textarea" v-model="loadFormData.content" :rows="2"></el-input>
            </el-form-item>
          </template>
          <template v-if="loadFormData.type === 'node'">
            <el-form-item v-for="(data, index) in loadFormData.nodes" :key="index" class="node" :label="$t('upstream.node')"
              :prop="'nodes.' + index"
              :rules="{
                required: true,
                trigger: 'change',
                validator: handleNodeValidator
              }"
            >
              <el-row :gutter="20" style="width: 100%;">
                <el-col :span="10">
                  <el-input v-model="data.host" :placeholder="$t('upstream.host')"></el-input>
                </el-col>
                <el-col :span="5">
                  <el-input v-model="data.port" :placeholder="$t('upstream.port')"></el-input>
                </el-col>
                <el-col :span="4">
                  <el-input v-model="data.weight" :placeholder="$t('upstream.weight')"></el-input>
                </el-col>
                <el-col :span="4">
                  <el-select v-model="data.schema">
                    <el-option v-for="data in upstreamSchemaList" :key="data.value"
                      :label="data.name"
                      :value="data.value"
                    />
                  </el-select>
                </el-col>
                <el-col :span="1">
                  <el-icon v-if="loadFormData.nodes.length > 1" class="icon" @click="handleNodeRemove(index)">
                    <Delete />
                  </el-icon>
                </el-col>
              </el-row>
            </el-form-item>
            <el-form-item>
              <a-button :icon="Plus" style="width: 100%;" @click="handleNodeAdd">
                {{ $t('upstream.add-node') }}
              </a-button>
            </el-form-item>
          </template>
          <template v-if="loadFormData.type === 'index'">
            <el-form-item :label="$t('upstream.index-path')" prop="static.path"
              :rules="{
                required: true,
                trigger: 'change',
                message: $t('extension.please-input-placeholder') + $t('upstream.index-path')
              }"
            >
              <el-input v-model="loadFormData.static.path" placeholder="/user/local/html"></el-input>
            </el-form-item>
            <el-form-item :label="$t('upstream.default-index-page')" :prop="'static.index'">
              <el-input v-model="loadFormData.static.index" placeholder="index.html"></el-input>
            </el-form-item>
            <el-form-item :label="$t('upstream.try-file-uri')" :prop="'static.fallback'">
              <el-input v-model="loadFormData.static.fallback" placeholder="index.html"></el-input>
            </el-form-item>
          </template>
          <el-form-item v-if="loadFormData.type === 'dns'" :label="$t('upstream.dns-domain')" prop="path"
            :rules="{
              required: true,
              trigger: 'change',
              message: $t('extension.please-input-placeholder') + $t('upstream.dns-domain')
            }"
          >
            <el-input v-model="loadFormData.domain" placeholder="http://www.xxx.com"></el-input>
          </el-form-item>
          <template v-if="loadFormData.type === 'service'">
            <el-form-item :label="$t('upstream.service.service-type')">
              <el-select v-model="loadFormData.service.type">
                <el-option v-for="(data, key) in upstreamServiceTypeList" :key="key" :value="data.value" :label="data.name" />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('upstream.service.service-name')" prop="service.name"
              :rules="{
                required: true,
                trigger: 'change',
                message: $t('extension.please-input-placeholder') + $t('upstream.service.service-name')
              }"
            >
              <el-input v-model="loadFormData.service.name"></el-input>
            </el-form-item>
            <el-form-item :label="$t('upstream.service.service-server')" prop="service.server"
              :rules="{
                required: true,
                trigger: 'change',
                message: $t('extension.please-input-placeholder') + $t('upstream.service.service-server')
              }"
            >
              <el-input v-model="loadFormData.service.server" placeholder="127.0.0.1:2181,127.0.0.1:2182"></el-input>
            </el-form-item>
            <el-form-item prop="service.path"
              :label="loadFormData.service.type === 'nacos' ? $t('upstream.service.service-groupname') : $t('upstream.service.service-path')"
              :rules="{
                required: true,
                trigger: 'change',
                message: $t('extension.please-input-placeholder') + (loadFormData.service.type === 'nacos' ? $t('upstream.service.service-groupname') : $t('upstream.service.service-path'))
              }"
            >
              <el-input v-model="loadFormData.service.path"
                :placeholder="loadFormData.service.type === 'nacos' ? 'DEFAULT_GROUP' : '/service/path'"
              ></el-input>
            </el-form-item>
          </template>
          <template v-if="loadFormData.type === 'ai'">
            <el-form-item :label="$t('upstream.ai.provider')" prop="ai.provider"
              :rules="{
                required: true,
                trigger: 'change',
                message: $t('extension.please-select-placeholder') + $t('upstream.ai.provider')
              }"
            >
              <el-select v-model="loadFormData.ai.provider" v-loading="progression.loading" :teleported="false"
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
            <el-form-item :label="$t('upstream.ai.model')" prop="ai.parameters.model"
              :rules="{
                required: true,
                trigger: 'blur',
                message: $t('extension.please-select-placeholder') + $t('upstream.ai.model')
              }"
            >
              <el-select v-model="loadFormData.ai.parameters.model">
                <el-option v-for="(data, index) in loadProviderModelList" :key="index" :value="data" :label="data" />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('upstream.ai.key')" prop="ai.parameters.keys">
              <el-select v-model="loadFormData.ai.parameters.keys" multiple collapse-tags collapse-tags-tooltip :max-collapse-tags="12">
                <el-option v-for="data in loadProviderKeyList" :key="data" :label="data" :value="data" />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('upstream.ai.stream')" prop="ai.parameters.stream">
              <el-switch v-model="loadFormData.ai.parameters.stream" />
            </el-form-item>
            <el-form-item :label="$t('upstream.ai.temperature')" prop="ai.parameters.temperature">
              <el-slider v-model="loadFormData.ai.parameters.temperature" show-input :step="0.01" :min="0" :max="2" />
            </el-form-item>
            <el-form-item :label="$t('upstream.ai.top-p')" prop="ai.parameters.topP">
              <el-slider v-model="loadFormData.ai.parameters.topP" show-input :step="0.01" :min="0" :max="1" />
            </el-form-item>
            <el-form-item :label="$t('upstream.ai.system-prompt')" prop="ai.parameters.systemPrompt">
              <el-input type="textarea" v-model="loadFormData.ai.parameters.systemPrompt" :rows="2"></el-input>
            </el-form-item>
            <el-form-item :label="$t('upstream.ai.user-prompt')" prop="ai.parameters.userPrompt">
              <el-input type="textarea" v-model="loadFormData.ai.parameters.userPrompt" :rows="2"></el-input>
            </el-form-item>
          </template>
          <template v-if="isUpstreamSupportHealthCheck(loadFormData.type)">
            <el-form-item :label="$t('upstream.proactive-health-check')">
              <el-switch v-model="loadFormData.healthy.proactive" />
            </el-form-item>
            <el-form-item :label="$t('upstream.passive-health-check')" v-if="isUpstreamSupportHealthCheck(loadFormData.type)">
              <el-switch v-model="loadFormData.healthy.passive" />
            </el-form-item>
          </template>
        </div>
      </el-form>
    </div>
    <div class="footer inline-flex-r-c-e">
      <el-button type="primary" :icon="Document" :loading="progression.loading"
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
  Document
} from '@element-plus/icons-vue'
import {
  upstreamTypeList,
  upstreamSchemaList,
  upstreamAlgorithmList,
  upstreamContentTypeList,
  upstreamServiceTypeList,
  isUpstreamSupportHealthCheck
} from '@/hooks/page/useUpstream'
import type { FormRules } from 'element-plus'
import { useRequest } from '@/hooks/useRequest'
import { loadProviderIcon } from '@/config/data/model'

const loadFormData = reactive<any>({
  name: '',
  remark: '',
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
  domain: ''
})
const loadProviderList = ref<any[]>([])
const loadProviderModelList = computed(() => {
  if (!loadFormData.ai.provider) {
    return []
  }
  const matchedProvider = loadProviderList.value.find(data => data.id === loadFormData.ai.provider)
  if (!matchedProvider) {
    return []
  }
  return matchedProvider.models
})
const loadProviderKeyList = computed(() => { 
  if (!loadFormData.ai.provider) {
    return []
  }
  const matchedProvider = loadProviderList.value.find(data => data.id === loadFormData.ai.provider)
  if (!matchedProvider) {
    return []
  }
  const keys = matchedProvider.keys
  if (!keys) {
    return []
  }
  return keys.filter((data: any) => data.status === 1).map((data: any) => data.key)
})
const i18n = useI18n()
const loadFormRules = reactive<FormRules<any>>({
  name: [
    { trigger: 'change', required: true, message: i18n.t('extension.please-input-placeholder') + i18n.t('extension.name') }
  ]
})

const nameRef = ref()
const formRef = ref()
const router = useRouter()
const { ioload, iopost, progression } = useRequest()

onMounted(() => {
  nameRef.value.focus()
  handleLoad()
  handleProviderLoad()
})

const handleLoad = () => {
  const route = router.currentRoute.value
  const params = {
    zone: route.params.id,
    id: route.params.uid
  }
  ioload('zone', 'loadUpstreamInfo', params).then((result) => {
    Object.keys(result).forEach((key) => {
      loadFormData[key] = result[key]
    })
  })
}
const handleProviderLoad = () => {
  ioload('provider', 'loadProviderList', null).then((result) => {
    loadProviderList.value = result
    loadFormData.ai.provider = result.length > 0 ? result[0].id : ''
    loadFormData.ai.model = loadProviderModelList.value.length > 0 ? loadProviderModelList.value[0] : ''
  })
}
const handleNodeAdd = () => {
  loadFormData.nodes.push({
    host: '',
    port: 80,
    weight: 1,
    schema: 'http'
  })
}
const handleNodeRemove = (index: number) => {
  loadFormData.nodes.splice(index, 1)
}
const handleNodeValidator = (rule: any, value: any, callback: any) => {
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
const handleProviderChange = () => {
  const modelList = unref(loadProviderModelList)
  loadFormData.ai.parameters.model = modelList.length > 0 ? modelList[0] : ''
  loadFormData.ai.parameters.keys = []
}
const handleSubmit = () => {
  formRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false
    }
    const route = router.currentRoute.value
    const params: any = {
      id: route.params.uid,
      zone: route.params.id,
      name: loadFormData.name,
      type: loadFormData.type,
      algorithm: loadFormData.algorithm,
      remark: loadFormData.remark,
      parameters: {}
    }
    if (loadFormData.type === 'echo') {
      params.parameters.code = loadFormData.code
      params.parameters.contentType = loadFormData.contentType
      params.parameters.content = loadFormData.content
    } else if (loadFormData.type === 'index') {
      params.parameters.static = {
        path: loadFormData.static.path,
        index: loadFormData.static.index || '',
        fallback: loadFormData.static.fallback || ''
      }
    } else if (loadFormData.type === 'node') {
      params.parameters.nodes = loadFormData.nodes.map((node: any) => {
        return {
          host: node.host.trim(),
          port: node.port,
          weight: node.weight,
          schema: node.schema
        }
      })
      params.parameters.healthy = {
        type: loadFormData.healthy.type,
        proactive: loadFormData.healthy.proactive,
        passive: loadFormData.healthy.passive
      }
    } else if (loadFormData.type === 'service') {
      params.parameters.service = {
        type: loadFormData.service.type,
        name: loadFormData.service.name,
        server: loadFormData.service.server,
        path: loadFormData.service.path
      }
    } else if (loadFormData.type === 'dns') {
      params.parameters.domain = loadFormData.domain
    } else if (loadFormData.type === 'ai') {
      params.parameters.ai = {
        provider: loadFormData.ai.provider,
        parameters: {
          model: loadFormData.ai.parameters.model,
          keys: loadFormData.ai.parameters.keys,
          stream: loadFormData.ai.parameters.stream,
          temperature: loadFormData.ai.parameters.temperature,
          topP: loadFormData.ai.parameters.topP,
          systemPrompt: loadFormData.ai.parameters.systemPrompt,
          userPrompt: loadFormData.ai.parameters.userPrompt
        }
      }
    }
    iopost('zone', 'xhrUpstreamUpdate', params, {
      onMessage: true
    }).then(() => {
      const zone = route.params.id
      router.push({ path: `/zone/upstream/${zone}` })
    })
  })
}
const handleBack = () => {
  router.back()
}
</script>
