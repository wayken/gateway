package cloud.apposs.gateway.dashboard.api.database;

import cloud.apposs.bootor.BootorHttpRequest;
import cloud.apposs.cache.CacheConfig;
import cloud.apposs.cache.CacheManager;
import cloud.apposs.cachex.database.Entity;
import cloud.apposs.cachex.database.Updater;
import cloud.apposs.cachex.database.Where;
import cloud.apposs.gateway.dashboard.GatewayDashboardConfig;
import cloud.apposs.gateway.dashboard.api.database.app.RoleDef;
import cloud.apposs.gateway.dashboard.api.database.app.SessionDef;
import cloud.apposs.gateway.dashboard.api.database.app.SessionLogDef;
import cloud.apposs.gateway.dashboard.api.database.app.UserDef;
import cloud.apposs.gateway.dashboard.api.database.model.SessionModel;
import cloud.apposs.gateway.dashboard.util.NetUtil;
import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.logger.Logger;
import cloud.apposs.react.React;
import cloud.apposs.rest.annotation.*;
import cloud.apposs.util.*;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import dev.samstevens.totp.util.Utils;

@Executor
@RestAction
public class SessionApi implements AutoCloseable {
    @Autowired
    private UserApi userApi;

    @Autowired
    private RoleApi roleApi;

    @Autowired
    private SessionLogApi sessionLogApi;

    private final CacheManager cache;

    @Autowired
    public SessionApi(GatewayDashboardConfig config) throws Exception {
        CacheConfig cacheConfig = config.getCacheXConfig().getCacheConfig();
        this.cache = new CacheManager(cacheConfig);
    }

    @Request.Get("/api/v1/session")
    public React<StandardResult> getSessionInfo(@Model SessionModel.Get request) {
        return React.emitter(() -> {
            // 判断用户会话是否登录
            String token = request.getToken();
            long sessionId = SessionDef.parseSessionId(token);
            if (sessionId <= 0) {
                return StandardResult.error(SessionDef.SessionErrno.SESSION_EXPIRED);
            }
            String sessionKey = SessionDef.getSessionCacheKey(sessionId);
            Param response = cache.hgetParam(sessionKey, token, SessionDef.Protocol.getInfoSessionSchema());
            if (response == null) {
                return StandardResult.error(SessionDef.SessionErrno.SESSION_EXPIRED);
            }
            // 获取用户信息
            long uid = response.getLong(SessionDef.Info.UID);
            Entity user = userApi.handleDataLoadById(uid);
            if (user == null) {
                return StandardResult.error(Errno.ENOT_FOUND);
            }
            response.setString(UserDef.Info.NAME, user.getString(UserDef.Info.NAME))
                    .setString(UserDef.Info.ACCT, user.getString(UserDef.Info.ACCT));
            // 获取用户对应的角色信息
            Param permissionInfo = handleUserPermissionInfoLoad(user);
            response.assign(permissionInfo, SessionDef.Info.IS_ADMIN);
            response.assign(permissionInfo, SessionDef.Info.PERMISSIONS);
            return StandardResult.success(response);
        });
    }

