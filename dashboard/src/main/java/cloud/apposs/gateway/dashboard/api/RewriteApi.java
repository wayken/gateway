package cloud.apposs.gateway.dashboard.api;

import cloud.apposs.gateway.dashboard.api.database.app.CommonDef;
import cloud.apposs.gateway.dashboard.api.model.RewriteModel;
import cloud.apposs.gateway.dashboard.interceptor.OperationLog;
import cloud.apposs.gateway.dashboard.interceptor.OperationType;
import cloud.apposs.gateway.dashboard.interceptor.Permission;
import cloud.apposs.gateway.dashboard.node.INode;
import cloud.apposs.gateway.dashboard.util.Ids;
import cloud.apposs.gateway.plugin.runner.rewrite.RewriteConstant;
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
public class RewriteApi {
    @Autowired
    private INode node;

    @Request.Get("/api/v1/gateway/{zone}/rewrite/request/rule/{id}")
    public React<StandardResult> gettRewriteRequestRuleInfo(@Variable("zone") String zone, @Variable("id") String id) {
        return React.emitter(() -> {
            Param infomation = node.getRewriteRequestRuleInfo(zone, id);
            if (infomation == null) {
                return StandardResult.error(Errno.ENOT_FOUND, "Rewrite Request Rule " + id + " not found");
            }
            return StandardResult.success(infomation, false);
        });
    }

    @Request.Get("/api/v1/gateway/{zone}/rewrite/request/rules")
    @Permission("website:rewrite:request:list")
    public React<StandardResult> getRewriteRequestRuleList(@Variable("zone") String zone) {
        return React.emitter(() -> {
            Table<Param> dataList = node.getRewriteRequestRuleList(zone);
            if (dataList == null) {
                dataList = Table.builder();
            }
            return StandardResult.success(dataList, false);
        });
    }

