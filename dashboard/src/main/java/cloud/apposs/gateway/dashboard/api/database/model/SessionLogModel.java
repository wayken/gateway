package cloud.apposs.gateway.dashboard.api.database.model;

import cloud.apposs.bootor.resolver.parameter.ModelParametric;
import cloud.apposs.rest.validator.checker.Digits;

public class SessionLogModel {
    public static class List extends ModelParametric {
        @Digits
        private int start;

        @Digits
        private int limit;

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }
    }
}
