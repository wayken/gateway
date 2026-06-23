import request from '@/api/request'

export default {
  loadProviderInfo: (params: any) => {
    return request.get(`/api/v1/gateway/provider/${params.id}`, params)
  },
  loadProviderList: (params: any) => {
    return request.get('/api/v1/gateway/providers', params)
  },
  xhrProviderAdd: (params: any) => {
    return request.put('/api/v1/gateway/provider', params)
  },
  xhrProviderUpdate: (params: any) => {
    return request.post(`/api/v1/gateway/provider/${params.id}`, params)
  },
  xhrProviderRemove: (params: any) => {
    return request.delete(`/api/v1/gateway/provider/${params.id}`)
  }
}
