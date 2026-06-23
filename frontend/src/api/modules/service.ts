import request from '@/api/request'

export default {
  loadServiceList: (params: any) => {
    return request.get('/api/v1/gateway/services', params)
  }
}
