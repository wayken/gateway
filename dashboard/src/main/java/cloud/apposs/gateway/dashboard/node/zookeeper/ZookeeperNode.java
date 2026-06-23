package cloud.apposs.gateway.dashboard.node.zookeeper;

import cloud.apposs.gateway.GatewayConstants;
import cloud.apposs.gateway.dashboard.node.INode;
import cloud.apposs.gateway.global.certificate.CertificateConstant;
import cloud.apposs.gateway.global.ips.IpAddressConstant;
import cloud.apposs.gateway.plugin.runner.cache.CacheConstant;
import cloud.apposs.gateway.plugin.runner.limit.LimitConstant;
import cloud.apposs.gateway.plugin.runner.redirect.RedirectConstant;
import cloud.apposs.gateway.plugin.runner.rewrite.RewriteConstant;
import cloud.apposs.gateway.plugin.runner.waf.WafConstant;
import cloud.apposs.gateway.route.RouteConstant;
import cloud.apposs.gateway.upstream.UpstreamConstant;
import cloud.apposs.gateway.util.GatewayUtil;
import cloud.apposs.gateway.watch.Node;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.util.JsonUtil;
import cloud.apposs.util.Param;
import cloud.apposs.util.Table;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 基于Zookeeper配置中心的节点操作
 */
public class ZookeeperNode implements INode {
    private final String path;

    private final String globalPath;

    private final String zonesPath;

    private final CuratorFramework zkClient;

    private final Charset charset;

    public ZookeeperNode(String zkServers, int connectTimeout,
                         int sessionTimeout, String path, Charset charset) throws Exception {
        this.path = path;
        this.globalPath = GatewayUtil.getGlobalPath(path);
        this.zonesPath = GatewayUtil.getZonesPath(path);
        this.charset = charset;
        // 创建Zookeeper客户端
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.zkClient = CuratorFrameworkFactory.builder()
                .connectString(zkServers)
                .sessionTimeoutMs(sessionTimeout)
                .connectionTimeoutMs(connectTimeout)
                .retryPolicy(retryPolicy)
                .build();
        this.zkClient.start();
        handleZkPathsInit();
    }

    @Override
    public Param getZoneInfo(String zone) throws Exception {
        String zonePath = zonesPath + "/" + zone;
        Param infomation = Param.builder(Zone.KEY_ID, zone);
        Param setting = null;
        String settingPath = zonePath + Node.SETTING.getPath() + "/" + GatewayConstants.GATEWAY_SEETING_PATH_ZONE;
        if (zkClient.checkExists().forPath(settingPath) != null) {
            byte[] data = zkClient.getData().forPath(settingPath);
            setting = JsonUtil.parseJsonParam(data, charset);
            if (setting != null) {
                infomation.putAll(setting);
            }
        }
        if (!infomation.containsKey(Zone.KEY_STATUS)) {
            infomation.put(Zone.KEY_STATUS, Zone.STATUS_ENABLE);
        }
        return infomation;
    }

    @Override
    public boolean isZoneExist(String name) throws Exception {
        String zonePath = zonesPath + "/" + name;
        return zkClient.checkExists().forPath(zonePath) != null;
    }

    @Override
    public boolean addZone(String id, String name, List<String> match, int status, String remark) throws Exception {
        String zonePath = zonesPath + "/" + id;
        zkClient.create().creatingParentsIfNeeded().forPath(zonePath);
        handleZkZoneInit(id);
        Param metadata = Param.builder(Zone.KEY_NAME, name)
                .setList(Zone.KEY_MATCH, match)
                .setInt(Zone.KEY_STATUS, status)
                .setString(Zone.KEY_REMARK, remark);
        String settingPath = zonesPath + "/" + id + Node.SETTING.getPath() + "/" + GatewayConstants.GATEWAY_SEETING_PATH_ZONE;
        zkClient.create().creatingParentsIfNeeded().forPath(settingPath, JsonUtil.toJson(metadata).getBytes(charset));
        return true;
    }

    @Override
    public boolean updateZone(String id, String name, List<String> match, int status, String remark)  throws Exception {
        String zonePath = zonesPath + "/" + id;
        if (zkClient.checkExists().forPath(zonePath) == null) {
            return false;
        }
        Param metadata = Param.builder(Zone.KEY_NAME, name)
                .setList(Zone.KEY_MATCH, match)
                .setInt(Zone.KEY_STATUS, status)
                .setString(Zone.KEY_REMARK, remark);
        String settingPath = zonesPath + "/" + id + Node.SETTING.getPath() + "/" + GatewayConstants.GATEWAY_SEETING_PATH_ZONE;
        if (zkClient.checkExists().forPath(settingPath) == null) {
            zkClient.create().creatingParentsIfNeeded().forPath(settingPath, JsonUtil.toJson(metadata).getBytes(charset));
        } else {
            zkClient.setData().forPath(settingPath, JsonUtil.toJson(metadata).getBytes(charset));
        }
        return true;
    }

    @Override
    public boolean deleteZone(String zone) throws Exception {
        String zonePath = zonesPath + "/" + zone;
        if (zkClient.checkExists().forPath(zonePath) == null) {
            return false;
        }
        zkClient.delete().deletingChildrenIfNeeded().forPath(zonePath);
        return true;
    }

