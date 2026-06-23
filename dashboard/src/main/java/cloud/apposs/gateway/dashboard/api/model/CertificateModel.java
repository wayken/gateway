package cloud.apposs.gateway.dashboard.api.model;

import cloud.apposs.bootor.resolver.parameter.ModelParametric;
import cloud.apposs.rest.validator.checker.NotBlank;

public class CertificateModel {
    public static class Add extends ModelParametric {
        @NotBlank
        private String domain;

        @NotBlank
        private String keyData;

        @NotBlank
        private String certData;

        @NotBlank(require = false)
        private String remark;

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getKeyData() {
            return keyData;
        }

        public void setKeyData(String keyData) {
            this.keyData = keyData;
        }

        public String getCertData() {
            return certData;
        }

        public void setCertData(String certData) {
            this.certData = certData;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

    public static class Update extends ModelParametric {
        @NotBlank
        private String id;

        @NotBlank
        private String domain;

        @NotBlank(require = false)
        private String keyData;

        @NotBlank(require = false)
        private String certData;

        @NotBlank(require = false)
        private String remark;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getKeyData() {
            return keyData;
        }

        public void setKeyData(String keyData) {
            this.keyData = keyData;
        }

        public String getCertData() {
            return certData;
        }

        public void setCertData(String certData) {
            this.certData = certData;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}
