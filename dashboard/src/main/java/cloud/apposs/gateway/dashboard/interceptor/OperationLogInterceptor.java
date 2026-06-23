package cloud.apposs.gateway.dashboard.interceptor;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.bootor.interceptor.BooterInterceptorAdaptor;
import cloud.apposs.gateway.dashboard.api.database.OperationLogApi;
import cloud.apposs.gateway.dashboard.api.database.app.CommonDef;
import cloud.apposs.gateway.dashboard.api.database.app.OperationLogDef;
import cloud.apposs.gateway.dashboard.api.database.app.SessionDef;
import cloud.apposs.gateway.dashboard.util.NetUtil;
import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.rest.Handler;
import cloud.apposs.rest.annotation.Order;
import cloud.apposs.util.Param;
import cloud.apposs.util.StandardResult;

import java.util.Calendar;

@Component
@Order(1028)
public class OperationLogInterceptor extends BooterInterceptorAdaptor {
    @Autowired
    private OperationLogApi operationLogApi;

    @Override
    public void postHandler(BootorHttpRequest request, BootorHttpResponse response, Handler handler, Object result) throws Exception {
        // 没有配置Operation注解直接跳过检测
        OperationLog operationAnnotation = handler.getAnnotation(OperationLog.class);
        if (operationAnnotation == null) {
            return;
        }
        if (!(result instanceof StandardResult)) {
            return;
        }
        StandardResult standardResult = (StandardResult) result;
        if (!standardResult.isSuccess()) {
            return;
        }
        String module = operationAnnotation.module();
        int operationType = operationAnnotation.type();
        Param parameters = request.getParam();
        Param dataResult = (Param) standardResult.getResult();
        String moduleId = dataResult.getString(CommonDef.Info.ID);
        if (moduleId == null) {
            return;
        }
        long userId = (Long) request.getAttribute(SessionDef.SESSION_ATTRIBUTE_KEY_USER_ID);
        Param infomation = Param.builder(OperationLogDef.Info.IP, NetUtil.getRequestIp(request))
                .setLong(OperationLogDef.Info.USER_ID, userId)
                .setString(OperationLogDef.Info.MODULE, module)
                .setString(OperationLogDef.Info.MODULE_ID, moduleId)
                .setInt(OperationLogDef.Info.OPERATION_TYPE, operationType)
                .setString(OperationLogDef.Info.URI, request.getUri().getPath())
                .setString(OperationLogDef.Info.CONTENT, parameters.toJson())
                .setString(OperationLogDef.Info.USER_AGENT, request.getHeader("User-Agent"))
                .setCalendar(OperationLogDef.Info.OPERATION_TIME, Calendar.getInstance());
        operationLogApi.handleDataAdd(infomation);
    }
}
