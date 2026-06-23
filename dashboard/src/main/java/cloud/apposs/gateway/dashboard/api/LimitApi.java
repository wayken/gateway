package cloud.apposs.gateway.dashboard.api;

import cloud.apposs.gateway.dashboard.api.database.app.CommonDef;
import cloud.apposs.gateway.dashboard.api.model.LimitModel;
import cloud.apposs.gateway.dashboard.interceptor.OperationLog;
import cloud.apposs.gateway.dashboard.interceptor.OperationType;
import cloud.apposs.gateway.dashboard.interceptor.Permission;
import cloud.apposs.gateway.dashboard.node.INode;
import cloud.apposs.gateway.dashboard.util.Ids;
import cloud.apposs.gateway.plugin.runner.limit.LimitConstant;
import cloud.apposs.gateway.plugin.runner.waf.WafConstant;
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

import java.util.ArrayList;
import java.util.List;

@RestAction
public class LimitApi {
    @Autowired
    private INode node;

    @Request.Get("/api/v1/gateway/{zone}/limit/rule/{id}")
    public React<StandardResult> getLimitRuleInfo(@Variable("zone") String zone, @Variable("id") String id) {
        return React.emitter(() -> {
            Param infomation = node.getLimitRuleInfo(zone, id);
            if (infomation == null) {
                return StandardResult.error(Errno.ENOT_FOUND, "Limit Rule " + id + " not found");
            }
            return StandardResult.success(infomation, false);
        });
    }

    @Request.Get("/api/v1/gateway/{zone}/limit/rules")
    @Permission("website:protection:limit:list")
    public React<StandardResult> getLimitRuleList(@Variable("zone") String zone) {
        return React.emitter(() -> {
            Table<Param> dataList = node.getLimitRuleList(zone);
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

    @Request.Put("/api/v1/gateway/{zone}/limit/rule")
    @Permission("website:protection:limit:add")
    @OperationLog(type = OperationType.ADD, module = "limit")
    public React<StandardResult> addLimitRule(@Variable("zone") String zone, @Model LimitModel.Add request) {
        return React.emitter(() -> {
            String id = Ids.getInstance().nextId();
            Param infomation = Param.builder(LimitConstant.KEY_ID, id)
                    .setString(LimitConstant.KEY_NAME, request.getName())
                    .setInt(LimitConstant.KEY_STATUS, request.getStatus())
                    .setString(LimitConstant.KEY_RULE, request.getRule())
                    .setInt(LimitConstant.KEY_BURST, request.getBurst())
                    .setString(LimitConstant.KEY_BURST_UNIT, request.getBurstUnit())
                    .setParam(LimitConstant.KEY_ACTION, request.getAction())
                    .setList(LimitConstant.KEY_ROUTES, request.getRoutes())
                    .setInt(LimitConstant.KEY_PRIORITY, request.getPriority())
                    .setString(WafConstant.KEY_REMARK, request.getRemark());
            List<Param> resources = request.getResources();
            List<Param> resourceList = new ArrayList<>(resources.size());
            for (Param resource : resources) {
                resourceList.add(resource);
            }
            infomation.setList(LimitConstant.KEY_RESOURCES, resourceList);
            boolean success = node.addLimitRule(zone, id, infomation);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    @Request.Post("/api/v1/gateway/{zone}/limit/rule/{id}")
    @Permission("website:protection:limit:edit")
    @OperationLog(type = OperationType.UPDATE, module = "limit")
    public React<StandardResult> updateLimitRule(@Variable("zone") String zone, @Variable("id") String id, @Model LimitModel.Update request) {
        return React.emitter(() -> {
            Param infomation = Param.builder(WafConstant.KEY_NAME, request.getName())
                    .setInt(LimitConstant.KEY_STATUS, request.getStatus())
                    .setString(LimitConstant.KEY_RULE, request.getRule())
                    .setInt(LimitConstant.KEY_BURST, request.getBurst())
                    .setString(LimitConstant.KEY_BURST_UNIT, request.getBurstUnit())
                    .setParam(LimitConstant.KEY_ACTION, request.getAction())
                    .setList(LimitConstant.KEY_ROUTES, request.getRoutes())
                    .setInt(LimitConstant.KEY_PRIORITY, request.getPriority())
                    .setString(LimitConstant.KEY_REMARK, request.getRemark());
            List<Param> resources = request.getResources();
            List<Param> resourceList = new ArrayList<>(resources.size());
            for (Param resource : resources) {
                resourceList.add(resource);
            }
            infomation.setList(LimitConstant.KEY_RESOURCES, resourceList);
            boolean success = node.updateLimitRule(zone, request.getId(), infomation);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    @Request.Delete("/api/v1/gateway/{zone}/limit/rule/{id}")
    @Permission("website:protection:limit:delete")
    @OperationLog(type = OperationType.DELETE, module = "limit")
    public React<StandardResult> removeLimitRule(@Variable("zone") String zone, @Variable("id") String id) {
        return React.emitter(() -> {
            boolean success = node.removeLimitRule(zone, id);
            if (!success) {
                return StandardResult.error(Errno.EARGUMENT, "Remove Limit Rule " + id + " Failed");
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }
}