    @Request.Put("/api/v1/gateway/{zone}/rewrite/request/rule")
    @Permission("website:rewrite:request:add")
    @OperationLog(type = OperationType.ADD, module = "request")
    public React<StandardResult> addRewriteRequestRule(@Variable("zone") String zone, @Model RewriteModel.Response.Add request) {
        return React.emitter(() -> {
            String id = Ids.getInstance().nextId();
            Param infomation = Param.builder(RewriteConstant.KEY_NAME, request.getName())
                    .setInt(RewriteConstant.KEY_STATUS, request.getStatus())
                    .setString(RewriteConstant.KEY_RULE, request.getRule())
                    .setList(RewriteConstant.KEY_ACTION, request.getAction())
                    .setList(RewriteConstant.KEY_ROUTES, request.getRoutes())
                    .setString(RewriteConstant.KEY_REMARK, request.getRemark());
            boolean success = node.addRewriteRequestRule(zone, id, infomation);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    @Request.Post("/api/v1/gateway/{zone}/rewrite/request/rule/{id}")
    @Permission("website:rewrite:request:edit")
    @OperationLog(type = OperationType.UPDATE, module = "request")
    public React<StandardResult> updateRewriteRequestRule(@Variable("zone") String zone, @Variable("id") String id, @Model RewriteModel.Response.Update request) {
        return React.emitter(() -> {
            Param infomation = Param.builder(RewriteConstant.KEY_NAME, request.getName())
                    .setInt(RewriteConstant.KEY_STATUS, request.getStatus())
                    .setString(RewriteConstant.KEY_RULE, request.getRule())
                    .setList(RewriteConstant.KEY_ACTION, request.getAction())
                    .setList(RewriteConstant.KEY_ROUTES, request.getRoutes())
                    .setString(RewriteConstant.KEY_REMARK, request.getRemark());
            boolean success = node.updateRewriteRequestRule(zone, request.getId(), infomation);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    @Request.Delete("/api/v1/gateway/{zone}/rewrite/request/rule/{id}")
    @Permission("website:rewrite:request:delete")
    @OperationLog(type = OperationType.DELETE, module = "request")
    public React<StandardResult> removeRewriteRequestRule(@Variable("zone") String zone, @Variable("id") String id) {
        return React.emitter(() -> {
            boolean success = node.removeRewriteRequestRule(zone, id);
            if (!success) {
                return StandardResult.error(Errno.EARGUMENT, "Rewrite Request Rule " + id + " delete failed");
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    @Request.Get("/api/v1/gateway/{zone}/rewrite/response/rule/{id}")
    public React<StandardResult> gettRewriteResponseRuleInfo(@Variable("zone") String zone, @Variable("id") String id) {
        return React.emitter(() -> {
            Param infomation = node.getRewriteResponseRuleInfo(zone, id);
            if (infomation == null) {
                return StandardResult.error(Errno.ENOT_FOUND, "Rewrite Response Rule " + id + " not found");
            }
            return StandardResult.success(infomation, false);
        });
    }

    @Request.Get("/api/v1/gateway/{zone}/rewrite/response/rules")
    @Permission("website:rewrite:response:list")
    public React<StandardResult> getRewriteResponseRuleList(@Variable("zone") String zone) {
        return React.emitter(() -> {
            Table<Param> dataList = node.getRewriteResponseRuleList(zone);
            if (dataList == null) {
                dataList = Table.builder();
            }
            return StandardResult.success(dataList, false);
        });
    }

    @Request.Put("/api/v1/gateway/{zone}/rewrite/response/rule")
    @Permission("website:rewrite:response:add")
    @OperationLog(type = OperationType.ADD, module = "response")
    public React<StandardResult> addRewriteResponseRule(@Variable("zone") String zone, @Model RewriteModel.Response.Add request) {
        return React.emitter(() -> {
            String id = Ids.getInstance().nextId();
            Param infomation = Param.builder(RewriteConstant.KEY_NAME, request.getName())
                    .setInt(RewriteConstant.KEY_STATUS, request.getStatus())
                    .setString(RewriteConstant.KEY_RULE, request.getRule())
                    .setList(RewriteConstant.KEY_ACTION, request.getAction())
                    .setList(RewriteConstant.KEY_ROUTES, request.getRoutes())
                    .setString(RewriteConstant.KEY_REMARK, request.getRemark());
            boolean success = node.addRewriteResponseRule(zone, id, infomation);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    @Request.Post("/api/v1/gateway/{zone}/rewrite/response/rule/{id}")
    @Permission("website:rewrite:response:edit")
    @OperationLog(type = OperationType.UPDATE, module = "response")
    public React<StandardResult> updateRewriteResponseRule(@Variable("zone") String zone, @Variable("id") String id, @Model RewriteModel.Response.Update request) {
        return React.emitter(() -> {
            Param infomation = Param.builder(RewriteConstant.KEY_NAME, request.getName())
                    .setInt(RewriteConstant.KEY_STATUS, request.getStatus())
                    .setString(RewriteConstant.KEY_RULE, request.getRule())
                    .setList(RewriteConstant.KEY_ACTION, request.getAction())
                    .setList(RewriteConstant.KEY_ROUTES, request.getRoutes())
                    .setString(RewriteConstant.KEY_REMARK, request.getRemark());
            boolean success = node.updateRewriteResponseRule(zone, request.getId(), infomation);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    @Request.Delete("/api/v1/gateway/{zone}/rewrite/response/rule/{id}")
    @Permission("website:rewrite:response:delete")
    @OperationLog(type = OperationType.DELETE, module = "response")
    public React<StandardResult> removeRewriteResponseRule(@Variable("zone") String zone, @Variable("id") String id) {
        return React.emitter(() -> {
            boolean success = node.removeRewriteResponseRule(zone, id);
            if (!success) {
                return StandardResult.error(Errno.EARGUMENT, "Rewrite Response Rule " + id + " delete failed");
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }
}
