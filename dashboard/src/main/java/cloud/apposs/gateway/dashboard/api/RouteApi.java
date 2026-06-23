package cloud.apposs.gateway.dashboard.api;

import cloud.apposs.gateway.dashboard.api.database.app.CommonDef;
import cloud.apposs.gateway.dashboard.api.model.RouteModel;
import cloud.apposs.gateway.dashboard.interceptor.OperationLog;
import cloud.apposs.gateway.dashboard.interceptor.OperationType;
import cloud.apposs.gateway.dashboard.interceptor.Permission;
import cloud.apposs.gateway.dashboard.node.INode;
import cloud.apposs.gateway.dashboard.util.Ids;
import cloud.apposs.gateway.route.RouteConstant;
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
public class RouteApi {
    @Autowired
    private INode node;

    // 获取区域网关路由列表
    @Request.Get("/api/v1/gateway/{zone}/routes")
    @Permission("website:route:list")
    public React<StandardResult> getRouteList(@Variable("zone") String zone) {
        return React.emitter(() -> {
            Table<Param> routeList = node.getRouteList(zone);
            // dataList中的数据是Param对象，按ID大小排序
            routeList.sort((o1, o2) -> {
                String id1 = o1.getString(RouteConstant.KEY_ID);
                String id2 = o2.getString(RouteConstant.KEY_ID);
                return id1.compareTo(id2);
            });
            return StandardResult.success(routeList, false);
        });
    }

    // 获取区域网关路由信息
    @Request.Get("/api/v1/gateway/{zone}/route/{id}")
    public React<StandardResult> getRouteInfo(@Variable("zone") String zone, @Variable("id") String id) {
        return React.emitter(() -> {
            Param infomation = node.getRouteInfo(zone, id);
            if (infomation == null) {
                return StandardResult.error(Errno.ENOT_FOUND, "Route " + id + " not found");
            }
            return StandardResult.success(infomation, false);
        });
    }

    // 添加区域网关路由
    @Request.Put("/api/v1/gateway/{zone}/route")
    @Permission("website:route:add")
    @OperationLog(type = OperationType.ADD, module = "route")
    public React<StandardResult> addRoute(@Variable("zone") String zone, @Model RouteModel.Add request) {
        return React.emitter(() -> {
            String id = Ids.getInstance().nextId();
            Param infomation = Param.builder(RouteConstant.KEY_TYPE, request.getType())
                    .setString(RouteConstant.KEY_NAME, request.getName())
                    .setString(RouteConstant.KEY_PATH, request.getPath())
                    .setString(RouteConstant.KEY_UPSTREAM_ID, request.getUpstreamId())
                    .setString(RouteConstant.KEY_AUTH_ID, request.getAuthId())
                    .setInt(RouteConstant.KEY_PRIORITY, request.getPriority())
                    .setInt(RouteConstant.KEY_STATUS, request.getStatus())
                    .setList(RouteConstant.KEY_METHODS, request.getMethods())
                    .setString(RouteConstant.KEY_RULE, request.getRule())
                    .setString(RouteConstant.KEY_REMARK, request.getRemark());
            if (request.getUpstreamId().equals(RouteConstant.UPSTREAM_ID_MANUAL)) {
                infomation.setParam(RouteConstant.KEY_UPSTREAM, request.getUpstream());
            }
            boolean success = node.addRoute(zone, id, infomation);
            if (!success) {
                return StandardResult.error(Errno.EARGUMENT, "Route " + request.getId() + " add failed");
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    // 更新区域网关路由
    @Request.Post("/api/v1/gateway/{zone}/route/{id}")
    @Permission("website:route:edit")
    @OperationLog(type = OperationType.UPDATE, module = "route")
    public React<StandardResult> updateRoute(@Variable("zone") String zone, @Variable("id") String id, @Model RouteModel.Update request) {
        return React.emitter(() -> {
            Param infomation = Param.builder(RouteConstant.KEY_ID, id)
                    .setString(RouteConstant.KEY_TYPE, request.getType())
                    .setString(RouteConstant.KEY_NAME, request.getName())
                    .setString(RouteConstant.KEY_PATH, request.getPath())
                    .setString(RouteConstant.KEY_UPSTREAM_ID, request.getUpstreamId())
                    .setString(RouteConstant.KEY_AUTH_ID, request.getAuthId())
                    .setInt(RouteConstant.KEY_PRIORITY, request.getPriority())
                    .setInt(RouteConstant.KEY_STATUS, request.getStatus())
                    .setList(RouteConstant.KEY_METHODS, request.getMethods())
                    .setString(RouteConstant.KEY_RULE, request.getRule())
                    .setString(RouteConstant.KEY_REMARK, request.getRemark());
            if (request.getUpstreamId().equals(RouteConstant.UPSTREAM_ID_MANUAL)) {
                infomation.setParam(RouteConstant.KEY_UPSTREAM, request.getUpstream());
            }
            boolean success = node.updateRoute(zone, id, infomation);
            if (!success) {
                return StandardResult.error(Errno.EARGUMENT, "Route " + id + " update failed");
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    // 删除区域网关路由
    @Request.Delete("/api/v1/gateway/{zone}/route/{id}")
    @Permission("website:route:delete")
    @OperationLog(type = OperationType.DELETE, module = "route")
    public React<StandardResult> removeRoute(@Variable("zone") String zone, @Variable("id") String id) {
        return React.emitter(() -> {
            boolean success = node.removeRoute(zone, id);
            if (!success) {
                return StandardResult.error(Errno.EARGUMENT, "Route " + id + " delete failed");
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }
}
