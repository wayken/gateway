import {
  Lock,
  Check,
  Compass,
  Document,
  Message,
  Avatar
} from '@element-plus/icons-vue'
import countries from '@/config/countries.json'

// 规则类型
export type Rule = {
  field: string;
  operator: string;
  value?: string | number;
  key?: string;
  logical?: string;
}

export const loadCommonActionTypeList = [
  {
    name: 'block',
    value: 'block',
    color: '#f56c6c',
    icon: markRaw(Lock)
  },
  {
    name: 'skip',
    value: 'skip',
    color: '#67c23a',
    icon: markRaw(Check)
  },
  {
    name: 'log',
    value: 'log',
    color: '#1ab394',
    icon: markRaw(Document)
  },
  {
    name: 'cookie_challenge',
    value: 'cookie_challenge',
    color: '#5e34ba',
    icon: markRaw(Compass)
  },
  {
    name: 'js_challenge',
    value: 'js_challenge',
    color: '#409eff',
    icon: markRaw(Message)
  },
  {
    name: 'interactive_challenge',
    value: 'interactive_challenge',
    color: '#5b65e1',
    icon: markRaw(Avatar)
  }
]

export const loadCacheActionTypeList = [
  {
    name: 'cache-force',
    value: 'cache-force',
    color: '#f56c6c',
    icon: markRaw(Lock)
  },
  {
    name: 'cache-control',
    value: 'cache-control',
    color: '#67c23a',
    icon: markRaw(Check)
  }
]

// 规则操作符列表，用于生成规则表单
export const loadCommonRuleOperatorList = [
  {
    name: 'eq',
    value: '=='
  },
  {
    name: 'ne',
    value: '!='
  },
  {
    name: 'gt',
    value: '>',
    numberic: true
  },
  {
    name: 'ge',
    value: '>=',
    numberic: true
  },
  {
    name: 'lt',
    value: '<',
    numberic: true
  },
  {
    name: 'le',
    value: '<=',
    numberic: true
  },
  {
    name: 'contains',
    value: '.contains'
  },
  {
    name: 'starts_with',
    value: '.startsWith'
  },
  {
    name: 'ends_with',
    value: '.endsWith'
  },
  {
    name: 'matches',
    value: '~='
  }
]
export const loadIpRuleOperatorList = [
  {
    name: 'eq',
    value: '.eq'
  },
  {
    name: 'ne',
    value: '.ne'
  },
  {
    name: 'in',
    value: '.in'
  },
  {
    name: 'not_in',
    value: '.notIn'
  },
  {
    name: 'in_list',
    value: '.inList'
  },
  {
    name: 'not_in_list',
    value: '.notInList'
  }
]
export const loadCountryRuleOperatorList = [
  {
    name: 'eq',
    value: '=='
  },
  {
    name: 'ne',
    value: '!='
  }
]

// 规则字段列表，用于生成规则表单
export const loadRuleFieldList = [
  {
    name: 'ip',
    value: 'http.ip.src',
    operators: loadIpRuleOperatorList
  },
  {
    name: 'path',
    value: 'http.path'
  },
  {
    name: 'url',
    value: 'http.request.url'
  },
  {
    name: 'host',
    value: 'http.host'
  },
  {
    name: 'method',
    value: 'http.method',
    select: true,
    options: [
      {
        name: 'GET',
        value: 'GET'
      },
      {
        name: 'POST',
        value: 'POST'
      },
      {
        name: 'PUT',
        value: 'PUT'
      },
      {
        name: 'DELETE',
        value: 'DELETE'
      },
      {
        name: 'PATCH',
        value: 'PATCH'
      },
      {
        name: 'HEAD',
        value: 'HEAD'
      },
      {
        name: 'OPTIONS',
        value: 'OPTIONS'
      },
      {
        name: 'TRACE',
        value: 'TRACE'
      },
      {
        name: 'CONNECT',
        value: 'CONNECT'
      }
    ]
  },
  {
    name: 'referer',
    value: 'http.referer'
  },
  {
    name: 'header',
    value: 'http.headers',
    multiple: true
  },
  {
    name: 'cookie',
    value: 'http.cookies',
    multiple: true
  },
  {
    name: 'parameter',
    value: 'http.parameters',
    multiple: true
  },
  {
    name: 'schema',
    value: 'http.schema'
  },
  {
    name: 'useragent',
    value: 'http.user-agent'
  },
  {
    name: 'country',
    value: 'http.country',
    select: true,
    operators: loadCountryRuleOperatorList,
    options: countries.map(country => ({
      name: country.name,
      value: country.name
    }))
  }
]

// 规则周期列表
export const loadRulePeriodList = [
  {
    value: 1,
    unit: 'second'
  },
  {
    value: 1,
    unit: 'minute'
  },
  {
    value: 1,
    unit: 'hour'
  },
  {
    value: 1,
    unit: 'day'
  }
]

// 获取通用动作类型信息
export const getCommonActionTypeInfo = (type: string) => {
  return loadCommonActionTypeList.find(data => data.value === type)
}

// 获取缓存动作类型信息
export const getCacheActionTypeInfo = (type: string) => {
  return loadCacheActionTypeList.find(data => data.value === type)
}

// 判断字段是否支持多值，即 `Key -> Value`
export const isFieldMultiple = (field: string) => {
  return loadRuleFieldList.find(data => data.value === field)?.multiple
}

// 获取字段是运算符列表
export const getFieldOperatorList = (field: string) => {
  return loadRuleFieldList.find(data => data.value === field)?.operators || loadCommonRuleOperatorList
}

// 判断字段是否为下拉框类型
export const isFieldSelect = (field: string) => {
  return loadRuleFieldList.find(data => data.value === field)?.select
}
export const getSelectOptions = (field: string) => {
  return loadRuleFieldList.find(data => data.value === field)?.options
}

// 判断操作符是否支持数字类型的值
export const isOperatorNumberic = (operator: string) => {
  return loadCommonRuleOperatorList.find(data => data.value === operator)?.numberic
}

// 判断操作符是否需要在值两边加引号，例如`startsWith`/`endsWith`
export const isOperatorWithQuotes = (operator: string) => {
  return [
    '.startsWith',
    '.endsWith',
    '.contains',
    '.eq',
    '.ne',
    '.in',
    '.notIn',
    '.inList',
    '.notInList'
  ].includes(operator)
}
