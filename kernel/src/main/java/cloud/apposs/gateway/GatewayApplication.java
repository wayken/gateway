package cloud.apposs.gateway;

import cloud.apposs.gateway.config.YamlLoader;
import cloud.apposs.gateway.netty.NettyApplicationContext;
import cloud.apposs.util.GetOpt;

/**
 * 网关服务入口，
 * 注意：
 * <pre>
 * 1、网关是属于业务层和后台层的门面模式，所有请求均是以网关作为第一道出口
 * 2、网关服务除了鉴权、限流、灰度、自定义通用功能插件等基本逻辑外，禁止在网关写业务相关的代码，保持网关服务轻量简洁、无状态
 * </pre>
 * 启动网关：java -jar teambeit-gateway-kernel.jar -c ~/etc/gateway/gateway.yaml 2>&1 &
 */
public class GatewayApplication {
    public static void main(String[] args) throws Exception {
        GatewayApplication.run(args);
    }

    /**
     * 启动运行网关服务
     */
    public static ApplicationContext run(String... args) throws Exception {
        return run(generateConfiguration(args), args);
    }

    public static ApplicationContext run(GatewayConfig config, String... args) throws Exception {
        return new NettyApplicationContext(config).run(args);
    }

    public static ApplicationContext build(String... args) throws Exception {
        return build(generateConfiguration(args));
    }

    public static ApplicationContext build(GatewayConfig config) throws Exception {
        return new NettyApplicationContext(config);
    }

    public static GatewayConfig generateConfiguration(String... args) throws Exception {
        String yamlFile = GatewayConstants.DEFAULT_CONFIG_FILE;
        // 判断是否从命令行中传递配置文件路径
        GetOpt option = new GetOpt(args);
        if (option.containsKey("c")) {
            yamlFile = option.get("c");
        }
        GatewayConfig config = new GatewayConfig();
        YamlLoader.parse(config, yamlFile);
        return config;
    }
}
