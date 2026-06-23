package cloud.apposs.gateway.dashboard.api;

import cloud.apposs.gateway.dashboard.api.database.app.CommonDef;
import cloud.apposs.gateway.dashboard.api.model.IpModel;
import cloud.apposs.gateway.dashboard.interceptor.OperationLog;
import cloud.apposs.gateway.dashboard.interceptor.OperationType;
import cloud.apposs.gateway.dashboard.interceptor.Permission;
import cloud.apposs.gateway.dashboard.node.INode;
import cloud.apposs.gateway.dashboard.util.Ids;
import cloud.apposs.gateway.global.ips.IpAddressConstant;
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
public class IpApi {
    @Autowired
    private INode node;

    @Request.Get("/api/v1/gateway/ip/{id}")
    public React<StandardResult> getIpInfo(@Variable("id") String id) {
        return React.emitter(() -> {
            Param infomation = node.getIpInfo(id);
            if (infomation == null) {
                return StandardResult.error(Errno.ENOT_FOUND, "Ips IP " + id + " not found");
            }
            return StandardResult.success(infomation, false);
        });
    }

    @Request.Get("/api/v1/gateway/ips")
    @Permission("system:setting:ipgroup:list")
    public React<StandardResult> getIpList() {
        return React.emitter(() -> {
            Table<Param> dataList = node.getIpList();
            if (dataList == null) {
                dataList = Table.builder();
            }
            return StandardResult.success(dataList);
        });
    }

    @Request.Put("/api/v1/gateway/ip")
    @Permission("system:setting:ipgroup:add")
    @OperationLog(type = OperationType.ADD, module = "ipgroup")
    public React<StandardResult> addIp(@Model IpModel.Add request) {
        return React.emitter(() -> {
            String id = Ids.getInstance().nextId();
            Param infomation = Param.builder(IpAddressConstant.KEY_ID, id)
                    .setString(IpAddressConstant.KEY_NAME, request.getName())
                    .setList(IpAddressConstant.KEY_CIDRS, request.getCidrs())
                    .setString(IpAddressConstant.KEY_REMARK, request.getRemark());
            boolean success = node.addIp(id, infomation);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    @Request.Post("/api/v1/gateway/ip/{id}")
    @Permission("system:setting:ipgroup:edit")
    @OperationLog(type = OperationType.UPDATE, module = "ipgroup")
    public React<StandardResult> updateIp(@Variable("id") String id, @Model IpModel.Update request) {
        return React.emitter(() -> {
            Param infomation = Param.builder(IpAddressConstant.KEY_ID, id)
                    .setString(IpAddressConstant.KEY_NAME, request.getName())
                    .setList(IpAddressConstant.KEY_CIDRS, request.getCidrs())
                    .setString(IpAddressConstant.KEY_REMARK, request.getRemark());
            boolean success = node.updateIp(request.getId(), infomation);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }

    @Request.Delete("/api/v1/gateway/ip/{id}")
    @Permission("system:setting:ipgroup:delete")
    @OperationLog(type = OperationType.DELETE, module = "ipgroup")
    public React<StandardResult> deleteIp(@Variable("id") String id) {
        return React.emitter(() -> {
            boolean success = node.deleteIp(id);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success(Param.builder(CommonDef.Info.ID, id));
        });
    }
}