    @Override
    public Table<Param> getZoneList() throws Exception {
        // 扫描Zookeeper节点/gateway下所有的Zone节点
        List<String> zonePathList = zkClient.getChildren().forPath(zonesPath);
        Table<Param> zones = new Table<Param>();
        for (String zonePath : zonePathList) {
            Param zone = new Param();
            Param setting = null;
            // 扫描Zookeeper节点/gateway/{zone}/setting/zone下的Zone配置，需要先判断目录是否存在，不存在则创建
            String settingPath = zonesPath + "/" + zonePath + Node.SETTING.getPath() + "/" + GatewayConstants.GATEWAY_SEETING_PATH_ZONE;
            if (zkClient.checkExists().forPath(settingPath) != null) {
                byte[] data = zkClient.getData().forPath(settingPath);
                setting = JsonUtil.parseJsonParam(data, charset);
            }
            zone.put(Zone.KEY_ID, zonePath);
            if (setting != null) {
                zone.putAll(setting);
            }
            if (!zone.containsKey(Zone.KEY_STATUS)) {
                zone.setInt(Zone.KEY_STATUS, Zone.STATUS_ENABLE);
            }
            zones.add(zone);
        }
        return zones;
    }

    @Override
    public Table<Param> getServiceList() throws Exception {
        Table<Param> services = new Table<Param>();
        return services;
    }

    @Override
    public Param getIpInfo(String id) throws Exception {
        String path = globalPath + Node.GLOBAL_IPS.getPath() + "/" + id;
        if (zkClient.checkExists().forPath(path) == null) {
            return null;
        }
        byte[] data = zkClient.getData().forPath(path);
        Param infomation = JsonUtil.parseJsonParam(data, charset);
        infomation.put(IpAddressConstant.KEY_ID, id);
        return infomation;
    }

    @Override
    public Table<Param> getIpList() throws Exception {
        List<String> pathList = zkClient.getChildren().forPath(globalPath + Node.GLOBAL_IPS.getPath());
        Table<Param> dataList = new Table<Param>();
        for (String path : pathList) {
            byte[] data = zkClient.getData().forPath(globalPath + Node.GLOBAL_IPS.getPath() + "/" + path);
            Param infomation = JsonUtil.parseJsonParam(data, charset);
            infomation.put(IpAddressConstant.KEY_ID, path);
            dataList.add(infomation);
        }
        return dataList;
    }

    @Override
    public boolean addIp(String id, Param infomation) throws Exception {
        String path = globalPath + Node.GLOBAL_IPS.getPath() + "/" + id;
        if (zkClient.checkExists().forPath(path) != null) {
            return false;
        }
        zkClient.create().creatingParentsIfNeeded().forPath(path, JsonUtil.toJson(infomation).getBytes(charset));
        return true;
    }

    @Override
    public boolean updateIp(String id, Param infomation) throws Exception {
        String path = globalPath + Node.GLOBAL_IPS.getPath() + "/" + id;
        if (zkClient.checkExists().forPath(path) == null) {
            return false;
        }
        zkClient.setData().forPath(path, JsonUtil.toJson(infomation).getBytes(charset));
        return true;
    }

    @Override
    public boolean deleteIp(String id) throws Exception {
        String path = globalPath + Node.GLOBAL_IPS.getPath() + "/" + id;
        if (zkClient.checkExists().forPath(path) == null) {
            return false;
        }
        zkClient.delete().forPath(path);
        return true;
    }

    @Override
    public Param getCertificateInfo(String id) throws Exception {
        String path = globalPath + Node.GLOBAL_CERTIFICATES.getPath() + "/" + id;
        if (zkClient.checkExists().forPath(path) == null) {
            return null;
        }
        byte[] data = zkClient.getData().forPath(path);
        Param infomation = JsonUtil.parseJsonParam(data, charset);
        infomation.put(CertificateConstant.KEY_ID, id);
        return infomation;
    }

    @Override
    public Table<Param> getCertificateList() throws Exception {
        List<String> pathList = zkClient.getChildren().forPath(globalPath + Node.GLOBAL_CERTIFICATES.getPath());
        Table<Param> dataList = new Table<Param>();
        for (String path : pathList) {
            byte[] data = zkClient.getData().forPath(globalPath + Node.GLOBAL_CERTIFICATES.getPath() + "/" + path);
            Param infomation = JsonUtil.parseJsonParam(data, charset);
            infomation.put(CertificateConstant.KEY_ID, path);
            dataList.add(infomation);
        }
        return dataList;
    }

    @Override
    public boolean addCertificate(String id, Param infomation) throws Exception {
        String path = globalPath + Node.GLOBAL_CERTIFICATES.getPath() + "/" + id;
        if (zkClient.checkExists().forPath(path) != null) {
            return false;
        }
        zkClient.create().creatingParentsIfNeeded().forPath(path, JsonUtil.toJson(infomation).getBytes(charset));
        return true;
    }

    @Override
    public boolean updateCertificate(String id, Param infomation) throws Exception {
        String path = globalPath + Node.GLOBAL_CERTIFICATES.getPath() + "/" + id;
        if (zkClient.checkExists().forPath(path) == null) {
            return false;
        }
        zkClient.setData().forPath(path, JsonUtil.toJson(infomation).getBytes(charset));
        return true;
    }

