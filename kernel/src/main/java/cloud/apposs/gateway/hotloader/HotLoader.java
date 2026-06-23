package cloud.apposs.gateway.hotloader;

import cloud.apposs.gateway.GatewayConfig;
import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.logger.Logger;

import java.util.Objects;
import java.util.Timer;

/**
 * 热加载进程，用于热加载网关插件实现类
 */
public class HotLoader {
    /** 插件热加载的 LIB 包路径 */
    private final String hotLoadPath;

    private final HotLoadRunner runner;

    public HotLoader(GatewayConfig configuration, GatewayContext context) {
        this.hotLoadPath = configuration.getPluginLibary();
        this.runner = new HotLoadRunner(hotLoadPath, context);
    }

    /**
     * 手动触发自定义插件加载
     */
    public void scan() {
        if (Objects.nonNull(runner)) {
            runner.scan();
        }
    }
}
