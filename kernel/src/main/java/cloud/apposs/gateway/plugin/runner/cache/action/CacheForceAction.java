package cloud.apposs.gateway.plugin.runner.cache.action;

import cloud.apposs.gateway.GatewayConfig;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.plugin.runner.cache.CacheConstant;
import cloud.apposs.gateway.rules.Action;
import cloud.apposs.gateway.rules.Facts;
import cloud.apposs.util.Param;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class CacheForceAction implements Action {
    private String dataCachePath;

    public CacheForceAction(GatewayConfig config, Param parameters) {
        this.dataCachePath = config.getDataSourcePath();
    }

    @Override
    public void execute(Facts facts, Object... arguments) throws Exception {
        GatewayHttpRequest request = (GatewayHttpRequest) arguments[1];
        GatewayHttpResponse response = (GatewayHttpResponse) arguments[2];
        String host = request.getRemoteHost();
        String url = request.getUrl();
        String method = request.getMethod();
        // 判断缓存目录是否存在，存在则代表可以读取缓存数据
        String dataPath = CacheConstant.getCachePath(dataCachePath, host, url, method);
        String headerPath = CacheConstant.getCacheHeaderPath(dataPath);
        String contentPath = CacheConstant.getCacheContentPath(dataPath);
        File headerFile = new File(headerPath);
        File contentFile = new File(contentPath);
        if (!headerFile.exists() || !contentFile.exists()) {
            throw new FileNotFoundException(contentPath);
        }
        // 遍历头部信息内容，按每行写入响应头
        BufferedReader reader = new BufferedReader(new FileReader(headerFile));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] header = line.split(":");
            if (header.length == 2) {
                response.putHeader(header[0], header[1]);
            }
        }
        response.putHeader(CacheConstant.HEADER_CACHE_STATUS, CacheConstant.CacheStatus.HIT);
        response.write(contentFile, true);
    }
}