    @Override
    public boolean deleteCertificate(String id) throws Exception {
        String path = globalPath + Node.GLOBAL_CERTIFICATES.getPath() + "/" + id;
        if (zkClient.checkExists().forPath(path) == null) {
            return false;
        }
        zkClient.delete().forPath(path);
        return true;
    }

    @Override
    public Param getAuthInfo(String id) throws Exception {
        String path = globalPath + Node.GLOBAL_AUTHS.getPath() + "/" + id;
        if (zkClient.checkExists().forPath(path) == null) {
            return null;
        }
        byte[] data = zkClient.getData().forPath(path);
        Param infomation = JsonUtil.parseJsonParam(data, charset);
        infomation.put(IpAddressConstant.KEY_ID, id);
        return infomation;
    }

    @Override
    public Table<Param> getAuthList() throws Exception {
        List<String> pathList = zkClient.getChildren().forPath(globalPath + Node.GLOBAL_AUTHS.getPath());
        Table<Param> dataList = new Table<Param>();
        for (String path : pathList) {
            byte[] data = zkClient.getData().forPath(globalPath + Node.GLOBAL_AUTHS.getPath() + "/" + path);
            Param infomation = JsonUtil.parseJsonParam(data, charset);
            infomation.put(IpAddressConstant.KEY_ID, path);
            dataList.add(infomation);
        }
        return dataList;
    }

    @Override
    public boolean addAuth(String id, Param infomation) throws Exception {
        String path = globalPath + Node.GLOBAL_AUTHS.getPath() + "/" + id;
        if (zkClient.checkExists().forPath(path) != null) {
            return false;
        }
        zkClient.create().creatingParentsIfNeeded().forPath(path, JsonUtil.toJson(infomation).getBytes(charset));
        return true;
    }

    @Override
    public boolean updateAuth(String id, Param infomation) throws Exception {
        String path = globalPath + Node.GLOBAL_AUTHS.getPath() + "/" + id;
        if (zkClient.checkExists().forPath(path) == null) {
            return false;
        }
        zkClient.setData().forPath(path, JsonUtil.toJson(infomation).getBytes(charset));
        return true;
    }

    @Override
    public boolean deleteAuth(String id) throws Exception {
        String path = globalPath + Node.GLOBAL_AUTHS.getPath() + "/" + id;
        if (zkClient.checkExists().forPath(path) == null) {
            return false;
        }
        zkClient.delete().forPath(path);
        return true;
    }

    @Override
    public Param getProviderInfo(String id) throws Exception {
        String path = globalPath + Node.GLOBAL_PROVIDERS.getPath() + "/" + id;
        if (zkClient.checkExists().forPath(path) == null) {
            return null;
        }
        byte[] data = zkClient.getData().forPath(path);
        Param infomation = JsonUtil.parseJsonParam(data, charset);
        infomation.put(IpAddressConstant.KEY_ID, id);
        return infomation;
    }

    @Override
    public Table<Param> getProviderList() throws Exception {
        List<String> pathList = zkClient.getChildren().forPath(globalPath + Node.GLOBAL_PROVIDERS.getPath());
        Table<Param> dataList = new Table<Param>();
        for (String path : pathList) {
            byte[] data = zkClient.getData().forPath(globalPath + Node.GLOBAL_PROVIDERS.getPath() + "/" + path);
            Param infomation = JsonUtil.parseJsonParam(data, charset);
            infomation.put(IpAddressConstant.KEY_ID, path);
            dataList.add(infomation);
        }
        return dataList;
    }

    @Override
    public boolean addProvider(String id, Param infomation) throws Exception {
        String path = globalPath + Node.GLOBAL_PROVIDERS.getPath() + "/" + id;
        if (zkClient.checkExists().forPath(path) != null) {
            return false;
        }
        zkClient.create().creatingParentsIfNeeded().forPath(path, JsonUtil.toJson(infomation).getBytes(charset));
        return true;
    }

    @Override
    public boolean updateProvider(String id, Param infomation) throws Exception {
        String path = globalPath + Node.GLOBAL_PROVIDERS.getPath() + "/" + id;
        CuratorZookeeperClient zookeeperClient = zkClient.getZookeeperClient();
        zkClient.newNamespaceAwareEnsurePath(path).ensure(zookeeperClient);
        zkClient.setData().forPath(path, JsonUtil.toJson(infomation).getBytes(charset));
        return true;
    }

    @Override
    public boolean deleteProvider(String id) throws Exception {
        String path = globalPath + Node.GLOBAL_PROVIDERS.getPath() + "/" + id;
        if (zkClient.checkExists().forPath(path) == null) {
            return false;
        }
        zkClient.delete().forPath(path);
        return true;
    }

    @Override
    public Table<Param> getRouteList(String zone) throws Exception {
        // 扫描Zookeeper节点/gateway/routes下的所有路由配置
        String routePath = zonesPath + "/" + zone + Node.ROUTES.getPath();
        List<String> routePathList = zkClient.getChildren().forPath(routePath);
        Table<Param> routeTable = new Table<Param>();
        for (String path : routePathList) {
            byte[] data = zkClient.getData().forPath(routePath + "/" + path);
            Param route = JsonUtil.parseJsonParam(data, charset);
            route.put(RouteConstant.KEY_ID, path);
            routeTable.add(route);
        }
        return routeTable;
    }

