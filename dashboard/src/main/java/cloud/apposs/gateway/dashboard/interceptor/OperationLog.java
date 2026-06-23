package cloud.apposs.gateway.dashboard.interceptor;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.bootor.BootorHttpResponse;
import cloud.apposs.rest.Handler;
import cloud.apposs.rest.annotation.Request;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 拦截器注解，用于进行用户操作日志拦截，被注解的Handler在成功处理之后必须返回
 * <pre>
 *     return StandardResult.success(Param.builder(CommonDef.ID, id));
 * </pre>
 * 方便{@link OperationLogInterceptor#postHandler(BootorHttpRequest, BootorHttpResponse, Handler, Object)}进行操作日志解析和存储
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Request(method = Request.Method.GET)
public @interface OperationLog {
    int type();

    String module();
}
