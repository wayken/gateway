package cloud.apposs.gateway.dashboard.api;

import cloud.apposs.gateway.dashboard.api.model.PluginModel;
import cloud.apposs.gateway.dashboard.node.INode;
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
public class PluginApi {
    @Autowired
    private INode node;

    @Request.Get("/api/v1/gateway/{zone}/plugins")
    public React<StandardResult> getPluginList(@Variable("zone") String zone) {
        return React.emitter(() -> {
            Table<Param> dataList = node.getPluginList(zone);
            if (dataList == null) {
                dataList = Table.builder();
            }
            return StandardResult.success(dataList);
        });
    }

    @Request.Post("/api/v1/gateway/{zone}/plugins")
    public React<StandardResult> updateIp(@Variable("zone") String zone, @Model PluginModel.Update request) {
        return React.emitter(() -> {
            boolean success = node.updatePluginList(zone, request.getPlugins());
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success();
        });
    }
}
