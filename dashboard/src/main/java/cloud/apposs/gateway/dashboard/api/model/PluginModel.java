package cloud.apposs.gateway.dashboard.api.model;

import cloud.apposs.bootor.resolver.parameter.ModelParametric;
import cloud.apposs.rest.validator.checker.NotBlank;
import cloud.apposs.rest.validator.checker.NotEmpty;
import cloud.apposs.util.Param;

import java.util.List;

public class PluginModel {
    public static class Update extends ModelParametric {
        @NotBlank
        private String zone;

        @NotEmpty
        private List<Param> plugins;

        public String getZone() {
            return zone;
        }

        public void setZone(String zone) {
            this.zone = zone;
        }

        public List<Param> getPlugins() {
            return plugins;
        }

        public void setPlugins(List<Param> plugins) {
            this.plugins = plugins;
        }
    }
}
