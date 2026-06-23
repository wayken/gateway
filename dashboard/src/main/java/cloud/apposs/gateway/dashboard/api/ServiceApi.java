package cloud.apposs.gateway.dashboard.api;

import cloud.apposs.gateway.dashboard.node.INode;
import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.react.React;
import cloud.apposs.rest.annotation.Request;
import cloud.apposs.rest.annotation.RestAction;
import cloud.apposs.util.Param;
import cloud.apposs.util.StandardResult;
import cloud.apposs.util.Table;

@RestAction
public class ServiceApi {
    @Autowired
    private INode node;

    // 获取网关集群列表
    @Request.Get("/api/v1/gateway/services")
    public React<StandardResult> getZoneList() {
        return React.emitter(() -> {
            Table<Param> dataList = node.getServiceList();
            return StandardResult.success(dataList);
        });
    }
}
