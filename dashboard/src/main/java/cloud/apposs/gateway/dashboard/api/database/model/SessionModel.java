package cloud.apposs.gateway.dashboard.api.database.model;

import cloud.apposs.bootor.resolver.parameter.ModelParametric;
import cloud.apposs.rest.validator.checker.NotBlank;

public class SessionModel {
    public static class Get extends ModelParametric {
        @NotBlank
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    public static class Login extends ModelParametric {
        @NotBlank
        private String acct;

        @NotBlank
        private String pwd;

        public String getAcct() {
            return acct;
        }

        public void setAcct(String acct) {
            this.acct = acct;
        }

        public String getPwd() {
            return pwd;
        }

        public void setPwd(String pwd) {
            this.pwd = pwd;
        }
    }

    public static class Logout extends ModelParametric {
        @NotBlank
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    public static class Mfa extends ModelParametric {
        @NotBlank
        private String acct;

        @NotBlank
        private String pwd;

        @NotBlank
        private String code;

        public String getAcct() {
            return acct;
        }

        public void setAcct(String acct) {
            this.acct = acct;
        }

        public String getPwd() {
            return pwd;
        }

        public void setPwd(String pwd) {
            this.pwd = pwd;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
