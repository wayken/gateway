package cloud.apposs.gateway.plugin.runner.rewrite;

public class RewriteConstant {
    // 修改请求头规则
    public static final String REWRITE_REQUEST_PATH = "/rewrite/request";
    // 修改响应头规则
    public static final String REWRITE_RESPONSE_PATH = "/rewrite/response";

    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_RULE = "rule";
    public static final String KEY_STATUS = "status";
    public static final String KEY_ROUTES = "routes";
    public static final String KEY_REMARK = "remark";
    public static final String KEY_ACTION = "action";
    public static final String KEY_ACTION_TYPE = "type";
    public static final String KEY_ACTION_TYPE_REMOVE = "remove";
    public static final String KEY_ACTION_TYPE_STATIC = "static";
    public static final String KEY_ACTION_TYPE_DYNAMIC = "dynamic";
    public static final String KEY_ACTION_HEADER_NAME = "name";
    public static final String KEY_ACTION_HEADER_VALUE = "value";

    public static final String RULE_TYPE_PATTERN = "pattern";
    public static final String RULE_TYPE_MVEL = "mvel";
}
