import {
  Plus,
  Edit,
  Delete,
  Location
} from '@element-plus/icons-vue'

export const loadOperationTypeList = [
  {
    id: 0,
    name: 'add',
    color: '#409eff',
    icon: markRaw(Plus)
  },
  {
    id: 1,
    name: 'edit',
    color: '#67c23a',
    icon: markRaw(Edit)
  },
  {
    id: 2,
    name: 'delete',
    color: '#f56c6c',
    icon: markRaw(Delete)
  },
  {
    id: -1,
    name: 'unknown',
    color: '#909399',
    icon: markRaw(Location)
  }
]

export const loadOperationTypeInfo = (value: number) => {
  return loadOperationTypeList.find(data => data.id === value) || loadOperationTypeList[3]
}
