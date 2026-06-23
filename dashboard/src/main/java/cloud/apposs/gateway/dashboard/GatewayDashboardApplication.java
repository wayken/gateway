package cloud.apposs.gateway.dashboard;

import cloud.apposs.bootor.ApplicationContext;
import cloud.apposs.bootor.HttpApplication;
import cloud.apposs.gateway.config.YamlLoader;
import cloud.apposs.util.GetOpt;

/**
 * 网关控制台服务，用于网关的监控和管理，提供如下功能：
 * <pre>
 *     1. 网关操作界面，包括网关配置、插件配置、网关监控、网关管理等（SPA静态页部署）
 *     2. 网关操作接口，包括网关监控数据、网关配置数据、网关插件数据等（RESTful接口）
 * </pre>
 * 启动控制台服务：java -jar teambeit-gateway-dashboard.jar -c ~/etc/gateway/gateway-dashboard.yaml 2>&1 &
 */

public class GatewayDashboardApplication {
    public static void main(String[] args) throws Exception {
        ApplicationContext application = HttpApplication.build(generateConfiguration(args));
        application.setBanner(new GatewayDashboardBanner());
        application.run(GatewayDashboardApplication.class, args);
    }

    public static GatewayDashboardConfig generateConfiguration(String... args) throws Exception {
        String yamlFile = GatewayDashboardConstants.DEFAULT_CONFIG_FILE;
        // 判断是否从命令行中传递配置文件路径
        GetOpt option = new GetOpt(args);
        if (option.containsKey("c")) {
            yamlFile = option.get("c");
        }
        GatewayDashboardConfig config = new GatewayDashboardConfig();
        YamlLoader.parse(config, yamlFile);
        return config;
    }
}
