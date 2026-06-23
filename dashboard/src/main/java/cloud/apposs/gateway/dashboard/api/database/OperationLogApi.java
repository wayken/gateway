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
import cloud.apposs.gateway.dashboard.api.database.app.OperationLogDef;
import cloud.apposs.gateway.dashboard.api.database.app.UserDef;
import cloud.apposs.gateway.dashboard.api.database.loader.OperationLogLoader;
import cloud.apposs.gateway.dashboard.api.database.loader.OperationLogLoader.OperationLogCacheKey;
import cloud.apposs.gateway.dashboard.api.database.model.OperationLogModel;
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
public class OperationLogApi {
    public static final int DEFAULT_WORKER_ID = 1;
    public static final int DEFAULT_IDC_ID = 1;

    private final DataSource<OperationLogCacheKey> source;

    private final Actor actor;

    private final IdWorker idWorker;

    @Autowired
    public OperationLogApi(GatewayDashboardConfig config) throws Exception {
        CacheXConfig cacheXConfig = new CacheXConfig();
        CacheXConfig.DbConfig dbConfig = cacheXConfig.getDbConfig();
        dbConfig.setDialect(config.getDatabaseDialect());
        dbConfig.setJdbcUrl(config.getDatabaseUrl());
        dbConfig.setUsername(config.getDatabaseUsername());
        dbConfig.setPassword(config.getDatabasePassword());
        dbConfig.setMinConnections(config.getDatabaseMinPoolSize());
        dbConfig.setMaxConnections(config.getDatabaseMaxPoolSize());
        this.source = new DataSource<>(cacheXConfig, new OperationLogLoader());
        this.actor = new Actor();
        this.idWorker = IdWorker.builder(DEFAULT_WORKER_ID, DEFAULT_IDC_ID);
    }

    @Request.Get("/api/v1/operation/logs")
    public React<StandardResult> getLogList(@Model OperationLogModel.List request) {
        return React.emitter(() -> {
            Query query = Query.builder(
                    ColumnLabel.create(OperationLogDef.Table.TABLE_OPERATION_LOG, OperationLogDef.Info.ID),
                    ColumnLabel.create(OperationLogDef.Table.TABLE_OPERATION_LOG, OperationLogDef.Info.USER_ID),
                    ColumnLabel.create(OperationLogDef.Table.TABLE_OPERATION_LOG, OperationLogDef.Info.IP),
                    ColumnLabel.create(OperationLogDef.Table.TABLE_OPERATION_LOG, OperationLogDef.Info.MODULE),
                    ColumnLabel.create(OperationLogDef.Table.TABLE_OPERATION_LOG, OperationLogDef.Info.MODULE_ID),
                    ColumnLabel.create(OperationLogDef.Table.TABLE_OPERATION_LOG, OperationLogDef.Info.URI),
                    ColumnLabel.create(OperationLogDef.Table.TABLE_OPERATION_LOG, OperationLogDef.Info.OPERATION_TYPE),
                    ColumnLabel.create(OperationLogDef.Table.TABLE_OPERATION_LOG, OperationLogDef.Info.CONTENT),
                    ColumnLabel.create(OperationLogDef.Table.TABLE_OPERATION_LOG, OperationLogDef.Info.USER_AGENT),
                    ColumnLabel.create(OperationLogDef.Table.TABLE_OPERATION_LOG, OperationLogDef.Info.OPERATION_TIME),
                    ColumnLabel.create(UserDef.Table.TABLE_USER, UserDef.Info.NAME)
            );
            query.join(UserDef.Table.TABLE_USER).on(ColumnLabel.create(UserDef.Table.TABLE_USER, UserDef.Info.ID),
                    Where.EQ, ColumnLabel.create(OperationLogDef.Table.TABLE_OPERATION_LOG, OperationLogDef.Info.USER_ID));
            query.orderBy(ColumnLabel.create(OperationLogDef.Table.TABLE_OPERATION_LOG, OperationLogDef.Info.OPERATION_TIME), true);
            query.limit(request.getStart(), request.getLimit());
            Table<Entity> result = source.query(NoCacheKey.getInstance(), query, OperationLogDef.Protocol.getInfoOperationLogSchema());
            if (result == null) {
                return StandardResult.error(Errno.ENOT_FOUND);
            }
            Table<Param> response = Table.builder();
            for (Entity data : result) {
                Param infomation = handleDataFormat(data);
                response.add(infomation);
            }
            return StandardResult.success(response, false);
        });
    }

    @Request.Get("/api/v1/operation/log/count")
    public React<StandardResult> getLogCount() {
        return React.emitter(() -> {
            Query query = Query.builder(Query.COUNT_FIELDS);
            Param total = source.select(NoCacheKey.getInstance(), query, OperationLogDef.Protocol.getInfoOperationLogSchema());
            return StandardResult.success(total);
        });
    }

    public long handleDataAdd(Param data) throws Exception {
        long id = idWorker.nextId();
        OperationLogCacheKey cacheKey = new OperationLogCacheKey(id);
        Calendar now = Calendar.getInstance();
        Entity infomation = Entity.builder();
        infomation.setLong(UserDef.Info.ID, id)
            .setLong(OperationLogDef.Info.USER_ID, data.getLong(OperationLogDef.Info.USER_ID))
            .setString(OperationLogDef.Info.IP, data.getString(OperationLogDef.Info.IP))
            .setString(OperationLogDef.Info.MODULE, data.getString(OperationLogDef.Info.MODULE))
            .setString(OperationLogDef.Info.MODULE_ID, data.getString(OperationLogDef.Info.MODULE_ID))
            .setString(OperationLogDef.Info.URI, data.getString(OperationLogDef.Info.URI))
            .setInt(OperationLogDef.Info.OPERATION_TYPE, data.getInt(OperationLogDef.Info.OPERATION_TYPE))
            .setString(OperationLogDef.Info.CONTENT, data.getString(OperationLogDef.Info.CONTENT))
            .setString(OperationLogDef.Info.USER_AGENT, data.getString(OperationLogDef.Info.USER_AGENT))
            .setCalendar(OperationLogDef.Info.OPERATION_TIME, now);
        source.put(cacheKey, infomation, OperationLogDef.Protocol.getInfoOperationLogSchema());
        return id;
    }

    private Param handleDataFormat(Entity data) {
        Param infomation = Param.builder(data,
            OperationLogDef.Info.ID,
            OperationLogDef.Info.USER_ID,
            OperationLogDef.Info.IP,
            OperationLogDef.Info.MODULE,
            OperationLogDef.Info.MODULE_ID,
            OperationLogDef.Info.URI,
            OperationLogDef.Info.OPERATION_TYPE,
            OperationLogDef.Info.CONTENT,
            OperationLogDef.Info.USER_AGENT
        );
        Calendar date = data.getCalendar(OperationLogDef.Info.OPERATION_TIME);
        infomation.setLong(CommonDef.Info.DATE, date.getTimeInMillis());
        return infomation;
    }
}
