package cloud.apposs.gateway.dashboard.api;

import cloud.apposs.gateway.ai.AIProvider;
import cloud.apposs.gateway.ai.AIProviderConstant;
import cloud.apposs.gateway.dashboard.api.model.ProviderModel;
import cloud.apposs.gateway.dashboard.interceptor.Permission;
import cloud.apposs.gateway.dashboard.node.INode;
import cloud.apposs.gateway.dashboard.util.Ids;
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
public class ProviderApi {
    @Autowired
    private INode node;

    @Request.Get("/api/v1/gateway/provider/{id}")
    public React<StandardResult> getProviderInfo(@Variable("id") String id) {
        return React.emitter(() -> {
            Param infomation = node.getProviderInfo(id);
            if (infomation == null) {
                return StandardResult.error(Errno.ENOT_FOUND, "Provider " + id + " not found");
            }
            return StandardResult.success(infomation, false);
        });
    }

    @Request.Get("/api/v1/gateway/providers")
    @Permission("system:setting:provider:list")
    public React<StandardResult> getProviderList() {
        return React.emitter(() -> {
            Table<Param> dataList = node.getProviderList();
            if (dataList == null) {
                dataList = Table.builder();
            }
            AIProvider.mergeSystemProviders(dataList);
            return StandardResult.success(dataList, false);
        });
    }

    @Request.Put("/api/v1/gateway/provider")
    @Permission("system:setting:provider:add")
    public React<StandardResult> addProvider(@Model ProviderModel.Add request) {
        return React.emitter(() -> {
            String id = Ids.getInstance().nextId();
            Param infomation = Param.builder(AIProviderConstant.KEY_ID, id)
                    .setString(AIProviderConstant.KEY_NAME, request.getName())
                    .setString(AIProviderConstant.KEY_URL, request.getUrl())
                    .setString(AIProviderConstant.KEY_TYPE, request.getType())
                    .setBoolean(AIProviderConstant.KEY_SYSTEM, false)
                    .setTable(AIProviderConstant.KEY_MODELS, request.getModels())
                    .setTable(AIProviderConstant.KEY_KEYS, request.getKeys());
            boolean success = node.addProvider(id, infomation);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            Param response = Param.builder(AIProviderConstant.KEY_ID, id);
            return StandardResult.success(response);
        });
    }

    @Request.Post("/api/v1/gateway/provider/{id}")
    @Permission("system:setting:provider:edit")
    public React<StandardResult> updateProvider(@Variable("id") String id, @Model ProviderModel.Update request) {
        return React.emitter(() -> {
            AIProvider provider = AIProvider.matchSystemProvider(id);
            Param infomation = Param.builder(AIProviderConstant.KEY_ID, id)
                    .setString(AIProviderConstant.KEY_NAME, request.getName())
                    .setString(AIProviderConstant.KEY_URL, request.getUrl())
                    .setString(AIProviderConstant.KEY_TYPE, request.getType())
                    .setBoolean(AIProviderConstant.KEY_SYSTEM, provider != null)
                    .setTable(AIProviderConstant.KEY_MODELS, request.getModels())
                    .setTable(AIProviderConstant.KEY_KEYS, request.getKeys());
            boolean success = node.updateProvider(request.getId(), infomation);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success();
        });
    }

    @Request.Delete("/api/v1/gateway/provider/{id}")
    @Permission("system:setting:provider:delete")
    public React<StandardResult> deleteProvider(@Variable("id") String id) {
        return React.emitter(() -> {
            boolean success = node.deleteProvider(id);
            if (!success) {
                return StandardResult.error(Errno.ERROR);
            }
            return StandardResult.success();
        });
    }
}
