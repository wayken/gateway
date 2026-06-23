package cloud.apposs.gateway.dashboard.api.database;

import cloud.apposs.bootor.cachex.DataSource;
import cloud.apposs.cachex.CacheXConfig;
import cloud.apposs.cachex.NoCacheKey;
import cloud.apposs.cachex.database.Entity;
import cloud.apposs.cachex.database.Query;
import cloud.apposs.cachex.database.Where;
import cloud.apposs.cachex.jdbc.ColumnLabel;
import cloud.apposs.gateway.dashboard.GatewayDashboardConfig;
import cloud.apposs.gateway.dashboard.api.database.app.CommonDef;
import cloud.apposs.gateway.dashboard.api.database.app.SessionLogDef;
import cloud.apposs.gateway.dashboard.api.database.app.UserDef;
import cloud.apposs.gateway.dashboard.api.database.loader.SessionLogLoader;
import cloud.apposs.gateway.dashboard.api.database.loader.SessionLogLoader.SessionLogCacheKey;
import cloud.apposs.gateway.dashboard.api.database.model.SessionLogModel;
import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.react.React;
import cloud.apposs.react.actor.Actor;
import cloud.apposs.rest.annotation.Executor;
import cloud.apposs.rest.annotation.Model;
import cloud.apposs.rest.annotation.Request;
import cloud.apposs.rest.annotation.RestAction;
import cloud.apposs.util.*;

import java.util.Calendar;

@Executor
@RestAction
public class SessionLogApi {
    public static final int DEFAULT_WORKER_ID = 1;
    public static final int DEFAULT_IDC_ID = 1;

    private final DataSource<SessionLogCacheKey> source;

    private final Actor actor;

    private final IdWorker idWorker;

    @Autowired
    public SessionLogApi(GatewayDashboardConfig config) throws Exception {
        CacheXConfig cacheXConfig = new CacheXConfig();
        CacheXConfig.DbConfig dbConfig = cacheXConfig.getDbConfig();
        dbConfig.setDialect(config.getDatabaseDialect());
        dbConfig.setJdbcUrl(config.getDatabaseUrl());
        dbConfig.setUsername(config.getDatabaseUsername());
        dbConfig.setPassword(config.getDatabasePassword());
        dbConfig.setMinConnections(config.getDatabaseMinPoolSize());
        dbConfig.setMaxConnections(config.getDatabaseMaxPoolSize());
        this.source = new DataSource<>(cacheXConfig, new SessionLogLoader());
        this.actor = new Actor();
        this.idWorker = IdWorker.builder(DEFAULT_WORKER_ID, DEFAULT_IDC_ID);
    }

    @Request.Get("/api/v1/session/logs")
    public React<StandardResult> getLogList(@Model SessionLogModel.List request) {
        return React.emitter(() -> {
            Query query = Query.builder(
                    ColumnLabel.create(SessionLogDef.Table.TABLE_SESSION_LOG, SessionLogDef.Info.ID),
                    ColumnLabel.create(SessionLogDef.Table.TABLE_SESSION_LOG, SessionLogDef.Info.USER_ID),
                    ColumnLabel.create(SessionLogDef.Table.TABLE_SESSION_LOG, SessionLogDef.Info.IP),
                    ColumnLabel.create(SessionLogDef.Table.TABLE_SESSION_LOG, SessionLogDef.Info.USER_AGENT),
                    ColumnLabel.create(SessionLogDef.Table.TABLE_SESSION_LOG, SessionLogDef.Info.LOGIN_TIME),
                    ColumnLabel.create(UserDef.Table.TABLE_USER, UserDef.Info.NAME)
            );
            query.join(UserDef.Table.TABLE_USER).on(ColumnLabel.create(UserDef.Table.TABLE_USER, UserDef.Info.ID),
                    Where.EQ, ColumnLabel.create(SessionLogDef.Table.TABLE_SESSION_LOG, SessionLogDef.Info.USER_ID));
            query.orderBy(ColumnLabel.create(SessionLogDef.Table.TABLE_SESSION_LOG, SessionLogDef.Info.LOGIN_TIME), true);
            query.limit(request.getStart(), request.getLimit());
            Table<Entity> result = source.query(NoCacheKey.getInstance(), query, SessionLogDef.Protocol.getInfoSessionLogSchema());
            if (result == null) {
                return StandardResult.error(Errno.ENOT_FOUND);
            }
            for (Entity data : result) {
                handleDataConvert(data);
            }
            return StandardResult.success(result);
        });
    }

    @Request.Get("/api/v1/session/log/count")
    public React<StandardResult> getLogCount() {
        return React.emitter(() -> {
            Query query = Query.builder(Query.COUNT_FIELDS);
            Param total = source.select(NoCacheKey.getInstance(), query, SessionLogDef.Protocol.getInfoSessionLogSchema());
            return StandardResult.success(total);
        });
    }

    public long handleDataAdd(Param data) throws Exception {
        long id = idWorker.nextId();
        SessionLogCacheKey cacheKey = new SessionLogCacheKey(id);
        Calendar now = Calendar.getInstance();
        Entity infomation = Entity.builder();
        infomation.setLong(UserDef.Info.ID, id)
                .setLong(SessionLogDef.Info.USER_ID, data.getLong(SessionLogDef.Info.USER_ID))
                .setString(SessionLogDef.Info.IP, data.getString(SessionLogDef.Info.IP))
                .setString(SessionLogDef.Info.USER_AGENT, data.getString(SessionLogDef.Info.USER_AGENT))
                .setCalendar(SessionLogDef.Info.LOGIN_TIME, now);
        source.put(cacheKey, infomation, SessionLogDef.Protocol.getInfoSessionLogSchema());
        return id;
    }

    private void handleDataConvert(Entity data) {
        Calendar date = data.getCalendar(SessionLogDef.Info.LOGIN_TIME);
        data.setLong(CommonDef.Info.DATE, date.getTimeInMillis());
        data.remove(SessionLogDef.Info.LOGIN_TIME);
    }
}
