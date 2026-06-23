package cloud.apposs.gateway.util;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 证书工具类
 */
public final class CertificateUtil {
    /**
     * 从 PEM 字符串中加载所有证书
     */
    public static List<X509Certificate> loadCertificates(String content) throws Exception {
        List<X509Certificate> certificates = new ArrayList<>();
        byte[] pemBytes = content.getBytes();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(pemBytes)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            while (bais.available() > 0) {
                X509Certificate cert = (X509Certificate) cf.generateCertificate(bais);
                certificates.add(cert);
            }
        }
        return certificates;
    }

    /**
     * 将Mon Jun 30 08:30:30 CST 2025一类的证书时间字符串转换成时间戳
     */
    public static long parseCertificateTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        try {
            Date date = sdf.parse(time);
            return date.getTime();
        } catch (ParseException e) {
        }
        return -1;
    }
}
