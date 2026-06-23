package cloud.apposs.gateway.zone;

import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.util.JsonUtil;
import cloud.apposs.util.Param;

public final class ZoneParser {
    private ZoneParser() {
    }

    /**
     * 解析网域配置，内容格式如下：
     * <pre>
     *   {
     *     "name": "xxx",
     *     "match": ["xxx.com", "xxx2.com"]
     *     ...
     *   }
     * </pre>
     *
     * @param  id      网域ID
     * @param  content 网域配置内容
     * @return 网域对象
     * @throws Exception 如果配置不符合规范则抛出异常
     */
    public static Zone parse(String id, String content) throws Exception {
        Zone zone = new Zone(id);
        if (content == null || content.isEmpty()) {
            return zone;
        }
        Param zoneInfo = JsonUtil.parseJsonParam(content);
        // 解析上游基本配置
        zone.setName(zoneInfo.getString(Zone.KEY_NAME));
        zone.setMatch(zoneInfo.getTableWithoutNull(Zone.KEY_MATCH));
        zone.setStatus(zoneInfo.getInt(Zone.KEY_STATUS, 1));
        return zone;
    }
}
