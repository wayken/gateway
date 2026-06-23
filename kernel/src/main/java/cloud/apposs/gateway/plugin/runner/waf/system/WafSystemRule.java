package cloud.apposs.gateway.plugin.runner.waf.system;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.rules.Rule;

/**
 * WAF系统规则接口
 */
public interface WafSystemRule {
    /**
     * 规则名称
     */
    String name();

    /**
     * 规则过滤
     *
     * @param  request  请求对象
     * @param  response 响应对象
     * @return 返回true表示规则匹配，返回false表示规则不匹配
     */
    boolean match(GatewayHttpRequest request, GatewayHttpResponse response) throws Exception;

    /**
     * 获取自定义匹配规则
     */
    Rule getRule();

    /**
     * 设置自定义匹配规则
     */
    void setRule(Rule rule);

    /**
     * 规则是否启用
     */
    boolean isEnable();

    /**
     * 设置规则是否启用
     */
    void setEnable(boolean enable);
}
