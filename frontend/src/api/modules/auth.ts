import request from '@/api/request'

export default {
  loadAuthInfo: (params: any) => {
    return request.get(`/api/v1/gateway/auth/${params.id}`, params)
  },
  loadAuthList: (params: any) => {
    return request.get('/api/v1/gateway/auths', params)
  },
  xhrAuthAdd: (params: any) => {
    return request.put('/api/v1/gateway/auth', params)
  },
  xhrAuthUpdate: (params: any) => {
    return request.post(`/api/v1/gateway/auth/${params.id}`, params)
  },
  xhrAuthRemove: (params: any) => {
    return request.delete(`/api/v1/gateway/auth/${params.id}`)
  }
}
