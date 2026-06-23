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
import cloud.apposs.gateway.dashboard.api.database.app.RoleDef;
import cloud.apposs.gateway.dashboard.api.database.loader.RoleLoader;
import cloud.apposs.gateway.dashboard.api.database.loader.RoleLoader.RoleCacheKey;
import cloud.apposs.gateway.dashboard.api.database.model.RoleModel;
import cloud.apposs.gateway.dashboard.interceptor.Permission;
import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.logger.Logger;
import cloud.apposs.react.React;
import cloud.apposs.react.actor.Actor;
import cloud.apposs.rest.annotation.*;
import cloud.apposs.util.*;

import java.util.Calendar;

@Executor
@RestAction
public class RoleApi implements AutoCloseable {
    public static final int DEFAULT_WORKER_ID = 1;
    public static final int DEFAULT_IDC_ID = 1;

    private final DataSource<RoleCacheKey> source;

    private final Actor actor;

    private final IdWorker idWorker;

    @Autowired
    public RoleApi(GatewayDashboardConfig config) throws Exception {
        CacheXConfig cacheXConfig = new CacheXConfig();
        CacheXConfig.DbConfig dbConfig = cacheXConfig.getDbConfig();
        dbConfig.setDialect(config.getDatabaseDialect());
        dbConfig.setJdbcUrl(config.getDatabaseUrl());
        dbConfig.setUsername(config.getDatabaseUsername());
        dbConfig.setPassword(config.getDatabasePassword());
        dbConfig.setMinConnections(config.getDatabaseMinPoolSize());
        dbConfig.setMaxConnections(config.getDatabaseMaxPoolSize());
        this.source = new DataSource<>(cacheXConfig, new RoleLoader());
        this.actor = new Actor();
        this.idWorker = IdWorker.builder(DEFAULT_WORKER_ID, DEFAULT_IDC_ID);
    }

    @Request.Get("/api/v1/database/role/{id}")
    public React<StandardResult> getRoleInfo(@Variable("id") Long id) {
        return React.emitter(() -> {
            Entity result = handleDataLoadById(id);
            if (result == null) {
                return StandardResult.error(Errno.ENOT_FOUND);
            }
            Param infomation = handleDataConvert(result);
            return StandardResult.success(infomation);
        });
    }

    @Request.Get("/api/v1/database/roles")
    @Permission("system:authority:role:list")
    public React<StandardResult> getRoleList() {
        return React.emitter(() -> {
            Query query = Query.builder("*");
            Table<Entity> result = source.query(NoCacheKey.getInstance(), query, RoleDef.Protocol.getInfoRoleSchema());
            if (result == null) {
                return StandardResult.error(Errno.ENOT_FOUND);
            }
            Table<Param> response = Table.builder();
            for (Entity data : result) {
                Param infomation = handleDataConvert(data);
                response.add(infomation);
            }
            return StandardResult.success(response);
        });
    }

    @Request.Put("/api/v1/database/role")
    @Permission("system:authority:role:add")
    public React<StandardResult> addRole(@Model RoleModel.Add request) {
        return React.emitter(() -> {
            long id = idWorker.nextId();
            RoleLoader.RoleCacheKey cacheKey = new RoleLoader.RoleCacheKey(id);
            Calendar now = Calendar.getInstance();
            Entity infomation = Entity.builder();
            infomation.setLong(RoleDef.Info.ID, id)
                    .setString(RoleDef.Info.NAME, request.getName())
                    .setInt(RoleDef.Info.IS_ADMIN, request.isAdmin() ? 1 : 0)
                    .setString(RoleDef.Info.REMARK, request.getRemark())
                    .setCalendar(RoleDef.Info.CREATE_TIME, now)
                    .setCalendar(RoleDef.Info.UPDATE_TIME, now);
            if (request.getPermissions() != null) {
                infomation.setString(RoleDef.Info.PERMISSIONS, request.getPermissions().toJson());
            }
            source.put(cacheKey, infomation, RoleDef.Protocol.getInfoRoleSchema());
            Param response = Param.builder().setLong(RoleDef.Info.ID, id);
            Logger.info("Add role success, id=%d;flow=%d;", id, request.getFlow());
            return StandardResult.success(response);
        });
    }

