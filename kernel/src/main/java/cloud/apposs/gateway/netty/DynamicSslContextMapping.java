package cloud.apposs.gateway.netty;

import io.netty.handler.ssl.SslContext;
import io.netty.util.Mapping;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义动态域名证书服务，支持
 * <pre>
 *     1. 支持动态更新域名对应的证书（更新 SslContext 实例即可生效）
 *     2. 无需重启服务即可完成证书续期和热更新
 *     3. 可灵活支持通配符域名匹配、自定义优先级匹配规则
 * </pre>
 */
public class DynamicSslContextMapping implements Mapping<String, SslContext> {
    // 通配符
    public static final String WILDCARD = "*";

    private final ConcurrentHashMap<String, SslContext> domainContextMapping = new ConcurrentHashMap<>();

    @Override
    public SslContext map(String domain) {
        SslContext context = handleSSLDomainContextMapping(domain);
        if (context == null) {
            throw new IllegalStateException("No SslContext found for domain: " + domain);
        }
        return context;
    }

    public void put(String domain, SslContext sslContext) {
        domainContextMapping.put(domain.toLowerCase(), sslContext);
    }

    public void remove(String domain) {
        domainContextMapping.remove(domain.toLowerCase());
    }

    private boolean isWildcardMatch(String pattern, String domain) {
        if (!pattern.startsWith("*.")) {
            return false;
        }
        String suffix = pattern.substring(1);
        return domain.endsWith(suffix);
    }

    private SslContext handleSSLDomainContextMapping(String domain) {
        if (domain == null) {
            return domainContextMapping.get(WILDCARD);
        }
        String lowerDomain = domain.toLowerCase();
        SslContext context = domainContextMapping.get(lowerDomain);
        if (context != null) {
            return context;
        }
        for (Map.Entry<String, SslContext> entry : domainContextMapping.entrySet()) {
            String domainPattern = entry.getKey();
            if (isWildcardMatch(domainPattern, lowerDomain)) {
                return entry.getValue();
            }
        }
        return domainContextMapping.get(WILDCARD);
    }
}
