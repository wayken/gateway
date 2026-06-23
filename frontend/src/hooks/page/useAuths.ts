import {
  MessageBox,
  Location,
  Service
} from '@element-plus/icons-vue'

export const loadTypeList = [
  {
    name: 'key',
    value: 'key',
    color: '#409eff',
    icon: markRaw(Location)
  },
  {
    name: 'jwt',
    value: 'jwt',
    color: '#5b65e1',
    icon: markRaw(Service)
  },
  {
    name: 'server',
    value: 'server',
    color: '#fbad41',
    icon: markRaw(MessageBox)
  }
]

export const loadSourceList = [
  'header', 'query'
]

export const loadSchemaList = [
  'http', 'https'
]

export const loadAuthTypeInfo = (value: string) => {
  return loadTypeList.find(item => item.value === value)
}
