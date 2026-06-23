package cloud.apposs.gateway.dashboard.api.database.app;

import cloud.apposs.protobuf.ProtoSchema;

import java.util.Calendar;

public final class RoleDef {
    public static final class Table {
        public static final String TABLE_ROLE = "role";
    }

    public static final class Info {
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String IS_ADMIN = "is_admin";
        public static final String STATUS = "status";
        public static final String PERMISSIONS = "permissions";
        public static final String REMARK = "remark";
        public static final String CREATE_TIME = "create_time";
        public static final String UPDATE_TIME = "update_time";
    }

    public static final class Protocol {
        // 表元信息
        private static final ProtoSchema INFO_ROLE_SCHEMA = ProtoSchema.mapSchema();
        static {
            INFO_ROLE_SCHEMA.addKey(Info.ID, Long.class);
            INFO_ROLE_SCHEMA.addKey(Info.NAME, String.class);
            INFO_ROLE_SCHEMA.addKey(Info.IS_ADMIN, Integer.class);
            INFO_ROLE_SCHEMA.addKey(Info.STATUS, Integer.class);
            INFO_ROLE_SCHEMA.addKey(Info.PERMISSIONS, String.class);
            INFO_ROLE_SCHEMA.addKey(Info.REMARK, String.class);
            INFO_ROLE_SCHEMA.addKey(Info.CREATE_TIME, Calendar.class);
            INFO_ROLE_SCHEMA.addKey(Info.UPDATE_TIME, Calendar.class);
        }

        public static ProtoSchema getInfoRoleSchema() {
            return INFO_ROLE_SCHEMA;
        }
    }
}
