package cloud.apposs.gateway.dashboard.api.database.app;

import cloud.apposs.protobuf.ProtoSchema;

import java.util.Calendar;

public final class UserDef {
    public static final class Table {
        public static final String TABLE_USER = "user";
    }

    public static final class Info {
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String ACCT = "acct";
        public static final String PWD = "pwd";
        public static final String FLAG = "flag";
        public static final String ROLE_IDS = "role_ids";
        public static final String TWOFA_SCHEME = "twofa_scheme";
        public static final String TWOFA_SECRET = "twofa_secret";
        public static final String CREATE_TIME = "create_time";
    }

    public static final class Protocol {
        // 表元信息
        private static final ProtoSchema INFO_USER_SCHEMA = ProtoSchema.mapSchema();
        static {
            INFO_USER_SCHEMA.addKey(Info.ID, Long.class);
            INFO_USER_SCHEMA.addKey(Info.NAME, String.class);
            INFO_USER_SCHEMA.addKey(Info.ACCT, String.class);
            INFO_USER_SCHEMA.addKey(Info.PWD, Byte[].class);
            INFO_USER_SCHEMA.addKey(Info.FLAG, Integer.class);
            INFO_USER_SCHEMA.addKey(Info.ROLE_IDS, String.class);
            INFO_USER_SCHEMA.addKey(Info.TWOFA_SCHEME, String.class);
            INFO_USER_SCHEMA.addKey(Info.TWOFA_SECRET, String.class);
            INFO_USER_SCHEMA.addKey(Info.CREATE_TIME, Calendar.class);
        }

        public static ProtoSchema getInfoUserSchema() {
            return INFO_USER_SCHEMA;
        }
    }

    public static final class Flag {
        // 用户是否启用双因子认证
        public static final int FLAG_ENABLE_TWOFA = 0x01;
    }

    public static final class TwofaScheme {
        public static final String TOTP = "totp";
    }
}
