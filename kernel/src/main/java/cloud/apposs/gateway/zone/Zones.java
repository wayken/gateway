package cloud.apposs.gateway.zone;

import cloud.apposs.gateway.GatewayConstants;
import cloud.apposs.logger.Logger;
import cloud.apposs.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zone区域管理器，用于管理所有的Zone区域信息，包括区域路由匹配等
 */
public final class Zones {
    /** 所有网域列表 */
    private final List<Zone> zones = new ArrayList<Zone>();

    /**
     * 匹配域名->网域映射，方便快速通过请求域名来定位对应的Zone对象，冗余数据
     */
    private final Map<String, Zone> zoneMapping = new ConcurrentHashMap<>();

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public void addZone(Zone zone) {
        zones.add(zone);
        if (zone.isDefault()) {
            zoneMapping.put(GatewayConstants.PATH_MATCHER_ANY, zone);
        } else {
            List<String> match = zone.getMatch();
            for (String domain : match) {
                if (zoneMapping.containsKey(domain)) {
                    Logger.warn("Zone '%s' Match Domain '%s' Already Exist, Skip!!!", zone.getId(), domain);
                    continue;
                }
                zoneMapping.put(domain, zone);
            }
        }
        Logger.info("Zone '%s' Add Success", zone.getId());
    }

    public boolean updateZone(String zoneId, Zone updatedZone) {
        for (Zone zone : zones) {
            if (!zone.getId().equals(zoneId)) {
                continue;
            }
            // 先找出updatedZone有的域名，但是zone没有的域名，需要添加到zoneMapping中，反之则删除
            List<String> zoneMatch = zone.getMatch();
            List<String> updatedMatch = updatedZone.getMatch();
            for (String domain : updatedMatch) {
                if (!zoneMatch.contains(domain)) {
                    zoneMapping.put(domain, zone);
                }
            }
            for (String domain : zoneMatch) {
                if (!updatedMatch.contains(domain)) {
                    zoneMapping.remove(domain);
                }
            }
            zone.update(updatedZone);
            Logger.info("Zone '%s' Update Success", zone);
            return true;
        }
        return false;
    }

    public void removeZone(Zone zone) {
        zones.remove(zone);
        List<String> match = zone.getMatch();
        for (String domain : match) {
            zoneMapping.remove(domain);
        }
        Logger.info("Zone '%s' Remove Success", zone.getId());
    }

    public Zones addAllZone(List<Zone> zones) {
        if (zones == null) {
            return this;
        }
        for (Zone zone : zones) {
            addZone(zone);
        }
        return this;
    }

    public List<Zone> getZones() {
        return zones;
    }

    /**
     * 根据Zone ID获取Zone区域
     *
     *
     * @param  id ZoneID
     * @return Zone区域
     */
    public Zone getZone(String id) {
        for (Zone zone : zones) {
            if (zone.getId().equals(id)) {
                return zone;
            }
        }
        return null;
    }

    /**
     * 根据请求的Host匹配对应的Zone区域，匹配规则如下：
     * <pre>
     * 1. 如果没有匹配则返回默认Zone区域，即全局域
     * 2. 如果有多个Zone匹配则返回Host和Zone精确匹配的Zone，如果没有精确匹配则返回第一个匹配的Zone
     * </pre>
     */
    public Zone matchZone(String host) {
        // 先获取精确匹配的Zone区域，快速定位并返回
        Zone matchedExactZone = zoneMapping.get(host);
        if (matchedExactZone != null && matchedExactZone.isEnable()) {
            return matchedExactZone;
        }
        // 没有精确匹配的Zone区域，则需要遍历进行Host正则匹配
        Zone matchDefaultZone = null;
        for (Map.Entry<String, Zone> zoneEntry : zoneMapping.entrySet()) {
            Zone zone = zoneEntry.getValue();
            if (zone.isDefault()) {
                if (matchDefaultZone == null) {
                    matchDefaultZone = zone;
                }
                continue;
            }
            if (!zone.isEnable()) {
                continue;
            }
            String match = zoneEntry.getKey();
            if (!pathMatcher.match(match, host)) {
                continue;
            }
            return zone;
        }
        return matchDefaultZone.isEnable() ? matchDefaultZone : null;
    }
}