    @ReadCmd
    @Request.Post("/api/v1/session/login")
    public React<StandardResult> loginSession(@Model SessionModel.Login request, BootorHttpRequest bootorHttpRequest) {
        return React.emitter(() -> {
            // 获取用户信息
            Entity result = userApi.handleDataLoadByAcct(request.getAcct());
            if (ResultSets.isEmpty(result)) {
                Logger.error("user not found error;acct=%s;flow=%d;", request.getAcct(), request.getFlow());
                return StandardResult.error(Errno.ENOT_FOUND);
            }
            byte[] password = result.getBytes(UserDef.Info.PWD);
            if (!request.getPwd().equals(Convertor.bytes2hex(password))) {
                Logger.error("password not match error;acct=%s;flow=%d;", request.getAcct(), request.getFlow());
                return StandardResult.error(SessionDef.SessionErrno.SESSION_USER_INVALID);
            }
            // 判断是否需要二次验证
            String twofaSecret = result.getString(UserDef.Info.TWOFA_SECRET);
            boolean isUserEnableTwofa = !StrUtil.isEmpty(twofaSecret);
            if (isUserEnableTwofa) {
                int flag = result.getInt(UserDef.Info.FLAG);
                if (!MixBit.build(flag).matched(UserDef.Flag.FLAG_ENABLE_TWOFA)) {
                    // 如果用户开启了二次验证，但还未绑定二次验证，则返回二维码进行绑定
                    String qrCode = handleQrCodeGenerate(request.getAcct(), twofaSecret);
                    Param response = Param.builder(SessionDef.DTO.TWOFA, true)
                            .setString(SessionDef.DTO.TWOFA_SCHEME, result.getString(UserDef.Info.TWOFA_SCHEME))
                            .setString(SessionDef.DTO.TWOFA_QRCODE, qrCode);
                    return StandardResult.success(response);
                } else {
                    // 如果用户已经绑定了二次验证，则进行二次验证
                    Param response = Param.builder(SessionDef.DTO.TWOFA, true)
                            .setString(SessionDef.DTO.TWOFA_SCHEME, result.getString(UserDef.Info.TWOFA_SCHEME));
                    return StandardResult.success(response);
                }
            }
            // 添加登录会话
            long id = result.getLong(UserDef.Info.ID);
            String token = handleAddSession(id, bootorHttpRequest);
            Param response = Param.builder().setString(SessionDef.Info.TOKEN, token);
            Logger.info("add session ok;uid=%d;flow=%d;", id, request.getFlow());
            return StandardResult.success(response);
        });
    }

    @ReadCmd
    @Request.Post("/api/v1/session/mfa")
    public React<StandardResult> mfaSession(@Model SessionModel.Mfa request, BootorHttpRequest bootorHttpRequest) {
        return React.emitter(() -> {
            // 获取用户信息
            Entity result = userApi.handleDataLoadByAcct(request.getAcct());
            if (ResultSets.isEmpty(result)) {
                Logger.error("user not found error;acct=%s;flow=%d;", request.getAcct(), request.getFlow());
                return StandardResult.error(Errno.ENOT_FOUND);
            }
            byte[] password = result.getBytes(UserDef.Info.PWD);
            if (!request.getPwd().equals(Convertor.bytes2hex(password))) {
                Logger.error("password not match error;acct=%s;flow=%d;", request.getAcct(), request.getFlow());
                return StandardResult.error(SessionDef.SessionErrno.SESSION_USER_INVALID);
            }
            // MFA二次验证
            String secret = result.getString(UserDef.Info.TWOFA_SECRET);
            TimeProvider timeProvider = new SystemTimeProvider();
            CodeGenerator codeGenerator = new DefaultCodeGenerator();
            CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
            boolean successful = verifier.isValidCode(secret, request.getCode());
            if (!successful) {
                Logger.error("mfa code not match error;acct=%s;code=%s;flow=%d;",
                        request.getAcct(), request.getCode(), request.getFlow());
                return StandardResult.error(SessionDef.SessionErrno.SESSION_MFA_INVALID);
            }
            // 更新MFA已经绑定
            MixBit mixBit = MixBit.build(result.getInt(UserDef.Info.FLAG));
            if (!mixBit.matched(UserDef.Flag.FLAG_ENABLE_TWOFA)) {
                long flag = mixBit.active(UserDef.Flag.FLAG_ENABLE_TWOFA);
                Where where = Where.builder(UserDef.Info.ACCT, Where.EQ, request.getAcct());
                Updater updater = Updater.builder(UserDef.Info.FLAG, flag);
                updater.where(where);
                userApi.handleDataUpdator(updater);
            }
            // 添加登录会话
            long id = result.getLong(UserDef.Info.ID);
            String token = handleAddSession(id, bootorHttpRequest);
            Param response = Param.builder().setString(SessionDef.Info.TOKEN, token);
            Logger.info("add session ok;uid=%d;flow=%d;", id, request.getFlow());
            return StandardResult.success(response);
        });
    }

