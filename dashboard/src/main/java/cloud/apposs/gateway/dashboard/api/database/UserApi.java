package cloud.apposs.gateway.dashboard.api.database;

import cloud.apposs.bootor.cachex.DataSource;
import cloud.apposs.cachex.CacheKey;
import cloud.apposs.cachex.CacheXConfig;
import cloud.apposs.cachex.NoCacheKey;
import cloud.apposs.cachex.database.Entity;
import cloud.apposs.cachex.database.Query;
import cloud.apposs.cachex.database.Updater;
import cloud.apposs.cachex.database.Where;
import cloud.apposs.gateway.dashboard.GatewayDashboardConfig;
import cloud.apposs.gateway.dashboard.api.database.app.CommonDef;
import cloud.apposs.gateway.dashboard.api.database.app.UserDef;
import cloud.apposs.gateway.dashboard.api.database.loader.UserLoader;
import cloud.apposs.gateway.dashboard.api.database.loader.UserLoader.UserCacheKey;
import cloud.apposs.gateway.dashboard.api.database.model.UserModel;
import cloud.apposs.gateway.dashboard.interceptor.Permission;
import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.logger.Logger;
import cloud.apposs.react.React;
import cloud.apposs.react.actor.Actor;
import cloud.apposs.rest.annotation.*;
import cloud.apposs.util.*;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;

import java.util.Calendar;

@Executor
@RestAction
public class UserApi implements AutoCloseable {
    public static final int DEFAULT_WORKER_ID = 1;
    public static final int DEFAULT_IDC_ID = 1;

    private final DataSource<UserCacheKey> source;

    private final Actor actor;

    private final IdWorker idWorker;

    @Autowired
    public UserApi(GatewayDashboardConfig config) throws Exception {
        CacheXConfig cacheXConfig = new CacheXConfig();
        CacheXConfig.DbConfig dbConfig = cacheXConfig.getDbConfig();
        dbConfig.setDialect(config.getDatabaseDialect());
        dbConfig.setJdbcUrl(config.getDatabaseUrl());
        dbConfig.setUsername(config.getDatabaseUsername());
        dbConfig.setPassword(config.getDatabasePassword());
        dbConfig.setMinConnections(config.getDatabaseMinPoolSize());
        dbConfig.setMaxConnections(config.getDatabaseMaxPoolSize());
        this.source = new DataSource<>(cacheXConfig, new UserLoader());
        this.actor = new Actor();
        this.idWorker = IdWorker.builder(DEFAULT_WORKER_ID, DEFAULT_IDC_ID);
    }

    @Request.Get("/api/v1/database/user/{id}")
    public React<StandardResult> getUserInfo(@Variable("id") Long id) {
        return React.emitter(() -> {
            Entity result = handleDataLoadById(id);
            if (result == null) {
                return StandardResult.error(Errno.ENOT_FOUND);
            }
            handleDataConvert(result);
            return StandardResult.success(result);
        });
    }

    @Request.Get("/api/v1/database/users")
    @Permission("system:authority:user:list")
    public React<StandardResult> getUserList() {
        return React.emitter(() -> {
            Table<Entity> result = source.query(NoCacheKey.getInstance(), null, UserDef.Protocol.getInfoUserSchema());
            if (result == null) {
                return StandardResult.error(Errno.ENOT_FOUND);
            }
            for (Entity data : result) {
                handleDataConvert(data);
            }
            return StandardResult.success(result);
        });
    }

    @Request.Put("/api/v1/database/user")
    @Permission("system:authority:user:add")
    public React<StandardResult> addUser(@Model UserModel.Add request) {
        return React.emitter(() -> {
            long id = idWorker.nextId();
            UserCacheKey cacheKey = new UserCacheKey(id);
            Calendar now = Calendar.getInstance();
            Entity infomation = Entity.builder();
            infomation.setLong(UserDef.Info.ID, id)
                    .setString(UserDef.Info.ACCT, request.getAcct())
                    .setString(UserDef.Info.NAME, request.getName())
                    .setString(UserDef.Info.ROLE_IDS, request.getRoles().toJson())
                    .setCalendar(UserDef.Info.CREATE_TIME, now);
            infomation.setBytes(UserDef.Info.PWD, Convertor.hex2bytes(request.getPwd()));
            source.put(cacheKey, infomation, UserDef.Protocol.getInfoUserSchema());
            Param response = Param.builder().setLong(UserDef.Info.ID, id);
            Logger.info("Add user success, id=%d;flow=%d;", id, request.getFlow());
            return StandardResult.success(response);
        });
    }

