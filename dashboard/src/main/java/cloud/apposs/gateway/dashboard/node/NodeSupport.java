package cloud.apposs.gateway.dashboard.node;

import cloud.apposs.gateway.GatewayConstants;
import cloud.apposs.gateway.dashboard.GatewayDashboardConfig;
import cloud.apposs.gateway.dashboard.node.fs.FSNode;
import cloud.apposs.gateway.dashboard.node.zookeeper.ZookeeperNode;

import java.nio.charset.Charset;

public final class NodeSupport {
    public static INode getNode(GatewayDashboardConfig configuration) throws Exception {
        String nodeType = configuration.getNodeType();
        if (GatewayDashboardConfig.NODE_TYPE_FS.equals(nodeType)) {
            return new FSNode(configuration.getNodePath());
        } else if (GatewayDashboardConfig.NODE_TYPE_ZOOKEEPER.equals(nodeType)) {
            return new ZookeeperNode(configuration.getNodeHost() + ":" + configuration.getNodePort(), 60000, 60000,
                    configuration.getNodePath(), Charset.forName(configuration.getCharset()));
        } else {
            throw new IllegalArgumentException("Unsupported node type: " + nodeType);
        }
    }
}
