package cloud.apposs.gateway.global.certificate;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.GatewayException;
import cloud.apposs.util.HttpStatus;
import cloud.apposs.util.JsonUtil;
import cloud.apposs.util.Param;

public final class CertificateParser {
    public static Certificate parse(String id, String content, GatewayContext context) throws Exception {
        Param infomation = JsonUtil.parseJsonParam(content);
        if (infomation == null || infomation.isEmpty()) {
            throw new GatewayException(HttpStatus.HTTP_STATUS_500, "Certificate: " + id + " config is invalid");
        }
        String domain = infomation.getString(CertificateConstant.KEY_DOMAIN);
        String keyData = infomation.getString(CertificateConstant.KEY_KEY_DATA);
        String certData = infomation.getString(CertificateConstant.KEY_CERT_DATA);
        if (domain == null || keyData == null || certData == null) {
            throw new GatewayException(HttpStatus.HTTP_STATUS_500, "Certificate: " + id + " config is invalid");
        }
        return new Certificate(id, domain, keyData, certData);
    }
}
