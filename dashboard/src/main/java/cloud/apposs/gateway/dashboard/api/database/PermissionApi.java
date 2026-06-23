package cloud.apposs.gateway.dashboard.api.database;

import cloud.apposs.bootor.cachex.DataSource;
import cloud.apposs.cachex.CacheXConfig;
import cloud.apposs.gateway.dashboard.GatewayDashboardConfig;
import cloud.apposs.gateway.dashboard.api.database.loader.PermissionLoader;
import cloud.apposs.gateway.dashboard.api.database.loader.PermissionLoader.PermissionCacheKey;
import cloud.apposs.ioc.annotation.Autowired;
import cloud.apposs.react.actor.Actor;
import cloud.apposs.rest.annotation.Executor;
import cloud.apposs.rest.annotation.RestAction;
import cloud.apposs.util.IdWorker;

@Executor
@RestAction
public class PermissionApi implements AutoCloseable {
    public static final int DEFAULT_WORKER_ID = 1;
    public static final int DEFAULT_IDC_ID = 1;

    private final DataSource<PermissionCacheKey> source;

    private final Actor actor;

    private final IdWorker idWorker;

    @Autowired
    public PermissionApi(GatewayDashboardConfig config) throws Exception {
        CacheXConfig cacheXConfig = new CacheXConfig();
        CacheXConfig.DbConfig dbConfig = cacheXConfig.getDbConfig();
        dbConfig.setDialect(config.getDatabaseDialect());
        dbConfig.setJdbcUrl(config.getDatabaseUrl());
        dbConfig.setUsername(config.getDatabaseUsername());
        dbConfig.setPassword(config.getDatabasePassword());
        dbConfig.setMinConnections(config.getDatabaseMinPoolSize());
        dbConfig.setMaxConnections(config.getDatabaseMaxPoolSize());
        this.source = new DataSource<>(cacheXConfig, new PermissionLoader());
        this.actor = new Actor();
        this.idWorker = IdWorker.builder(DEFAULT_WORKER_ID, DEFAULT_IDC_ID);
    }

    @Override
    public void close() throws Exception {
        source.shutdown();
        actor.shutdown();
    }
}
