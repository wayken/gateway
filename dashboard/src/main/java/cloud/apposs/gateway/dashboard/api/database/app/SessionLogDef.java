package cloud.apposs.gateway.dashboard.api.database.app;

import cloud.apposs.protobuf.ProtoSchema;

import java.util.Calendar;

public final class SessionLogDef {
    public static final class Table {
        public static final String TABLE_SESSION_LOG = "user_session_log";
    }

    public static final class Info {
        public static final String ID = "id";
        public static final String USER_ID = "user_id";
        public static final String IP = "ip";
        public static final String LOCATION = "location";
        public static final String USER_AGENT = "user_agent";
        public static final String LOGIN_TIME = "login_time";
    }

    public static final class Protocol {
        // 表元信息
        private static final ProtoSchema INFO_SESSION_LOG_SCHEMA = ProtoSchema.mapSchema();
        static {
            INFO_SESSION_LOG_SCHEMA.addKey(Info.ID, Long.class);
            INFO_SESSION_LOG_SCHEMA.addKey(Info.USER_ID, Long.class);
            INFO_SESSION_LOG_SCHEMA.addKey(Info.IP, String.class);
            INFO_SESSION_LOG_SCHEMA.addKey(Info.LOCATION, String.class);
            INFO_SESSION_LOG_SCHEMA.addKey(Info.USER_AGENT, String.class);
            INFO_SESSION_LOG_SCHEMA.addKey(Info.LOGIN_TIME, Calendar.class);
        }

        public static ProtoSchema getInfoSessionLogSchema() {
            return INFO_SESSION_LOG_SCHEMA;
        }
    }
}
