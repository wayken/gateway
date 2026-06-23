package cloud.apposs.gateway.dashboard.api.database.app;

import cloud.apposs.protobuf.ProtoSchema;

import java.util.Calendar;

public final class OperationLogDef {
    public static final class Table {
        public static final String TABLE_OPERATION_LOG = "user_operation_log";
    }

    public static final class Info {
        public static final String ID = "id";
        public static final String USER_ID = "user_id";
        public static final String IP = "ip";
        public static final String LOCATION = "location";
        public static final String USER_AGENT = "user_agent";
        public static final String MODULE = "module";
        public static final String MODULE_ID = "module_id";
        public static final String URI = "uri";
        public static final String OPERATION_TYPE = "operation_type";
        public static final String CONTENT = "content";
        public static final String OPERATION_TIME = "operation_time";
    }

    public static final class Protocol {
        // 表元信息
        private static final ProtoSchema INFO_OPERATION_LOG_SCHEMA = ProtoSchema.mapSchema();
        static {
            INFO_OPERATION_LOG_SCHEMA.addKey(Info.ID, Long.class);
            INFO_OPERATION_LOG_SCHEMA.addKey(Info.USER_ID, Long.class);
            INFO_OPERATION_LOG_SCHEMA.addKey(Info.IP, String.class);
            INFO_OPERATION_LOG_SCHEMA.addKey(Info.LOCATION, String.class);
            INFO_OPERATION_LOG_SCHEMA.addKey(Info.USER_AGENT, String.class);
            INFO_OPERATION_LOG_SCHEMA.addKey(Info.MODULE, String.class);
            INFO_OPERATION_LOG_SCHEMA.addKey(Info.MODULE_ID, String.class);
            INFO_OPERATION_LOG_SCHEMA.addKey(Info.URI, String.class);
            INFO_OPERATION_LOG_SCHEMA.addKey(Info.OPERATION_TYPE, Integer.class);
            INFO_OPERATION_LOG_SCHEMA.addKey(Info.CONTENT, String.class);
            INFO_OPERATION_LOG_SCHEMA.addKey(Info.OPERATION_TIME, Calendar.class);
        }

        public static ProtoSchema getInfoOperationLogSchema() {
            return INFO_OPERATION_LOG_SCHEMA;
        }
    }
}