    @Override
    public Param getRouteInfo(String zone, String routeId) throws Exception {
        // 扫描Zookeeper指定节点/gateway/routes/{routeId}下的路由配置
        String routePath = zonesPath + "/" + zone + Node.ROUTES.getPath() + "/" + routeId;
        if (zkClient.checkExists().forPath(routePath) == null) {
            return null;
        }
        byte[] data = zkClient.getData().forPath(routePath);
        Param route = JsonUtil.parseJsonParam(data, charset);
        route.put(RouteConstant.KEY_ID, routeId);
        return route;
    }

    @Override
    public boolean addRoute(String zone, String routeId, Param route) throws Exception {
        // 添加路由配置到Zookeeper节点/gateway/routes/{routeId}
        String routePath = zonesPath + "/" + zone + Node.ROUTES.getPath() + "/" + routeId;
        // 路由ID已经存在则不允许添加
        if (zkClient.checkExists().forPath(routePath) != null) {
            return false;
        }
        zkClient.create().creatingParentsIfNeeded().forPath(routePath, route.toJson().getBytes(charset));
        return true;
    }

    @Override
    public boolean updateRoute(String zone, String routeId, Param route) throws Exception {
        // 更新路由配置到Zookeeper节点/gateway/routes/{routeId}
        String routePath = zonesPath + "/" + zone + Node.ROUTES.getPath() + "/" + routeId;
        // 路由ID不存在则不允许更新
        if (zkClient.checkExists().forPath(routePath) == null) {
            return false;
        }
        zkClient.setData().forPath(routePath, route.toJson().getBytes(charset));
        return true;
    }

    @Override
    public boolean removeRoute(String zone, String routeId) throws Exception {
        // 删除Zookeeper节点/gateway/routes/{routeId}
        String routePath = zonesPath + "/" + zone + Node.ROUTES.getPath() + "/" + routeId;
        // 路由ID不存在则直接返回删除成功
        if (zkClient.checkExists().forPath(routePath) == null) {
            return true;
        }
        zkClient.delete().forPath(routePath);
        return true;
    }

    @Override
    public Table<Param> getUpstreamList(String zone) throws Exception {
        // 扫描Zookeeper节点/gateway/upstreams下的所有上游配置
        String upstreamPath = zonesPath + "/" + zone + Node.UPSTREAMS.getPath();
        List<String> upstreamPathList = zkClient.getChildren().forPath(upstreamPath);
        Table<Param> upstreamTable = new Table<Param>();
        for (String path : upstreamPathList) {
            byte[] data = zkClient.getData().forPath(upstreamPath + "/" + path);
            Param upstream = JsonUtil.parseJsonParam(data, charset);
            upstream.put(UpstreamConstant.KEY_ID, path);
            upstreamTable.add(upstream);
        }
        return upstreamTable;
    }

    @Override
    public Param getUpstreamInfo(String zone, String upstreamId) throws Exception {
        // 扫描Zookeeper指定节点/gateway/upstreams/{upstreamId}下的上游配置
        String upstreamPath = zonesPath + "/" + zone + Node.UPSTREAMS.getPath() + "/" + upstreamId;
        if (zkClient.checkExists().forPath(upstreamPath) == null) {
            return null;
        }
        byte[] data = zkClient.getData().forPath(upstreamPath);
        Param upstream = JsonUtil.parseJsonParam(data, charset);
        upstream.put(UpstreamConstant.KEY_ID, upstreamId);
        return upstream;
    }

    @Override
    public boolean addUpstream(String zone, String upstreamId, Param upstream) throws Exception {
        // 添加上游配置到Zookeeper节点/gateway/upstreams/{upstreamId}
        String upstreamPath = zonesPath + "/" + zone + Node.UPSTREAMS.getPath() + "/" + upstreamId;
        // 上游ID已经存在则不允许添加
        if (zkClient.checkExists().forPath(upstreamPath) != null) {
            return false;
        }
        zkClient.create().creatingParentsIfNeeded().forPath(upstreamPath, upstream.toJson().getBytes(charset));
        return true;
    }

    @Override
    public boolean updateUpstream(String zone, String upstreamId, Param upstream) throws Exception {
        // 更新上游配置到Zookeeper节点/gateway/upstreams/{upstreamId}
        String upstreamPath = zonesPath + "/" + zone + Node.UPSTREAMS.getPath() + "/" + upstreamId;
        // 上游ID不存在则不允许更新
        if (zkClient.checkExists().forPath(upstreamPath) == null) {
            return false;
        }
        zkClient.setData().forPath(upstreamPath, upstream.toJson().getBytes(charset));
        return true;
    }

    @Override
    public boolean removeUpstream(String zone, String upstreamId) throws Exception {
        // 删除Zookeeper节点/gateway/upstreams/{upstreamId}
        String upstreamPath = zonesPath + "/" + zone + Node.UPSTREAMS.getPath() + "/" + upstreamId;
        // 上游ID不存在则直接返回删除成功
        if (zkClient.checkExists().forPath(upstreamPath) == null) {
            return true;
        }
        zkClient.delete().forPath(upstreamPath);
        return true;
    }

