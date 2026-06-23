package cloud.apposs.gateway.dashboard.api;

import cloud.apposs.gateway.dashboard.api.database.app.CommonDef;
import cloud.apposs.gateway.dashboard.api.model.UpstreamModel;
import cloud.apposs.gateway.dashboard.interceptor.OperationLog;
import cloud.apposs.gateway.dashboard.interceptor.OperationType;
import cloud.apposs.gateway.dashboard.interceptor.Permission;
import cloud.apposs.gateway.dashboard.node.INode;
import cloud.apposs.gateway.dashboard.util.Ids;
import cloud.apposs.gateway.upstream.UpstreamConstant;
import cloud.apposs.gateway.upstream.UpstreamType;
import cloud.apposs.gateway.upstream.ai.AIUpstream;
import cloud.apposs.gateway.upstream.echo.EchoUpstream;
import cloud.apposs.gateway.upstream.index.IndexUpstream;
import cloud.apposs.gateway.upstream.node.NodeUpstream;
import cloud.apposs.gateway.upstream.service.ServiceUpstream;
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
public class UpstreamApi {
    @Autowired
    private INode node;

    // 获取上游服务列表
    @Request.Get("/api/v1/gateway/{zone}/upstreams")
    @Permission("website:upstream:list")
    public React<StandardResult> getUpstreamList(@Variable("zone") String zone) {
        return React.emitter(() -> {
            Table<Param> dataList = node.getUpstreamList(zone);
            return StandardResult.success(dataList);
        });
    }

    // 获取上游服务信息
    @Request.Get("/api/v1/gateway/{zone}/upstream/{id}")
    public React<StandardResult> getUpstreamInfo(@Variable("zone") String zone, @Variable("id") String id) {
        return React.emitter(() -> {
            Param routeInfo = node.getUpstreamInfo(zone, id);
            if (routeInfo == null) {
                return StandardResult.error(Errno.ENOT_FOUND, "Upstream " + id + " not found");
            }
            return StandardResult.success(routeInfo);
        });
    }

    // 添加上游服务
    @Request.Put("/api/v1/gateway/{zone}/upstream")
    @Permission("website:upstream:add")
    @OperationLog(type = OperationType.ADD, module = "upstream")
    public React<StandardResult> addUpstream(@Variable("zone") String zone, @Model UpstreamModel.Add request) {
        return React.emitter(() -> {
            String id = Ids.getInstance().nextId();
            String type = request.getType();
            Param infomation = Param.builder(UpstreamConstant.KEY_NAME, request.getName())
                    .setString(UpstreamConstant.KEY_TYPE, request.getType())
                    .setString(UpstreamConstant.KEY_ALGORITHM, request.getAlgorithm());
            Param parameters = request.getParameters();
            if (UpstreamType.ECHO.matched(type)) {
                infomation.put(EchoUpstream.KEY_CODE, parameters.getInt(EchoUpstream.KEY_CODE));
                infomation.put(EchoUpstream.KEY_CONTENT_TYPE, parameters.getString(EchoUpstream.KEY_CONTENT_TYPE));
                infomation.put(EchoUpstream.KEY_CONTENT, parameters.getString(EchoUpstream.KEY_CONTENT));
            } else if (UpstreamType.INDEX.matched(type)) {
                infomation.put(IndexUpstream.KEY_STATIC, parameters.getParam(IndexUpstream.KEY_STATIC));
            }  else if (UpstreamType.NODE.matched(type)) {
                infomation.put(NodeUpstream.KEY_NODES, parameters.getTable(NodeUpstream.KEY_NODES));
                infomation.put(NodeUpstream.KEY_HEALTHY, parameters.getParam(NodeUpstream.KEY_HEALTHY));
            } else if (UpstreamType.SERVICE.matched(type)) {
                infomation.put(ServiceUpstream.KEY_SERVICE, parameters.getParam(ServiceUpstream.KEY_SERVICE));
                infomation.put(NodeUpstream.KEY_HEALTHY, parameters.getParam(NodeUpstream.KEY_HEALTHY));
            } else if (UpstreamType.AI.matched(type)) {
                infomation.put(AIUpstream.KEY_AI, parameters.getParam(AIUpstream.KEY_AI));
            }
            boolean success = node.addUpstream(zone, id, infomation);
            if (!success) {
                return StandardResult.error(Errno.EARGUMENT, "Upstream " + id + " add failed");
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    // 更新上游服务
    @Request.Post("/api/v1/gateway/{zone}/upstream/{id}")
    @Permission("website:upstream:edit")
    @OperationLog(type = OperationType.UPDATE, module = "upstream")
    public React<StandardResult> updateUpstream(@Variable("zone") String zone, @Variable("id") String id, @Model UpstreamModel.Update request) {
        return React.emitter(() -> {
            String type = request.getType();
            Param infomation = Param.builder(UpstreamConstant.KEY_NAME, request.getName())
                    .setString(UpstreamConstant.KEY_TYPE, request.getType())
                    .setString(UpstreamConstant.KEY_ALGORITHM, request.getAlgorithm())
                    .setString(UpstreamConstant.KEY_REMARK, request.getRemark());
            Param parameters = request.getParameters();
            if (UpstreamType.ECHO.matched(type)) {
                infomation.put(EchoUpstream.KEY_CODE, parameters.getInt(EchoUpstream.KEY_CODE));
                infomation.put(EchoUpstream.KEY_CONTENT_TYPE, parameters.getString(EchoUpstream.KEY_CONTENT_TYPE));
                infomation.put(EchoUpstream.KEY_CONTENT, parameters.getString(EchoUpstream.KEY_CONTENT));
            } else if (UpstreamType.INDEX.matched(type)) {
                infomation.put(IndexUpstream.KEY_STATIC, parameters.getParam(IndexUpstream.KEY_STATIC));
            } else if (UpstreamType.NODE.matched(type)) {
                infomation.put(NodeUpstream.KEY_NODES, parameters.getTable(NodeUpstream.KEY_NODES));
                infomation.put(NodeUpstream.KEY_HEALTHY, parameters.getParam(NodeUpstream.KEY_HEALTHY));
            } else if (UpstreamType.SERVICE.matched(type)) {
                infomation.put(ServiceUpstream.KEY_SERVICE, parameters.getParam(ServiceUpstream.KEY_SERVICE));
                infomation.put(NodeUpstream.KEY_HEALTHY, parameters.getParam(NodeUpstream.KEY_HEALTHY));
            } else if (UpstreamType.AI.matched(type)) {
                infomation.put(AIUpstream.KEY_AI, parameters.getParam(AIUpstream.KEY_AI));
            }
            boolean success = node.updateUpstream(zone, id, infomation);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    // 删除上游服务
    @Request.Delete("/api/v1/gateway/{zone}/upstream/{id}")
    @Permission("website:upstream:delete")
    @OperationLog(type = OperationType.DELETE, module = "upstream")
    public React<StandardResult> removeUpstream(@Variable("zone") String zone, @Variable("id") String id) {
        return React.emitter(() -> {
            boolean success = node.removeUpstream(zone, id);
            if (!success) {
                return StandardResult.error(Errno.ENOT_FOUND, "Upstream " + id + " not found");
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }
}
