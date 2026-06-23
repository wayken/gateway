package cloud.apposs.gateway.global.certificate;

/**
 * HTTPS 证书封装
 */
public class Certificate {
    private final String id;

    // 匹配域名
    private final String domain;

    // 私钥
    private final String keyData;

    // 证书
    private final String certData;

    public Certificate(String id, String domain, String keyData, String certData) {
        this.id = id;
        this.domain = domain;
        this.keyData = keyData;
        this.certData = certData;
    }

    public String getId() {
        return id;
    }

    public String getDomain() {
        return domain;
    }

    public String getKeyData() {
        return keyData;
    }

    public String getCertData() {
        return certData;
    }

    public String toString() {
        return "Certificate [id=" + id + ", domain=" + domain + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Certificate other = (Certificate) obj;
        return id.equals(other.id) && domain.equals(other.domain);
    }
}
