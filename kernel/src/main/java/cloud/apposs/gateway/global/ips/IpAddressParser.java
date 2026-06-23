package cloud.apposs.gateway.global.ips;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayException;
import cloud.apposs.util.HttpStatus;
import cloud.apposs.util.JsonUtil;
import cloud.apposs.util.Param;
import cloud.apposs.util.Table;

public final class IpAddressParser {
    public static IpAddress parse(String id, String content, GatewayContext context) throws Exception {
        Param infomation = JsonUtil.parseJsonParam(content);
        if (infomation == null || infomation.isEmpty()) {
            throw new GatewayException(HttpStatus.HTTP_STATUS_500, "IpAddress: " + id + " config is invalid");
        }
        String name = infomation.getString(IpAddressConstant.KEY_NAME);
        Table<String> cidrs = infomation.getTable(IpAddressConstant.KEY_CIDRS);
        if (name == null || cidrs == null) {
            throw new GatewayException(HttpStatus.HTTP_STATUS_500, "IpAddress: " + id + " config is invalid");
        }
        return new IpAddress(id, name, cidrs);
    }
}
