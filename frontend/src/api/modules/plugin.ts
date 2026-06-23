import request from '@/api/request'

export default {
  loadPluginList: (params: any) => {
    return request.get(`/api/v1/gateway/${params.zone}/plugins`, params)
  },
  xhrPluginListUpdate: (params: any) => {
    return request.post(`/api/v1/gateway/${params.zone}/plugins`, params)
  }
}
