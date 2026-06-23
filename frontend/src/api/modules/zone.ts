import request from '@/api/request'

export default {
  loadZoneInfo: (params: any) => {
    return request.get(`/api/v1/gateway/zone/${params.zone}`, params)
  },
  loadZoneList: (params: any) => {
    return request.get('/api/v1/gateway/zones', params)
  },
  loadZoneSequenceInfo: (params: any) => {
    return request.get(`/api/v1/gateway/zone/${params.zone}/sequence`, params)
  },
  loadRouteInfo: (params: any) => {
    return request.get(`/api/v1/gateway/${params.zone}/route/${params.id}`)
  },
  loadRouteList: (params: any) => {
    return request.get(`/api/v1/gateway/${params.zone}/routes`, params)
  },
  loadUpstreamInfo: (params: any) => {
    return request.get(`/api/v1/gateway/${params.zone}/upstream/${params.id}`)
  },
  loadUpstreamList: (params: any) => {
    return request.get(`/api/v1/gateway/${params.zone}/upstreams`, params)
  },
  loadWafRuleInfo: (params: any) => {
    return request.get(`/api/v1/gateway/${params.zone}/waf/rule/${params.id}`, params)
  },
  loadWafRuleList: (params: any) => {
    return request.get(`/api/v1/gateway/${params.zone}/waf/rules`, params)
  },
  loadCacheRuleInfo: (params: any) => {
    return request.get(`/api/v1/gateway/${params.zone}/cache/rule/${params.id}`, params)
  },
  loadCacheRuleList: (params: any) => {
    return request.get(`/api/v1/gateway/${params.zone}/cache/rules`, params)
  },
  loadLimitRuleInfo: (params: any) => {
    return request.get(`/api/v1/gateway/${params.zone}/limit/rule/${params.id}`, params)
  },
  loadLimitRuleList: (params: any) => {
    return request.get(`/api/v1/gateway/${params.zone}/limit/rules`, params)
  },
  loadRewriteRedirectRuleInfo: (params: any) => {
    return request.get(`/api/v1/gateway/${params.zone}/rewrite/redirect/rule/${params.id}`, params)
  },
  loadRewriteRedirectRuleList: (params: any) => {
    return request.get(`/api/v1/gateway/${params.zone}/rewrite/redirect/rules`, params)
  },
  loadRewriteRequestRuleInfo: (params: any) => {
    return request.get(`/api/v1/gateway/${params.zone}/rewrite/request/rule/${params.id}`, params)
  },
  loadRewriteRequestRuleList: (params: any) => {
    return request.get(`/api/v1/gateway/${params.zone}/rewrite/request/rules`, params)
  },
  loadRewriteResponseRuleInfo: (params: any) => {
    return request.get(`/api/v1/gateway/${params.zone}/rewrite/response/rule/${params.id}`, params)
  },
  loadRewriteResponseRuleList: (params: any) => {
    return request.get(`/api/v1/gateway/${params.zone}/rewrite/response/rules`, params)
  },
  xhrZoneAdd: (params: any) => {
    return request.put('/api/v1/gateway/zone', params)
  },
  xhrZoneUpdate: (params: any) => {
    return request.post(`/api/v1/gateway/zone/${params.zone}`, params)
  },
  xhrZoneRemove: (params: any) => {
    return request.delete(`/api/v1/gateway/zone/${params.zone}`)
  },
  xhrRouteAdd: (params: any) => {
    return request.put(`/api/v1/gateway/${params.zone}/route`, params)
  },
  xhrRouteUpdate: (params: any) => {
    return request.post(`/api/v1/gateway/${params.zone}/route/${params.id}`, params)
  },
  xhrRouteRemove: (params: any) => {
    return request.delete(`/api/v1/gateway/${params.zone}/route/${params.id}`)
  },
  xhrUpstreamAdd: (params: any) => {
    return request.put(`/api/v1/gateway/${params.zone}/upstream`, params)
  },
  xhrUpstreamUpdate: (params: any) => {
    return request.post(`/api/v1/gateway/${params.zone}/upstream/${params.id}`, params)
  },
  xhrUpstreamRemove: (params: any) => {
    return request.delete(`/api/v1/gateway/${params.zone}/upstream/${params.id}`)
  },
  xhrWafRuleAdd: (params: any) => {
    return request.put(`/api/v1/gateway/${params.zone}/waf/rule`, params)
  },
  xhrWafRuleUpdate: (params: any) => {
    return request.post(`/api/v1/gateway/${params.zone}/waf/rule/${params.id}`, params)
  },
  xhrWafRuleRemove: (params: any) => {
    return request.delete(`/api/v1/gateway/${params.zone}/waf/rule/${params.id}`)
  },
  xhrCacheRuleAdd: (params: any) => {
    return request.put(`/api/v1/gateway/${params.zone}/cache/rule`, params)
  },
  xhrCacheRuleUpdate: (params: any) => {
    return request.post(`/api/v1/gateway/${params.zone}/cache/rule/${params.id}`, params)
  },
  xhrCacheRuleRemove: (params: any) => {
    return request.delete(`/api/v1/gateway/${params.zone}/cache/rule/${params.id}`)
  },
  xhrLimitRuleAdd: (params: any) => {
    return request.put(`/api/v1/gateway/${params.zone}/limit/rule`, params)
  },
  xhrLimitRuleUpdate: (params: any) => {
    return request.post(`/api/v1/gateway/${params.zone}/limit/rule/${params.id}`, params)
  },
  xhrLimitRuleRemove: (params: any) => {
    return request.delete(`/api/v1/gateway/${params.zone}/limit/rule/${params.id}`)
  },
  xhrRewriteRedirectAdd: (params: any) => {
    return request.put(`/api/v1/gateway/${params.zone}/rewrite/redirect/rule`, params)
  },
  xhrRewriteRedirectUpdate: (params: any) => {
    return request.post(`/api/v1/gateway/${params.zone}/rewrite/redirect/rule/${params.id}`, params)
  },
  xhrRewriteRedirectRemove: (params: any) => {
    return request.delete(`/api/v1/gateway/${params.zone}/rewrite/redirect/rule/${params.id}`)
  },
  xhrRewriteRequestAdd: (params: any) => {
    return request.put(`/api/v1/gateway/${params.zone}/rewrite/request/rule`, params)
  },
  xhrRewriteRequestUpdate: (params: any) => {
    return request.post(`/api/v1/gateway/${params.zone}/rewrite/request/rule/${params.id}`, params)
  },
  xhrRewriteResponseAdd: (params: any) => {
    return request.put(`/api/v1/gateway/${params.zone}/rewrite/response/rule`, params)
  },
  xhrRewriteResponseUpdate: (params: any) => {
    return request.post(`/api/v1/gateway/${params.zone}/rewrite/response/rule/${params.id}`, params)
  }
}
