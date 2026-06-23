package cloud.apposs.gateway.watch.fs;

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
import cloud.apposs.logger.Logger;
import cloud.apposs.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.*;

public final class FileGlobalWatcher implements Runnable {
    private final String watchDir;

    private final GatewayContext context;

    private final IWatch watch;

    private final WatchService watchService;

    private final AtomicBoolean running = new AtomicBoolean(true);

    public FileGlobalWatcher(String watchDir, GatewayContext context, FSWatch watch) throws Exception {
        this.watchDir = GatewayUtil.getGlobalPath(watchDir);
        Path path = Paths.get(watchDir);
        boolean exists = Files.exists(path);
        if (!exists) {
            Files.createDirectories(path);
        }
        this.context = context;
        this.watch = watch;
        this.watchService = FileSystems.getDefault().newWatchService();
        this.handleFsPathsInit();
        List<IpAddress> ips = pullIps();
        context.getGlobal().setIps(ips);
        List<Certificate> certificates = pullCertificates();
        context.getGlobal().setCertificates(certificates);
        Map<String, Auth> auths = pullAuths();
        context.getGlobal().setAuths(auths);
        Map<String, AIProvider> providers = AIProvider.mergeSystemProviders(pullProviders());
        context.getGlobal().setProviders(providers);
    }

    public List<IpAddress> pullIps() throws Exception {
        // 扫描文件系统节点/gateway/global/ips下的所有IP集列表，然后解析数据内容
        File ipsDir = new File(watchDir + Node.GLOBAL_IPS.getPath());
        if (!ipsDir.exists()) {
            ipsDir.mkdirs();
            return null;
        }
        File[] ipsFiles = ipsDir.listFiles();
        if (ipsFiles == null) {
            return null;
        }
        List<IpAddress> ips = new ArrayList<>(ipsFiles.length);
        for (File ipFile : ipsFiles) {
            String ipPath = ipFile.getName();
            String ipContent = FileUtil.readString(ipFile);
            IpAddress ip = IpAddressParser.parse(ipPath, ipContent, context);
            ips.add(ip);
        }
        return ips;
    }

    public List<Certificate> pullCertificates() throws Exception {
        // 扫描文件系统节点/gateway/global/certificates下的所有证书列表，然后解析数据内容
        File certificatesDir = new File(watchDir + Node.GLOBAL_CERTIFICATES.getPath());
        if (!certificatesDir.exists()) {
            certificatesDir.mkdirs();
            return null;
        }
        File[] certificatesFiles = certificatesDir.listFiles();
        if (certificatesFiles == null) {
            return null;
        }
        List<Certificate> certificates = new ArrayList<>(certificatesFiles.length);
        for (File certificatesFile : certificatesFiles) {
            String path = certificatesFile.getName();
            String content = FileUtil.readString(certificatesFile);
            Certificate certificate = CertificateParser.parse(path, content, context);
            certificates.add(certificate);
        }
        return certificates;
    }

    public Map<String, Auth> pullAuths() throws Exception {
        // 扫描文件系统节点/gateway/global/auths下的所有认证列表，然后解析数据内容
        File authsDir = new File(watchDir + Node.GLOBAL_AUTHS.getPath());
        if (!authsDir.exists()) {
            authsDir.mkdirs();
            return null;
        }
        File[] authsFiles = authsDir.listFiles();
        if (authsFiles == null) {
            return null;
        }
        Map<String, Auth> auths = new HashMap<>(authsFiles.length);
        for (File authFile : authsFiles) {
            String authPath = authFile.getName();
            String authContent = FileUtil.readString(authFile);
            Auth auth = AuthParser.parse(authPath, authContent, context);
            auths.put(auth.getId(), auth);
        }
        return auths;
    }

    public Map<String, AIProvider> pullProviders() throws Exception {
        // 扫描文件系统节点/gateway/global/providers下的所有认证列表，然后解析数据内容
        File providersDir = new File(watchDir + Node.GLOBAL_PROVIDERS.getPath());
        if (!providersDir.exists()) {
            providersDir.mkdirs();
            return null;
        }
        File[] providersFiles = providersDir.listFiles();
        if (providersFiles == null) {
            return null;
        }
        Map<String, AIProvider> providers = new HashMap<>(providersFiles.length);
        for (File authFile : providersFiles) {
            String authPath = authFile.getName();
            String authContent = FileUtil.readString(authFile);
            AIProvider provider = AIProviderParser.parse(authPath, authContent, context);
            providers.put(provider.getId(), provider);
        }
        return providers;
    }

