package cloud.apposs.gateway.dashboard.node;

import cloud.apposs.util.Param;
import cloud.apposs.util.Table;

import java.util.List;

/**
 * 服务节点接口，用于定义节点的基本操作，即配置节点的增删改查操作，
 * 由不同的类实现，如Zookeeper/FS/ETCD，各节点配置规范如下：
 * <pre>
 * 1. 配置路由：/gateway/routes/{routeId}
 * 2. 配置上游：/gateway/upstreams/{upstreamId}
 * </pre>
 */
public interface INode {
    /**
     * 获取网关网域信息
     *
     * @param zone 网域
     */
    Param getZoneInfo(String zone) throws Exception;

    /**
     * 获取网关网域列表
     */
    Table<Param> getZoneList() throws Exception;

    /**
     * 判断网域是否存在
     *
     * @param  name 网域名称
     * @return 存在返回true，否则返回false
     */
    boolean isZoneExist(String name) throws Exception;

    /**
     * 添加网关网域，
     * 规范：在添加网域之后需要同时初始下网域下的routes和upstreams目录
     *
     * @param  id    网域ID
     * @param  name  网域名称
     * @param  match 网域匹配站点
     * @return 添加成功返回true，否则返回false
     */
    boolean addZone(String id, String name, List<String> match, int status, String remark) throws Exception;

    /**
     * 更新网关网域
     *
     * @param  id     网域ID
     * @param  name   网域名称
     * @param  match  网域匹配站点
     * @param  status 网域状态
     * @param  remark 网域备注
     * @return 更新成功返回true，否则返回false
     */
    boolean updateZone(String id, String name, List<String> match, int status, String remark) throws Exception;

    /**
     * 删除网关网域
     *
     * @param  zone 网域
     * @return 删除成功返回true，否则返回false
     */
    boolean deleteZone(String zone) throws Exception;

    /**
     * 获取网关集群列表
     */
    Table<Param> getServiceList() throws Exception;

    /**
     * 获取IP组信息
     *
     * @param id IP组ID
     */
    Param getIpInfo(String id) throws Exception;

    /**
     * 获取网关IP组列表
     */
    Table<Param> getIpList() throws Exception;

    /**
     * 添加网关IP组
     *
     * @param  id         IP组ID
     * @param  infomation IP组数据
     */
    boolean addIp(String id, Param infomation) throws Exception;

    /**
     * 更新网关IP组
     *
     * @param  id         IP组ID
     * @param  infomation IP组数据
     * @return 更新成功返回true，否则返回false
     */
    boolean updateIp(String id, Param infomation) throws Exception;

    /**
     * 删除网关IP组
     *
     * @param id IP组ID
     */
    boolean deleteIp(String id) throws Exception;

    /**
     * 获取证书信息
     *
     * @param id 证书ID
     */
    Param getCertificateInfo(String id) throws Exception;

    /**
     * 获取网关证书列表
     */
    Table<Param> getCertificateList() throws Exception;

    /**
     * 添加网关证书
     *
     * @param  id         证书ID
     * @param  infomation 证书数据
     */
    boolean addCertificate(String id, Param infomation) throws Exception;

    /**
     * 更新网关证书
     *
     * @param  id         证书ID
     * @param  infomation 证书数据
     * @return 更新成功返回true，否则返回false
     */
    boolean updateCertificate(String id, Param infomation) throws Exception;

    /**
     * 删除网关证书
     *
     * @param id 证书ID
     */
    boolean deleteCertificate(String id) throws Exception;

    /**
     * 获取鉴权信息
     *
     * @param id 鉴权ID
     */
    Param getAuthInfo(String id) throws Exception;

    /**
     * 获取网关鉴权列表
     */
    Table<Param> getAuthList() throws Exception;

    /**
     * 添加网关鉴权
     *
     * @param  id         鉴权ID
     * @param  infomation 鉴权数据
     */
    boolean addAuth(String id, Param infomation) throws Exception;

    /**
     * 更新网关鉴权
     *
     * @param  id         鉴权ID
     * @param  infomation 鉴权数据
     * @return 更新成功返回true，否则返回false
     */
    boolean updateAuth(String id, Param infomation) throws Exception;

    /**
     * 删除网关鉴权
     *
     * @param id 鉴权ID
     */
    boolean deleteAuth(String id) throws Exception;