    @Override
    public Param getWafRuleInfo(String zone, String id) throws Exception {
        // 扫描Zookeeper指定节点/gateway/{zone}/waf/default/{id}下的WAF规则配置
        String wafRulePath = zonesPath + "/" + zone + WafConstant.DEFAULT_WAF_PATH + "/" + id;
        if (zkClient.checkExists().forPath(wafRulePath) == null) {
            return null;
        }
        byte[] data = zkClient.getData().forPath(wafRulePath);
        Param wafRule = JsonUtil.parseJsonParam(data, charset);
        wafRule.put(WafConstant.KEY_ID, id);
        return wafRule;
    }

    @Override
    public Table<Param> getWafRuleList(String zone) throws Exception {
        String wafRulePath = zonesPath + "/" + zone + WafConstant.DEFAULT_WAF_PATH;
        Table<Param> dataList = new Table<Param>();
        if (zkClient.checkExists().forPath(wafRulePath) == null) {
            return dataList;
        }
        List<String> wafRulePathList = zkClient.getChildren().forPath(wafRulePath);
        for (String path : wafRulePathList) {
            byte[] data = zkClient.getData().forPath(wafRulePath + "/" + path);
            Param wafRule = JsonUtil.parseJsonParam(data, charset);
            wafRule.put(WafConstant.KEY_ID, path);
            dataList.add(wafRule);
        }
        return dataList;
    }

    @Override
    public boolean addWafRule(String zone, String id, Param data) throws Exception {
        // 添加上游配置到Zookeeper节点/gateway/{zone}/waf/default/{id}
        String wafRulePath = zonesPath + "/" + zone + WafConstant.DEFAULT_WAF_PATH + "/" + id;
        // 上游ID已经存在则不允许添加
        if (zkClient.checkExists().forPath(wafRulePath) != null) {
            return false;
        }
        zkClient.create().creatingParentsIfNeeded().forPath(wafRulePath, data.toJson().getBytes(charset));
        return true;
    }

    @Override
    public boolean updateWafRule(String zone, String id, Param data) throws Exception {
        // 更新上游配置到Zookeeper节点/gateway/{zone}/waf/default/{id}
        String wafRulePath = zonesPath + "/" + zone + WafConstant.DEFAULT_WAF_PATH + "/" + id;
        // 上游ID不存在则不允许更新
        if (zkClient.checkExists().forPath(wafRulePath) == null) {
            return false;
        }
        zkClient.setData().forPath(wafRulePath, data.toJson().getBytes(charset));
        return true;
    }

    @Override
    public boolean removeWafRule(String zone, String id) throws Exception {
        // 删除Zookeeper节点/gateway/{zone}/waf/default/{id}
        String wafRulePath = zonesPath + "/" + zone + WafConstant.DEFAULT_WAF_PATH + "/" + id;
        // 上游ID不存在则直接返回删除成功
        if (zkClient.checkExists().forPath(wafRulePath) == null) {
            return true;
        }
        zkClient.delete().forPath(wafRulePath);
        return true;
    }

    @Override
    public Param getLimitRuleInfo(String zone, String id) throws Exception {
        // 扫描Zookeeper指定节点/gateway/{zone}/limit/{id}下的限流规则配置
        String limitRulePath = zonesPath + "/" + zone + LimitConstant.DEFAULT_LIMIT_PATH + "/" + id;
        if (zkClient.checkExists().forPath(limitRulePath) == null) {
            return null;
        }
        byte[] data = zkClient.getData().forPath(limitRulePath);
        Param limitRule = JsonUtil.parseJsonParam(data, charset);
        limitRule.put(WafConstant.KEY_ID, id);
        return limitRule;
    }

    @Override
    public Table<Param> getLimitRuleList(String zone) throws Exception {
        String limitRulePath = zonesPath + "/" + zone + LimitConstant.DEFAULT_LIMIT_PATH;
        Table<Param> dataList = new Table<Param>();
        if (zkClient.checkExists().forPath(limitRulePath) == null) {
            return dataList;
        }
        List<String> limitRulePathList = zkClient.getChildren().forPath(limitRulePath);
        for (String path : limitRulePathList) {
            byte[] data = zkClient.getData().forPath(limitRulePath + "/" + path);
            Param rule = JsonUtil.parseJsonParam(data, charset);
            rule.put(WafConstant.KEY_ID, path);
            dataList.add(rule);
        }
        return dataList;
    }

    @Override
    public boolean addLimitRule(String zone, String id, Param data) throws Exception {
        // 添加配置到Zookeeper节点/gateway/{zone}/limit/{id}
        String limitRulePath = zonesPath + "/" + zone + LimitConstant.DEFAULT_LIMIT_PATH + "/" + id;
        // 规则ID已经存在则不允许添加
        if (zkClient.checkExists().forPath(limitRulePath) != null) {
            return false;
        }
        zkClient.create().creatingParentsIfNeeded().forPath(limitRulePath, data.toJson().getBytes(charset));
        return true;
    }

    @Override
    public boolean updateLimitRule(String zone, String id, Param infomation) throws Exception {
        // 更新配置到Zookeeper节点/gateway/{zone}/limit/{id}
        String limitRulePath = zonesPath + "/" + zone + LimitConstant.DEFAULT_LIMIT_PATH + "/" + id;
        // 规则ID不存在则不允许更新
        if (zkClient.checkExists().forPath(limitRulePath) == null) {
            return false;
        }
        zkClient.setData().forPath(limitRulePath, infomation.toJson().getBytes(charset));
        return true;
    }

