package cloud.apposs.gateway.upstream;

import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.logger.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link Upstream}管理器，负责管理所有的上游转发配置，全局单例
 */
public class UpstreamSupport {
    /**
     * 上游转发列表，Key为上游ID，Value为上游配置
     */
    private final Map<String, Upstream> upstreamList = new ConcurrentHashMap<String, Upstream>();

    public UpstreamSupport addUpstream(Zone zone, Upstream upstream) {
        if (upstream == null) {
            return this;
        }
        upstreamList.put(upstream.getId(), upstream);
        Logger.info("Zone '%s' Add %s Success", zone.getId(), upstream);
        return this;
    }

    public Upstream getUpstream(String upstreamId) {
        if (upstreamId == null || upstreamId.isEmpty()) {
            return null;
        }
        return upstreamList.get(upstreamId);
    }

    public UpstreamSupport updateUpstream(Zone zone, Upstream upstream) {
        if (upstream == null) {
            return this;
        }
        // 获取旧的上游，释放资源
        Upstream oldUpstream = upstreamList.get(upstream.getId());
        if (oldUpstream != null) {
            oldUpstream.shutdown();
        }
        // 更新上游配置
        upstreamList.put(upstream.getId(), upstream);
        Logger.info("Zone '%s' Update %s Success", zone.getId(), upstream);
        return this;
    }

    public UpstreamSupport removeUpstream(Zone zone, String upstreamId) {
        if (upstreamId == null || upstreamId.trim().isEmpty()) {
            return this;
        }
        Upstream upstream = upstreamList.remove(upstreamId);
        if (upstream != null) {
            upstream.shutdown();
        }
        Logger.info("Zone '%s' Remove %s Success", zone.getId(), upstream);
        return this;
    }
}
