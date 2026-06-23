import {
  Share
} from '@element-plus/icons-vue'

export const routeTypeList = [
  {
    name: 'restful',
    value: 'restful',
    color: '#409eff',
    icon: markRaw(Share)
  }
]
export const loadRouteTypeInfo = (value: string) => {
  return routeTypeList.find(data => data.value === value)
}
