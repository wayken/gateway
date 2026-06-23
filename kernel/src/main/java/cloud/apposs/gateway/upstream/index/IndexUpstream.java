package cloud.apposs.gateway.upstream.index;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayException;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.upstream.AbstractUpstream;
import cloud.apposs.gateway.upstream.UpstreamType;
import cloud.apposs.gateway.util.WebUtil;
import cloud.apposs.react.React;
import cloud.apposs.util.HttpStatus;
import cloud.apposs.util.Param;

import java.io.File;
import java.nio.file.Paths;

/**
 * 目录文件读取上游，负责转发请求读取对应文件目录
 */
public class IndexUpstream extends AbstractUpstream {
    public static final String KEY_STATIC = "static";
    public static final String KEY_PATH = "path";
    public static final String KEY_INDEX = "index";
    public static final String KEY_FALLBACK = "fallback";

    /** 文件读取目录 */
    private String directory;

    /** 当请求不指定目录时默认直接读取哪个文件 */
    private String index = "index.html";

    /** 是否将请求路径作为文件名 */
    private boolean alias = false;

    /** 当请求的文件不存在时，是否使用备用文件 */
    private String fallback = null;

    public IndexUpstream(String id, String name, GatewayContext context, Param configuration) {
        super(id, UpstreamType.INDEX.name(), name, context, configuration);
        if (!configuration.containsKey(KEY_STATIC)) {
            throw new IllegalArgumentException("IndexUpstream parameter root must not be null");
        }
        Param staticConfig = configuration.getParam(KEY_STATIC);
        String dataSourcePath = context.getConfig().getDataSourcePath();
        String path = staticConfig.getString(KEY_PATH).trim();
        // 如果路径不是绝对路径，则使用数据源路径作为前缀
        if (!Paths.get(path).isAbsolute()) {
            path = dataSourcePath + File.separator + path;
        }
        this.directory = path;
        if (staticConfig.containsKey(KEY_INDEX)) {
            this.index = staticConfig.getString(KEY_INDEX).trim();
        }
        if (staticConfig.containsKey(KEY_FALLBACK)) {
            this.fallback = staticConfig.getString(KEY_FALLBACK).trim();
        }
    }

    @Override
    public React<?> request(GatewayHttpRequest request, GatewayHttpResponse response, GatewayContext context) throws Exception {
        return React.emitter(() -> {
            String path = WebUtil.getRequestPath(request);
            // 如果请求路径是根路径则默认读取index.html文件
            if (path.equals("/")) {
                path = path + index;
            }
            // 路径安全校验
            if (path.contains("..")) {
                throw new GatewayException(HttpStatus.HTTP_STATUS_403, "Read \"" + path + "\" Failed (Illegal Path)");
            }
            String filePath = directory + path;
            if (alias) {
                filePath = directory + "/" + path.substring(path.lastIndexOf("/") + 1);
            }
            File readFile = new File(filePath);
            if (!readFile.exists()) {
                // 如果文件不存在且配置了备用回退文件，则尝试读取备用回退文件，主要应用于SPA页
                if (fallback != null && !fallback.isEmpty()) {
                    readFile = new File(directory + "/" + fallback);
                }
            }
            if (!readFile.exists()) {
                throw new GatewayException(HttpStatus.HTTP_STATUS_404, "Read \"" + path + "\" Failed (File Not Found)");
            }
            return readFile;
        });
    }
}
