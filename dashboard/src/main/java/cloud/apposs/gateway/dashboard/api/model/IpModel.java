package cloud.apposs.gateway.dashboard.api.model;

import cloud.apposs.bootor.resolver.parameter.ModelParametric;
import cloud.apposs.rest.validator.checker.NotBlank;
import cloud.apposs.rest.validator.checker.NotEmpty;

import java.util.List;

public class IpModel {
    public static class Add extends ModelParametric {
        @NotBlank
        private String name;

        @NotEmpty
        private List<String> cidrs;

        @NotBlank(require = false)
        private String remark;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getCidrs() {
            return cidrs;
        }

        public void setCidrs(List<String> cidrs) {
            this.cidrs = cidrs;
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
        private String name;

        @NotEmpty
        private List<String> cidrs;

        @NotBlank(require = false)
        private String remark;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getCidrs() {
            return cidrs;
        }

        public void setCidrs(List<String> cidrs) {
            this.cidrs = cidrs;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}
