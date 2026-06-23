package cloud.apposs.gateway.dashboard.api.model;

import cloud.apposs.bootor.resolver.parameter.ModelParametric;
import cloud.apposs.rest.validator.checker.NotBlank;
import cloud.apposs.rest.validator.checker.NotNull;
import cloud.apposs.util.Param;

public class AuthModel {
    public static class Add extends ModelParametric {
        @NotBlank
        private String type;

        @NotBlank
        private String name;

        @NotNull
        private Param parameters;

        @NotBlank(require = false)
        private String remark;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Param getParameters() {
            return parameters;
        }

        public void setParameters(Param parameters) {
            this.parameters = parameters;
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
        private String type;

        @NotBlank
        private String name;

        @NotNull
        private Param parameters;

        @NotBlank(require = false)
        private String remark;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Param getParameters() {
            return parameters;
        }

        public void setParameters(Param parameters) {
            this.parameters = parameters;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}
