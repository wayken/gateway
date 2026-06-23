package cloud.apposs.gateway.plugin.runner.logger;

import cloud.apposs.gateway.GatewayConfig;
import cloud.apposs.gateway.GatewayHttpRequest;
import cloud.apposs.gateway.GatewayHttpResponse;
import cloud.apposs.gateway.annotation.CommonPlugin;
import cloud.apposs.gateway.plugin.PluginAdapter;
import cloud.apposs.gateway.plugin.PluginResult;
import cloud.apposs.gateway.route.Route;
import cloud.apposs.gateway.variable.VariableParser;
import cloud.apposs.gateway.zone.Zone;
import cloud.apposs.logger.Configuration;
import cloud.apposs.logger.Level;
import cloud.apposs.logger.Log;
import cloud.apposs.react.React;
import cloud.apposs.util.Param;

import java.util.Properties;

/**
 * 日志记录插件，用于记录请求的访问日志
 */
@CommonPlugin
public class LoggerPlugin extends PluginAdapter {
    public static final String NAME = "Logger";
    private static final int PLUGIN_PRIORITY = 1040;

    public static final String KEY_LOG_ENABLE = "enable";
    public static final String KEY_APPENDAR = "appendar";
    public static final String KEY_LOG_PATH = "path";
    public static final String KEY_LOG_FORMAT = "format";

    public static final String ATTR_KEY_TIME = "AttrKeyLoggerTime";

    private Log logger;

    private boolean enable = true;

    /**
     * 日志解析器
     */
    private VariableParser parser;

    public LoggerPlugin(GatewayConfig config) {
        super(NAME);
        Param pluginConfig = Param.builder(LoggerPlugin.KEY_APPENDAR, config.getAccessLogAppender())
                .setString(LoggerPlugin.KEY_LOG_PATH, config.getAccessLogPath())
                .setBoolean(LoggerPlugin.KEY_LOG_ENABLE, config.isAccessLogEnable())
                .setString(LoggerPlugin.KEY_LOG_FORMAT, config.getAccessLogFormat());
        this.enable = pluginConfig.getBoolean(KEY_LOG_ENABLE, true);
        if (enable) {
            Properties properties = new Properties();
            properties.put(Configuration.Prefix.APPENDER, pluginConfig.getString(KEY_APPENDAR, "console"));
            properties.put(Configuration.Prefix.LEVEL, Level.INFO.getString());
            properties.put(Configuration.Prefix.FILE, pluginConfig.getString(KEY_LOG_PATH, "logs"));
            properties.put(Configuration.Prefix.FORMAT, "%m%n%E");
            this.logger = new Log(properties);
            this.parser = new VariableParser(pluginConfig.getString(KEY_LOG_FORMAT));
        }
    }

    @Override
    public int getPriority() {
        return PLUGIN_PRIORITY;
    }

    @Override
    public React<PluginResult> preFilter(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route) {
        // 记录请求开始时间
        request.setAttribute(ATTR_KEY_TIME, System.currentTimeMillis());
        return React.just(PluginResult.SUCCESS);
    }

    @Override
    public void afterCompletion(GatewayHttpRequest request, GatewayHttpResponse response, Zone zone, Route route, Throwable throwable) {
        if (!enable) {
            return;
        }
        if (throwable != null) {
            logger.error(throwable, parser.parse(request, response));
        } else {
            logger.info(parser.parse(request, response));
        }
    }
}
