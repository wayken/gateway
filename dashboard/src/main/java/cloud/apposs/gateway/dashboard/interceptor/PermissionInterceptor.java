package cloud.apposs.gateway.dashboard.interceptor;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.bootor.interceptor.BooterInterceptorAdaptor;
import cloud.apposs.gateway.dashboard.api.database.RoleApi;
import cloud.apposs.gateway.dashboard.api.database.UserApi;
import cloud.apposs.gateway.dashboard.api.database.app.RoleDef;
import cloud.apposs.gateway.dashboard.api.database.app.SessionDef;
import cloud.apposs.gateway.dashboard.api.database.app.UserDef;
import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.react.React;
import cloud.apposs.rest.Handler;
import cloud.apposs.rest.annotation.Order;
import cloud.apposs.util.*;

@Component
@Order(1025)
public class PermissionInterceptor extends BooterInterceptorAdaptor {
    @Autowired
    private UserApi userApi;

    @Autowired
    private RoleApi roleApi;

    @Override
    public React<Boolean> preHandle(BootorHttpRequest request, BootorHttpResponse response, Handler handler) throws Exception {
        return React.emitter(() -> {
            // 没有配置Permission注解直接跳过检测
            Permission permissionAnnotation = handler.getAnnotation(Permission.class);
            if (permissionAnnotation == null) {
                return true;
            }
            // 用户登录ID为空，拒绝
            Long uid = (Long) request.getAttribute(SessionDef.SESSION_ATTRIBUTE_KEY_USER_ID);
            if (uid == null) {
                return false;
            }
            // 不存在用户？拒绝
            Param userInfo = userApi.handleDataLoadById(uid);
            if (userInfo == null) {
                return false;
            }
            // 用户没有配置角色，拒绝
            String roleIds = userInfo.getString(UserDef.Info.ROLE_IDS);
            if (StrUtil.isEmpty(roleIds)) {
                return false;
            }
            // 开始校验用户角色权限
            Table<String> roleIdList = JsonUtil.parseJsonTable(roleIds);
            for (String roleId : roleIdList) {
                Param roleInfo = roleApi.handleDataLoadById(Long.parseLong(roleId));
                if (ResultSets.isEmpty(roleInfo)) {
                    continue;
                }
                boolean isAdmin = roleInfo.getInt(RoleDef.Info.IS_ADMIN) == 1;
                if (isAdmin) {
                    return true;
                }
                String permissions = roleInfo.getString(RoleDef.Info.PERMISSIONS);
                if (StrUtil.isEmpty(permissions)) {
                    continue;
                }
                Table<String> permissionList = JsonUtil.parseJsonTable(permissions);
                if (permissionList.contains(permissionAnnotation.value())) {
                    return true;
                }
            }
            return false;
        });
    }
}
