package cloud.apposs.gateway.zone;

import cloud.apposs.gateway.listener.Listener;
import cloud.apposs.gateway.listener.ListenerSupport;
import cloud.apposs.gateway.plugin.Plugin;
import cloud.apposs.gateway.plugin.PluginSupport;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.route.RouteParser;
import cloud.apposs.gateway.route.Router;
import cloud.apposs.gateway.upstream.Upstream;
import cloud.apposs.gateway.upstream.UpstreamSupport;
import cloud.apposs.util.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 网关域，用于区分不同的路由网域，实现不同域名下的路由隔离，类似Nginx中的Server块
 * 网域规则`/gateway/zones/{zone}`示例如下：
 * <pre>
 *     `/gateway/zones/zone.default`: 默认路由网域
 *     `/gateway/zones/2272335705999111`: zoneId为2272335705999111路由网域
 *     `/gateway/zones/2272335705999111/setting/zone`: zoneId为2272335705999111路由网域配置，为空则代表不做任何匹配
 * </pre>
 * 通过不同的域名进行隔离区分，当请求到达网关时会根据请求的域名进行路由网域匹配，如果不指定zone添加则默认为`zone.default`。
 * 在网域下会有不同的模块配置，如路由配置、插件配置等，包括：
 * <pre>
 *     `/gateway/zones/zone.default/route`: 网域路由配置
 *     `/gateway/zones/zone.default/plugin`: 网域插件配置
 *     `/gateway/zones/zone.default/upstream`: 网域上游配置
 *
 *     `/gateway/zones/zone.default/waf`: 网域WAF配置，为Waf插件扩展使用
 *     `/gateway/zones/zone.default/cache`: 网域缓存配置，为Cache插件扩展使用
 *     `/gateway/zones/zone.default/rewrite`: 网域请求改写配置，为Rewrite插件扩展使用
 * </pre>
 * 业务规范：
 * 1. 匹配网域必须符合域名规范，如`xxx.com`，`teambeit.com`，`work.teambeit.com`等
 * 2. 如果一级域名下有多个业务模块，如`work.teambeit.com`、`admin.teambeit.com`等，可以通过二级域名进行区分，即分别以上面创建两个不同的网域
 */
public final class Zone {
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_MATCH = "match";
    public static final String KEY_PLUGINS = "plugins";
    public static final String KEY_STATUS = "status";
    public static final String KEY_REMARK = "remark";

    public static final Integer STATUS_ENABLE = 1;
    public static final Integer STATUS_DISABLE = 0;

    /**
     * 默认路由网域，不配置指定域名则使用默认域名并进行兜底匹配
     */
    public static final String DEFAULT_ZONE = "zone.default";

    /**
     * 路由网域ID
     */
    private final String id;


    /**
     * 路由网域名称
     */
    private String name;

    /**
     * 路由网域匹配规则，支持正则表达式
     */
    private List<String> match;

    /**
     * 路由网域备注
     */
    private String remark;

    /**
     * 路由状态，0表示禁用，1表示启用
     */
    private Integer status = STATUS_ENABLE;

    /**
     * 插件配置映射，配置示例如下：
     * <pre>
     *  {
     *    "waf": {
     *      "status": true,
     *      "config": {
     *        ...
     *      }
     *    },
     *    "cache": {
     *      "status": true,
     *      "config": {
     *        ...
     *      }
     *    }
     *  }
     */
    private final Map<String, Param> pluginConfigMapping = new HashMap<>();

    /**
     * 插件管理
     */
    private final PluginSupport pluginSupport = new PluginSupport();

    /** 上游服务支持 */
    protected final UpstreamSupport upstreamSupport = new UpstreamSupport();

    /** 路由表 */
    protected final Router router = new Router();

    /** 监听器支持 */
    private final ListenerSupport listenerSupport = new ListenerSupport();

