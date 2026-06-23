import request from '@/api/request'

export default {
  loadUserInfo: (params: any) => {
    return request.get(`/api/v1/database/user/${params.id}`, params)
  },
  loadUserList: (params: any) => {
    return request.get('/api/v1/database/users', params)
  },
  loadRoleInfo: (params: any) => {
    return request.get(`/api/v1/database/role/${params.id}`, params)
  },
  loadRoleList: (params: any) => {
    return request.get('/api/v1/database/roles', params)
  },
  xhrUserAdd: (params: any) => {
    return request.put('/api/v1/database/user', params)
  },
  xhrUserUpdate: (params: any) => {
    return request.post(`/api/v1/database/user/${params.id}`, params)
  },
  xhrUserRemove: (params: any) => {
    return request.delete(`/api/v1/database/user/${params.id}`)
  },
  xhrRoleAdd: (params: any) => {
    return request.put('/api/v1/database/role', params)
  },
  xhrRoleRemove: (params: any) => {
    return request.delete(`/api/v1/database/role/${params.id}`)
  },
  xhrRoleUpdate: (params: any) => {
    return request.post(`/api/v1/database/role/${params.id}`, params)
  }
}