    /**
     * 获取AI服务商信息
     *
     * @param id AI服务商ID
     */
    Param getProviderInfo(String id) throws Exception;

    /**
     * 获取网关AI服务商列表
     */
    Table<Param> getProviderList() throws Exception;

    /**
     * 添加网关证书
     *
     * @param  id         AI服务商ID
     * @param  infomation AI服务商数据
     */
    boolean addProvider(String id, Param infomation) throws Exception;

    /**
     * 更新网关证书
     *
     * @param  id         AI服务商ID
     * @param  infomation AI服务商数据
     * @return 更新成功返回true，否则返回false
     */
    boolean updateProvider(String id, Param infomation) throws Exception;

    /**
     * 删除网关AI服务商
     *
     * @param id AI服务商ID
     */
    boolean deleteProvider(String id) throws Exception;

    /**
     * 获取网关网域列表，
     * 规范：如果网域为空则需要创建默认网域zone.default并返回
     *
     * @param zone 网域
     */
    Table<Param> getRouteList(String zone) throws Exception;

    /**
     * 获取网关上游列表
     *
     * @param  zone    网域
     * @param  routeId 路由ID
     * @return 返回对应的路由配置
     */
    Param getRouteInfo(String zone, String routeId) throws Exception;

    /**
     * 添加网关路由
     *
     * @param  zone  网域
     * @param  route 路由配置
     * @return 添加成功返回true，否则返回false
     */
    boolean addRoute(String zone, String routeId, Param route) throws Exception;

    /**
     * 更新网关路由
     *
     * @param  zone    网域
     * @param  routeId 路由ID
     * @param  route   路由配置
     * @return 更新成功返回true，否则返回false
     */
    boolean updateRoute(String zone, String routeId, Param route) throws Exception;

    /**
     * 删除网关路由
     *
     * @param  zone    网域
     * @param  routeId 路由ID
     * @return 删除成功返回true，否则返回false
     */
    boolean removeRoute(String zone, String routeId) throws Exception;

    /**
     * 获取网关上游列表
     *
     * @param zone 网域
     */
    Table<Param> getUpstreamList(String zone) throws Exception;

    /**
     * 获取网关上游信息
     *
     * @param  zone       网域
     * @param  upstreamId 上游ID
     * @return 返回对应的上游配置
     */
    Param getUpstreamInfo(String zone, String upstreamId) throws Exception;

    /**
     * 添加网关上游
     *
     * @param  zone       网域
     * @param  upstreamId 上游ID
     * @param  upstream   上游配置
     * @return 添加成功返回true，否则返回false
     */
    boolean addUpstream(String zone, String upstreamId, Param upstream) throws Exception;

    /**
     * 更新网关上游
     *
     * @param  zone       网域
     * @param  upstreamId 上游ID
     * @param  upstream   上游配置
     * @return 更新成功返回true，否则返回false
     */
    boolean updateUpstream(String zone, String upstreamId, Param upstream) throws Exception;

    /**
     * 删除网关上游
     *
     * @param  zone       网域
     * @param  upstreamId 上游ID
     * @return 删除成功返回true，否则返回false
     */
    boolean removeUpstream(String zone, String upstreamId) throws Exception;

    /**
     * 获取WAF规则列表
     */
    Table<Param> getWafRuleList(String zone) throws Exception;

    /**
     * 获取WAF规则
     */
    Param getWafRuleInfo(String zone, String id) throws Exception;

    /**
     * 添加WAF规则
     *
     * @param  zone   网域
     * @param  id     规则ID
     * @param  data   规则内容
     */
    boolean addWafRule(String zone, String id, Param data) throws Exception;

    /**
     * 更新WAF规则
     *
     * @param  zone 网域
     * @param  id   规则ID
     * @param  data 规则内容
     */
    boolean updateWafRule(String zone, String id, Param data) throws Exception;

    /**
     * 删除WAF规则
     *
     * @param  zone 网域
     * @param  id   规则ID
     */
    boolean removeWafRule(String zone, String id) throws Exception;

    /**
     * 获取缓存规则列表
     */
    Table<Param> getCacheRuleList(String zone) throws Exception;

    /**
     * 获取缓存规则
     */
    Param getCacheRuleInfo(String zone, String id) throws Exception;

