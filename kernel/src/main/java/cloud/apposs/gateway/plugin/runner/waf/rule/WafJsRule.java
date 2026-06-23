package cloud.apposs.gateway.plugin.runner.waf.rule;

import cloud.apposs.cache.CacheManager;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.rules.Facts;
import cloud.apposs.gateway.util.WebUtil;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Cookie;
import cloud.apposs.util.Param;
import cloud.apposs.util.RandomUtil;
import cloud.apposs.util.Table;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * 基于JS校验的拦截规则
 */
public class WafJsRule extends WafRule {
    private static final String CACHE_KEY_PREFIX = "Sys:Waf:Js:";
    private static final String HEADER_COOKIE_PREFIX = "X-W-";
    private static final String HEADER_COOKIE_SID_KEY = "X-W-SID";

    // 表元信息
    private static final int WAF_JS_PASS_TIME = 5 * 60 * 1000;
    private static final int WAF_MAX_FAIL_TIMES = 3;
    private static final ProtoSchema WAF_DATASET_SCHEMA = ProtoSchema.mapSchema();
    static {
        WAF_DATASET_SCHEMA.addKey(WafJsRule.Info.DataSet.ID, String.class);
        WAF_DATASET_SCHEMA.addKey(WafJsRule.Info.DataSet.SIGNATURE, String.class);
    }
    private static final ProtoSchema WAF_JS_SCHEMA = ProtoSchema.mapSchema();
    static {
        WAF_JS_SCHEMA.addKey(WafJsRule.Info.DATE, Long.class);
        WAF_JS_SCHEMA.addKey(WafJsRule.Info.PASS, Boolean.class);
        WAF_JS_SCHEMA.addKey(WafJsRule.Info.FAIL, Integer.class);
        WAF_JS_SCHEMA.addKey(WafJsRule.Info.DATASET, Table.class, ProtoSchema.listSchema(Param.class, WAF_DATASET_SCHEMA));
    }

    private final CacheManager cache;

    public WafJsRule(String name, int status, int priority, List<String> matchedRuleIds, boolean skippable, CacheManager cache) {
        super(name, status, priority, matchedRuleIds, skippable);
        this.cache = cache;
    }

    @Override
    public boolean evaluate(Facts facts, Object... arguments) {
        boolean result = super.evaluate(facts, arguments);
        if (!result) {
            return false;
        }

        // 匹配到了规则，进行后续的JS质询操作
        // 1. 先判断是否已经触发了质询，如果是首次触发，需要进行初始化记录并将JS质询页面返回给客户端
        GatewayHttpRequest request = (GatewayHttpRequest) arguments[2];
        GatewayHttpResponse response = (GatewayHttpResponse) arguments[3];
        String realIp = WebUtil.getRequestIp(request);
        String ipCacheKey = CACHE_KEY_PREFIX + realIp;
        Param wafJsData = cache.getParam(ipCacheKey, getWafJSSchema());
        if (wafJsData == null) {
            Table<Param> dataSet = Table.builder();
            String cookieSid = UUID.randomUUID().toString();
            String cookieSignature = RandomUtil.getRandomString(32);
            dataSet.add(Param.builder(WafJsRule.Info.DataSet.ID, cookieSid).setString(WafJsRule.Info.DataSet.SIGNATURE, cookieSignature));
            wafJsData = Param.builder()
                    .setLong(WafJsRule.Info.DATE, 0L)
                    .setBoolean(WafJsRule.Info.PASS, false)
                    .setInt(WafJsRule.Info.FAIL, 0)
                    .setList(WafJsRule.Info.DATASET, dataSet);
            cache.put(ipCacheKey, wafJsData, getWafJSSchema());
            try {
                String content = handleResponseContentGenerator();
                response.write(content, true);
            } catch (IOException e) {
                return false;
            }
            return false;
        }
        // 2. 如果已经有记录则进行验证
        // 如果已经是缓存封禁，则直接封禁
        if (!wafJsData.getBoolean(WafJsRule.Info.PASS)) {
            return true;
        }
        // 判断是否已经验证过并且未过期，则直接通过
        long date = wafJsData.getLong(WafJsRule.Info.DATE);
        if (System.currentTimeMillis() - date < WAF_JS_PASS_TIME) {
            return false;
        }
        // 3. 如果没有通过验证，则进行验证
        String signatureCookie = getSignatureCookie(request);
        // 如果客户端没有传递签名Cookie，则判断是否超过最大验证数量
        if (signatureCookie == null) {
            int fail = wafJsData.getInt(WafJsRule.Info.FAIL);
            if (fail >= WAF_MAX_FAIL_TIMES) {
                return true;
            }
            wafJsData.setInt(WafJsRule.Info.FAIL, fail + 1);
            String cookieSid = UUID.randomUUID().toString();
            String cookieSignature = RandomUtil.getRandomString(32);
            Table<Param> dataSet = wafJsData.getTable(WafJsRule.Info.DATASET);
            dataSet.add(Param.builder(WafJsRule.Info.DataSet.ID, cookieSid).setString(WafJsRule.Info.DataSet.SIGNATURE, cookieSignature));
            cache.put(ipCacheKey, wafJsData, getWafJSSchema());
            response.setCookie(new Cookie(HEADER_COOKIE_SID_KEY, cookieSid));
            response.setCookie(new Cookie(HEADER_COOKIE_PREFIX + cookieSid, cookieSignature));
            return false;
        }
        // 验证签名
        Table<Param> dataSet = wafJsData.getTable(WafJsRule.Info.DATASET);
        for (Param data : dataSet) {
            String signature = data.getString(WafJsRule.Info.DataSet.SIGNATURE);
            if (signature.equals(signatureCookie)) {
                wafJsData.setLong(WafJsRule.Info.DATE, System.currentTimeMillis());
                wafJsData.setBoolean(WafJsRule.Info.PASS, true);
                cache.put(ipCacheKey, wafJsData, getWafJSSchema());
                return false;
            }
            dataSet.remove(data);
            cache.put(ipCacheKey, wafJsData, getWafJSSchema());
        }
        return true;
    }

    private String getSignatureCookie(GatewayHttpRequest request) {
        Cookie cookieSid = request.getCookie(HEADER_COOKIE_SID_KEY);
        if (cookieSid == null) {
            return null;
        }
        Cookie cookieSignature = request.getCookie(HEADER_COOKIE_PREFIX + cookieSid);
        if (cookieSignature == null) {
            return null;
        }
        return cookieSignature.value();
    }

    private String handleResponseContentGenerator() throws IOException {
        // 读取当前项目下resources/pages/forbiden.html文件内容返回给客户端
        InputStream pageInputStream = getClass().getClassLoader().getResourceAsStream("pages/security.html");
        if (pageInputStream == null) {
            return null;
        }
        try {
            // 读取文件内容
            byte[] content = new byte[pageInputStream.available()];
            pageInputStream.read(content);
            return new String(content);
        } finally {
            pageInputStream.close();
        }
    }

    private static ProtoSchema getWafJSSchema() {
        return WAF_JS_SCHEMA;
    }

    private static class Info {
        // 验证时间
        public static final String DATE = "date";
        // 验证是否通过
        public static final String PASS = "pass";
        // 验证失败次数
        public static final String FAIL = "fail";
        // 验证客户端列表
        public static final String DATASET = "dataset";
        public static final class DataSet {
            public static final String ID = "id";
            public static final String SIGNATURE = "signature";
        }
    }
}
