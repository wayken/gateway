package cloud.apposs.gateway.annotation;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.plugin.PluginSupport;
import cloud.apposs.ioc.annotation.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 公共过滤器注解，用于标识一个过滤器为公共过滤器，并且会被所有路由共享，属于全局单例模式，
 * 具体详见{@link PluginSupport#getCommonPluginList(GatewayContext)}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface CommonPlugin {
}
