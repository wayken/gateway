package cloud.apposs.gateway.plugin.runner.limit;

public final class LimitConstant {
    /** 规则配置路径 */
    public static final String DEFAULT_LIMIT_PATH = "/limit";

    /** 规则配置项 */
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_RULE = "rule";
    public static final String KEY_STATUS = "status";
    public static final String KEY_ROUTE = "route";
    public static final String KEY_RESOURCES = "resources";
    public static final String KEY_RESOURCE_KEY = "key";
    public static final String KEY_RESOURCE_VALUE = "value";
    public static final String KEY_BURST = "burst";
    public static final String KEY_BURST_UNIT = "burstUnit";
    public static final String KEY_ROUTES = "routes";
    public static final String KEY_PRIORITY = "priority";
    public static final String KEY_ACTION = "action";
    public static final String KEY_REMARK = "remark";

    /** 规则动作 */
    public static class Action {
        // 配置项
        public static final String KEY_TYPE = "type";
        public static final String KEY_RESPONSE = "response";
        public static final String KEY_CODE = "code";
        public static final String KEY_CONTENT = "content";
        public static final String KEY_DURATION = "duration";
        public static final String KEY_DURATION_UNIT = "unit";

        // 动作类型
        public static final String BLOCK = "block";
        public static final String LOG = "log";
        public static final String SKIP = "skip";
        public static final String JS_CHALLENGE = "js_challenge";
        public static final String INTERACTIVE_CHALLENGE = "interactive_challenge";

        // 时间单位
        public static final String TIME_UNIT_SECOND = "second";
        public static final String TIME_UNIT_MINUTE = "minute";
        public static final String TIME_UNIT_HOUR = "hour";
        public static final String TIME_UNIT_DAY = "day";

        public static boolean isSkip(String type) {
            return SKIP.equals(type) || LOG.equals(type);
        }
    }
}
