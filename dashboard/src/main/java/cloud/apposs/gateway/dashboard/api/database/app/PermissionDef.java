package cloud.apposs.gateway.dashboard.api.database.app;

import cloud.apposs.protobuf.ProtoSchema;

public final class PermissionDef {
    public static final class Table {
        public static final String TABLE_PERMISSION = "permission";
    }

    public static final class Info {
        public static final String ID = "id";
        public static final String ROLE_ID = "role_id";
        public static final String RESOURCE = "resource";
        public static final String ACTION = "action";
        public static final String CREATE_TIME = "create_time";
    }

    public static final class Protocol {
        // 表元信息
        private static final ProtoSchema INFO_PERMISSION_SCHEMA = ProtoSchema.mapSchema();
        static {
            INFO_PERMISSION_SCHEMA.addKey(Info.ID, Long.class);
            INFO_PERMISSION_SCHEMA.addKey(Info.ROLE_ID, Long.class);
            INFO_PERMISSION_SCHEMA.addKey(Info.RESOURCE, String.class);
            INFO_PERMISSION_SCHEMA.addKey(Info.ACTION, String.class);
            INFO_PERMISSION_SCHEMA.addKey(Info.CREATE_TIME, Long.class);
        }

        public static ProtoSchema getInfoPermissionSchema() {
            return INFO_PERMISSION_SCHEMA;
        }
    }
}
