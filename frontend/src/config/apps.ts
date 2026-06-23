import {
  PriceTag,
  Document,
  DocumentChecked,
  Calendar,
  Folder,
  ChatDotRound
} from '@element-plus/icons-vue'

export default [
  {
    key: 'mission',
    color: '#3370ff',
    icon: markRaw(PriceTag)
  },
  {
    key: 'document',
    color: '#22d7bb',
    icon: markRaw(Document)
  },
  {
    key: 'todolist',
    color: '#fc587b',
    icon: markRaw(DocumentChecked)
  },
  {
    key: 'calendar',
    color: '#ff8801',
    icon: markRaw(Calendar)
  },
  {
    key: 'pan',
    color: '#3ad1ff',
    icon: markRaw(Folder)
  },
  {
    key: 'messenger',
    color: '#0c8dfc',
    icon: markRaw(ChatDotRound)
  }
]
