package cloud.apposs.gateway.plugin.runner.limit.rule;

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
public class LimitJsRule extends LimitRule {
    private static final String CACHE_KEY_PREFIX = "Sys:Limit:Js:";
    private static final String HEADER_COOKIE_PREFIX = "X-W-";
    private static final String HEADER_COOKIE_SID_KEY = "X-W-SID";

    // 表元信息
    private static final int LIMIT_JS_PASS_TIME = 5 * 60 * 1000;
    private static final int LIMIT_MAX_FAIL_TIMES = 3;
    private static final ProtoSchema LIMIT_DATASET_SCHEMA = ProtoSchema.mapSchema();
    static {
        LIMIT_DATASET_SCHEMA.addKey(Info.DataSet.ID, String.class);
        LIMIT_DATASET_SCHEMA.addKey(Info.DataSet.SIGNATURE, String.class);
    }
    private static final ProtoSchema LIMIT_JS_SCHEMA = ProtoSchema.mapSchema();
    static {
        LIMIT_JS_SCHEMA.addKey(Info.DATE, Long.class);
        LIMIT_JS_SCHEMA.addKey(Info.PASS, Boolean.class);
        LIMIT_JS_SCHEMA.addKey(Info.FAIL, Integer.class);
        LIMIT_JS_SCHEMA.addKey(Info.DATASET, Table.class, ProtoSchema.listSchema(Param.class, LIMIT_DATASET_SCHEMA));
    }

    private final CacheManager cache;

    public LimitJsRule(String name, int status, int priority, List<String> matchedRuleIds, String resource, int burst, String burstUnit, CacheManager cache) {
        super(cache, name, status, priority, matchedRuleIds, resource, burst, burstUnit);
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
        Param limitJsData = cache.getParam(ipCacheKey, getLimitJSSchema());
        if (limitJsData == null) {
            Table<Param> dataSet = Table.builder();
            String cookieSid = UUID.randomUUID().toString();
            String cookieSignature = RandomUtil.getRandomString(32);
            dataSet.add(Param.builder(Info.DataSet.ID, cookieSid).setString(Info.DataSet.SIGNATURE, cookieSignature));
            limitJsData = Param.builder()
                    .setLong(Info.DATE, 0L)
                    .setBoolean(Info.PASS, false)
                    .setInt(Info.FAIL, 0)
                    .setList(Info.DATASET, dataSet);
            cache.put(ipCacheKey, limitJsData, getLimitJSSchema());
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
        if (!limitJsData.getBoolean(Info.PASS)) {
            return true;
        }
        // 判断是否已经验证过并且未过期，则直接通过
        long date = limitJsData.getLong(Info.DATE);
        if (System.currentTimeMillis() - date < LIMIT_JS_PASS_TIME) {
            return false;
        }
        // 3. 如果没有通过验证，则进行验证
        String signatureCookie = getSignatureCookie(request);
        // 如果客户端没有传递签名Cookie，则判断是否超过最大验证数量
        if (signatureCookie == null) {
            int fail = limitJsData.getInt(Info.FAIL);
            if (fail >= LIMIT_MAX_FAIL_TIMES) {
                return true;
            }
            limitJsData.setInt(Info.FAIL, fail + 1);
            String cookieSid = UUID.randomUUID().toString();
            String cookieSignature = RandomUtil.getRandomString(32);
            Table<Param> dataSet = limitJsData.getTable(Info.DATASET);
            dataSet.add(Param.builder(Info.DataSet.ID, cookieSid).setString(Info.DataSet.SIGNATURE, cookieSignature));
            cache.put(ipCacheKey, limitJsData, getLimitJSSchema());
            response.setCookie(new Cookie(HEADER_COOKIE_SID_KEY, cookieSid));
            response.setCookie(new Cookie(HEADER_COOKIE_PREFIX + cookieSid, cookieSignature));
            return false;
        }
        // 验证签名
        Table<Param> dataSet = limitJsData.getTable(Info.DATASET);
        for (Param data : dataSet) {
            String signature = data.getString(Info.DataSet.SIGNATURE);
            if (signature.equals(signatureCookie)) {
                limitJsData.setLong(Info.DATE, System.currentTimeMillis());
                limitJsData.setBoolean(Info.PASS, true);
                cache.put(ipCacheKey, limitJsData, getLimitJSSchema());
                return false;
            }
            dataSet.remove(data);
            cache.put(ipCacheKey, limitJsData, getLimitJSSchema());
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

    private static ProtoSchema getLimitJSSchema() {
        return LIMIT_JS_SCHEMA;
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