    @Override
    public boolean removeLimitRule(String zone, String id) throws Exception {
        // 删除Zookeeper节点/gateway/{zone}/limit/{id}
        String limitRulePath = zonesPath + "/" + zone + LimitConstant.DEFAULT_LIMIT_PATH + "/" + id;
        // 规则ID不存在则直接返回删除成功
        if (zkClient.checkExists().forPath(limitRulePath) == null) {
            return true;
        }
        zkClient.delete().forPath(limitRulePath);
        return true;
    }

    @Override
    public Param getCacheRuleInfo(String zone, String id) throws Exception {
        // 扫描Zookeeper指定节点/gateway/{zone}/cache/{id}下的缓存规则配置
        String cacheRulePath = zonesPath + "/" + zone + CacheConstant.DEFAULT_CACHE_PATH + "/" + id;
        if (zkClient.checkExists().forPath(cacheRulePath) == null) {
            return null;
        }
        byte[] data = zkClient.getData().forPath(cacheRulePath);
        Param cacheRule = JsonUtil.parseJsonParam(data, charset);
        cacheRule.put(CacheConstant.KEY_ID, id);
        return cacheRule;
    }

    @Override
    public Table<Param> getCacheRuleList(String zone) throws Exception {
        String cacheRulePath = zonesPath + "/" + zone + CacheConstant.DEFAULT_CACHE_PATH;
        Table<Param> dataList = new Table<Param>();
        if (zkClient.checkExists().forPath(cacheRulePath) == null) {
            return dataList;
        }
        List<String> cacheRulePathList = zkClient.getChildren().forPath(cacheRulePath);
        for (String path : cacheRulePathList) {
            byte[] data = zkClient.getData().forPath(cacheRulePath + "/" + path);
            Param cacheRule = JsonUtil.parseJsonParam(data, charset);
            cacheRule.put(CacheConstant.KEY_ID, path);
            dataList.add(cacheRule);
        }
        return dataList;
    }

    @Override
    public boolean addCacheRule(String zone, String id, Param data) throws Exception {
        // 添加上游配置到Zookeeper节点/gateway/{zone}/cache/default/{id}
        String cacheRulePath = zonesPath + "/" + zone + CacheConstant.DEFAULT_CACHE_PATH + "/" + id;
        // 上游ID已经存在则不允许添加
        if (zkClient.checkExists().forPath(cacheRulePath) != null) {
            return false;
        }
        zkClient.create().creatingParentsIfNeeded().forPath(cacheRulePath, data.toJson().getBytes(charset));
        return true;
    }

    @Override
    public boolean updateCacheRule(String zone, String id, Param data) throws Exception {
        // 更新上游配置到Zookeeper节点/gateway/{zone}/cache/default/{id}
        String cacheRulePath = zonesPath + "/" + zone + CacheConstant.DEFAULT_CACHE_PATH + "/" + id;
        // 上游ID不存在则不允许更新
        if (zkClient.checkExists().forPath(cacheRulePath) == null) {
            return false;
        }
        zkClient.setData().forPath(cacheRulePath, data.toJson().getBytes(charset));
        return true;
    }

    @Override
    public boolean removeCacheRule(String zone, String id) throws Exception {
        // 删除Zookeeper节点/gateway/{zone}/cache/default/{id}
        String cacheRulePath = zonesPath + "/" + zone + CacheConstant.DEFAULT_CACHE_PATH + "/" + id;
        // 上游ID不存在则直接返回删除成功
        if (zkClient.checkExists().forPath(cacheRulePath) == null) {
            return true;
        }
        zkClient.delete().forPath(cacheRulePath);
        return true;
    }

    @Override
    public Param getRedirectRuleInfo(String zone, String id) throws Exception {
        // 扫描Zookeeper指定节点/gateway/{zone}/redirect/{id}下的重定向规则配置
        String redirectRulePath = zonesPath + "/" + zone + RedirectConstant.DEFAULT_REDIRECT_PATH + "/" + id;
        if (zkClient.checkExists().forPath(redirectRulePath) == null) {
            return null;
        }
        byte[] data = zkClient.getData().forPath(redirectRulePath);
        Param redirectRule = JsonUtil.parseJsonParam(data, charset);
        redirectRule.put(CacheConstant.KEY_ID, id);
        return redirectRule;
    }

    @Override
    public Table<Param> getRedirectRuleList(String zone) throws Exception {
        String redirectRulePath = zonesPath + "/" + zone + RedirectConstant.DEFAULT_REDIRECT_PATH;
        Table<Param> dataList = new Table<Param>();
        if (zkClient.checkExists().forPath(redirectRulePath) == null) {
            return dataList;
        }
        List<String> redirectRulePathList = zkClient.getChildren().forPath(redirectRulePath);
        for (String path : redirectRulePathList) {
            byte[] data = zkClient.getData().forPath(redirectRulePath + "/" + path);
            Param redirectRule = JsonUtil.parseJsonParam(data, charset);
            redirectRule.put(RedirectConstant.KEY_ID, path);
            dataList.add(redirectRule);
        }
        return dataList;
    }

