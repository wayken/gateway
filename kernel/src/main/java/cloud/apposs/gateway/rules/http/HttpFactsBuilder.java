package cloud.apposs.gateway.rules.http;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.rules.Facts;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求FACTS构建器，构建参数信息如下：
 * <pre>
 * route.id: 路由ID
 * http.method: 请求方法
 * http.uri: 请求URI
 * http.path: 请求路径
 * http.url: 请求URL
 * http.host: 请求HOST
 * http.referer: 请求来源
 * http.user-agent: 请求客户端信息
 * http.protocol: 请求协议
 * http.remote.addr: 请求客户端IP
 * http.cookies.xx: 请求Cookie，xx为Cookie名称
 * http.headers.xx: 请求Header，xx为Header名称
 * http.parameters.xx: 请求参数，xx为参数名称
 * </pre>
 */
public class HttpFactsBuilder {
    public static Facts build(GatewayContext context, GatewayHttpRequest request) {
        Facts facts = new Facts();
        Map<String, Object> commons = new HashMap<String, Object>();
        Map<String, String> cookies = new HashMap<String, String>();
        Map<String, String> headers = new HashMap<String, String>();
        Map<String, String> parameters = new HashMap<String, String>();
        commons.put("method", request.getMethod());
        commons.put("uri", request.getUri());
        commons.put("path", request.getUri().getPath());
        commons.put("url", request.getUrl());
        commons.put("host", request.getHeader("Host", true));
        commons.put("referer", request.getHeader("Referer", true));
        commons.put("user-agent", request.getHeader("User-Agent", true));
        commons.put("protocol", request.getProtocol());
        commons.put("schema", request.getSchema());
        commons.put("ip", new HashMap<String, Object>() {{
            put("src", new HttpSrcIpRuleHandler(context, request));
        }});
        commons.put("cookies", cookies);
        commons.put("headers", headers);
        commons.put("parameters", parameters);
        facts.put("http", commons);
        // 解析HTTP请求Cookie
        String cookie = request.getHeader("Cookie", true);
        if (cookie != null) {
            String[] cookieList = cookie.split(";");
            cookies.putAll(new HashMap<String, String>() {{
                for (String cookieItem : cookieList) {
                    String[] cookieItemArray = cookieItem.split("=");
                    if (cookieItemArray.length == 2) {
                        put(cookieItemArray[0].trim(), cookieItemArray[1].trim());
                    }
                }
            }});
        }
        // 解析HTTP请求Header
        for (String key : request.getHeaders().keySet()) {
            headers.put(key.toLowerCase(), request.getHeader(key));
        }
        // 解析HTTP请求参数
        Map<String, String> parameter = request.getParameters();
        parameters.putAll(parameter);
        return facts;
    }
}
