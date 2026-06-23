package cloud.apposs.gateway.dashboard.api.model;

import cloud.apposs.bootor.resolver.parameter.ModelParametric;
import cloud.apposs.rest.validator.checker.Digits;
import cloud.apposs.rest.validator.checker.NotBlank;
import cloud.apposs.rest.validator.checker.NotEmpty;

import java.util.List;

public class ZoneModel {
    public static class Add extends ModelParametric {
        @NotBlank
        private String name;

        @NotEmpty
        private List<String> match;

        @Digits(require = false)
        private int status = 0;

        @NotBlank(require = false)
        private String remark;

        public String getName() {
            return name;
        }

        public List<String> getMatch() {
            return match;
        }

        public void setMatch(List<String> match) {
            this.match = match;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
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

        @NotBlank(require = false)
        private String name;

        @NotEmpty(require = false)
        private List<String> match;

        @Digits(require = false)
        private int status = 0;

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

        public List<String> getMatch() {
            return match;
        }

        public void setMatch(List<String> match) {
            this.match = match;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}
