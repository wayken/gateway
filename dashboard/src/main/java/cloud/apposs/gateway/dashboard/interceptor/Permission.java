package cloud.apposs.gateway.dashboard.interceptor;

import cloud.apposs.rest.annotation.Request;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 拦截器注解，用于进行用户接口权限认证
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Request(method = Request.Method.GET)
public @interface Permission {
    String value();
}