    @ReadCmd
    @Request.Post("/api/v1/session/logout")
    public React<StandardResult> logoutSession(@Model SessionModel.Logout request) {
        return React.emitter(() -> {
            String token = request.getToken();
            long sessionId = SessionDef.parseSessionId(token);
            if (sessionId <= 0) {
                return StandardResult.error(Errno.ERROR);
            }
            String sessionKey = SessionDef.getSessionCacheKey(sessionId);
            boolean suessess = cache.remove(sessionKey, token);
            Logger.info("delete session ok;key=%s;token=%s;flow=%d;", sessionKey, token, request.getFlow());
            return suessess ? StandardResult.success() : StandardResult.error(Errno.ERROR);
        });
    }

    public CacheManager getCache() {
        return cache;
    }

    // 获取用户对应的角色权限列表，用于每次登录时都会加载用户对应的角色权限列表，用于权限校验
    private Param handleUserPermissionInfoLoad(Param userInfo) throws Exception {
        Param permissionInfo = Param.builder();
        String roleIds = userInfo.getString(UserDef.Info.ROLE_IDS);
        if (!StrUtil.isEmpty(roleIds)) {
            Table<String> roleIdList = JsonUtil.parseJsonTable(roleIds);
            for (String roleId : roleIdList) {
                Param roleInfo = roleApi.handleDataLoadById(Long.parseLong(roleId));
                if (ResultSets.isEmpty(roleInfo)) {
                    continue;
                }
                boolean isAdmin = roleInfo.getInt(RoleDef.Info.IS_ADMIN) == 1;
                if (isAdmin) {
                    permissionInfo.setBoolean(SessionDef.Info.IS_ADMIN, true);
                    break;
                }
                String permissions = roleInfo.getString(RoleDef.Info.PERMISSIONS);
                Table<String> sessionPermission = Table.builder();
                permissionInfo.setTable(SessionDef.Info.PERMISSIONS, sessionPermission);
                if (!StrUtil.isEmpty(permissions)) {
                    Table<String> permissionList = JsonUtil.parseJsonTable(permissions);
                    for (String permission : permissionList) {
                        if (!sessionPermission.contains(permission)) {
                            sessionPermission.add(permission);
                        }
                    }
                }
            }
        }
        return permissionInfo;
    }

    private String handleAddSession(long id, BootorHttpRequest request) throws Exception {
        String token = SessionDef.genSessionId(id);
        Param sessionInfo = Param.builder()
                .setLong(SessionDef.Info.UID, id)
                .setString(SessionDef.Info.TOKEN, token);
        boolean success = cache.hput(SessionDef.getSessionCacheKey(id), token,
                sessionInfo, SessionDef.Protocol.getInfoSessionSchema());
        if (!success) {
            return null;
        }
        String ip = NetUtil.getRequestIp(request);
        Param sessionLogInfo = Param.builder(SessionLogDef.Info.IP, ip)
                .setLong(SessionLogDef.Info.USER_ID, id)
                .setString(SessionLogDef.Info.USER_AGENT, request.getHeader("User-Agent"));
        sessionLogApi.handleDataAdd(sessionLogInfo);
        return token;
    }

    private static String handleQrCodeGenerate(String acct, String secret) throws Exception {
        QrData data = new QrData.Builder()
                .label(acct)
                .secret(secret)
                .issuer("TeambeitGateway")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
        ZxingPngQrGenerator generator = new ZxingPngQrGenerator();
        generator.setImageSize(180);
        byte[] imageData = generator.generate(data);
        String mimeType = generator.getImageMimeType();
        return Utils.getDataUriForImage(imageData, mimeType);
    }

    @Override
    public void close() {
        cache.shutdown();
    }
}
