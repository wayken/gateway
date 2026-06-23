package cloud.apposs.gateway.dashboard.api;

import cloud.apposs.gateway.dashboard.api.database.app.CommonDef;
import cloud.apposs.gateway.dashboard.api.model.ZoneModel;
import cloud.apposs.gateway.dashboard.interceptor.OperationLog;
import cloud.apposs.gateway.dashboard.interceptor.OperationType;
import cloud.apposs.gateway.dashboard.interceptor.Permission;
import cloud.apposs.gateway.dashboard.node.INode;
import cloud.apposs.gateway.dashboard.util.Ids;
import cloud.apposs.gateway.zone.Zone;
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
public class ZoneApi {
    @Autowired
    private INode node;

    // 获取区域网关路由信息
    @Request.Get("/api/v1/gateway/zone/{zone}")
    public React<StandardResult> getZoneInfo(@Variable("zone") String zone) {
        return React.emitter(() -> {
            Param infomation = node.getZoneInfo(zone);
            if (infomation == null) {
                return StandardResult.error(Errno.ENOT_FOUND, "Zone " + zone + " not found");
            }
            return StandardResult.success(infomation);
        });
    }

    // 获取区域列表
    @Request.Get("/api/v1/gateway/zones")
    @Permission("system:website:list")
    public React<StandardResult> getZoneList() {
        return React.emitter(() -> {
            Table<Param> dataList = node.getZoneList();
            // dataList中的数据是Param对象，需要将Param.id中为zone.default放到最前，其次是ID最小的放到最前
            dataList.sort((o1, o2) -> {
                String id1 = o1.getString(Zone.KEY_ID);
                String id2 = o2.getString(Zone.KEY_ID);
                if (Zone.DEFAULT_ZONE.equals(id1)) {
                    return -1;
                }
                if (Zone.DEFAULT_ZONE.equals(id2)) {
                    return 1;
                }
                return id1.compareTo(id2);
            });

            return StandardResult.success(dataList);
        });
    }

    // 获取网关流量序列
    @Request.Get("/api/v1/gateway/zone/{zone}/sequence")
    public React<StandardResult> getSequenceInfo(@Variable("zone") String zone) {
        return React.emitter(() -> {
            if (!node.isZoneExist(zone)) {
                return StandardResult.error(Errno.ENOT_FOUND);
            }
            Param infomation = Param.builder().setLong("route", node.getRouteList(zone).stream().filter(item -> item.getInt("status") == 1).count())
                    .setInt("upstream", node.getUpstreamList(zone).size())
                    .setLong("limit", node.getLimitRuleList(zone).stream().filter(item -> item.getInt("status") == 1).count())
                    .setLong("cache", node.getCacheRuleList(zone).stream().filter(item -> item.getInt("status") == 1).count())
                    .setLong("waf", node.getWafRuleList(zone).stream().filter(item -> item.getInt("status") == 1).count())
                    .setLong("limit", node.getLimitRuleList(zone).stream().filter(item -> item.getInt("status") == 1).count())
                    .setLong("redirect", node.getRedirectRuleList(zone).stream().filter(item -> item.getInt("status") == 1).count())
                    .setLong("rewrite-request", node.getRewriteRequestRuleList(zone).stream().filter(item -> item.getInt("status") == 1).count())
                    .setLong("rewrite-response", node.getRewriteResponseRuleList(zone).stream().filter(item -> item.getInt("status") == 1).count());
            return StandardResult.success(infomation);
        });
    }

    // 添加区域网关
    @Request.Put("/api/v1/gateway/zone")
    @Permission("system:website:add")
    @OperationLog(type = OperationType.ADD, module = "website")
    public React<StandardResult> addZone(@Model ZoneModel.Add request) {
        return React.emitter(() -> {
            String name = request.getName();
            if (node.isZoneExist(name)) {
                return StandardResult.error(Errno.EALREADY_EXISTS);
            }
            String id = Ids.getInstance().nextId();
            boolean success = node.addZone(id, request.getName(), request.getMatch(), request.getStatus(), request.getRemark());
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    // 更新区域网关
    @Request.Post("/api/v1/gateway/zone/{zone}")
    @Permission("system:website:edit")
    @OperationLog(type = OperationType.UPDATE, module = "website")
    public React<StandardResult> updateZone(@Variable("zone") String zone, @Model ZoneModel.Update request) {
        return React.emitter(() -> {
            if (!node.isZoneExist(zone)) {
                return StandardResult.error(Errno.ENOT_FOUND);
            }
            boolean success = node.updateZone(request.getId(), request.getName(), request.getMatch(), request.getStatus(), request.getRemark());
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, zone));
        });
    }

    @Request.Delete("/api/v1/gateway/zone/{zone}")
    @Permission("system:website:delete")
    @OperationLog(type = OperationType.DELETE, module = "website")
    public React<StandardResult> deleteZone(@Variable("zone") String zone) {
        return React.emitter(() -> {
            if (!node.isZoneExist(zone)) {
                return StandardResult.error(Errno.ENOT_FOUND);
            }
            boolean success = node.deleteZone(zone);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, zone));
        });
    }
}
