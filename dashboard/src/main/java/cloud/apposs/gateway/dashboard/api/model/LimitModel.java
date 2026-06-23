package cloud.apposs.gateway.dashboard.api.model;

import cloud.apposs.bootor.resolver.parameter.ModelParametric;
import cloud.apposs.rest.validator.checker.Digits;
import cloud.apposs.rest.validator.checker.NotBlank;
import cloud.apposs.rest.validator.checker.NotEmpty;
import cloud.apposs.rest.validator.checker.NotNull;
import cloud.apposs.rest.validator.checker.Number;
import cloud.apposs.util.Pair;
import cloud.apposs.util.Param;

import java.util.List;

public class LimitModel {
    public static class Add extends ModelParametric {
        @NotBlank
        private String name;

        @Digits
        private int status = 0;

        @NotNull
        private List<String> routes;

        @Digits
        private int priority = 0;

        @NotBlank(require = false)
        private String rule;

        @NotNull
        private List<Param> resources;

        @Number(min = 1)
        private int burst;

        @NotBlank
        private String burstUnit;

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

        public String getRule() {
            return rule;
        }

        public void setRule(String rule) {
            this.rule = rule;
        }

        public List<Param> getResources() {
            return resources;
        }

        public void setResources(List<Param> resources) {
            this.resources = resources;
        }

        public int getBurst() {
            return burst;
        }

        public void setBurst(int burst) {
            this.burst = burst;
        }

        public String getBurstUnit() {
            return burstUnit;
        }

        public void setBurstUnit(String burstUnit) {
            this.burstUnit = burstUnit;
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

        @NotNull
        private List<String> routes;

        @Digits
        private int priority = 0;

        @NotBlank(require = false)
        private String rule;

        @NotNull
        private List<Param> resources;

        @Number(min = 1)
        private int burst;

        @NotBlank
        private String burstUnit;

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

        public String getRule() {
            return rule;
        }

        public void setRule(String rule) {
            this.rule = rule;
        }

        public List<Param> getResources() {
            return resources;
        }

        public void setResources(List<Param> resources) {
            this.resources = resources;
        }

        public Param getAction() {
            return action;
        }

        public void setAction(Param action) {
            this.action = action;
        }

        public int getBurst() {
            return burst;
        }

        public void setBurst(int burst) {
            this.burst = burst;
        }

        public String getBurstUnit() {
            return burstUnit;
        }

        public void setBurstUnit(String burstUnit) {
            this.burstUnit = burstUnit;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}
