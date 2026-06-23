package cloud.apposs.gateway.dashboard.node.fs;

import cloud.apposs.gateway.GatewayConstants;
import cloud.apposs.gateway.dashboard.node.INode;
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
import cloud.apposs.util.FileUtil;
import cloud.apposs.util.JsonUtil;
import cloud.apposs.util.Param;
import cloud.apposs.util.Table;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 基于文件系统的节点操作
 */
public class FSNode implements INode {
    private final String rootDir;

    private final String globalDir;

    private final String zonesDir;

    public FSNode(String nodeDir) throws IOException {
        if (nodeDir == null || nodeDir.trim().isEmpty()) {
            throw new IllegalArgumentException("nodeDir");
        }
        this.rootDir = nodeDir;
        this.globalDir = GatewayUtil.getGlobalPath(nodeDir);
        this.zonesDir = GatewayUtil.getZonesPath(nodeDir);
        handleFsPathsInit();
    }

    @Override
    public Param getZoneInfo(String zone) {
        File zoneDir = new File(zonesDir + File.separator + zone);
        if (!zoneDir.exists()) {
            return null;
        }
        Param infomation = new Param();
        infomation.put(Zone.KEY_ID, zone);
        File settingFile = new File(zoneDir, Node.SETTING.getPath() + "/" + GatewayConstants.GATEWAY_SEETING_PATH_ZONE);
        if (!settingFile.exists()) {
            infomation.setInt(Zone.KEY_STATUS, Zone.STATUS_ENABLE);
            return infomation;
        }
        String content = FileUtil.readString(settingFile);
        Param setting = JsonUtil.parseJsonParam(content);
        if (setting == null) {
            infomation.setInt(Zone.KEY_STATUS, Zone.STATUS_ENABLE);
            return infomation;
        }
        infomation.putAll(setting);
        return infomation;
    }

    @Override
    public boolean isZoneExist(String name) throws Exception {
        File zoneDir = new File(zonesDir + File.separator + name);
        return zoneDir.exists();
    }

    @Override
    public boolean addZone(String id, String name, List<String> match, int status, String remark) throws Exception {
        File zoneDir = new File(zonesDir + File.separator + id);
        boolean success = zoneDir.mkdirs();
        handleFsZoneInit(zoneDir.getPath());
        Param metadata = Param.builder(Zone.KEY_NAME, name)
                .setList(Zone.KEY_MATCH, match)
                .setInt(Zone.KEY_STATUS, status)
                .setString(Zone.KEY_REMARK, remark);
        File settingFile = new File(zoneDir, Node.SETTING.getPath() + "/" + GatewayConstants.GATEWAY_SEETING_PATH_ZONE);
        FileUtil.write(JsonUtil.toJson(metadata), settingFile, false);
        return true;
    }

    @Override
    public boolean updateZone(String id, String name, List<String> match, int status, String remark) throws Exception {
        File zoneDir = new File(zonesDir + File.separator + id);
        if (!zoneDir.exists()) {
            return false;
        }
        Param metadata = Param.builder(Zone.KEY_NAME, name)
                .setList(Zone.KEY_MATCH, match)
                .setInt(Zone.KEY_STATUS, status)
                .setString(Zone.KEY_REMARK, remark);
        File settingFile = new File(zoneDir, Node.SETTING.getPath() + "/" + GatewayConstants.GATEWAY_SEETING_PATH_ZONE);
        if (!settingFile.exists()) {
           settingFile.createNewFile();
        }
        FileUtil.write(JsonUtil.toJson(metadata), settingFile, false);
        return true;
    }

    @Override
    public boolean deleteZone(String zone) throws Exception {
        File zoneDir = new File(zonesDir + File.separator + zone);
        if (!zoneDir.exists()) {
            return false;
        }
        return FileUtil.deleteDir(zoneDir);
    }

