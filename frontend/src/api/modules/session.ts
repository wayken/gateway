import request from '@/api/request'

// 会话管理模块
export default {
  loadSessionInfo: (params: any) => {
    return request.get('/api/v1/session', params)
  },
  xhrSessionLogin: (params: any) => {
    return request.post('/api/v1/session/login', params)
  },
  xhrSessionMfa: (params: any) => {
    return request.post('/api/v1/session/mfa', params)
  },
  xhrSessionLogout: (params: any) => {
    return request.post('/api/v1/session/logout', params)
  }
}
