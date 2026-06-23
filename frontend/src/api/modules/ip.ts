import request from '@/api/request'

export default {
  loadIpInfo: (params: any) => {
    return request.get(`/api/v1/gateway/ip/${params.id}`, params)
  },
  loadIpList: (params: any) => {
    return request.get('/api/v1/gateway/ips', params)
  },
  xhrIpAdd: (params: any) => {
    return request.put('/api/v1/gateway/ip', params)
  },
  xhrIpUpdate: (params: any) => {
    return request.post(`/api/v1/gateway/ip/${params.id}`, params)
  },
  xhrIpRemove: (params: any) => {
    return request.delete(`/api/v1/gateway/ip/${params.id}`)
  }
}
