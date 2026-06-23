package cloud.apposs.gateway.ai;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayException;
import cloud.apposs.util.HttpStatus;
import cloud.apposs.util.JsonUtil;
import cloud.apposs.util.Param;
import cloud.apposs.util.Table;

public final class AIProviderParser {
    public static AIProvider parse(String id, String content, GatewayContext context) throws Exception {
        Param infomation = JsonUtil.parseJsonParam(content);
        if (infomation == null || infomation.isEmpty()) {
            throw new GatewayException(HttpStatus.HTTP_STATUS_500, "AIProvider: " + id + " config is invalid");
        }
        String name = infomation.getString(AIProviderConstant.KEY_NAME);
        String type = infomation.getString(AIProviderConstant.KEY_TYPE);
        boolean system = infomation.getBoolean(AIProviderConstant.KEY_TYPE, false);
        String url = infomation.getString(AIProviderConstant.KEY_URL);
        Table<String> models = infomation.getTable(AIProviderConstant.KEY_MODELS);
        Table<Param> keys = infomation.getTable(AIProviderConstant.KEY_KEYS);
        if (name == null || type == null || url == null) {
            throw new GatewayException(HttpStatus.HTTP_STATUS_500, "Certificate: " + id + " config is invalid");
        }
        return new AIProvider(id, name, type, system, url, models, keys);
    }
}
