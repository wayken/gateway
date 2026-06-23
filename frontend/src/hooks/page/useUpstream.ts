import {
  Share,
  MessageBox,
  Document,
  Location,
  Service,
  Help
} from '@element-plus/icons-vue'

export const upstreamTypeList = [
  {
    name: 'node',
    value: 'node',
    color: '#409eff',
    icon: markRaw(Location)
  },
  {
    name: 'echo',
    value: 'echo',
    color: '#5b65e1',
    icon: markRaw(Service)
  },
  {
    name: 'dns',
    value: 'dns',
    color: '#fbad41',
    icon: markRaw(MessageBox)
  },
  {
    name: 'service',
    value: 'service',
    color: '#eb3f87',
    icon: markRaw(Share)
  },
  {
    name: 'index',
    value: 'index',
    color: '#67c23a',
    icon: markRaw(Document)
  },
  {
    name: 'ai',
    value: 'ai',
    color: '#ff8801',
    icon: markRaw(Help)
  }
]

export const upstreamSchemaList = [
  {
    name: 'HTTP',
    value: 'http'
  },
  {
    name: 'HTTPS',
    value: 'https'
  }
]

export const upstreamAlgorithmList = [
  {
    name: 'roundrobin',
    value: 'roundrobin'
  },
  {
    name: 'lessconn',
    value: 'lessconn'
  },
  {
    name: 'hash',
    value: 'hash'
  },
  {
    name: 'random',
    value: 'random'
  },
  {
    name: 'fair',
    value: 'fair'
  }
]

export const upstreamServiceTypeList = [
  {
    name: 'Nacos',
    value: 'nacos'
  },
  {
    name: 'Zookeeper',
    value: 'zookeeper'
  }
]

export const upstreamContentTypeList = [
  {
    name: 'text',
    value: 'text/plain'
  },
  {
    name: 'html',
    value: 'text/html'
  },
  {
    name: 'json',
    value: 'application/json'
  },
  {
    name: 'xml',
    value: 'application/xml'
  }
]

export const loadUpstreamTypeInfo = (value: string) => {
  return upstreamTypeList.find(data => data.value === value)
}

export const isUpstreamSupportHealthCheck = (value: string) => {
  return ['node'].includes(value)
}

export const isUpstreamSupportWebSocket = (value: string) => {
  return ['node', 'service', 'dns'].includes(value)
}
