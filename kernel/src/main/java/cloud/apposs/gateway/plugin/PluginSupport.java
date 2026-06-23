package cloud.apposs.gateway.plugin;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.annotation.CommonPlugin;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.zone.Zone;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 插件过滤器支持类，用于管理插件的添加和移除，其配置项存储于配置注册中心中，
 * 一个插件管理器对应一个路由，之所以这么设计是因为每个路由的插件是独立的，并且可以动态变更插件配置
 */
public class PluginSupport {
    /**
     * 创建一个插件列表（用于存放插件实例）
     */
    private final Set<Plugin> pluginList;

    public PluginSupport() {
        this.pluginList = new TreeSet<>();
    }

    /**
     * 添加插件，如果插件已经存在则不添加
     *
     * @param  plugin 插件
     * @return 是否添加成功
     */
    public boolean addPlugin(Plugin plugin) {
        if (pluginList.contains(plugin)) {
            return false;
        }
        return pluginList.add(plugin);
    }

    /**
     * 移除插件
     */
    public void removePlugin(Plugin plugin) {
        pluginList.remove(plugin);
    }

    /**
     * 获取网域下指定插件
     */
    public Plugin getPlugin(String name) {
        for (Plugin plugin : pluginList) {
            if (plugin.name().equals(name)) {
                return plugin;
            }
        }
        return null;
    }

    /**
     * 获取当前网域下的插件列表
     */
    public Set<Plugin> getPluginList() {
        return pluginList;
    }

    /**
     * 网关请求处理之后数据发送之前进行调用
     */
    public void postFilter(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route, Object value) throws Exception {
        // 倒序执行，即最后一个插件最先执行
        Iterator<Plugin> iterator = ((TreeSet<Plugin>) pluginList).descendingIterator();
        while (iterator.hasNext()) {
            Plugin plugin = iterator.next();
            plugin.postFilter(request, response, zone, route, value);
        }
    }

    /**
     * 整个请求处理完毕回调方法，无论请求逻辑处理<b>有没有成功</b>，
     * 一般用于性能监控中在此记录结束时间并输出消耗时间，还可以进行一些资源清理
     */
    public void afterCompletion(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route) {
        afterCompletion(request, response, zone, route, null);
    }

    /**
     * 请求结束时，无论有业务方逻辑处理有没有成功，从最后一个拦截器开始拦截
     */
    public void afterCompletion(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route, Throwable throwable) {
        Iterator<Plugin> iterator = ((TreeSet<Plugin>) pluginList).descendingIterator();
        while (iterator.hasNext()) {
            Plugin plugin = iterator.next();
            plugin.afterCompletion(request, response, zone, route, throwable);
        }
    }

    /**
     * 获取插件列表，该方法只在网关启动调用
     *
     * @param context 网关上下文
     */
    public static List<Plugin> getCommonPluginList(GatewayContext context) {
        // 添加默认全局插件，属于每个Zone网域全局单例
        return context.getCompiler().getBeanAnnotationList(CommonPlugin.class);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (Plugin plugin : pluginList) {
            builder.append(plugin.name()).append(",");
        }
        if (pluginList.size() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append("]");
        return builder.toString();
    }
}
