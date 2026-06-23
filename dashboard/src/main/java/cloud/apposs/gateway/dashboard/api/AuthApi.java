package cloud.apposs.gateway.dashboard.api;

import cloud.apposs.gateway.dashboard.api.database.app.CommonDef;
import cloud.apposs.gateway.dashboard.api.model.AuthModel;
import cloud.apposs.gateway.dashboard.interceptor.OperationLog;
import cloud.apposs.gateway.dashboard.interceptor.OperationType;
import cloud.apposs.gateway.dashboard.interceptor.Permission;
import cloud.apposs.gateway.dashboard.node.INode;
import cloud.apposs.gateway.dashboard.util.Ids;
import cloud.apposs.gateway.global.auth.AuthConstant;
import cloud.apposs.gateway.global.auth.AuthType;
import cloud.apposs.gateway.global.auth.jwt.JwtAuth;
import cloud.apposs.gateway.global.auth.key.KeyAuth;
import cloud.apposs.gateway.global.auth.server.ServerAuth;
import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.react.React;
import cloud.apposs.rest.annotation.Model;
import cloud.apposs.rest.annotation.Request;
import cloud.apposs.rest.annotation.RestAction;
import cloud.apposs.rest.annotation.Variable;
import cloud.apposs.util.Errno;
import cloud.apposs.util.Param;
import cloud.apposs.util.StandardResult;
import cloud.apposs.util.Table;

@RestAction
public class AuthApi {
    @Autowired
    private INode node;

    @Request.Get("/api/v1/gateway/auth/{id}")
    public React<StandardResult> getAuthInfo(@Variable("id") String id) {
        return React.emitter(() -> {
            Param infomation = node.getAuthInfo(id);
            if (infomation == null) {
                return StandardResult.error(Errno.ENOT_FOUND, "Auth " + id + " not found");
            }
            return StandardResult.success(infomation, false);
        });
    }

    @Request.Get("/api/v1/gateway/auths")
    @Permission("system:setting:auth:list")
    public React<StandardResult> getAuthList() {
        return React.emitter(() -> {
            Table<Param> dataList = node.getAuthList();
            if (dataList == null) {
                dataList = Table.builder();
            }
            return StandardResult.success(dataList, false);
        });
    }

    @Request.Put("/api/v1/gateway/auth")
    @Permission("system:setting:auth:add")
    @OperationLog(type = OperationType.ADD, module = "auth")
    public React<StandardResult> addAuth(@Model AuthModel.Add request) {
        return React.emitter(() -> {
            String id = Ids.getInstance().nextId();
            Param infomation = Param.builder(AuthConstant.KEY_ID, id)
                    .setString(AuthConstant.KEY_NAME, request.getName())
                    .setString(AuthConstant.KEY_TYPE, request.getType())
                    .setString(AuthConstant.KEY_REMARK, request.getRemark());
            Param parameters = request.getParameters();
            if (AuthType.KEY.matched(request.getType())) {
                infomation.assign(parameters, KeyAuth.KEY_CREDENTIAL);
                infomation.assign(parameters, KeyAuth.KEY_SOURCE);
                infomation.assign(parameters, KeyAuth.KEY_PARAM);
            } else if (AuthType.JWT.matched(request.getType())) {
                infomation.assign(parameters, JwtAuth.KEY_CREDENTIAL);
                infomation.assign(parameters, JwtAuth.KEY_SOURCE);
                infomation.assign(parameters, JwtAuth.KEY_PARAM);
            } else if (AuthType.SERVER.matched(request.getType())) {
                infomation.assign(parameters, ServerAuth.KEY_NODES);
            }
            boolean success = node.addAuth(id, infomation);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    @Request.Post("/api/v1/gateway/auth/{id}")
    @Permission("system:setting:auth:edit")
    @OperationLog(type = OperationType.UPDATE, module = "auth")
    public React<StandardResult> updateAuth(@Variable("id") String id, @Model AuthModel.Update request) {
        return React.emitter(() -> {
            Param infomation = Param.builder(AuthConstant.KEY_ID, id)
                    .setString(AuthConstant.KEY_NAME, request.getName())
                    .setString(AuthConstant.KEY_TYPE, request.getType())
                    .setString(AuthConstant.KEY_REMARK, request.getRemark());
            Param parameters = request.getParameters();
            if (AuthType.KEY.matched(request.getType())) {
                infomation.assign(parameters, KeyAuth.KEY_CREDENTIAL);
                infomation.assign(parameters, KeyAuth.KEY_SOURCE);
                infomation.assign(parameters, KeyAuth.KEY_PARAM);
            } else if (AuthType.JWT.matched(request.getType())) {
                infomation.assign(parameters, JwtAuth.KEY_CREDENTIAL);
                infomation.assign(parameters, JwtAuth.KEY_SOURCE);
                infomation.assign(parameters, JwtAuth.KEY_PARAM);
            } else if (AuthType.SERVER.matched(request.getType())) {
                infomation.assign(parameters, ServerAuth.KEY_NODES);
            }
            boolean success = node.updateAuth(request.getId(), infomation);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    @Request.Delete("/api/v1/gateway/auth/{id}")
    @Permission("system:setting:auth:delete")
    @OperationLog(type = OperationType.DELETE, module = "auth")
    public React<StandardResult> deleteAuth(@Variable("id") String id) {
        return React.emitter(() -> {
            boolean success = node.deleteAuth(id);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }
}
