package cloud.apposs.gateway.dashboard.api.model;

import cloud.apposs.bootor.resolver.parameter.ModelParametric;
import cloud.apposs.rest.validator.checker.NotBlank;
import cloud.apposs.rest.validator.checker.NotEmpty;
import cloud.apposs.util.Param;
import cloud.apposs.util.Table;

public class ProviderModel {
    public static class Add extends ModelParametric {
        @NotBlank
        private String name;

        @NotBlank
        private String type;

        @NotBlank
        private String url;

        @NotEmpty(require = false)
        private Table<String> models = Table.builder();

        @NotEmpty(require = false)
        private Table<Param> keys = Table.builder();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Table<String> getModels() {
            return models;
        }

        public void setModels(Table<String> models) {
            this.models = models;
        }

        public Table<Param> getKeys() {
            return keys;
        }

        public void setKeys(Table<Param> keys) {
            this.keys = keys;
        }
    }

    public static class Update extends ModelParametric {
        @NotBlank
        private String id;

        @NotBlank
        private String name;

        @NotBlank
        private String type;

        @NotBlank
        private String url;

        @NotEmpty(require = false)
        private Table<String> models = Table.builder();

        @NotEmpty(require = false)
        private Table<Param> keys = Table.builder();

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

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Table<String> getModels() {
            return models;
        }

        public void setModels(Table<String> models) {
            this.models = models;
        }

        public Table<Param> getKeys() {
            return keys;
        }

        public void setKeys(Table<Param> keys) {
            this.keys = keys;
        }
    }
}
