package cloud.apposs.gateway.plugin.runner.ip2region;

import cloud.apposs.gateway.GatewayConstants;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.annotation.CommonPlugin;
import cloud.apposs.gateway.plugin.Plugin;
import cloud.apposs.gateway.plugin.PluginAdapter;
import cloud.apposs.gateway.plugin.PluginResult;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.rules.Facts;
import cloud.apposs.gateway.util.WebUtil;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.react.React;
import cloud.apposs.util.Param;
import org.lionsoul.ip2region.xdb.Searcher;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

/**
 * 获取IP地理位置信息插件，
 * 参考：
 * <pre>
 *     https://github.com/lionsoul2014/ip2region
 *     https://blog.csdn.net/weixin_63944437/article/details/144993968
 * </pre>
 * 默认的 region 信息都固定了格式：国家|区域|省份|城市|ISP，缺省的地域信息默认是0
 */
@CommonPlugin
public class Ip2RegionPlugin extends PluginAdapter {
    public static final String NAME = "Ip2Region";
    private static final int PLUGIN_PRIORITY = 1030;

    public static final String KEY_ENABLE = "enable";
    public static final String KEY_IP_REGION = "X-IP-Region";

    private Searcher searcher;

    public Ip2RegionPlugin() throws IOException {
        super(NAME);
        InputStream ris = getClass().getClassLoader().getResourceAsStream("ip2region.xdb");
        if (Objects.nonNull(ris)) {
            byte[] dbBinStr = new byte[ris.available()];
            ris.read(dbBinStr);
            searcher = Searcher.newWithBuffer(dbBinStr);
        }
    }

    @Override
    public Plugin initialize(Zone zone, Param configiuration) {
        super.initialize(zone, configiuration);
        return this;
    }

    @Override
    public int getPriority() {
        return PLUGIN_PRIORITY;
    }

    private String handleIpContryMatched(String region) {
        if (region == null) {
            return null;
        }
        String[] regionArray = region.split("\\|");
        if (regionArray.length < 5) {
            return null;
        }
        return regionArray[0];
    }

    @Override
    @SuppressWarnings("unchecked")
    public React<PluginResult> preFilter(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route) throws Exception {
        return React.emitter(() -> {
            if (searcher == null) {
                return PluginResult.SUCCESS;
            }
            // 无论有没有开启插件，都默认设置请求区域数据到MVEL表达式中
            String ipAddress = WebUtil.getRequestIp(request);
            String region = searcher.search(ipAddress);
            String country = handleIpContryMatched(region);
            if (country != null) {
                Facts facts = (Facts) request.getAttribute(GatewayConstants.REQUEST_ATTRIBUTE_RULES_FACTS);
                Map<String, Object> fact = (Map<String, Object>) facts.getFact("http").getValue();
                fact.put("country", country);
            }
            // 只在开启插件的情况下才执行请求头区域设置
            Param configuration = configiurationMapping.get(zone.getId());
            if (configuration == null) {
                return PluginResult.SUCCESS;
            }
            if (!configuration.getBoolean(KEY_ENABLE, false)) {
                return PluginResult.SUCCESS;
            }
            request.addHeader(KEY_IP_REGION, region);
            return PluginResult.SUCCESS;
        });
    }
}
