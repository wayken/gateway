package cloud.apposs.gateway.dashboard.api.database.model;

import cloud.apposs.bootor.resolver.parameter.ModelParametric;
import cloud.apposs.rest.validator.checker.Bool;
import cloud.apposs.rest.validator.checker.Id;
import cloud.apposs.rest.validator.checker.NotBlank;
import cloud.apposs.rest.validator.checker.NotEmpty;
import cloud.apposs.util.Table;

public class UserModel {
    public static class Add extends ModelParametric {
        @NotBlank
        private String name;

        @NotBlank
        private String acct;

        @NotBlank
        private String pwd;

        @NotEmpty
        private Table<String> roles;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

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

        public Table<String> getRoles() {
            return roles;
        }

        public void setRoles(Table<String> roles) {
            this.roles = roles;
        }
    }

    public static class Update extends ModelParametric {
        @Id
        private Long id;

        @NotBlank
        private String name;

        @NotBlank
        private String acct;

        @NotBlank(require = false)
        private String pwd;

        @Bool(require = false)
        private boolean twofa = false;

        @NotEmpty
        private Table<String> roles;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

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

        public boolean isTwofa() {
            return twofa;
        }

        public void setTwofa(boolean twofa) {
            this.twofa = twofa;
        }

        public Table<String> getRoles() {
            return roles;
        }

        public void setRoles(Table<String> roles) {
            this.roles = roles;
        }
    }

    public static class Delete extends ModelParametric {
        @Id
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}
