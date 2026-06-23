package cloud.apposs.gateway.watch.zookeeper;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.ai.AIProvider;
import cloud.apposs.gateway.ai.AIProviderParser;
import cloud.apposs.gateway.global.auth.Auth;
import cloud.apposs.gateway.global.auth.AuthParser;
import cloud.apposs.gateway.global.certificate.Certificate;
import cloud.apposs.gateway.global.certificate.CertificateParser;
import cloud.apposs.gateway.global.ips.IpAddress;
import cloud.apposs.gateway.global.ips.IpAddressParser;
import cloud.apposs.gateway.util.GatewayUtil;
import cloud.apposs.gateway.watch.IWatch;
import cloud.apposs.gateway.watch.Node;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PathGlobalWatcher {
    protected final GatewayContext context;

    private final IWatch watcher;

    private final String path;

    private final CuratorFramework zkClient;

    private final ZookeeperPathSupport pathSupport;

    private final Charset charset;

    public PathGlobalWatcher(GatewayContext context, IWatch watcher, String path, CuratorFramework zkClient,
                            ZookeeperPathSupport pathSupport, Charset charset) throws Exception {
        this.context = context;
        this.watcher = watcher;
        this.path = GatewayUtil.getGlobalPath(path);
        this.zkClient = zkClient;
        this.pathSupport = pathSupport;
        this.charset = charset;
        handleZkPathsInit();
        List<IpAddress> ips = pullIps();
        context.getGlobal().setIps(ips);
        List<Certificate> certificates = pullCertificates();
        context.getGlobal().setCertificates(certificates);
        Map<String, Auth> auths = pullAuths();
        context.getGlobal().setAuths(auths);
        Map<String, AIProvider> providers = AIProvider.mergeSystemProviders(pullProviders());
        context.getGlobal().setProviders(providers);
    }

    public void start() throws Exception {
        handleIpsPathWatch();
        handleCertificatesPathWatch();
        handleAuthsPathWatch();
        handleProvidersPathWatch();
    }

    public List<IpAddress> pullIps() throws Exception {
        // 扫描Zookeeper节点/gateway/global/ips下的所有IP集列表，然后解析数据内容
        CuratorZookeeperClient zookeeperClient = zkClient.getZookeeperClient();
        zkClient.newNamespaceAwareEnsurePath(path + Node.GLOBAL_IPS.getPath()).ensure(zookeeperClient);
        List<String> ipsPathList = zkClient.getChildren().forPath(path + Node.GLOBAL_IPS.getPath());
        if (ipsPathList == null) {
            return null;
        }
        List<IpAddress> ips = new ArrayList<IpAddress>();
        for (String ipPath : ipsPathList) {
            String ipFullPath = path + Node.GLOBAL_IPS.getPath() + "/" + ipPath;
            String ipContent = new String(zkClient.getData().forPath(ipFullPath), charset);
            IpAddress ip = IpAddressParser.parse(ipPath, ipContent, context);
            ips.add(ip);
        }
        return ips;
    }

    private List<Certificate> pullCertificates() throws Exception {
        // 扫描Zookeeper节点/gateway/global/certificates下的所有证书列表，然后解析数据内容
        CuratorZookeeperClient zookeeperClient = zkClient.getZookeeperClient();
        zkClient.newNamespaceAwareEnsurePath(path + Node.GLOBAL_CERTIFICATES.getPath()).ensure(zookeeperClient);
        List<String> certificatesPathList = zkClient.getChildren().forPath(path + Node.GLOBAL_CERTIFICATES.getPath());
        if (certificatesPathList == null) {
            return null;
        }
        List<Certificate> certificates = new ArrayList<>(certificatesPathList.size());
        for (String certificatePath : certificatesPathList) {
            String certificateFullPath = path + Node.GLOBAL_CERTIFICATES.getPath() + "/" + certificatePath;
            String content = new String(zkClient.getData().forPath(certificateFullPath), charset);
            Certificate certificate = CertificateParser.parse(certificatePath, content, context);
            certificates.add(certificate);
        }
        return certificates;
    }

    private Map<String, Auth> pullAuths() throws Exception {
        // 扫描Zookeeper节点/gateway/global/auths下的所有授权列表，然后解析数据内容
        CuratorZookeeperClient zookeeperClient = zkClient.getZookeeperClient();
        zkClient.newNamespaceAwareEnsurePath(path + Node.GLOBAL_AUTHS.getPath()).ensure(zookeeperClient);
        List<String> authsPathList = zkClient.getChildren().forPath(path + Node.GLOBAL_AUTHS.getPath());
        if (authsPathList == null) {
            return null;
        }
        Map<String, Auth> auths = new HashMap<String, Auth>();
        for (String authPath : authsPathList) {
            String authFullPath = path + Node.GLOBAL_AUTHS.getPath() + "/" + authPath;
            String content = new String(zkClient.getData().forPath(authFullPath), charset);
            Auth auth = AuthParser.parse(authPath, content, context);
            auths.put(auth.getId(), auth);
        }
        return auths;
    }

    private Map<String, AIProvider> pullProviders() throws Exception {
        // 扫描Zookeeper节点/gateway/global/providers下的所有授权列表，然后解析数据内容
        CuratorZookeeperClient zookeeperClient = zkClient.getZookeeperClient();
        zkClient.newNamespaceAwareEnsurePath(path + Node.GLOBAL_PROVIDERS.getPath()).ensure(zookeeperClient);
        List<String> providersPathList = zkClient.getChildren().forPath(path + Node.GLOBAL_PROVIDERS.getPath());
        if (providersPathList == null) {
            return null;
        }
        Map<String, AIProvider> providers = new HashMap<String, AIProvider>();
        for (String providerPath : providersPathList) {
            String providerFullPath = path + Node.GLOBAL_AUTHS.getPath() + "/" + providerPath;
            String content = new String(zkClient.getData().forPath(providerFullPath), charset);
            AIProvider provider = AIProviderParser.parse(providerPath, content, context);
            providers.put(provider.getId(), provider);
        }
        return providers;
    }

    private void handleZkPathsInit() throws Exception {
        // 先判断根节点是否存在，不存在则创建
        CuratorZookeeperClient zookeeperClient = zkClient.getZookeeperClient();
        zkClient.newNamespaceAwareEnsurePath(path).ensure(zookeeperClient);
        // 初始化全局配置对应子节点
        String ipsPath = path + Node.GLOBAL_IPS.getPath();
        zkClient.newNamespaceAwareEnsurePath(ipsPath).ensure(zookeeperClient);
        String certificatesPath = path + Node.GLOBAL_CERTIFICATES.getPath();
        zkClient.newNamespaceAwareEnsurePath(certificatesPath).ensure(zookeeperClient);
        String authsPath = path + Node.GLOBAL_AUTHS.getPath();
        zkClient.newNamespaceAwareEnsurePath(authsPath).ensure(zookeeperClient);
    }

    private void handleIpsPathWatch() throws Exception {
        pathSupport.addPathChildrenCache(path + Node.GLOBAL_IPS.getPath(), (client, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    handleIpAdd(event.getData());
                    break;
                case CHILD_UPDATED:
                    handleIpUpdate(event.getData());
                    break;
                case CHILD_REMOVED:
                    handleIpRemove(event.getData());
                    break;
                default:
                    break;
            }
        });
    }

    private void handleCertificatesPathWatch() throws Exception {
        pathSupport.addPathChildrenCache(Node.GLOBAL_CERTIFICATES.getPath(), (client, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    handleCertificateAdd(event.getData());
                    break;
                case CHILD_UPDATED:
                    handleCertificateUpdate(event.getData());
                    break;
                case CHILD_REMOVED:
                    handleCertificateRemove(event.getData());
                    break;
                default:
                    break;
            }
        });
    }

    private void handleAuthsPathWatch() throws Exception {
        pathSupport.addPathChildrenCache(Node.GLOBAL_AUTHS.getPath(), (client, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    handleAuthAdd(event.getData());
                    break;
                case CHILD_UPDATED:
                    handleAuthUpdate(event.getData());
                    break;
                case CHILD_REMOVED:
                    handleAuthRemove(event.getData());
                    break;
                default:
                    break;
            }
        });
    }

    private void handleProvidersPathWatch() throws Exception {
        pathSupport.addPathChildrenCache(Node.GLOBAL_PROVIDERS.getPath(), (client, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    handleProviderAdd(event.getData());
                    break;
                case CHILD_UPDATED:
                    handleProviderUpdate(event.getData());
                    break;
                case CHILD_REMOVED:
                    handleProviderRemove(event.getData());
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * 处理IP集配置添加
     *
     * @param  data IP集配置数据
     * @return 是否处理成功
     */
    private boolean handleIpAdd(ChildData data) throws Exception {
        IpAddress infomation = parseIpFile(data);
        if (infomation == null) {
            return false;
        }
        context.getGlobal().addIp(infomation);
        return true;
    }

    /**
     * 处理IP集配置更新
     *
     * @param  data IP集配置数据
     * @return 是否处理成功
     */
    private boolean handleIpUpdate(ChildData data) throws Exception {
        IpAddress infomation = parseIpFile(data);
        if (infomation == null) {
            return false;
        }
        context.getGlobal().updateIp(infomation);
        return true;
    }

    /**
     * 处理IP集配置删除
     *
     * @param  data IP集配置数据
     * @return 是否处理成功
     */
    private boolean handleIpRemove(ChildData data) throws Exception {
        String path = data.getPath();
        path = path.substring(path.lastIndexOf("/") + 1);
        context.getGlobal().removeIp(path);
        return true;
    }

    /**
     * 添加证书
     *
     * @param  data 证书数据
     * @return 是否处理成功
     */
    private boolean handleCertificateAdd(ChildData data) throws Exception {
        Certificate infomation = parseCertificateFile(data);
        if (infomation == null) {
            return false;
        }
        context.getGlobal().addCertificate(infomation);
        return true;
    }

    /**
     * 证书更新
     *
     * @param  data 证书数据
     * @return 是否处理成功
     */
    private boolean handleCertificateUpdate(ChildData data) throws Exception {
        Certificate infomation = parseCertificateFile(data);
        if (infomation == null) {
            return false;
        }
        context.getGlobal().updateCertificate(infomation);
        return true;
    }

    /**
     * 证书删除
     *
     * @param  data 证书数据
     * @return 是否处理成功
     */
    private boolean handleCertificateRemove(ChildData data) throws Exception {
        String path = data.getPath();
        path = path.substring(path.lastIndexOf("/") + 1);
        context.getGlobal().removeCertificate(path);
        return true;
    }

    /**
     * 处理授权配置添加
     *
     * @param  data 授权配置数据
     * @return 是否处理成功
     */
    private boolean handleAuthAdd(ChildData data) throws Exception {
        Auth auth = parseAuthFile(data);
        if (auth == null) {
            return false;
        }
        context.getGlobal().addAuth(auth);
        return true;
    }

    /**
     * 处理授权配置更新
     *
     * @param  data 授权配置数据
     * @return 是否处理成功
     */
    private boolean handleAuthUpdate(ChildData data) throws Exception {
        Auth auth = parseAuthFile(data);
        if (auth == null) {
            return false;
        }
        context.getGlobal().updateAuth(auth);
        return true;
    }

    /**
     * 处理授权配置删除
     *
     * @param  data 授权配置数据
     * @return 是否处理成功
     */
    private boolean handleAuthRemove(ChildData data) throws Exception {
        String path = data.getPath();
        path = path.substring(path.lastIndexOf("/") + 1);
        context.getGlobal().removeAuth(path);
        return true;
    }

    /**
     * 处理AI服务商配置添加
     *
     * @param  data AI服务商配置数据
     * @return 是否处理成功
     */
    private boolean handleProviderAdd(ChildData data) throws Exception {
        AIProvider provider = parseProviderFile(data);
        if (provider == null) {
            return false;
        }
        context.getGlobal().addProvider(provider);
        return true;
    }

    /**
     * 处理AI服务商配置更新
     *
     * @param  data AI服务商配置数据
     * @return 是否处理成功
     */
    private boolean handleProviderUpdate(ChildData data) throws Exception {
        AIProvider provider = parseProviderFile(data);
        if (provider == null) {
            return false;
        }
        context.getGlobal().updateProvider(provider);
        return true;
    }

    /**
     * 处理AI服务商配置删除
     *
     * @param  data AI服务商配置数据
     * @return 是否处理成功
     */
    private boolean handleProviderRemove(ChildData data) throws Exception {
        String path = data.getPath();
        path = path.substring(path.lastIndexOf("/") + 1);
        context.getGlobal().removeProvider(path);
        return true;
    }

    private IpAddress parseIpFile(ChildData data) throws Exception {
        // 截取配置的ID，即/gateway/global/ips/xxx中的xxx
        String path = data.getPath();
        path = path.substring(path.lastIndexOf("/") + 1);
        byte[] byteData = data.getData();
        String content = new String(byteData, charset);
        return IpAddressParser.parse(path, content, context);
    }

    private Certificate parseCertificateFile(ChildData data) throws Exception {
        // 截取配置的ID，即/gateway/global/certificate/xxx中的xxx
        String path = data.getPath();
        path = path.substring(path.lastIndexOf("/") + 1);
        byte[] byteData = data.getData();
        String content = new String(byteData, charset);
        return CertificateParser.parse(path, content, context);
    }

    private Auth parseAuthFile(ChildData data) throws Exception {
        // 截取配置的ID，即/gateway/global/certificate/xxx中的xxx
        String path = data.getPath();
        path = path.substring(path.lastIndexOf("/") + 1);
        byte[] byteData = data.getData();
        String content = new String(byteData, charset);
        return AuthParser.parse(path, content, context);
    }

    private AIProvider parseProviderFile(ChildData data) throws Exception {
        // 截取配置的ID，即/gateway/global/provider/xxx中的xxx
        String path = data.getPath();
        path = path.substring(path.lastIndexOf("/") + 1);
        byte[] byteData = data.getData();
        String content = new String(byteData, charset);
        return AIProviderParser.parse(path, content, context);
    }
}