    @Request.Post("/api/v1/database/user/{id}")
    @Permission("system:authority:user:edit")
    public React<StandardResult> updateUser(@Variable("id") long id, @Model UserModel.Update request) {
        return React.emitter(() -> {
            CacheKey<Long> cacheKey = new UserCacheKey(id);
            Where where = Where.builder(UserDef.Info.ID, Where.EQ, id);
            Query query = Query.builder(where);
            Entity infomation = source.select(cacheKey, query, UserDef.Protocol.getInfoUserSchema());
            if (ResultSets.isEmpty(infomation)) {
                return StandardResult.error(Errno.ENOT_FOUND);
            }
            Updater updater = Updater.builder(UserDef.Info.ID, id)
                    .add(UserDef.Info.NAME, request.getName())
                    .add(UserDef.Info.ACCT, request.getAcct())
                    .add(UserDef.Info.ROLE_IDS, request.getRoles().toJson());
            String twofaScheme = infomation.getString(UserDef.Info.TWOFA_SCHEME);
            boolean isUserTwofa = !StrUtil.isEmpty(twofaScheme);
            // 如果开启了二次验证，并且用户没有设置二次验证验证，则设置默认的二次验证方式
            if (request.isTwofa() && !isUserTwofa) {
                SecretGenerator secretGenerator = new DefaultSecretGenerator();
                String secret = secretGenerator.generate();
                updater.add(UserDef.Info.TWOFA_SCHEME, UserDef.TwofaScheme.TOTP);
                updater.add(UserDef.Info.TWOFA_SECRET, secret);
                updater.add(UserDef.Info.FLAG, Updater.UPDATE_LAND, ~UserDef.Flag.FLAG_ENABLE_TWOFA);
            } else if (!request.isTwofa() && isUserTwofa) {
                updater.add(UserDef.Info.TWOFA_SCHEME, Null.builder());
                updater.add(UserDef.Info.TWOFA_SECRET, "");
                updater.add(UserDef.Info.FLAG, Updater.UPDATE_LAND, ~UserDef.Flag.FLAG_ENABLE_TWOFA);
            }
            if (request.getPwd() != null) {
                updater.add(UserDef.Info.PWD, Convertor.hex2bytes(request.getPwd()));
            }
            updater.where(where);
            source.update(cacheKey, updater, UserDef.Protocol.getInfoUserSchema());
            Param response = Param.builder().setLong(UserDef.Info.ID, id);
            Logger.info("Update user success, id=%d;flow=%d;", id, request.getFlow());
            return StandardResult.success(response);
        });
    }

    @Request.Delete("/api/v1/database/user/{id}")
    @Permission("system:authority:user:delete")
    public React<StandardResult> deleteUser(@Variable("id") long id, @Model UserModel.Delete request) {
        return React.emitter(() -> {
            CacheKey<Long> cacheKey = new UserCacheKey(id);
            Where where = Where.builder(UserDef.Info.ID, Where.EQ, id);
            Query query = Query.builder(where);
            if (!source.exist(cacheKey, query, UserDef.Protocol.getInfoUserSchema())) {
                return StandardResult.error(Errno.ENOT_FOUND);
            }
            Param response = Param.builder().setLong(UserDef.Info.ID, id);
            source.delete(cacheKey, where);
            Logger.info("Delete user success, id=%d;flow=%d;", id, request.getFlow());
            return StandardResult.success(response);
        });
    }

    public DataSource<UserCacheKey> getSource() {
        return source;
    }

    // 根据id查询用户信息
    public Entity handleDataLoadById(Long id) throws Exception {
        CacheKey<Long> cacheKey = new UserCacheKey(id);
        Where where = Where.builder(UserDef.Info.ID, Where.EQ, id);
        Query query = Query.builder(where);
        return source.select(cacheKey, query, UserDef.Protocol.getInfoUserSchema());
    }

    // 根据账号查询用户信息
    public Entity handleDataLoadByAcct(String acct) throws Exception {
        Where where = Where.builder(UserDef.Info.ACCT, Where.EQ, acct);
        Query query = Query.builder(where);
        return source.select(NoCacheKey.getInstance(), query, UserDef.Protocol.getInfoUserSchema());
    }

    // 更新用户信息
    public void handleDataUpdator(Updater updater) throws Exception {
        source.update(NoCacheKey.getInstance(), updater, UserDef.Protocol.getInfoUserSchema());
    }

    private Param handleDataConvert(Entity data) {
        Param infomation = Param.builder(data,
            UserDef.Info.ID,
            UserDef.Info.NAME,
            UserDef.Info.ACCT,
            UserDef.Info.ROLE_IDS,
            UserDef.Info.FLAG
        );
        Calendar createTime = data.getCalendar(UserDef.Info.CREATE_TIME);
        infomation.setLong(CommonDef.Info.DATE, createTime.getTimeInMillis());
        String roleIds = data.getString(UserDef.Info.ROLE_IDS);
        if (!StrUtil.isEmpty(roleIds)) {
            infomation.setTable(UserDef.Info.ROLE_IDS, JsonUtil.parseJsonTable(roleIds));
        } else {
            infomation.setTable(UserDef.Info.ROLE_IDS, Table.builder());
        }
        return infomation;
    }

    @Override
    public void close() {
        source.shutdown();
        actor.shutdown();
    }
}
