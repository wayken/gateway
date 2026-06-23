package cloud.apposs.gateway;

import cloud.apposs.gateway.util.CertificateUtil;
import org.junit.Test;

import java.security.cert.X509Certificate;
import java.util.List;

public class TestCertificate {
    @Test
    public void testCertificateInfo() throws Exception {
        String content = "-----BEGIN CERTIFICATE-----\n" +
                "MIIE/TCCA+WgAwIBAgISBrwLvG5hFMZuMQCgC8JnkPciMA0GCSqGSIb3DQEBCwUA\n" +
                "MDMxCzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MQwwCgYDVQQD\n" +
                "EwNSMTAwHhcNMjUwNTI4MDA0MDQ5WhcNMjUwODI2MDA0MDQ4WjAcMRowGAYDVQQD\n" +
                "ExF3b3JrLnRlYW1iZWl0LmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoC\n" +
                "ggEBAO3s1AoVAasrGz7fWXhlYrZCgaa2zSr1q6IFdTsGn/sRAldGvtbaD9F0Dl6y\n" +
                "0Y48XObwOVtHKHBvB9KZPyjnV0i+LAH6MkJMZ3+ETgciVdQzc+ytZ8JFaEQw6M1I\n" +
                "wxUjG8pW3iuC7fvh/z7ljtUkNEo4DMiBzpjh5j5/ZNG18jsZiD/SwKN94QF/IZW5\n" +
                "+UfVlhIifLfxbcmJ8aAdouNt0NDyNPgXxzXVzZkT0WegMMlYh+tE4qKYn9eenmV8\n" +
                "FsFpW0wZoVW6fIRjK3txyNA8jisSzlO0HKHHzm9eazNACheXu07/fRt97ghqNCwl\n" +
                "+oeb8za/XqCGY20fA6WK/6035NMCAwEAAaOCAiAwggIcMA4GA1UdDwEB/wQEAwIF\n" +
                "oDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwDAYDVR0TAQH/BAIwADAd\n" +
                "BgNVHQ4EFgQU+OyEiB8BwZjw4d6Cij5YT0JBpI4wHwYDVR0jBBgwFoAUu7zDR6Xk\n" +
                "vKnGw6RyDBCNojXhyOgwMwYIKwYBBQUHAQEEJzAlMCMGCCsGAQUFBzAChhdodHRw\n" +
                "Oi8vcjEwLmkubGVuY3Iub3JnLzAcBgNVHREEFTATghF3b3JrLnRlYW1iZWl0LmNv\n" +
                "bTATBgNVHSAEDDAKMAgGBmeBDAECATAuBgNVHR8EJzAlMCOgIaAfhh1odHRwOi8v\n" +
                "cjEwLmMubGVuY3Iub3JnLzM1LmNybDCCAQMGCisGAQQB1nkCBAIEgfQEgfEA7wB1\n" +
                "AKRCxQZJYGFUjw/U6pz7ei0mRU2HqX8v30VZ9idPOoRUAAABlxSLP1sAAAQDAEYw\n" +
                "RAIgcgisqp88M6vuuTfeBTwj0nHQALb4zFPDm00J2/T+x+ICIGI4HcGIujDnDGuH\n" +
                "vP+UUdeferUQVJeRXLoIZBrJs7rzAHYAzPsPaoVxCWX+lZtTzumyfCLphVwNl422\n" +
                "qX5UwP5MDbAAAAGXFIs/cgAABAMARzBFAiB7F9xIOJUd4WleTRnqX6zXIP+KiF8S\n" +
                "XzAR0NcxZH9RbAIhAPGVvlWXIGdehpAoWWshn0VzGagQXR0D+X1v5LXeMbccMA0G\n" +
                "CSqGSIb3DQEBCwUAA4IBAQAot2wMCmtCMbUnkAg3kwlnMIG4uaHpWQ4Ih3OmQLyV\n" +
                "/xCGsqiyOm81KPnk2p1Tuarw9lgrlmzF7ZRG5hItjseO93e4T33bgUCXVWVKoXny\n" +
                "wv0Hd86Vn0ca9qyLZ6dN2GlmsndXNuQ+XEG1t7Uwrk8ZvBY7nR2OCDWtCnvMApnE\n" +
                "aNLn/lGaF6TJ9+adTB10606MNwj56gXIL+sMMXEajrwyO6rnfWCWBo2TZijm1nAk\n" +
                "CyraHzLZMz6+rRcw6GAnwH1YCxeMyyObDGXtTJh807b8SWj3aHr8R2FlsSXgceWp\n" +
                "9ICQh8yBVYzs2X/47xxtULVQhIdRiYxolasGMu8jqvsI\n" +
                "-----END CERTIFICATE-----\n";
        List<X509Certificate> certificates = CertificateUtil.loadCertificates(content);
        certificates.forEach(cert -> {
            System.out.println(cert.getIssuerX500Principal());
            System.out.println(cert.getSubjectX500Principal());
            System.out.println(cert.getNotBefore());
            System.out.println(cert.getNotAfter());
            System.out.println("-----------------------------");
        });
    }
}
