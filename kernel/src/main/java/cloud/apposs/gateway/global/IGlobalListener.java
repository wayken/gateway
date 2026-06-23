package cloud.apposs.gateway.global;

import java.util.EventListener;

/**
 * 网关全局配置监听器
 */
public interface IGlobalListener extends EventListener {
    void onGlobalChanged(Global global) throws Exception;
}
