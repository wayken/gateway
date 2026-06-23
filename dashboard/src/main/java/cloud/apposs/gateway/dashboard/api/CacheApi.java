package cloud.apposs.gateway.dashboard.api;

import cloud.apposs.gateway.dashboard.api.database.app.CommonDef;
import cloud.apposs.gateway.dashboard.api.model.CacheModel;
import cloud.apposs.gateway.dashboard.interceptor.OperationLog;
import cloud.apposs.gateway.dashboard.interceptor.OperationType;
import cloud.apposs.gateway.dashboard.interceptor.Permission;
import cloud.apposs.gateway.dashboard.node.INode;
import cloud.apposs.gateway.dashboard.util.Ids;
import cloud.apposs.gateway.plugin.runner.cache.CacheConstant;
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
public class CacheApi {
    @Autowired
    private INode node;

    // 获取区域缓存规则
    @Request.Get("/api/v1/gateway/{zone}/cache/rule/{id}")
    public React<StandardResult> getCacheRuleInfo(@Variable("zone") String zone, @Variable("id") String id) {
        return React.emitter(() -> {
            Param infomation = node.getCacheRuleInfo(zone, id);
            if (infomation == null) {
                return StandardResult.error(Errno.ENOT_FOUND, "Cache Rule " + id + " not found");
            }
            return StandardResult.success(infomation, false);
        });
    }

    // 获取区域缓存列表
    @Request.Get("/api/v1/gateway/{zone}/cache/rules")
    @Permission("website:cache:rule:list")
    public React<StandardResult> getCacheRuleList(@Variable("zone") String zone) {
        return React.emitter(() -> {
            Table<Param> dataList = node.getCacheRuleList(zone);
            if (dataList == null) {
                dataList = Table.builder();
            }
            // 根据优先级排序
            dataList.sort((o1, o2) -> {
                int priority1 = o1.getInt(CacheConstant.KEY_PRIORITY, 0);
                int priority2 = o2.getInt(CacheConstant.KEY_PRIORITY, 0);
                return priority1 - priority2;
            });
            return StandardResult.success(dataList, false);
        });
    }

    // 添加区域缓存规则
    @Request.Put("/api/v1/gateway/{zone}/cache/rule")
    @Permission("website:cache:rule:add")
    @OperationLog(type = OperationType.ADD, module = "cache")
    public React<StandardResult> addCacheRule(@Variable("zone") String zone, @Model CacheModel.Add request) {
        return React.emitter(() -> {
            String id = Ids.getInstance().nextId();
            Param infomation = Param.builder(CacheConstant.KEY_NAME, request.getName())
                    .setInt(CacheConstant.KEY_STATUS, request.getStatus())
                    .setString(CacheConstant.KEY_RULE, request.getRule())
                    .setParam(CacheConstant.KEY_ACTION, request.getAction())
                    .setList(CacheConstant.KEY_ROUTES, request.getRoutes())
                    .setInt(CacheConstant.KEY_PRIORITY, request.getPriority())
                    .setString(CacheConstant.KEY_REMARK, request.getRemark());
            boolean success = node.addCacheRule(zone, id, infomation);
            if (!success) {
                Logger.error("Add Cache Rule Error, zone=%s, id=%s, status=%d, routes=%s, rule=%s, action=%s",
                        zone, id, request.getStatus(), request.getRoutes(), request.getRule(), request.getAction());
                return StandardResult.error(Errno.ERROR);
            }
            Logger.info("Add Cache Rule Success, zone=%s, id=%s, status=%d, routes=%s, rule=%s, action=%s",
                    zone, id, request.getStatus(), request.getRoutes(), request.getRule(), request.getAction());
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    // 更新区域缓存规则
    @Request.Post("/api/v1/gateway/{zone}/cache/rule/{id}")
    @Permission("website:cache:rule:edit")
    @OperationLog(type = OperationType.UPDATE, module = "cache")
    public React<StandardResult> updateCacheRule(@Variable("zone") String zone, @Variable("id") String id, @Model CacheModel.Update request) {
        return React.emitter(() -> {
            Param infomation = Param.builder(CacheConstant.KEY_NAME, request.getName())
                    .setInt(CacheConstant.KEY_STATUS, request.getStatus())
                    .setString(CacheConstant.KEY_RULE, request.getRule())
                    .setParam(CacheConstant.KEY_ACTION, request.getAction())
                    .setList(CacheConstant.KEY_ROUTES, request.getRoutes())
                    .setInt(CacheConstant.KEY_PRIORITY, request.getPriority())
                    .setString(CacheConstant.KEY_REMARK, request.getRemark());
            boolean success = node.updateCacheRule(zone, request.getId(), infomation);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    // 删除缓存规则
    @Request.Delete("/api/v1/gateway/{zone}/cache/rule/{id}")
    @Permission("website:cache:rule:delete")
    @OperationLog(type = OperationType.DELETE, module = "cache")
    public React<StandardResult> removeCacheRule(@Variable("zone") String zone, @Variable("id") String id) {
        return React.emitter(() -> {
            boolean success = node.removeCacheRule(zone, id);
            if (!success) {
                return StandardResult.error(Errno.EARGUMENT, "Remove Cache Rule " + id + " Failed");
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }
}
