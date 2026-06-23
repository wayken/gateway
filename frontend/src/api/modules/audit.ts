import request from '@/api/request'

// 审计日志管理模块
export default {
  loadSessionLogList: (params: any) => {
    return request.get('/api/v1/session/logs', params)
  },
  loadSessionLogCount: (params: any) => {
    return request.get('/api/v1/session/log/count', params)
  },
  loadOperationLogList: (params: any) => {
    return request.get('/api/v1/operation/logs', params)
  },
  loadOperationLogCount: (params: any) => {
    return request.get('/api/v1/operation/log/count', params)
  }
}
