package cloud.apposs.gateway.dashboard.api.model;

import cloud.apposs.bootor.resolver.parameter.ModelParametric;
import cloud.apposs.rest.validator.checker.NotBlank;
import cloud.apposs.rest.validator.checker.NotNull;
import cloud.apposs.util.Param;

public class UpstreamModel {
    public static class Add extends ModelParametric {
        @NotBlank
        private String type;

        @NotBlank
        private String name;

        @NotBlank
        private String algorithm;

        @NotNull
        private Param parameters;

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

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public Param getParameters() {
            return parameters;
        }

        public void setParameters(Param parameters) {
            this.parameters = parameters;
        }
    }

    public static class Update extends ModelParametric {
        @NotBlank
        private String id;

        @NotBlank
        private String type;

        @NotBlank
        private String name;

        @NotBlank
        private String algorithm;

        @NotBlank(require = false)
        private String remark;

        @NotNull
        private Param parameters;

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

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public Param getParameters() {
            return parameters;
        }

        public void setParameters(Param parameters) {
            this.parameters = parameters;
        }
    }
}
