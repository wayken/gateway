package cloud.apposs.gateway.dashboard.node;

import cloud.apposs.gateway.dashboard.GatewayDashboardConfig;
import cloud.apposs.ioc.BeanFactory;
import cloud.apposs.ioc.BeanFactoryPostProcessor;
import cloud.apposs.ioc.BeansException;
import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.ioc.annotation.Component;

/**
 * 服务节点后置处理器，用于初始化节点配置
 */
@Component
public class NodePostProcessor implements BeanFactoryPostProcessor {
    @Autowired
    private GatewayDashboardConfig configuration;

    @Override
    public void postProcessBeanFactory(BeanFactory beanFactory) throws BeansException {
        INode node = null;
        try {
            node = NodeSupport.getNode(configuration);
        } catch (Exception e) {
            throw new BeansException("Failed to create node", e);
        }
        beanFactory.addBean(node);
    }
}
