package cloud.apposs.gateway.dashboard.api;

import cloud.apposs.gateway.dashboard.api.database.app.CommonDef;
import cloud.apposs.gateway.dashboard.api.model.WafModel;
import cloud.apposs.gateway.dashboard.interceptor.OperationLog;
import cloud.apposs.gateway.dashboard.interceptor.OperationType;
import cloud.apposs.gateway.dashboard.interceptor.Permission;
import cloud.apposs.gateway.dashboard.node.INode;
import cloud.apposs.gateway.dashboard.util.Ids;
import cloud.apposs.gateway.plugin.runner.waf.WafConstant;
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
public class WafApi {
    @Autowired
    private INode node;

    // 获取区域WAF规则
    @Request.Get("/api/v1/gateway/{zone}/waf/rule/{id}")
    public React<StandardResult> getWafRuleInfo(@Variable("zone") String zone, @Variable("id") String id) {
        return React.emitter(() -> {
            Param infomation = node.getWafRuleInfo(zone, id);
            if (infomation == null) {
                return StandardResult.error(Errno.ENOT_FOUND, "Waf Rule " + id + " not found");
            }
            return StandardResult.success(infomation, false);
        });
    }

    // 获取区域WAF列表
    @Request.Get("/api/v1/gateway/{zone}/waf/rules")
    @Permission("website:protection:rule:list")
    public React<StandardResult> getWafRuleList(@Variable("zone") String zone) {
        return React.emitter(() -> {
            Table<Param> dataList = node.getWafRuleList(zone);
            if (dataList == null) {
                dataList = Table.builder();
            }
            // 根据优先级排序
            dataList.sort((o1, o2) -> {
                int priority1 = o1.getInt(WafConstant.KEY_PRIORITY, 0);
                int priority2 = o2.getInt(WafConstant.KEY_PRIORITY, 0);
                return priority1 - priority2;
            });
            return StandardResult.success(dataList, false);
        });
    }

    // 添加区域WAF规则
    @Request.Put("/api/v1/gateway/{zone}/waf/rule")
    @Permission("website:protection:rule:add")
    @OperationLog(type = OperationType.ADD, module = "rule")
    public React<StandardResult> addWafRule(@Variable("zone") String zone, @Model WafModel.Add request) {
        return React.emitter(() -> {
            String id = Ids.getInstance().nextId();
            Param infomation = Param.builder(WafConstant.KEY_NAME, request.getName())
                    .setInt(WafConstant.KEY_STATUS, request.getStatus())
                    .setString(WafConstant.KEY_RULE, request.getRule())
                    .setParam(WafConstant.KEY_ACTION, request.getAction())
                    .setList(WafConstant.KEY_ROUTES, request.getRoutes())
                    .setInt(WafConstant.KEY_PRIORITY, request.getPriority())
                    .setString(WafConstant.KEY_REMARK, request.getRemark());
            boolean success = node.addWafRule(zone, id, infomation);
            if (!success) {
                Logger.error("Add WAF Rule Error, zone=%s, id=%s, status=%d, routes=%s, rule=%s, action=%s",
                        zone, id, request.getStatus(), request.getRoutes(), request.getRule(), request.getAction());
                return StandardResult.error(Errno.ERROR);
            }
            Logger.info("Add WAF Rule Success, zone=%s, id=%s, status=%d, routes=%s, rule=%s, action=%s",
                    zone, id, request.getStatus(), request.getRoutes(), request.getRule(), request.getAction());
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    // 更新区域WAF规则
    @Request.Post("/api/v1/gateway/{zone}/waf/rule/{id}")
    @Permission("website:protection:rule:edit")
    @OperationLog(type = OperationType.UPDATE, module = "rule")
    public React<StandardResult> updateWafRule(@Variable("zone") String zone, @Variable("id") String id, @Model WafModel.Update request) {
        return React.emitter(() -> {
            Param infomation = Param.builder(WafConstant.KEY_NAME, request.getName())
                    .setInt(WafConstant.KEY_STATUS, request.getStatus())
                    .setString(WafConstant.KEY_RULE, request.getRule())
                    .setParam(WafConstant.KEY_ACTION, request.getAction())
                    .setList(WafConstant.KEY_ROUTES, request.getRoutes())
                    .setInt(WafConstant.KEY_PRIORITY, request.getPriority())
                    .setString(WafConstant.KEY_REMARK, request.getRemark());
            boolean success = node.updateWafRule(zone, request.getId(), infomation);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    // 删除WAF规则
    @Request.Delete("/api/v1/gateway/{zone}/waf/rule/{id}")
    @Permission("website:protection:rule:delete")
    @OperationLog(type = OperationType.DELETE, module = "rule")
    public React<StandardResult> removeWafRule(@Variable("zone") String zone, @Variable("id") String id) {
        return React.emitter(() -> {
            boolean success = node.removeWafRule(zone, id);
            if (!success) {
                return StandardResult.error(Errno.EARGUMENT, "Remove Waf Rule " + id + " Failed");
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }
}
