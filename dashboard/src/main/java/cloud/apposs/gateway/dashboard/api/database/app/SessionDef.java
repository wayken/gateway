package cloud.apposs.gateway.dashboard.api.database.app;

import cloud.apposs.protobuf.ProtoBuf;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Encoder;
import cloud.apposs.util.Errno;
import cloud.apposs.util.SysUtil;

public final class SessionDef {
    // 用户会话登录Key，数据结构为：Session-{UserId} =》 {SessionInfo}，主要用于会话登录校验
    public static final String SESSION_CACHE_KEY = "Session";
    public static final String SESSION_ATTRIBUTE_KEY_USER_ID = "Session-Attribute-UID";

    public static final class Info {
        public static final String UID = "uid";
        public static final String TOKEN = "token";
        public static final String IS_ADMIN = "admin";
        public static final String PERMISSIONS = "permissions";
    }

    public static final class DTO {
        public static final String TWOFA = "twofa";
        public static final String TWOFA_SCHEME = "scheme";
        public static final String TWOFA_QRCODE = "qrcode";
    }

    public static final class Protocol {
        private static final ProtoSchema INFO_SESSION_SCHEMA = ProtoSchema.mapSchema();
        static {
            INFO_SESSION_SCHEMA.addKey(Info.UID, Long.class);
            INFO_SESSION_SCHEMA.addKey(Info.TOKEN, String.class);
        }

        private static final ProtoSchema INFO_PERMISSION_SCHEMA = ProtoSchema.mapSchema();
        static {
            INFO_PERMISSION_SCHEMA.addKey(Info.IS_ADMIN, Boolean.class);
            INFO_PERMISSION_SCHEMA.addKey(Info.PERMISSIONS, cloud.apposs.util.Table.class, ProtoSchema.listSchema(String.class));
        }

        public static ProtoSchema getInfoSessionSchema() {
            return INFO_SESSION_SCHEMA;
        }

        public static ProtoSchema getInfoPermissionSchema() {
            return INFO_PERMISSION_SCHEMA;
        }
    }

    /**
     * 生成SessionID
     */
    public static String genSessionId(long id) {
        int time = (int) (System.currentTimeMillis() / 1000);
        int random = SysUtil.random();
        ProtoBuf idBuf = ProtoBuf.allocate(64);
        idBuf.putLong(0, id);
        idBuf.putInt(2, time);
        idBuf.putInt(3, random);
        idBuf.buffer().rewind();
        return Encoder.encodeBase64Url(idBuf.array());
    }

    /**
     * 从sessionId中反解aid和mid
     */
    public static long parseSessionId(String sessionId) {
        try {
            byte[] idBuf = Encoder.decodeBase64Url(sessionId);
            if (idBuf == null) {
                return -1;
            }
            ProtoBuf buffer = ProtoBuf.wrap(idBuf);
            return buffer.getLong(0);
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getSessionCacheKey(long id) {
        return SESSION_CACHE_KEY + "-" + id;
    }

    public static final class SessionErrno extends Errno {
        public SessionErrno(int value, String description) {
            super(value, description);
        }

        /** 会话失效或者过期 */
        public static final Errno SESSION_EXPIRED = new SessionErrno(1004001, "Session expired");

        /** 用户名或者密码错误 */
        public static final Errno SESSION_USER_INVALID = new SessionErrno(1004002, "Session User invalid");

        /** MFA验证码错误 */
        public static final Errno SESSION_MFA_INVALID = new SessionErrno(1004003, "Session MFA invalid");
    }
}
