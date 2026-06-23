package cloud.apposs.gateway.listener;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.zone.Zone;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ListenerSupport {
    private final Set<Listener> listenerList = new HashSet<Listener>();

    public boolean addListener(Listener listener) {
        if (listenerList.contains(listener)) {
            return false;
        }
        return listenerList.add(listener);
    }

    public void removeListener(Listener listener) {
        listenerList.remove(listener);
    }

    public Set<Listener> getListenerList() {
        return listenerList;
    }

    public void onCompletion(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route) {
        Iterator<Listener> iterator = listenerList.iterator();
        while (iterator.hasNext()) {
            Listener listener = iterator.next();
            listener.onCompletion(request, response, zone, route, null);
        }
    }

    public void onCompletion(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route, Throwable throwable) {
        Iterator<Listener> iterator = listenerList.iterator();
        while (iterator.hasNext()) {
            Listener listener = iterator.next();
            listener.onCompletion(request, response, zone, route, throwable);
        }
    }

    /**
     * 获取监听器列表，该方法只在网关启动调用
     *
     * @param context 网关上下文
     */
    public static List<Listener> getCommonListenerList(GatewayContext context) {
        // 添加默认全局插件，属于每个Zone网域全局单例
        return context.getCompiler().getBeanHierarchyList(Listener.class);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (Listener listener : listenerList) {
            builder.append(listener).append(",");
        }
        if (listenerList.size() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append("]");
        return builder.toString();
    }
}