    /**
     * 添加缓存规则
     *
     * @param  zone   网域
     * @param  id     规则ID
     * @param  data   规则内容
     */
    boolean addCacheRule(String zone, String id, Param data) throws Exception;

    /**
     * 更新缓存规则
     *
     * @param  zone 网域
     * @param  id   规则ID
     * @param  data 规则内容
     */
    boolean updateCacheRule(String zone, String id, Param data) throws Exception;

    /**
     * 删除缓存规则
     *
     * @param  zone 网域
     * @param  id   规则ID
     */
    boolean removeCacheRule(String zone, String id) throws Exception;

    /**
     * 获取限流规则
     */
    Param getLimitRuleInfo(String zone, String id) throws Exception;

    /**
     * 获取限流规则列表
     */
    Table<Param> getLimitRuleList(String zone) throws Exception;

    /**
     * 添加限流规则
     *
     * @param  zone 网域
     * @param  id   规则ID
     * @param  data 规则内容
     */
    boolean addLimitRule(String zone, String id, Param data) throws Exception;

    /**
     * 获取限流规则
     */
    boolean updateLimitRule(String zone, String id, Param infomation) throws Exception;

    /**
     * 删除限流规则
     *
     * @param  zone 网域
     * @param  id   规则ID
     */
    boolean removeLimitRule(String zone, String id) throws Exception;

    /**
     * 获取重定向规则列表
     */
    Table<Param> getRedirectRuleList(String zone) throws Exception;

    /**
     * 获取重定向规则
     */
    Param getRedirectRuleInfo(String zone, String id) throws Exception;

    /**
     * 添加重定向规则
     *
     * @param  zone   网域
     * @param  id     规则ID
     * @param  data   规则内容
     */
    boolean addRedirectRule(String zone, String id, Param data) throws Exception;

    /**
     * 更新重定向规则
     *
     * @param  zone 网域
     * @param  id   规则ID
     * @param  data 规则内容
     */
    boolean updateRedirectRule(String zone, String id, Param data) throws Exception;

    /**
     * 删除重定向规则
     *
     * @param  zone 网域
     * @param  id   规则ID
     */
    boolean removeRedirectRule(String zone, String id) throws Exception;

    /**
     * 获取请求重写规则
     */
    Param getRewriteRequestRuleInfo(String zone, String id) throws Exception;

    /**
     * 获取请求重写规则列表
     */
    Table<Param> getRewriteRequestRuleList(String zone) throws Exception;

    /**
     * 添加请求重写规则
     *
     * @param  zone 网域
     * @param  id   规则ID
     * @param  data 规则内容
     */
    boolean addRewriteRequestRule(String zone, String id, Param data) throws Exception;

    /**
     * 更新请求重写规则
     *
     * @param  zone 网域
     * @param  id   规则ID
     * @param  data 规则内容
     */
    boolean updateRewriteRequestRule(String zone, String id, Param data) throws Exception;

    /**
     * 删除请求重写规则
     *
     * @param  zone 网域
     * @param  id   规则ID
     */
    boolean removeRewriteRequestRule(String zone, String id) throws Exception;

    /**
     * 获取响应重写规则
     */
    Param getRewriteResponseRuleInfo(String zone, String id) throws Exception;

    /**
     * 获取响应重写规则列表
     */
    Table<Param> getRewriteResponseRuleList(String zone) throws Exception;

    /**
     * 添加响应重写规则
     *
     * @param  zone 网域
     * @param  id   规则ID
     * @param  data 规则内容
     */
    boolean addRewriteResponseRule(String zone, String id, Param data) throws Exception;

    /**
     * 更新响应重写规则
     *
     * @param  zone 网域
     * @param  id   规则ID
     * @param  data 规则内容
     */
    boolean updateRewriteResponseRule(String zone, String id, Param data) throws Exception;

    /**
     * 删除响应重写规则
     *
     * @param  zone 网域
     * @param  id   规则ID
     */
    boolean removeRewriteResponseRule(String zone, String id) throws Exception;

    /**
     * 获取网域插件列表
     *
     * @param zone 网域
     */
    Table<Param> getPluginList(String zone) throws Exception;

    /**
     * 更新网域插件列表
     *
     * @param zone    网域
     * @param plugins 插件列表
     */
    boolean updatePluginList(String zone, List<Param> plugins) throws Exception;
}