    @Override
    public void run() {
        try {
            Paths.get(watchDir).register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            handleFileNodeWatch(watchDir);
            Logger.info("Gateway File Watch Service for Path: " + watchDir + " regist success");
            while (running.get()) {
                final WatchKey key = watchService.take();
                try {
                    handleWatchEvent(key);
                } catch (Exception e) {
                    // 有可能配置解析有异常，输出错误后继续监听
                    Logger.warn(e, "Gateway File Watch Service for Path: " + watchDir + " Error");
                }
                boolean valid = key.reset();
                if (!valid) {
                    Logger.warn("Gateway File Watch Service for Path: " + watchDir + " key is invalid");
                }
            }
        } catch (ClosedWatchServiceException e) {
            if (!running.get()) {
                Logger.info("Gateway File Watch Service for Path: " + watchDir + " closed");
            } else {
                Logger.error(e, "Gateway File Watch Service for Path: " + watchDir + " watching closed");
            }
        } catch (Exception e) {
            Logger.error(e, "Gateway File Watch Service for Path: " + watchDir + " watching failed");
        }
    }

    private void handleFsPathsInit() {
        // 初始化Fs Global节点
        File rootDir = new File(watchDir);
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
        // 初始化全局配置对应子节点
        String ipsPath = watchDir + Node.GLOBAL_IPS.getPath();
        File ipsDir = new File(ipsPath);
        if (!ipsDir.exists()) {
            ipsDir.mkdirs();
        }
        String certificatesPath = watchDir + Node.GLOBAL_CERTIFICATES.getPath();
        File certificatesDir = new File(certificatesPath);
        if (!certificatesDir.exists()) {
            certificatesDir.mkdirs();
        }
        String authsPath = watchDir + Node.GLOBAL_AUTHS.getPath();
        File authsDir = new File(authsPath);
        if (!authsDir.exists()) {
            authsDir.mkdirs();
        }
        String providersPath = watchDir + Node.GLOBAL_PROVIDERS.getPath();
        File providersDir = new File(providersPath);
        if (!providersDir.exists()) {
            providersDir.mkdirs();
        }
    }

    private void handleFileNodeWatch(String zone) throws IOException {
        Path ipsPath = Paths.get(zone + Node.GLOBAL_IPS.getPath());
        if (!Files.exists(ipsPath)) {
            Files.createDirectories(ipsPath);
        }
        ipsPath.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        Path certificatesPath = Paths.get(zone + Node.GLOBAL_CERTIFICATES.getPath());
        if (!Files.exists(certificatesPath)) {
            Files.createDirectories(certificatesPath);
        }
        certificatesPath.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        Path authsPath = Paths.get(zone + Node.GLOBAL_AUTHS.getPath());
        if (!Files.exists(authsPath)) {
            Files.createDirectories(authsPath);
        }
        authsPath.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        Path providersPath = Paths.get(zone + Node.GLOBAL_PROVIDERS.getPath());
        if (!Files.exists(providersPath)) {
            Files.createDirectories(providersPath);
        }
        providersPath.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    }

    private void handleWatchEvent(WatchKey key) throws Exception {
        for (WatchEvent<?> watchEvent : key.pollEvents()) {
            final WatchEvent.Kind<?> kind = watchEvent.kind();
            if (kind == OVERFLOW) {
                continue;
            }
            Path watchable = (Path) key.watchable();
            Path path = (Path) watchEvent.context();
            String ipsName = Node.GLOBAL_IPS.getPath().replace("/", "");
            String certificatesName = Node.GLOBAL_CERTIFICATES.getPath().replace("/", "");
            String authsName = Node.GLOBAL_AUTHS.getPath().replace("/", "");
            String providersName = Node.GLOBAL_PROVIDERS.getPath().replace("/", "");
            if (kind == ENTRY_CREATE) {
                if (watchable.endsWith(ipsName)) {
                    handleIpAdd(watchable, path);
                } else if (watchable.endsWith(certificatesName)) {
                    handleCertificateAdd(watchable, path);
                } else if (watchable.endsWith(authsName)) {
                    handleAuthAdd(watchable, path);
                } else if (watchable.endsWith(providersName)) {
                    handleProviderAdd(watchable, path);
                }
            } else if (kind == ENTRY_MODIFY) {
                if (watchable.endsWith(ipsName)) {
                    handleIpUpdate(watchable, path);
                } else if (watchable.endsWith(certificatesName)) {
                    handleCertificateUpdate(watchable, path);
                } else if (watchable.endsWith(authsName)) {
                    handleAuthUpdate(watchable, path);
                } else if (watchable.endsWith(providersName)) {
                    handleProviderUpdate(watchable, path);
                }
            } else if (kind == ENTRY_DELETE) {
                if (watchable.endsWith(ipsName)) {
                    handleIpRemove(watchable, path);
                } else if (watchable.endsWith(certificatesName)) {
                    handleCertificateRemove(watchable, path);
                } else if (watchable.endsWith(authsName)) {
                    handleAuthRemove(watchable, path);
                } else if (watchable.endsWith(providersName)) {
                    handleProviderRemove(watchable, path);
                }
            }
        }
    }

