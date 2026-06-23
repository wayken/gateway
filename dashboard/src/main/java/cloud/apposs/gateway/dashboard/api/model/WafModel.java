package cloud.apposs.gateway.dashboard.api.model;

import cloud.apposs.bootor.resolver.parameter.ModelParametric;
import cloud.apposs.rest.validator.checker.Digits;
import cloud.apposs.rest.validator.checker.NotBlank;
import cloud.apposs.rest.validator.checker.NotEmpty;
import cloud.apposs.rest.validator.checker.NotNull;
import cloud.apposs.util.Param;

import java.util.List;

public class WafModel {
    public static class Add extends ModelParametric {
        @NotBlank
        private String name;

        @Digits
        private int status = 0;

        @NotBlank
        private String rule;

        @NotNull
        private List<String> routes;

        @Digits
        private int priority = 0;

        @NotEmpty
        private Param action;

        @NotBlank(require = false)
        private String remark;

        public String getName() {
            return name;
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

        public String getRule() {
            return rule;
        }

        public void setRule(String rule) {
            this.rule = rule;
        }

        public List<String> getRoutes() {
            return routes;
        }

        public void setRoutes(List<String> routes) {
            this.routes = routes;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public Param getAction() {
            return action;
        }

        public void setAction(Param action) {
            this.action = action;
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

        @Digits
        private int status = 0;

        @NotBlank
        private String rule;

        @NotNull
        private List<String> routes;

        @Digits
        private int priority = 0;

        @NotEmpty
        private Param action;

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

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getRule() {
            return rule;
        }

        public void setRule(String rule) {
            this.rule = rule;
        }

        public List<String> getRoutes() {
            return routes;
        }

        public void setRoutes(List<String> routes) {
            this.routes = routes;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public Param getAction() {
            return action;
        }

        public void setAction(Param action) {
            this.action = action;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}
