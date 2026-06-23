package cloud.apposs.gateway.plugin.runner.waf;

public final class WafConstant {
    /** WAF规则配置路径 */
    public static final String DEFAULT_WAF_PATH = "/waf/default";
    public static final String SYSTEM_WAF_PATH = "/waf/system";

    /** WAF规则配置项 */
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_RULE = "rule";
    public static final String KEY_ROUTES = "routes";
    public static final String KEY_PRIORITY = "priority";
    public static final String KEY_STATUS = "status";
    public static final String KEY_ACTION = "action";
    public static final String KEY_REMARK = "remark";

    /** WAF规则动作 */
    public static class Action {
        // 配置项
        public static final String KEY_TYPE = "type";
        public static final String KEY_RESPONSE = "response";
        public static final String KEY_CODE = "code";
        public static final String KEY_CONTENT = "content";

        // 动作类型
        public static final String BLOCK = "block";
        public static final String LOG = "log";
        public static final String SKIP = "skip";
        // 拦截规则
        public static final String COOKIE_CHALLENGE = "cookie_challenge";
        public static final String JS_CHALLENGE = "js_challenge";
        public static final String INTERACTIVE_CHALLENGE = "interactive_challenge";

        public static boolean isSkip(String type) {
            return SKIP.equals(type) || LOG.equals(type);
        }
    }
}
