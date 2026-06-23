package cloud.apposs.gateway.global.auth;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.react.React;

/**
 * 服务鉴权接口，可实现包括：
 * <pre>
 *     1. API Key 认证
 *     2. JWT 认证
 *     3. 自定义授权服务认证
 * </pre>
 */
public interface Auth {
    /**
     * 获取鉴权ID
     */
    String getId();

    /**
     * 获取鉴权名称
     */
    String getName();

    /**
     * 获取鉴权类型，详见{@link AuthType}
     */
    String getType();

    /**
     * 进行请求鉴权
     */
    React<Boolean> auth(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route) throws Exception;
}
