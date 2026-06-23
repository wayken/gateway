package cloud.apposs.gateway.global.auth;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayException;
import cloud.apposs.gateway.global.auth.jwt.JwtAuth;
import cloud.apposs.gateway.global.auth.key.KeyAuth;
import cloud.apposs.gateway.global.auth.server.ServerAuth;
import cloud.apposs.util.HttpStatus;
import cloud.apposs.util.JsonUtil;
import cloud.apposs.util.Param;

public final class AuthParser {
    /**
     * 解析鉴权配置，内容格式如下：
     * <pre>
     *   {
     *     "name": "JwtAuth",
     *     "type": "jwt"
     *     ...
     *   }
     * </pre>
     *
     * @param  id      鉴权ID
     * @param  content 鉴权配置内容
     * @param  context 网关上下文
     * @return 鉴权对象，如果无法找到匹配的鉴权则返回null
     * @throws Exception 如果配置不符合规范则抛出异常
     */
    public static Auth parse(String id, String content, GatewayContext context) throws Exception {
        Param infomation = JsonUtil.parseJsonParam(content);
        return parse(id, infomation, context);
    }

    public static Auth parse(String id, Param infomation, GatewayContext context) throws Exception {
        // 解析鉴权基本配置
        String name = infomation.getString(AuthConstant.KEY_NAME);
        String type = infomation.getString(AuthConstant.KEY_TYPE);
        if (name == null || type == null) {
            throw new GatewayException(HttpStatus.HTTP_STATUS_500, "Auth: " + id + " config is invalid");
        }
        type = type.toLowerCase();
        // 根据上游类型解析不同的上游对象
        if (AuthType.KEY.matched(type)) {
            return new KeyAuth(id, name, context, infomation);
        } else if(AuthType.JWT.matched(type)) {
            return new JwtAuth(id, name, context, infomation);
        } else if(AuthType.SERVER.matched(type)) {
            return new ServerAuth(id, name, context, infomation);
        } else if (AuthType.CUSTOM.matched(type)) {
            // 如果是业务自定义上游，则通过网关上下文IOC容器来获取
            String authClassName = infomation.getString(AuthConstant.KEY_CLASS);
            if (authClassName == null || authClassName.isEmpty()) {
                return null;
            }
            return (Auth) context.getCompiler().getBean(authClassName);
        }
        return null;
    }
}
