package cloud.apposs.gateway.dashboard.api.model;

import cloud.apposs.bootor.resolver.parameter.ModelParametric;
import cloud.apposs.rest.validator.checker.Digits;
import cloud.apposs.rest.validator.checker.NotBlank;
import cloud.apposs.rest.validator.checker.NotNull;
import cloud.apposs.util.Param;

import java.util.List;

public class RewriteModel {
    public static class Request {
        public static class Add extends ModelParametric {
            @NotBlank
            private String name;

            @Digits
            private int status = 0;

            @NotNull
            private List<String> routes;

            @NotBlank(require = false)
            private String rule;

            @NotNull
            private List<Param> action;

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

            public String getRule() {
                return rule;
            }

            public void setRule(String rule) {
                this.rule = rule;
            }

            public List<Param> getAction() {
                return action;
            }

            public void setAction(List<Param> action) {
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

            @NotBlank(require = false)
            private String rule;

            @NotNull
            private List<Param> action;

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

            public String getRule() {
                return rule;
            }

            public void setRule(String rule) {
                this.rule = rule;
            }

            public List<Param> getAction() {
                return action;
            }

            public void setAction(List<Param> action) {
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

    public static class Response {
        public static class Add extends ModelParametric {
            @NotBlank
            private String name;

            @Digits
            private int status = 0;

            @NotNull
            private List<String> routes;

            @NotBlank(require = false)
            private String rule;

            @NotNull
            private List<Param> action;

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

            public String getRule() {
                return rule;
            }

            public void setRule(String rule) {
                this.rule = rule;
            }

            public List<Param> getAction() {
                return action;
            }

            public void setAction(List<Param> action) {
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

            @NotBlank(require = false)
            private String rule;

            @NotNull
            private List<Param> action;

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

            public String getRule() {
                return rule;
            }

            public void setRule(String rule) {
                this.rule = rule;
            }

            public List<Param> getAction() {
                return action;
            }

            public void setAction(List<Param> action) {
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
}