    private boolean handleIpAdd(Path rootPath, Path file) throws Exception {
        IpAddress infomation = parseIpAddressFile(rootPath, file);
        if (infomation == null) {
            return false;
        }
        context.getGlobal().addIp(infomation);
        return true;
    }

    private boolean handleIpUpdate(Path rootPath, Path file) throws Exception {
        IpAddress infomation = parseIpAddressFile(rootPath, file);
        if (infomation == null) {
            return false;
        }
        context.getGlobal().updateIp(infomation);
        return true;
    }

    private boolean handleIpRemove(Path rootPath, Path file) {
        String id = file.toString();
        context.getGlobal().removeIp(id);
        return true;
    }

    private IpAddress parseIpAddressFile(Path rootPath, Path file) throws Exception {
        String id = file.toString();
        Path filePath = rootPath.resolve(id);
        if (!waitForFileExistence(filePath, 4000)) {
            return null;
        }
        String content = FileUtil.readString(new File(filePath.toString()));
        return IpAddressParser.parse(id, content, context);
    }

    private boolean handleCertificateAdd(Path rootPath, Path file) throws Exception {
        Certificate infomation = parseCertificateFile(rootPath, file);
        if (infomation == null) {
            return false;
        }
        context.getGlobal().addCertificate(infomation);
        return true;
    }

    private boolean handleCertificateUpdate(Path rootPath, Path ipFile) throws Exception {
        Certificate infomation = parseCertificateFile(rootPath, ipFile);
        if (infomation == null) {
            return false;
        }
        context.getGlobal().updateCertificate(infomation);
        return true;
    }

    private boolean handleCertificateRemove(Path rootPath, Path file) throws Exception {
        String id = file.toString();
        context.getGlobal().removeCertificate(id);
        return true;
    }

    private Certificate parseCertificateFile(Path rootPath, Path ipFile) throws Exception {
        String id = ipFile.toString();
        Path filePath = rootPath.resolve(id);
        if (!waitForFileExistence(filePath, 4000)) {
            return null;
        }
        String content = FileUtil.readString(new File(filePath.toString()));
        return CertificateParser.parse(id, content, context);
    }

    private boolean handleAuthAdd(Path rootPath, Path file) throws Exception {
        Auth infomation = parseAuthFile(rootPath, file);
        if (infomation == null) {
            return false;
        }
        context.getGlobal().addAuth(infomation);
        return true;
    }

    private boolean handleAuthUpdate(Path rootPath, Path file) throws Exception {
        Auth infomation = parseAuthFile(rootPath, file);
        if (infomation == null) {
            return false;
        }
        context.getGlobal().updateAuth(infomation);
        return true;
    }

    private boolean handleAuthRemove(Path rootPath, Path file) throws Exception {
        String id = file.toString();
        context.getGlobal().removeAuth(id);
        return true;
    }

    private Auth parseAuthFile(Path rootPath, Path file) throws Exception {
        String id = file.toString();
        Path filePath = rootPath.resolve(id);
        if (!waitForFileExistence(filePath, 4000)) {
            return null;
        }
        String content = FileUtil.readString(new File(filePath.toString()));
        return AuthParser.parse(id, content, context);
    }

    private boolean handleProviderAdd(Path rootPath, Path file) throws Exception {
        AIProvider infomation = parseProviderFile(rootPath, file);
        if (infomation == null) {
            return false;
        }
        context.getGlobal().addProvider(infomation);
        return true;
    }

    private boolean handleProviderUpdate(Path rootPath, Path file) throws Exception {
        AIProvider infomation = parseProviderFile(rootPath, file);
        if (infomation == null) {
            return false;
        }
        context.getGlobal().updateProvider(infomation);
        return true;
    }

    private boolean handleProviderRemove(Path rootPath, Path file) throws Exception {
        String id = file.toString();
        context.getGlobal().removeProvider(id);
        return true;
    }

    private AIProvider parseProviderFile(Path rootPath, Path file) throws Exception {
        String id = file.toString();
        Path filePath = rootPath.resolve(id);
        if (!waitForFileExistence(filePath, 4000)) {
            return null;
        }
        String content = FileUtil.readString(new File(filePath.toString()));
        return AIProviderParser.parse(id, content, context);
    }

    private boolean waitForFileExistence(Path file, long timeout) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeout) {
            if (Files.exists(file)) {
                return true;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return false;
            }
        }
        return false;
    }

    public void close() {
        try {
            running.set(false);
            watchService.close();
        } catch (Exception e) {
            Logger.error(e, "Gateway File Watch Service for Path: " + watchDir + " close failed");
        }
    }
}