    public Zone(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMatch() {
        return match;
    }

    public void setMatch(List<String> match) {
        this.match = match;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public boolean isEnable() {
        return status == STATUS_ENABLE;
    }

    public PluginSupport getPluginSupport() {
        return pluginSupport;
    }

    public boolean update(Zone zone) {
        if (zone == null) {
            return false;
        }
        this.name = zone.name;
        this.match = zone.match;
        this.remark = zone.remark;
        this.status = zone.status;
        return true;
    }

    /**
     * 添加路由
     *
     * @param  route 路由
     * @return 是否添加成功
     */
    public boolean addRoute(Route route) {
        if (route == null) {
            return false;
        }
        // 如果上游服务ID不是手动配置，则从zone中获取对应的上游服务信息并设置到路由内存中方便快速查找
        if (!RouteParser.isManualUpstream(route)) {
            route.upstream(getUpstream(route.upstreamId()));
        }
        return router.addRoute(this, route);
    }

    /**
     * 更新路由，主要用于动态更新路由信息
     *
     * @param  route 新路由
     * @return 更新成功返回true，否则返回false
     */
    public boolean updateRoute(Route route) {
        if (route == null) {
            return false;
        }
        // 如果上游服务ID不是手动配置，则从zone中获取对应的上游服务信息并设置到路由内存中方便快速查找
        if (!RouteParser.isManualUpstream(route)) {
            route.upstream(getUpstream(route.upstreamId()));
        }
        return router.updateRoute(this, route);
    }

    /**
     * 删除路由，主要用于动态更新路由信息
     *
     * @param  routeId 路由ID
     * @return 删除成功返回true，否则返回false
     */
    public boolean removeRoute(String routeId) {
        return router.removeRoute(this, routeId);
    }

    /**
     * 添加上游服务
     *
     * @param  upstream 上游服务
     * @return 是否添加成功
     */
    public boolean addUpstream(Upstream upstream) {
        if (upstream == null) {
            return false;
        }
        upstreamSupport.addUpstream(this, upstream);
        return true;
    }

    public Upstream getUpstream(String upstreamId) {
        return upstreamSupport.getUpstream(upstreamId);
    }

    /**
     * 更新路由上游，主要用于动态更新路由信息
     *
     * @param  upstream 上游服务
     * @return 更新成功返回true，否则返回false
     */
    public boolean updateUpstream(Upstream upstream) {
        upstreamSupport.updateUpstream(this, upstream);
        return router.updateUpstream(this, upstream);
    }

    /**
     * 删除上游，主要用于动态更新路由信息
     *
     * @param  upstreamId 上游服务ID
     * @return 删除成功返回true，否则返回false
     */
    public boolean removeUpstream(String upstreamId) {
        upstreamSupport.removeUpstream(this, upstreamId);
        return router.removeUpstream(this, upstreamId);
    }

    /**
     * 添加插件
     *
     * @param  plugin 插件
     * @return 是否添加成功
     */
    public boolean addPlugin(Plugin plugin) {
        Param config = pluginConfigMapping.get(plugin.name());
        if (config != null) {
            plugin.initialize(this, config);
        }
        return pluginSupport.addPlugin(plugin);
    }

    public boolean updatePlugin(String pluginName, Param config) {
        Plugin plugin = pluginSupport.getPlugin(pluginName);
        if (plugin == null) {
            return false;
        }
        pluginConfigMapping.put(pluginName, config);
        plugin.initialize(this, config);
        return true;
    }

    public boolean putPluginConfigMapping(String pluginName, Param config) {
        pluginConfigMapping.put(pluginName, config);
        return true;
    }

    /**
     * 获取当前路由配置的插件列表
     */
    public Set<Plugin> getPluginList() {
        return pluginSupport.getPluginList();
    }

    /**
     * 是否为默认路由网域，即`zone.default`，类似Nginx中的`server_name _`
     */
    public boolean isDefault() {
        return DEFAULT_ZONE.equals(id);
    }

    public Router getRouter() {
        return router;
    }

    public Param getPluginConfig(String pluginName) {
        return pluginConfigMapping.get(pluginName);
    }

    public ListenerSupport getListenerSupport() {
        return listenerSupport;
    }

    public void addListener(Listener listener) {
        listenerSupport.addListener(listener);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Zone) {
            Zone zone = (Zone) obj;
            return id.equals(zone.id);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Zone {" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", match='" + match + '\'' +
                ", status=" + status +
                ", default=" + isDefault() +
                ", plugins=" + pluginSupport +
                '}';
    }
}