    @Override
    public Table<Param> getZoneList() throws Exception {
        // 扫描文件目录下的所有文件，以每个文件名作为Zone的NAME
        File rootDir = new File(zonesDir);
        if (!rootDir.exists()) {
            return null;
        }
        // 获取rootDir下的zone目录，如果不存在则创建zone.default目录
        File[] zoneDirs = rootDir.listFiles();
        if (zoneDirs == null) {
            return null;
        }
        Table<Param> zones = new Table<Param>();
        for (File zoneDir : zoneDirs) {
            // 获取zone目录/setting/zone 文件内容作为Zone的配置
            File settingFile = new File(zoneDir, Node.SETTING.getPath() + "/" + GatewayConstants.GATEWAY_SEETING_PATH_ZONE);
            if (!settingFile.exists()) {
                settingFile.createNewFile();
            }
            Param zone = new Param();
            String content = FileUtil.readString(settingFile);
            Param setting = JsonUtil.parseJsonParam(content);
            zone.put(Zone.KEY_ID, zoneDir.getName());
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
        File file = new File(globalDir + File.separator + Node.GLOBAL_IPS.getPath() + File.separator + id);
        if (!file.exists()) {
            return null;
        }
        String content = FileUtil.readString(file);
        return JsonUtil.parseJsonParam(content);
    }

    @Override
    public Table<Param> getIpList() throws Exception {
        File dir = new File(globalDir + File.separator + Node.GLOBAL_IPS.getPath());
        if (!dir.exists()) {
            return null;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        Table<Param> dataList = new Table<Param>();
        for (File data : files) {
            String content = FileUtil.readString(data);
            Param infomation = JsonUtil.parseJsonParam(content);
            dataList.add(infomation);
        }
        return dataList;
    }

    @Override
    public boolean addIp(String id, Param infomation) throws Exception {
        File file = new File(globalDir + File.separator + Node.GLOBAL_IPS.getPath() + File.separator + id);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileUtil.write(JsonUtil.toJson(infomation), file, false);
        return true;
    }

    @Override
    public boolean updateIp(String id, Param infomation) throws Exception {
        File file = new File(globalDir + File.separator + Node.GLOBAL_IPS.getPath() + File.separator + id);
        if (!file.exists()) {
            return false;
        }
        FileUtil.write(JsonUtil.toJson(infomation), file, false);
        return true;
    }

    @Override
    public boolean deleteIp(String id) throws Exception {
        File file = new File(globalDir + File.separator + Node.GLOBAL_IPS.getPath() + File.separator + id);
        if (!file.exists()) {
            return false;
        }
        return file.delete();
    }

    @Override
    public Param getCertificateInfo(String id) throws Exception {
        File file = new File(globalDir + File.separator + Node.GLOBAL_CERTIFICATES.getPath() + File.separator + id);
        if (!file.exists()) {
            return null;
        }
        String content = FileUtil.readString(file);
        return JsonUtil.parseJsonParam(content);
    }

    @Override
    public Table<Param> getCertificateList() throws Exception {
        File file = new File(globalDir + File.separator + Node.GLOBAL_CERTIFICATES.getPath());
        if (!file.exists()) {
            return null;
        }
        File[] files = file.listFiles();
        if (files == null) {
            return null;
        }
        Table<Param> dataList = new Table<Param>();
        for (File data : files) {
            String content = FileUtil.readString(data);
            Param infomation = JsonUtil.parseJsonParam(content);
            dataList.add(infomation);
        }
        return dataList;
    }

    @Override
    public boolean addCertificate(String id, Param infomation) throws Exception {
        File file = new File(globalDir + File.separator + Node.GLOBAL_CERTIFICATES.getPath() + File.separator + id);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileUtil.write(JsonUtil.toJson(infomation), file, false);
        return true;
    }

    @Override
    public boolean updateCertificate(String id, Param infomation) throws Exception {
        File file = new File(globalDir + File.separator + Node.GLOBAL_CERTIFICATES.getPath() + File.separator + id);
        if (!file.exists()) {
            return false;
        }
        FileUtil.write(JsonUtil.toJson(infomation), file, false);
        return true;
    }

    @Override
    public boolean deleteCertificate(String id) throws Exception {
        File file = new File(globalDir + File.separator + Node.GLOBAL_CERTIFICATES.getPath() + File.separator + id);
        if (!file.exists()) {
            return false;
        }
        return file.delete();
    }

    @Override
    public Param getAuthInfo(String id) throws Exception {
        File file = new File(globalDir + File.separator + Node.GLOBAL_AUTHS.getPath() + File.separator + id);
        if (!file.exists()) {
            return null;
        }
        String content = FileUtil.readString(file);
        return JsonUtil.parseJsonParam(content);
    }

    @Override
    public Table<Param> getAuthList() throws Exception {
        File dir = new File(globalDir + File.separator + Node.GLOBAL_AUTHS.getPath());
        if (!dir.exists()) {
            return null;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        Table<Param> dataList = new Table<Param>();
        for (File data : files) {
            String content = FileUtil.readString(data);
            Param infomation = JsonUtil.parseJsonParam(content);
            dataList.add(infomation);
        }
        return dataList;
    }

    @Override
    public boolean addAuth(String id, Param infomation) throws Exception {
        File file = new File(globalDir + File.separator + Node.GLOBAL_AUTHS.getPath() + File.separator + id);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileUtil.write(JsonUtil.toJson(infomation), file, false);
        return true;
    }

    @Override
    public boolean updateAuth(String id, Param infomation) throws Exception {
        File file = new File(globalDir + File.separator + Node.GLOBAL_AUTHS.getPath() + File.separator + id);
        if (!file.exists()) {
            return false;
        }
        FileUtil.write(JsonUtil.toJson(infomation), file, false);
        return true;
    }

    @Override
    public boolean deleteAuth(String id) throws Exception {
        File file = new File(globalDir + File.separator + Node.GLOBAL_AUTHS.getPath() + File.separator + id);
        if (!file.exists()) {
            return false;
        }
        return file.delete();
    }

    @Override
    public Param getProviderInfo(String id) throws Exception {
        File file = new File(globalDir + File.separator + Node.GLOBAL_PROVIDERS.getPath() + File.separator + id);
        if (!file.exists()) {
            return null;
        }
        String content = FileUtil.readString(file);
        return JsonUtil.parseJsonParam(content);
    }

    @Override
    public Table<Param> getProviderList() throws Exception {
        File file = new File(globalDir + File.separator + Node.GLOBAL_PROVIDERS.getPath());
        if (!file.exists()) {
            return null;
        }
        File[] files = file.listFiles();
        if (files == null) {
            return null;
        }
        Table<Param> dataList = new Table<Param>();
        for (File data : files) {
            String content = FileUtil.readString(data);
            Param infomation = JsonUtil.parseJsonParam(content);
            dataList.add(infomation);
        }
        return dataList;
    }

    @Override
    public boolean addProvider(String id, Param infomation) throws Exception {
        File file = new File(globalDir + File.separator + Node.GLOBAL_PROVIDERS.getPath() + File.separator + id);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileUtil.write(JsonUtil.toJson(infomation), file, false);
        return true;
    }

    @Override
    public boolean updateProvider(String id, Param infomation) throws Exception {
        File file = new File(globalDir + File.separator + Node.GLOBAL_PROVIDERS.getPath() + File.separator + id);
        if (!file.exists()) {
            // 创建父目录（如果不存在）
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        FileUtil.write(JsonUtil.toJson(infomation), file, false);
        return true;
    }

    @Override
    public boolean deleteProvider(String id) throws Exception {
        File file = new File(globalDir + File.separator + Node.GLOBAL_PROVIDERS.getPath() + File.separator + id);
        if (!file.exists()) {
            return false;
        }
        return file.delete();
    }

    @Override
    public Table<Param> getRouteList(String zone) {
        // 扫描文件目录下的所有文件，然后解析文件内容为路由配置
        File nodeDirFile = new File(zonesDir + "/" + zone);
        if (!nodeDirFile.exists()) {
            return null;
        }
        // 读取 `${watchDir}/routes` 目录下的所有文件，每个文件对应一个路由配置
        File[] routeFiles = new File(nodeDirFile, Node.ROUTES.getPath()).listFiles();
        if (routeFiles == null) {
            return null;
        }
        Table<Param> routes = new Table<Param>();
        for (File routeFile : routeFiles) {
            Param route = new Param();
            route.put(RouteConstant.KEY_ID, routeFile.getName());
            // 解析路由配置，文件路由配置格式为JSON数据
            String content = FileUtil.readString(routeFile);
            Param metadata = JsonUtil.parseJsonParam(content);
            route.putAll(metadata);
            routes.add(route);
        }
        return routes;
    }

    @Override
    public Param getRouteInfo(String zone, String routeId) {
        File routeFile = new File(zonesDir + "/" + zone + Node.ROUTES.getPath() + "/" + routeId);
        if (!routeFile.exists()) {
            return null;
        }
        String content = FileUtil.readString(routeFile);
        return JsonUtil.parseJsonParam(content);
    }

    @Override
    public boolean addRoute(String zone, String routeId, Param route) {
        File routeFile = new File(zonesDir + "/" + zone + Node.ROUTES.getPath() + "/" + routeId);
        // 路由ID已经存在则不允许添加
        if (routeFile.exists()) {
            return false;
        }
        return FileUtil.write(route.toJson(), routeFile, false);
    }

    @Override
    public boolean updateRoute(String zone, String routeId, Param route) {
        File routeFile = new File(zonesDir + "/" + zone + Node.ROUTES.getPath() + "/" + routeId);
        // 路由ID不存在则不允许更新
        if (!routeFile.exists()) {
            return false;
        }
        // 移除路由ID，路由ID为文件名
        route.remove(RouteConstant.KEY_ID);
        // 更新路由中存在的属性
        return FileUtil.write(route.toJson(), routeFile, false);
    }

    @Override
    public boolean removeRoute(String zone, String routeId) {
        File routeFile = new File(zonesDir + "/" + zone + Node.ROUTES.getPath() + "/" + routeId);
        // 路由ID不存在则不允许删除
        if (!routeFile.exists()) {
            return false;
        }
        return routeFile.delete();
    }

    @Override
    public Table<Param> getUpstreamList(String zone) {
        // 扫描文件目录下的所有文件，然后解析文件内容为路由配置
        File nodeDirFile = new File(zonesDir + "/" + zone);
        if (!nodeDirFile.exists()) {
            return null;
        }
        // 读取 `${watchDir}/routes` 目录下的所有文件，每个文件对应一个路由配置
        File[] upstreamFiles = new File(nodeDirFile, Node.UPSTREAMS.getPath()).listFiles();
        if (upstreamFiles == null) {
            return null;
        }
        Table<Param> upstreams = new Table<Param>();
        for (File upstreamFile : upstreamFiles) {
            Param upstream = new Param();
            upstream.put(UpstreamConstant.KEY_ID, upstreamFile.getName());
            // 解析路由配置，文件路由配置格式为JSON数据
            String content = FileUtil.readString(upstreamFile);
            Param metadata = JsonUtil.parseJsonParam(content);
            upstream.putAll(metadata);
            upstreams.add(upstream);
        }
        return upstreams;
    }

    @Override
    public Param getUpstreamInfo(String zone, String upstreamId) {
        File upstreamFile = new File(zonesDir + "/" + zone + Node.UPSTREAMS.getPath() + "/" + upstreamId);
        if (!upstreamFile.exists()) {
            return null;
        }
        String content = FileUtil.readString(upstreamFile);
        return JsonUtil.parseJsonParam(content);
    }

    @Override
    public boolean addUpstream(String zone, String upstreamId, Param upstream) {
        File upstreamFile = new File(zonesDir + "/" + zone + Node.UPSTREAMS.getPath() + "/" + upstreamId);
        // 上游ID已经存在则不允许添加
        if (upstreamFile.exists()) {
            return false;
        }
        return FileUtil.write(upstream.toJson(), upstreamFile, false);
    }

    @Override
    public boolean updateUpstream(String zone, String upstreamId, Param upstream) {
        File upstreamFile = new File(zonesDir + "/" + zone + Node.UPSTREAMS.getPath() + "/" + upstreamId);
        // 上游ID不存在则不允许更新
        if (!upstreamFile.exists()) {
            return false;
        }
        // 移除上游ID，上游ID为文件名
        upstream.remove(UpstreamConstant.KEY_ID);
        return FileUtil.write(upstream.toJson(), upstreamFile, false);
    }

    @Override
    public boolean removeUpstream(String zone, String upstreamId) {
        File upstreamFile = new File(zonesDir + "/" + zone + Node.UPSTREAMS.getPath() + "/" + upstreamId);
        // 上游ID不存在则不允许删除
        if (!upstreamFile.exists()) {
            return false;
        }
        return upstreamFile.delete();
    }

    @Override
    public Param getWafRuleInfo(String zone, String id) throws Exception {
        File wafRuleFile = new File(zonesDir + "/" + zone + WafConstant.DEFAULT_WAF_PATH + "/" + id);
        if (!wafRuleFile.exists()) {
            return null;
        }
        String content = FileUtil.readString(wafRuleFile);
        return JsonUtil.parseJsonParam(content);
    }

    @Override
    public Table<Param> getWafRuleList(String zone) {
        File wafDirFile = new File(zonesDir + "/" + zone + WafConstant.DEFAULT_WAF_PATH);
        if (!wafDirFile.exists()) {
            return null;
        }
        File[] wafFiles = wafDirFile.listFiles();
        if (wafFiles == null) {
            return null;
        }
        Table<Param> dataList = new Table<Param>();
        for (File file : wafFiles) {
            Param rule = Param.builder(WafConstant.KEY_ID, file.getName());
            String content = FileUtil.readString(file);
            Param metadata = JsonUtil.parseJsonParam(content);
            rule.putAll(metadata);
            dataList.add(rule);
        }
        return dataList;
    }

    @Override
    public boolean addWafRule(String zone, String id, Param data) {
        File wafRuleFile = new File(zonesDir + "/" + zone + WafConstant.DEFAULT_WAF_PATH + "/" + id);
        // 若规则文件已经存在则不允许添加
        if (wafRuleFile.exists()) {
            return false;
        }
        return FileUtil.write(data.toJson(), wafRuleFile, false);
    }

    @Override
    public boolean updateWafRule(String zone, String id, Param data) throws Exception {
        File wafRuleFile = new File(zonesDir + "/" + zone + WafConstant.DEFAULT_WAF_PATH + "/" + id);
        // 若规则文件不存在则不允许更新
        if (!wafRuleFile.exists()) {
            return false;
        }
        return FileUtil.write(data.toJson(), wafRuleFile, false);
    }

    @Override
    public boolean removeWafRule(String zone, String id) {
        File wafRuleFile = new File(zonesDir + "/" + zone + WafConstant.DEFAULT_WAF_PATH + "/" + id);
        // 若规则文件不存在则不允许删除
        if (!wafRuleFile.exists()) {
            return false;
        }
        return wafRuleFile.delete();
    }

    @Override
    public Param getLimitRuleInfo(String zone, String id) throws Exception {
        File limitRuleFile = new File(zonesDir + "/" + zone + LimitConstant.DEFAULT_LIMIT_PATH + "/" + id);
        if (!limitRuleFile.exists()) {
            return null;
        }
        String content = FileUtil.readString(limitRuleFile);
        return JsonUtil.parseJsonParam(content);
    }

    @Override
    public Table<Param> getLimitRuleList(String zone) throws Exception {
        File limitDirFile = new File(zonesDir + "/" + zone + LimitConstant.DEFAULT_LIMIT_PATH);
        if (!limitDirFile.exists()) {
            return null;
        }
        File[] limitFiles = limitDirFile.listFiles();
        if (limitFiles == null) {
            return null;
        }
        Table<Param> dataList = new Table<Param>();
        for (File file : limitFiles) {
            Param rule = Param.builder(LimitConstant.KEY_ID, file.getName());
            String content = FileUtil.readString(file);
            Param metadata = JsonUtil.parseJsonParam(content);
            rule.putAll(metadata);
            dataList.add(rule);
        }
        return dataList;
    }

    @Override
    public boolean addLimitRule(String zone, String id, Param data) throws Exception {
        File limitRuleFile = new File(zonesDir + "/" + zone + LimitConstant.DEFAULT_LIMIT_PATH + "/" + id);
        // 若规则文件已经存在则不允许添加
        if (limitRuleFile.exists()) {
            return false;
        }
        return FileUtil.write(data.toJson(), limitRuleFile, false);
    }

    @Override
    public boolean updateLimitRule(String zone, String id, Param infomation) throws Exception {
        File limitRuleFile = new File(zonesDir + "/" + zone + LimitConstant.DEFAULT_LIMIT_PATH + "/" + id);
        // 若规则文件不存在则不允许更新
        if (!limitRuleFile.exists()) {
            return false;
        }
        return FileUtil.write(infomation.toJson(), limitRuleFile, false);
    }

    @Override
    public boolean removeLimitRule(String zone, String id) throws Exception {
        File limitRuleFile = new File(zonesDir + "/" + zone + LimitConstant.DEFAULT_LIMIT_PATH + "/" + id);
        // 若规则文件不存在则不允许删除
        if (!limitRuleFile.exists()) {
            return false;
        }
        return limitRuleFile.delete();
    }

    @Override
    public Param getCacheRuleInfo(String zone, String id) throws Exception {
        File cacheRuleFile = new File(zonesDir + "/" + zone + CacheConstant.DEFAULT_CACHE_PATH + "/" + id);
        if (!cacheRuleFile.exists()) {
            return null;
        }
        String content = FileUtil.readString(cacheRuleFile);
        return JsonUtil.parseJsonParam(content);
    }

    @Override
    public Table<Param> getCacheRuleList(String zone) {
        File cacheDirFile = new File(zonesDir + "/" + zone + CacheConstant.DEFAULT_CACHE_PATH);
        if (!cacheDirFile.exists()) {
            return null;
        }
        File[] cacheFiles = cacheDirFile.listFiles();
        if (cacheFiles == null) {
            return null;
        }
        Table<Param> dataList = new Table<Param>();
        for (File file : cacheFiles) {
            Param rule = Param.builder(CacheConstant.KEY_ID, file.getName());
            String content = FileUtil.readString(file);
            Param metadata = JsonUtil.parseJsonParam(content);
            rule.putAll(metadata);
            dataList.add(rule);
        }
        return dataList;
    }

    @Override
    public boolean addCacheRule(String zone, String id, Param data) {
        File cacheRuleFile = new File(zonesDir + "/" + zone + CacheConstant.DEFAULT_CACHE_PATH + "/" + id);
        // 若规则文件已经存在则不允许添加
        if (cacheRuleFile.exists()) {
            return false;
        }
        return FileUtil.write(data.toJson(), cacheRuleFile, false);
    }

    @Override
    public boolean updateCacheRule(String zone, String id, Param data) throws Exception {
        File cacheRuleFile = new File(zonesDir + "/" + zone + CacheConstant.DEFAULT_CACHE_PATH + "/" + id);
        // 若规则文件不存在则不允许更新
        if (!cacheRuleFile.exists()) {
            return false;
        }
        return FileUtil.write(data.toJson(), cacheRuleFile, false);
    }

    @Override
    public boolean removeCacheRule(String zone, String id) {
        File cacheRuleFile = new File(zonesDir + "/" + zone + CacheConstant.DEFAULT_CACHE_PATH + "/" + id);
        // 若规则文件不存在则不允许删除
        if (!cacheRuleFile.exists()) {
            return false;
        }
        return cacheRuleFile.delete();
    }

    @Override
    public Param getRedirectRuleInfo(String zone, String id) throws Exception {
        File redirectRuleFile = new File(zonesDir + "/" + zone + RedirectConstant.DEFAULT_REDIRECT_PATH + "/" + id);
        if (!redirectRuleFile.exists()) {
            return null;
        }
        String content = FileUtil.readString(redirectRuleFile);
        return JsonUtil.parseJsonParam(content);
    }

    @Override
    public Table<Param> getRedirectRuleList(String zone) {
        File redirectDirFile = new File(zonesDir + "/" + zone + RedirectConstant.DEFAULT_REDIRECT_PATH);
        if (!redirectDirFile.exists()) {
            return null;
        }
        File[] redirectFiles = redirectDirFile.listFiles();
        if (redirectFiles == null) {
            return null;
        }
        Table<Param> dataList = new Table<Param>();
        for (File file : redirectFiles) {
            Param rule = Param.builder(RedirectConstant.KEY_ID, file.getName());
            String content = FileUtil.readString(file);
            Param metadata = JsonUtil.parseJsonParam(content);
            rule.putAll(metadata);
            dataList.add(rule);
        }
        return dataList;
    }

    @Override
    public boolean addRedirectRule(String zone, String id, Param data) {
        File redirectRuleFile = new File(zonesDir + "/" + zone + RedirectConstant.DEFAULT_REDIRECT_PATH + "/" + id);
        // 若规则文件已经存在则不允许添加
        if (redirectRuleFile.exists()) {
            return false;
        }
        return FileUtil.write(data.toJson(), redirectRuleFile, false);
    }

    @Override
    public boolean updateRedirectRule(String zone, String id, Param data) throws Exception {
        File redirectRuleFile = new File(zonesDir + "/" + zone + RedirectConstant.DEFAULT_REDIRECT_PATH + "/" + id);
        // 若规则文件不存在则不允许更新
        if (!redirectRuleFile.exists()) {
            return false;
        }
        return FileUtil.write(data.toJson(), redirectRuleFile, false);
    }

    @Override
    public boolean removeRedirectRule(String zone, String id) {
        File redirectRuleFile = new File(zonesDir + "/" + zone + RedirectConstant.DEFAULT_REDIRECT_PATH + "/" + id);
        // 若规则文件不存在则不允许删除
        if (!redirectRuleFile.exists()) {
            return false;
        }
        return redirectRuleFile.delete();
    }

    @Override
    public Param getRewriteRequestRuleInfo(String zone, String id) throws Exception {
        File rewriteRuleFile = new File(zonesDir + "/" + zone + RewriteConstant.REWRITE_REQUEST_PATH + "/" + id);
        if (!rewriteRuleFile.exists()) {
            return null;
        }
        String content = FileUtil.readString(rewriteRuleFile);
        return JsonUtil.parseJsonParam(content);
    }

    @Override
    public Table<Param> getRewriteRequestRuleList(String zone) {
        File rewriteDirFile = new File(zonesDir + "/" + zone + RewriteConstant.REWRITE_REQUEST_PATH);
        if (!rewriteDirFile.exists()) {
            return null;
        }
        File[] rewriteFiles = rewriteDirFile.listFiles();
        if (rewriteFiles == null) {
            return null;
        }
        Table<Param> dataList = new Table<Param>();
        for (File file : rewriteFiles) {
            Param rule = Param.builder(LimitConstant.KEY_ID, file.getName());
            String content = FileUtil.readString(file);
            Param metadata = JsonUtil.parseJsonParam(content);
            rule.putAll(metadata);
            dataList.add(rule);
        }
        return dataList;
    }

    @Override
    public boolean addRewriteRequestRule(String zone, String id, Param data) throws Exception {
        File rewriteRuleFile = new File(zonesDir + "/" + zone + RewriteConstant.REWRITE_REQUEST_PATH + "/" + id);
        // 若规则文件已经存在则不允许添加
        if (rewriteRuleFile.exists()) {
            return false;
        }
        return FileUtil.write(data.toJson(), rewriteRuleFile, false);
    }

    @Override
    public boolean updateRewriteRequestRule(String zone, String id, Param data) throws Exception {
        File rewriteRuleFile = new File(zonesDir + "/" + zone + RewriteConstant.REWRITE_REQUEST_PATH + "/" + id);
        // 若规则文件不存在则不允许更新
        if (!rewriteRuleFile.exists()) {
            return false;
        }
        return FileUtil.write(data.toJson(), rewriteRuleFile, false);
    }

    @Override
    public boolean removeRewriteRequestRule(String zone, String id) throws Exception {
        File rewriteRuleFile = new File(zonesDir + "/" + zone + RewriteConstant.REWRITE_REQUEST_PATH + "/" + id);
        // 若规则文件不存在则不允许删除
        if (!rewriteRuleFile.exists()) {
            return false;
        }
        return rewriteRuleFile.delete();
    }

    @Override
    public Param getRewriteResponseRuleInfo(String zone, String id) throws Exception {
        File rewriteRuleFile = new File(zonesDir + "/" + zone + RewriteConstant.REWRITE_RESPONSE_PATH + "/" + id);
        if (!rewriteRuleFile.exists()) {
            return null;
        }
        String content = FileUtil.readString(rewriteRuleFile);
        Param infomation = JsonUtil.parseJsonParam(content);
        infomation.setString(RewriteConstant.KEY_ID, id);
        return infomation;
    }

    @Override
    public Table<Param> getRewriteResponseRuleList(String zone) {
        File rewriteDirFile = new File(zonesDir + "/" + zone + RewriteConstant.REWRITE_RESPONSE_PATH);
        if (!rewriteDirFile.exists()) {
            return null;
        }
        File[] rewriteFiles = rewriteDirFile.listFiles();
        if (rewriteFiles == null) {
            return null;
        }
        Table<Param> dataList = new Table<Param>();
        for (File file : rewriteFiles) {
            Param rule = Param.builder(RewriteConstant.KEY_ID, file.getName());
            String content = FileUtil.readString(file);
            Param metadata = JsonUtil.parseJsonParam(content);
            rule.putAll(metadata);
            dataList.add(rule);
        }
        return dataList;
    }

    @Override
    public boolean addRewriteResponseRule(String zone, String id, Param data) throws Exception {
        File rewriteRuleFile = new File(zonesDir + "/" + zone + RewriteConstant.REWRITE_RESPONSE_PATH + "/" + id);
        // 若规则文件已经存在则不允许添加
        if (rewriteRuleFile.exists()) {
            return false;
        }
        return FileUtil.write(data.toJson(), rewriteRuleFile, false);
    }

    @Override
    public boolean updateRewriteResponseRule(String zone, String id, Param data) throws Exception {
        File rewriteRuleFile = new File(zonesDir + "/" + zone + RewriteConstant.REWRITE_RESPONSE_PATH + "/" + id);
        // 若规则文件不存在则不允许更新
        if (!rewriteRuleFile.exists()) {
            return false;
        }
        return FileUtil.write(data.toJson(), rewriteRuleFile, false);
    }

    @Override
    public boolean removeRewriteResponseRule(String zone, String id) throws Exception {
        File rewriteRuleFile = new File(zonesDir + "/" + zone + RewriteConstant.REWRITE_RESPONSE_PATH + "/" + id);
        // 若规则文件不存在则不允许删除
        if (!rewriteRuleFile.exists()) {
            return false;
        }
        return rewriteRuleFile.delete();
    }

    @Override
    public Table<Param> getPluginList(String zone) {
        File zoneDir = new File(zonesDir + File.separator + zone);
        if (!zoneDir.exists()) {
            return null;
        }
        File pluginsFile = new File(zoneDir, Node.SETTING.getPath() + "/" + GatewayConstants.GATEWAY_SEETING_PATH_PLUGINS);
        if (!pluginsFile.exists()) {
            return null;
        }
        String content = FileUtil.readString(pluginsFile);
        Table<Param> plugins = JsonUtil.parseJsonTable(content);
        return plugins;
    }

    @Override
    public boolean updatePluginList(String zone, List<Param> plugins) throws Exception {
        File zoneDir = new File(zonesDir + File.separator + zone);
        if (!zoneDir.exists()) {
            return false;
        }
        File pluginsFile = new File(zoneDir, Node.SETTING.getPath() + "/" + GatewayConstants.GATEWAY_SEETING_PATH_PLUGINS);
        return FileUtil.write(JsonUtil.toJson(plugins), pluginsFile, false);
    }

    private void handleFsPathsInit() {
        // 初始化Global节点，如果不存在/gateway/global则创建
        File globalDir = new File(this.globalDir);
        if (!globalDir.exists()) {
            globalDir.mkdirs();
        }
        File ipsDir = new File(this.globalDir + File.separator + Node.GLOBAL_IPS.getPath());
        if (!ipsDir.exists()) {
            ipsDir.mkdirs();
        }
        File certificatesDir = new File(this.globalDir + File.separator + Node.GLOBAL_CERTIFICATES.getPath());
        if (!certificatesDir.exists()) {
            certificatesDir.mkdirs();
        }
        // 初始化Zones节点，如果不存在/gateway/{zone}/routes和/gateway/{zone}/upstreams则创建
        File rootDir = new File(this.zonesDir);
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
        // 获取rootDir下的zone目录，如果不存在则创建zone.default目录
        File[] zoneDirs = rootDir.listFiles();
        if (zoneDirs == null || zoneDirs.length == 0) {
            File zoneDir = new File(this.zonesDir + File.separator + Zone.DEFAULT_ZONE);
            zoneDir.mkdirs();
            zoneDirs = rootDir.listFiles();
        }
        for (File zoneDir : zoneDirs) {
            if (!zoneDir.isDirectory()) {
                continue;
            }
            // 获取zone目录下的setting、routes和upstreams目录，如果不存在则创建
            handleFsZoneInit(zoneDir.getPath());
        }
    }

    private void handleFsZoneInit(String zone) {
        File settingDir = new File(zone + Node.SETTING.getPath());
        if (!settingDir.exists()) {
            settingDir.mkdirs();
        }
        File routesDir = new File(zone + Node.ROUTES.getPath());
        if (!routesDir.exists()) {
            routesDir.mkdirs();
        }
        File upstreamsDir = new File(zone + Node.UPSTREAMS.getPath());
        if (!upstreamsDir.exists()) {
            upstreamsDir.mkdirs();
        }
    }
}
