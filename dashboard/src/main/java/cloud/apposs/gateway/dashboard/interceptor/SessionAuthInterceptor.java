package cloud.apposs.gateway.dashboard.interceptor;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.bootor.WebUtil;
import cloud.apposs.bootor.interceptor.BooterInterceptorAdaptor;
import cloud.apposs.cache.CacheManager;
import cloud.apposs.gateway.dashboard.api.database.SessionApi;
import cloud.apposs.gateway.dashboard.api.database.app.SessionDef;
import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.react.React;
import cloud.apposs.rest.Handler;
import cloud.apposs.rest.annotation.Order;
import cloud.apposs.util.Param;
import cloud.apposs.util.StrUtil;

import java.util.Arrays;
import java.util.List;

@Component
@Order(1024)
public class SessionAuthInterceptor extends BooterInterceptorAdaptor {
    @Autowired
    private SessionApi sessionApi;

    private final List<String> whitePathList = Arrays.asList(
            "/api/v1/session",
            "/api/v1/session/login",
            "/api/v1/session/mfa",
            "/api/v1/session/logout"
    );

    @Override
    public React<Boolean> preHandle(BootorHttpRequest request, BootorHttpResponse response, Handler handler) throws Exception {
        return React.emitter(() -> {
            // 白名单路径直接放行
            String path = WebUtil.getRequestPath(request);
            if (whitePathList.contains(path)) {
                return true;
            }
            // 只针对API接口才需校验会话，静态资源不处理
            if (!path.startsWith("/api")) {
                return true;
            }
            // 校验会话是否有效
            String token = request.getHeader("x-auth-token");
            if (StrUtil.isEmpty(token)) {
                return false;
            }
            long sessionId = SessionDef.parseSessionId(token);
            if (sessionId <= 0) {
                return false;
            }
            // 校验会话是否有登录缓存
            String sessionKey = SessionDef.getSessionCacheKey(sessionId);
            CacheManager cache = sessionApi.getCache();
            Param sessionInfo = cache.hgetParam(sessionKey, token, SessionDef.Protocol.getInfoSessionSchema());
            if (sessionInfo == null) {
                return false;
            }
            request.setAttribute(SessionDef.SESSION_ATTRIBUTE_KEY_USER_ID, sessionInfo.getLong(SessionDef.Info.UID));
            return true;
        });
    }
}
