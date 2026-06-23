package cloud.apposs.gateway.dashboard.api.database.model;

import cloud.apposs.bootor.resolver.parameter.ModelParametric;
import cloud.apposs.rest.validator.checker.Bool;
import cloud.apposs.rest.validator.checker.Id;
import cloud.apposs.rest.validator.checker.NotBlank;
import cloud.apposs.util.Table;

public class RoleModel {
    public static class Add extends ModelParametric {
        @NotBlank
        private String name;

        @Bool
        private boolean admin;

        @NotBlank(require = false)
        private String remark;

        private Table<String> permissions;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isAdmin() {
            return admin;
        }

        public void setAdmin(boolean admin) {
            this.admin = admin;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public Table<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(Table<String> permissions) {
            this.permissions = permissions;
        }
    }

    public static class Update extends ModelParametric {
        @Id
        private Long id;

        @NotBlank
        private String name;

        @Bool
        private boolean admin;

        @NotBlank(require = false)
        private String remark;

        private Table<String> permissions;

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

        public boolean isAdmin() {
            return admin;
        }

        public void setAdmin(boolean admin) {
            this.admin = admin;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public Table<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(Table<String> permissions) {
            this.permissions = permissions;
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
