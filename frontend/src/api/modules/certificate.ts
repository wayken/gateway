import request from '@/api/request'

export default {
  loadCertificateInfo: (params: any) => {
    return request.get(`/api/v1/gateway/certificate/${params.id}`, params)
  },
  loadCertificateList: (params: any) => {
    return request.get('/api/v1/gateway/certificates', params)
  },
  xhrCertificateAdd: (params: any) => {
    return request.put('/api/v1/gateway/certificate', params)
  },
  xhrCertificateUpdate: (params: any) => {
    return request.post(`/api/v1/gateway/certificate/${params.id}`, params)
  },
  xhrCertificateRemove: (params: any) => {
    return request.delete(`/api/v1/gateway/certificate/${params.id}`)
  }
}
