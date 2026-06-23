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

import java.util.List;
import java.util.UUID;

/**
 * 基于Cookie校验的拦截规则，拦截原理如下
 * <pre>
 *     1. 拦截匹配的请求，并针对每个IP生成一个Cookie，Cookie中包含一个SID和SIGNATURE，SID用于标识Cookie，SIGNATURE用于验证Cookie
 *     2. 用户首次进来时会生成一个Cookie，并传递给客户端，因为IP可能是工作室IP，所以每个IP进来时会标记多一个ID
 *     3. 当用户再次进来时会验证Cookie，如果验证通过则表示用户已经通过验证，否则进行记录失败次数，如果超过最大次数则进行拦截
 *     4. 验证通过之后在指定时间内则不再进行验证
 * </pre>
 */
public class LimitCookieRule extends LimitRule {
    private static final String CACHE_KEY_PREFIX = "Sys:Limit:Cookie:";
    private static final String HEADER_COOKIE_PREFIX = "X-W-";
    private static final String HEADER_COOKIE_SID_KEY = "X-W-SID";

    // 表元信息
    private static final int LIMIT_COOKIE_PASS_TIME = 5 * 60 * 1000;
    private static final int LIMIT_MAX_FAIL_TIMES = 3;
    private static final ProtoSchema LIMIT_DATASET_SCHEMA = ProtoSchema.mapSchema();
    static {
        LIMIT_DATASET_SCHEMA.addKey(Info.DataSet.ID, String.class);
        LIMIT_DATASET_SCHEMA.addKey(Info.DataSet.SIGNATURE, String.class);
    }
    private static final ProtoSchema LIMIT_COOKIE_SCHEMA = ProtoSchema.mapSchema();
    static {
        LIMIT_COOKIE_SCHEMA.addKey(Info.DATE, Long.class);
        LIMIT_COOKIE_SCHEMA.addKey(Info.AUTHED, Boolean.class);
        LIMIT_COOKIE_SCHEMA.addKey(Info.FAIL, Integer.class);
        LIMIT_COOKIE_SCHEMA.addKey(Info.DATASET, Table.class, ProtoSchema.listSchema(Param.class, LIMIT_DATASET_SCHEMA));
    }

    private final CacheManager cache;

    public LimitCookieRule(String name, int status, int priority, List<String> matchedRuleIds, String resource, int burst, String burstUnit, CacheManager cache) {
        super(cache, name, status, priority, matchedRuleIds, resource, burst, burstUnit);
        this.cache = cache;
    }

    @Override
    public boolean evaluate(Facts facts, Object... arguments) {
        boolean result = super.evaluate(facts, arguments);
        if (!result) {
            return false;
        }

        // 匹配到了规则，进行后续的Cookie质询操作
        // 1. 先判断是否已经触发了质询，如果是首次触发，需要进行初始化记录并将Cookie返回给客户端
        GatewayHttpRequest request = (GatewayHttpRequest) arguments[1];
        GatewayHttpResponse response = (GatewayHttpResponse) arguments[2];
        String realIp = WebUtil.getRequestIp(request);
        String ipCacheKey = CACHE_KEY_PREFIX + realIp;
        Param limitCookieData = cache.getParam(ipCacheKey, getLimitCookieSchema());
        if (limitCookieData == null) {
            Table<Param> dataSet = Table.builder();
            String cookieSid = UUID.randomUUID().toString();
            String cookieSignature = RandomUtil.getRandomString(32);
            dataSet.add(Param.builder(Info.DataSet.ID, cookieSid).setString(Info.DataSet.SIGNATURE, cookieSignature));
            limitCookieData = Param.builder()
                    .setLong(Info.DATE, 0L)
                    .setBoolean(Info.AUTHED, false)
                    .setInt(Info.FAIL, 0)
                    .setList(Info.DATASET, dataSet);
            cache.put(ipCacheKey, limitCookieData, getLimitCookieSchema());
            response.setCookie(new Cookie(HEADER_COOKIE_SID_KEY, cookieSid));
            response.setCookie(new Cookie(HEADER_COOKIE_PREFIX + cookieSid, cookieSignature));
            return false;
        }
        // 2. 如果已经有记录则进行验证
        if (limitCookieData.getBoolean(Info.AUTHED)) {
            // 判断是否已经验证过并且未过期，则直接通过
            long date = limitCookieData.getLong(Info.DATE);
            if (System.currentTimeMillis() - date < LIMIT_COOKIE_PASS_TIME) {
                return false;
            }
        }
        // 3. 如果没有通过验证，则进行验证
        String signatureCookie = getSignatureCookie(request);
        // 如果客户端没有传递签名Cookie，则判断是否超过最大验证数量
        if (signatureCookie == null) {
            int fail = limitCookieData.getInt(Info.FAIL);
            if (fail >= LIMIT_MAX_FAIL_TIMES) {
                return true;
            }
            limitCookieData.setInt(Info.FAIL, fail + 1);
            String cookieSid = UUID.randomUUID().toString();
            String cookieSignature = RandomUtil.getRandomString(32);
            Table<Param> dataSet = limitCookieData.getTable(Info.DATASET);
            dataSet.add(Param.builder(Info.DataSet.ID, cookieSid).setString(Info.DataSet.SIGNATURE, cookieSignature));
            cache.put(ipCacheKey, limitCookieData, getLimitCookieSchema());
            response.setCookie(new Cookie(HEADER_COOKIE_SID_KEY, cookieSid));
            response.setCookie(new Cookie(HEADER_COOKIE_PREFIX + cookieSid, cookieSignature));
            return false;
        }
        // 验证签名
        Table<Param> dataSet = limitCookieData.getTable(Info.DATASET);
        for (Param data : dataSet) {
            String signature = data.getString(Info.DataSet.SIGNATURE);
            if (signature.equals(signatureCookie)) {
                limitCookieData.setLong(Info.DATE, System.currentTimeMillis());
                limitCookieData.setBoolean(Info.AUTHED, true);
                limitCookieData.setInt(Info.FAIL, 0);
                cache.put(ipCacheKey, limitCookieData, getLimitCookieSchema());
                return false;
            }
            dataSet.remove(data);
            cache.put(ipCacheKey, limitCookieData, getLimitCookieSchema());
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

    private static ProtoSchema getLimitCookieSchema() {
        return LIMIT_COOKIE_SCHEMA;
    }

    private static class Info {
        // 验证时间
        public static final String DATE = "date";
        // 验证是否通过
        public static final String AUTHED = "authed";
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