    @Override
    public boolean addRedirectRule(String zone, String id, Param data) throws Exception {
        // 添加上游配置到Zookeeper节点/gateway/{zone}/redirect/default/{id}
        String redirectRulePath = zonesPath + "/" + zone + RedirectConstant.DEFAULT_REDIRECT_PATH + "/" + id;
        // 上游ID已经存在则不允许添加
        if (zkClient.checkExists().forPath(redirectRulePath) != null) {
            return false;
        }
        zkClient.create().creatingParentsIfNeeded().forPath(redirectRulePath, data.toJson().getBytes(charset));
        return true;
    }

    @Override
    public boolean updateRedirectRule(String zone, String id, Param data) throws Exception {
        // 更新上游配置到Zookeeper节点/gateway/{zone}/redirect/default/{id}
        String redirectRulePath = zonesPath + "/" + zone + RedirectConstant.DEFAULT_REDIRECT_PATH + "/" + id;
        // 上游ID不存在则不允许更新
        if (zkClient.checkExists().forPath(redirectRulePath) == null) {
            return false;
        }
        zkClient.setData().forPath(redirectRulePath, data.toJson().getBytes(charset));
        return true;
    }

    @Override
    public boolean removeRedirectRule(String zone, String id) throws Exception {
        // 删除Zookeeper节点/gateway/{zone}/redirect/default/{id}
        String redirectRulePath = zonesPath + "/" + zone + RedirectConstant.DEFAULT_REDIRECT_PATH + "/" + id;
        // 上游ID不存在则直接返回删除成功
        if (zkClient.checkExists().forPath(redirectRulePath) == null) {
            return true;
        }
        zkClient.delete().forPath(redirectRulePath);
        return true;
    }

    @Override
    public Param getRewriteRequestRuleInfo(String zone, String id) throws Exception {
        String rewriteRulePath = zonesPath + "/" + zone + RewriteConstant.REWRITE_REQUEST_PATH + "/" + id;
        if (zkClient.checkExists().forPath(rewriteRulePath) == null) {
            return null;
        }
        byte[] data = zkClient.getData().forPath(rewriteRulePath);
        Param rule = JsonUtil.parseJsonParam(data, charset);
        rule.put(WafConstant.KEY_ID, id);
        return rule;
    }

    @Override
    public Table<Param> getRewriteRequestRuleList(String zone) throws Exception {
        String rewriteRulePath = zonesPath + "/" + zone + RewriteConstant.REWRITE_REQUEST_PATH;
        Table<Param> dataList = new Table<Param>();
        if (zkClient.checkExists().forPath(rewriteRulePath) == null) {
            return dataList;
        }
        List<String> rewriteRulePathList = zkClient.getChildren().forPath(rewriteRulePath);
        for (String path : rewriteRulePathList) {
            byte[] data = zkClient.getData().forPath(rewriteRulePath + "/" + path);
            Param rule = JsonUtil.parseJsonParam(data, charset);
            rule.put(WafConstant.KEY_ID, path);
            dataList.add(rule);
        }
        return dataList;
    }

    @Override
    public boolean addRewriteRequestRule(String zone, String id, Param data) throws Exception {
        // 添加上游配置到Zookeeper节点/gateway/{zone}/rewrite/request/{id}
        String rewriteRulePath = zonesPath + "/" + zone + RewriteConstant.REWRITE_REQUEST_PATH + "/" + id;
        // 上游ID已经存在则不允许添加
        if (zkClient.checkExists().forPath(rewriteRulePath) != null) {
            return false;
        }
        zkClient.create().creatingParentsIfNeeded().forPath(rewriteRulePath, data.toJson().getBytes(charset));
        return true;
    }

    @Override
    public boolean updateRewriteRequestRule(String zone, String id, Param data) throws Exception {
        // 更新上游配置到Zookeeper节点/gateway/{zone}/rewrite/request/{id}
        String rewriteRulePath = zonesPath + "/" + zone + RewriteConstant.REWRITE_REQUEST_PATH + "/" + id;
        // 上游ID不存在则不允许更新
        if (zkClient.checkExists().forPath(rewriteRulePath) == null) {
            return false;
        }
        zkClient.setData().forPath(rewriteRulePath, data.toJson().getBytes(charset));
        return true;
    }

    @Override
    public boolean removeRewriteRequestRule(String zone, String id) throws Exception {
        // 删除Zookeeper节点/gateway/{zone}/rewrite/request/{id}
        String rewriteRulePath = zonesPath + "/" + zone + RewriteConstant.REWRITE_REQUEST_PATH + "/" + id;
        // 上游ID不存在则直接返回删除成功
        if (zkClient.checkExists().forPath(rewriteRulePath) == null) {
            return true;
        }
        zkClient.delete().forPath(rewriteRulePath);
        return true;
    }

    @Override
    public Param getRewriteResponseRuleInfo(String zone, String id) throws Exception {
        String rewriteRulePath = zonesPath + "/" + zone + RewriteConstant.REWRITE_RESPONSE_PATH + "/" + id;
        if (zkClient.checkExists().forPath(rewriteRulePath) == null) {
            return null;
        }
        byte[] data = zkClient.getData().forPath(rewriteRulePath);
        Param rule = JsonUtil.parseJsonParam(data, charset);
        rule.put(RewriteConstant.KEY_ID, id);
        return rule;
    }

