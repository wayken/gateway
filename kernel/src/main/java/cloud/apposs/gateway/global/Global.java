package cloud.apposs.gateway.global;

import cloud.apposs.gateway.ai.AIProvider;
import cloud.apposs.gateway.global.auth.Auth;
import cloud.apposs.gateway.global.certificate.Certificate;
import cloud.apposs.gateway.global.ips.IpAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 网关全局配置信息
 */
public final class Global {
    public static final String KEY_CERTIFICATES = "certificates";

    private final Map<String, List<IGlobalListener>> listeners = new ConcurrentHashMap<>();

    // 全局IP组列表
    private List<IpAddress> ips = new ArrayList<>();

    // 全局证书配置列表
    private List<Certificate> certificates = new ArrayList<>();

    // 全局授权配置列表
    private Map<String, Auth> auths = new ConcurrentHashMap<>();

    // 全局AI配置列表
    private Map<String, AIProvider> providers = new ConcurrentHashMap<>();

    public void addListener(String name, IGlobalListener listener) {
        listeners.putIfAbsent(name, new ArrayList<IGlobalListener>());
        listeners.get(name).add(listener);
    }

    public List<IpAddress> getIps() {
        return ips;
    }

    public void addIp(IpAddress address) {
        ips.add(address);
    }

    public boolean updateIp(IpAddress updatedAddress) {
        for (IpAddress address : ips) {
            if (address.getId().equals(updatedAddress.getId())) {
                address.update(updatedAddress);
                return true;
            }
        }
        return false;
    }

    public boolean removeIp(String id) {
        for (IpAddress address : ips) {
            if (address.getId().equals(id)) {
                ips.remove(address);
                return true;
            }
        }
        return false;
    }

    public void setIps(List<IpAddress> ips) {
        this.ips = ips;
    }

    public List<Certificate> getCertificates() {
        return certificates;
    }

    public void addCertificate(Certificate certificate) throws Exception {
        certificates.add(certificate);
        handleLinsterFire(KEY_CERTIFICATES);
    }

    public boolean updateCertificate(Certificate certificate) throws Exception {
        for (Certificate cert : certificates) {
            if (cert.getId().equals(certificate.getId())) {
                certificates.remove(cert);
                certificates.add(certificate);
                handleLinsterFire(KEY_CERTIFICATES);
                return true;
            }
        }
        return false;
    }

    public boolean removeCertificate(String id) throws Exception {
        for (Certificate cert : certificates) {
            if (cert.getId().equals(id)) {
                certificates.remove(cert);
                handleLinsterFire(KEY_CERTIFICATES);
                return true;
            }
        }
        return false;
    }

    public void setCertificates(List<Certificate> certificates) {
        if (certificates == null) {
            this.certificates = new ArrayList<>();
        } else {
            this.certificates = certificates;
        }
    }

    public Auth getAuth(String id) {
        return auths.get(id);
    }

    public void addAuth(Auth auth) throws Exception {
        auths.put(auth.getId(), auth);
    }

    public boolean updateAuth(Auth auth) throws Exception {
        Auth oldAuth = auths.get(auth.getId());
        if (oldAuth != null) {
            auths.remove(oldAuth.getId());
            auths.put(auth.getId(), auth);
            return true;
        }
        return false;
    }

    public boolean removeAuth(String id) throws Exception {
        return auths.remove(id) != null;
    }

    public void setAuths(Map<String, Auth> auths) {
        if (auths == null) {
            this.auths = new ConcurrentHashMap<>();
        } else {
            this.auths = auths;
        }
    }

    public AIProvider getProvider(String id) {
        return providers.get(id);
    }

    public void addProvider(AIProvider provider) throws Exception {
        providers.put(provider.getId(), provider);
    }

    public boolean updateProvider(AIProvider provider) throws Exception {
        AIProvider oldAuth = providers.get(provider.getId());
        if (oldAuth != null) {
            providers.remove(oldAuth.getId());
            providers.put(provider.getId(), provider);
            return true;
        }
        return false;
    }

    public boolean removeProvider(String id) throws Exception {
        return providers.remove(id) != null;
    }

    public void setProviders(Map<String, AIProvider> providers) {
        if (providers == null) {
            this.providers = new ConcurrentHashMap<>();
        } else {
            this.providers = providers;
        }
    }

    private void handleLinsterFire(String key) throws Exception {
        List<IGlobalListener> listeners = this.listeners.get(key);
        if (listeners != null) {
            for (IGlobalListener listener : listeners) {
                listener.onGlobalChanged(this);
            }
        }
    }
}
