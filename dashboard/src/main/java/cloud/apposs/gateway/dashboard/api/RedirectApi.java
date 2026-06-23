package cloud.apposs.gateway.dashboard.api;

import cloud.apposs.gateway.dashboard.api.database.app.CommonDef;
import cloud.apposs.gateway.dashboard.api.model.RedirectModel;
import cloud.apposs.gateway.dashboard.interceptor.OperationLog;
import cloud.apposs.gateway.dashboard.interceptor.OperationType;
import cloud.apposs.gateway.dashboard.interceptor.Permission;
import cloud.apposs.gateway.dashboard.node.INode;
import cloud.apposs.gateway.dashboard.util.Ids;
import cloud.apposs.gateway.plugin.runner.redirect.RedirectConstant;
import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.logger.Logger;
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
public class RedirectApi {
    @Autowired
    private INode node;

    // 获取区域重定向规则
    @Request.Get("/api/v1/gateway/{zone}/rewrite/redirect/rule/{id}")
    public React<StandardResult> getRedirectRuleInfo(@Variable("zone") String zone, @Variable("id") String id) {
        return React.emitter(() -> {
            Param infomation = node.getRedirectRuleInfo(zone, id);
            if (infomation == null) {
                return StandardResult.error(Errno.ENOT_FOUND, "Redirect Rule " + id + " not found");
            }
            return StandardResult.success(infomation, false);
        });
    }

    // 获取区域重定向列表
    @Request.Get("/api/v1/gateway/{zone}/rewrite/redirect/rules")
    @Permission("website:rewrite:redirect:list")
    public React<StandardResult> getRedirectRuleList(@Variable("zone") String zone) {
        return React.emitter(() -> {
            Table<Param> dataList = node.getRedirectRuleList(zone);
            if (dataList == null) {
                dataList = Table.builder();
            }
            return StandardResult.success(dataList, false);
        });
    }

    // 添加区域重定向规则
    @Request.Put("/api/v1/gateway/{zone}/rewrite/redirect/rule")
    @Permission("website:rewrite:redirect:add")
    @OperationLog(type = OperationType.ADD, module = "redirect")
    public React<StandardResult> addRedirectRule(@Variable("zone") String zone, @Model RedirectModel.Add request) {
        return React.emitter(() -> {
            String id = Ids.getInstance().nextId();
            Param infomation = Param.builder(RedirectConstant.KEY_NAME, request.getName())
                    .setInt(RedirectConstant.KEY_STATUS, request.getStatus())
                    .setString(RedirectConstant.KEY_MODE, request.getMode())
                    .setString(RedirectConstant.KEY_RULE, request.getRule())
                    .setParam(RedirectConstant.KEY_ACTION, request.getAction())
                    .setList(RedirectConstant.KEY_ROUTES, request.getRoutes())
                    .setString(RedirectConstant.KEY_REMARK, request.getRemark());
            boolean success = node.addRedirectRule(zone, id, infomation);
            if (!success) {
                Logger.error("Add Redirect Rule Error, zone=%s, id=%s, status=%d, routes=%s, rule=%s, action=%s",
                        zone, id, request.getStatus(), request.getRoutes(), request.getRule(), request.getAction());
                return StandardResult.error(Errno.ERROR);
            }
            Logger.info("Add Redirect Rule Success, zone=%s, id=%s, status=%d, routes=%s, rule=%s, action=%s",
                    zone, id, request.getStatus(), request.getRoutes(), request.getRule(), request.getAction());
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    // 更新区域重定向规则
    @Request.Post("/api/v1/gateway/{zone}/rewrite/redirect/rule/{id}")
    @Permission("website:rewrite:redirect:edit")
    @OperationLog(type = OperationType.UPDATE, module = "redirect")
    public React<StandardResult> updateRedirectRule(@Variable("zone") String zone, @Variable("id") String id, @Model RedirectModel.Update request) {
        return React.emitter(() -> {
            Param infomation = Param.builder(RedirectConstant.KEY_NAME, request.getName())
                    .setInt(RedirectConstant.KEY_STATUS, request.getStatus())
                    .setString(RedirectConstant.KEY_MODE, request.getMode())
                    .setString(RedirectConstant.KEY_RULE, request.getRule())
                    .setParam(RedirectConstant.KEY_ACTION, request.getAction())
                    .setList(RedirectConstant.KEY_ROUTES, request.getRoutes())
                    .setString(RedirectConstant.KEY_REMARK, request.getRemark());
            boolean success = node.updateRedirectRule(zone, request.getId(), infomation);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    // 删除重定向规则
    @Request.Delete("/api/v1/gateway/{zone}/rewrite/redirect/rule/{id}")
    @Permission("website:rewrite:redirect:delete")
    @OperationLog(type = OperationType.DELETE, module = "redirect")
    public React<StandardResult> removeRedirectRule(@Variable("zone") String zone, @Variable("id") String id) {
        return React.emitter(() -> {
            boolean success = node.removeRedirectRule(zone, id);
            if (!success) {
                return StandardResult.error(Errno.EARGUMENT, "Remove Redirect Rule " + id + " Failed");
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }
}
