package cloud.apposs.gateway.plugin.runner.waf.action;

import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.plugin.runner.waf.WafConstant;
import cloud.apposs.gateway.rules.Action;
import cloud.apposs.gateway.rules.Facts;
import cloud.apposs.util.MediaType;
import cloud.apposs.util.Param;

import java.io.InputStream;

public class WafBlockAction implements Action {
    private final int code;

    private final String responseType;

    private final String content;

    public WafBlockAction(Param parameters) {
        this.code = parameters.getInt(WafConstant.Action.KEY_CODE, 403);
        this.responseType = parameters.getString(WafConstant.Action.KEY_RESPONSE);
        this.content = parameters.getString(WafConstant.Action.KEY_CONTENT, "");
    }

    @Override
    public void execute(Facts facts, Object... arguments) throws Exception {
        GatewayHttpResponse response = (GatewayHttpResponse) arguments[2];
        response.setStatus(code);
        String responseType = this.responseType == null ? MediaType.TEXT_HTML.toString() : this.responseType;
        response.setContentType(responseType + "; charset=UTF-8");
        String content = handleResponseContentGenerator();
        if (content != null) {
            response.write(content, true);
        }
    }

    // 根据配置的动作类型返回对应的内容，如果是走系统默认则返回默认的403页面内容
    public String handleResponseContentGenerator() throws Exception {
        if (responseType == null) {
            // 读取当前项目下resources/pages/forbiden.html文件内容返回给客户端
            InputStream pageInputStream = getClass().getClassLoader().getResourceAsStream("pages/forbidden.html");
            if (pageInputStream == null) {
                return null;
            }
            try {
                // 读取文件内容
                byte[] content = new byte[pageInputStream.available()];
                pageInputStream.read(content);
                return new String(content);
            } finally {
                pageInputStream.close();
            }
        }
        return content;
    }
}