    @Override
    public Table<Param> getRewriteResponseRuleList(String zone) throws Exception {
        String rewriteRulePath = zonesPath + "/" + zone + RewriteConstant.REWRITE_RESPONSE_PATH;
        Table<Param> dataList = new Table<Param>();
        if (zkClient.checkExists().forPath(rewriteRulePath) == null) {
            return dataList;
        }
        List<String> rewriteRulePathList = zkClient.getChildren().forPath(rewriteRulePath);
        for (String path : rewriteRulePathList) {
            byte[] data = zkClient.getData().forPath(rewriteRulePath + "/" + path);
            Param rule = JsonUtil.parseJsonParam(data, charset);
            rule.put(RewriteConstant.KEY_ID, path);
            dataList.add(rule);
        }
        return dataList;
    }

    @Override
    public boolean addRewriteResponseRule(String zone, String id, Param data) throws Exception {
        // 添加上游配置到Zookeeper节点/gateway/{zone}/rewrite/response/{id}
        String rewriteRulePath = zonesPath + "/" + zone + RewriteConstant.REWRITE_RESPONSE_PATH + "/" + id;
        // 上游ID已经存在则不允许添加
        if (zkClient.checkExists().forPath(rewriteRulePath) != null) {
            return false;
        }
        zkClient.create().creatingParentsIfNeeded().forPath(rewriteRulePath, data.toJson().getBytes(charset));
        return true;
    }

    @Override
    public boolean updateRewriteResponseRule(String zone, String id, Param data) throws Exception {
        // 更新上游配置到Zookeeper节点/gateway/{zone}/rewrite/response/{id}
        String rewriteRulePath = zonesPath + "/" + zone + RewriteConstant.REWRITE_RESPONSE_PATH + "/" + id;
        // 上游ID不存在则不允许更新
        if (zkClient.checkExists().forPath(rewriteRulePath) == null) {
            return false;
        }
        zkClient.setData().forPath(rewriteRulePath, data.toJson().getBytes(charset));
        return true;
    }

    @Override
    public boolean removeRewriteResponseRule(String zone, String id) throws Exception {
        // 删除Zookeeper节点/gateway/{zone}/rewrite/response/{id}
        String rewriteRulePath = zonesPath + "/" + zone + RewriteConstant.REWRITE_RESPONSE_PATH + "/" + id;
        // 上游ID不存在则直接返回删除成功
        if (zkClient.checkExists().forPath(rewriteRulePath) == null) {
            return true;
        }
        zkClient.delete().forPath(rewriteRulePath);
        return true;
    }

    @Override
    public Table<Param> getPluginList(String zone) throws Exception {
        String zonePath = zonesPath + "/" + zone;
        String pluginsPath = zonePath + Node.SETTING.getPath() + "/" + GatewayConstants.GATEWAY_SEETING_PATH_PLUGINS;
        Table<Param> plugins = new Table<Param>();
        if (zkClient.checkExists().forPath(pluginsPath) != null) {
            byte[] data = zkClient.getData().forPath(pluginsPath);
            String content = new String(data, charset);
            plugins = JsonUtil.parseJsonTable(content);
        }
        return plugins;
    }

    @Override
    public boolean updatePluginList(String zone, List<Param> plugins) throws Exception {
        String zonePath = zonesPath + "/" + zone;
        String pluginsPath = zonePath + Node.SETTING.getPath() + "/" + GatewayConstants.GATEWAY_SEETING_PATH_PLUGINS;
        zkClient.setData().forPath(pluginsPath, JsonUtil.toJson(plugins).getBytes(charset));
        return true;
    }

    private void handleZkPathsInit() throws Exception {
        // 初始化Global节点
        CuratorZookeeperClient zookeeperClient = zkClient.getZookeeperClient();
        zkClient.newNamespaceAwareEnsurePath(globalPath).ensure(zookeeperClient);
        zkClient.newNamespaceAwareEnsurePath(globalPath + Node.GLOBAL_IPS.getPath()).ensure(zookeeperClient);
        // 初始化Zones节点
        zkClient.newNamespaceAwareEnsurePath(zonesPath).ensure(zookeeperClient);
        // 遍历path下的所有zone目录，如果不存在子节点routes/upstreams则创建
        List<String> zonePathList = zkClient.getChildren().forPath(zonesPath);
        if (zonePathList == null || zonePathList.isEmpty()) {
            // 如果不存在则创建zone.default目录作为默认网域
            zonePathList = new ArrayList<>(Arrays.asList(Zone.DEFAULT_ZONE));
        }
        for (String zonePath : zonePathList) {
            handleZkZoneInit(zonePath);
        }
    }

    private void handleZkZoneInit(String zone) throws Exception {
        String zoneFullPath = zonesPath + "/" + zone;
        zkClient.newNamespaceAwareEnsurePath(zoneFullPath).ensure(zkClient.getZookeeperClient());
        zkClient.newNamespaceAwareEnsurePath(zoneFullPath + Node.SETTING.getPath()).ensure(zkClient.getZookeeperClient());
        zkClient.newNamespaceAwareEnsurePath(zoneFullPath + Node.ROUTES.getPath()).ensure(zkClient.getZookeeperClient());
        zkClient.newNamespaceAwareEnsurePath(zoneFullPath + Node.UPSTREAMS.getPath()).ensure(zkClient.getZookeeperClient());
    }
}
