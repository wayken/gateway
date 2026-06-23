package cloud.apposs.gateway.dashboard.api.model;

import cloud.apposs.bootor.resolver.parameter.ModelParametric;
import cloud.apposs.rest.validator.checker.Digits;
import cloud.apposs.rest.validator.checker.NotBlank;
import cloud.apposs.rest.validator.checker.NotEmpty;
import cloud.apposs.util.Param;

import java.util.List;

public class RouteModel {
    public static class Add extends ModelParametric {
        @NotBlank(require = false)
        private String id;

        @NotBlank
        private String type;

        @NotBlank
        private String name;

        @NotBlank
        private String path;

        @NotBlank
        private String upstreamId;

        @NotBlank(require = false)
        private Param upstream;

        @NotBlank(require = false)
        private String authId;

        @Digits
        private int priority = 0;

        @Digits
        private int status = 0;

        @NotEmpty
        private List<String> methods;

        @NotBlank(require = false)
        private String rule;

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

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getUpstreamId() {
            return upstreamId;
        }

        public void setUpstreamId(String upstreamId) {
            this.upstreamId = upstreamId;
        }

        public Param getUpstream() {
            return upstream;
        }

        public void setUpstream(Param upstream) {
            this.upstream = upstream;
        }

        public String getAuthId() {
            return authId;
        }

        public void setAuthId(String authId) {
            this.authId = authId;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public List<String> getMethods() {
            return methods;
        }

        public void setMethods(List<String> methods) {
            this.methods = methods;
        }

        public String getRule() {
            return rule;
        }

        public void setRule(String rule) {
            this.rule = rule;
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

        @NotBlank(require = false)
        private String path;

        @NotBlank
        private String upstreamId;

        @NotBlank(require = false)
        private Param upstream;

        @NotBlank(require = false)
        private String authId;

        @Digits
        private int priority = 0;

        @Digits
        private int status = 0;

        @NotEmpty
        private List<String> methods;

        @NotBlank(require = false)
        private String rule;

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

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getUpstreamId() {
            return upstreamId;
        }

        public void setUpstreamId(String upstreamId) {
            this.upstreamId = upstreamId;
        }

        public Param getUpstream() {
            return upstream;
        }

        public void setUpstream(Param upstream) {
            this.upstream = upstream;
        }

        public String getAuthId() {
            return authId;
        }

        public void setAuthId(String authId) {
            this.authId = authId;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public List<String> getMethods() {
            return methods;
        }

        public void setMethods(List<String> methods) {
            this.methods = methods;
        }

        public String getRule() {
            return rule;
        }

        public void setRule(String rule) {
            this.rule = rule;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}
