package cloud.apposs.gateway.plugin.runner.cache;

import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.rules.Facts;
import cloud.apposs.gateway.rules.http.HttpMVELRule;

import java.io.File;
import java.util.List;

public class CacheRule extends HttpMVELRule {
    private final String dataSourcePath;

    public CacheRule(String name, int status, List<String> matchedRuleIds, String dataSourcePath) {
        super(name, status, matchedRuleIds);
        this.dataSourcePath = dataSourcePath;
    }

    @Override
    public boolean evaluate(Facts facts, Object... arguments) {
        boolean result = super.evaluate(facts, arguments);
        if (!result) {
            return false;
        }
        // 判断缓存目录和文件是否存在
        GatewayHttpRequest request = (GatewayHttpRequest) arguments[1];
        String host = request.getRemoteHost();
        String url = request.getUrl();
        String method = request.getMethod();
        // 判断缓存目录是否存在，存在则代表可以读取缓存数据
        String dataPath = CacheConstant.getCachePath(dataSourcePath, host, url, method);
        String headerPath = CacheConstant.getCacheHeaderPath(dataPath);
        String contentPath = CacheConstant.getCacheContentPath(dataPath);
        File headerFile = new File(headerPath);
        File contentFile = new File(contentPath);
        if (!headerFile.exists() || !contentFile.exists()) {
            // 符合缓存条件，但没有缓存文件，需要进行后端转发并设置转发请求后的数据进行缓存
            request.setAttribute(CacheConstant.REQUEST_ATTRIBUTE_CACHEABLE, true);
            return false;
        }
        return true;
    }
}