    @Request.Post("/api/v1/database/role/{id}")
    @Permission("system:authority:role:edit")
    public React<StandardResult> updateRole(@Variable("id") long id, @Model RoleModel.Update request) {
        return React.emitter(() -> {
            CacheKey<Long> cacheKey = new RoleLoader.RoleCacheKey(id);
            Where where = Where.builder(RoleDef.Info.ID, Where.EQ, id);
            Query query = Query.builder(where);
            Entity infomation = source.select(cacheKey, query, RoleDef.Protocol.getInfoRoleSchema());
            if (ResultSets.isEmpty(infomation)) {
                return StandardResult.error(Errno.ENOT_FOUND);
            }
            Calendar now = Calendar.getInstance();
            Updater updater = Updater.builder(RoleDef.Info.ID, id)
                    .add(RoleDef.Info.NAME, request.getName())
                    .add(RoleDef.Info.IS_ADMIN, request.isAdmin() ? 1 : 0)
                    .add(RoleDef.Info.REMARK, request.getRemark())
                    .add(RoleDef.Info.UPDATE_TIME, now);
            if (request.getPermissions() != null) {
                updater.add(RoleDef.Info.PERMISSIONS, request.getPermissions().toJson());
            } else {
                updater.add(RoleDef.Info.PERMISSIONS, Null.builder());
            }
            updater.where(where);
            source.update(cacheKey, updater, RoleDef.Protocol.getInfoRoleSchema());
            Param response = Param.builder().setLong(RoleDef.Info.ID, id);
            Logger.info("Update role success, id=%d;flow=%d;", id, request.getFlow());
            return StandardResult.success(response);
        });
    }

    @Request.Delete("/api/v1/database/role/{id}")
    @Permission("system:authority:role:delete")
    public React<StandardResult> deleteRole(@Variable("id") long id, @Model RoleModel.Delete request) {
        return React.emitter(() -> {
            CacheKey<Long> cacheKey = new RoleLoader.RoleCacheKey(id);
            Where where = Where.builder(RoleDef.Info.ID, Where.EQ, id);
            Query query = Query.builder(where);
            if (!source.exist(cacheKey, query, RoleDef.Protocol.getInfoRoleSchema())) {
                return StandardResult.error(Errno.ENOT_FOUND);
            }
            Param response = Param.builder().setLong(RoleDef.Info.ID, id);
            source.delete(cacheKey, where);
            Logger.info("Delete role success, id=%d;flow=%d;", id, request.getFlow());
            return StandardResult.success(response);
        });
    }

    // 根据id查询角色信息
    public Entity handleDataLoadById(Long id) throws Exception {
        CacheKey<Long> cacheKey = new RoleLoader.RoleCacheKey(id);
        Where where = Where.builder(RoleDef.Info.ID, Where.EQ, id);
        Query query = Query.builder(where);
        return source.select(cacheKey, query, RoleDef.Protocol.getInfoRoleSchema());
    }

    private Param handleDataConvert(Entity data) {
        Param infomation = Param.builder(data,
            RoleDef.Info.ID,
            RoleDef.Info.NAME,
            RoleDef.Info.IS_ADMIN,
            RoleDef.Info.REMARK
        );
        Calendar createTime = data.getCalendar(RoleDef.Info.CREATE_TIME);
        infomation.setLong(CommonDef.Info.DATE, createTime.getTimeInMillis());
        String permissions = data.getString(RoleDef.Info.PERMISSIONS);
        if (permissions != null) {
            infomation.setTable(RoleDef.Info.PERMISSIONS, JsonUtil.parseJsonTable(permissions));
        }
        return infomation;
    }

    @Override
    public void close() throws Exception {
        source.shutdown();
        actor.shutdown();
    }
}
