package cloud.apposs.gateway.dashboard.api.database.loader;

import cloud.apposs.cachex.*;
import cloud.apposs.cachex.database.Entity;
import cloud.apposs.cachex.database.Query;
import cloud.apposs.cachex.database.Where;
import cloud.apposs.gateway.dashboard.api.database.app.OperationLogDef;
import cloud.apposs.gateway.dashboard.api.database.loader.OperationLogLoader.OperationLogCacheKey;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Ref;
import cloud.apposs.util.Table;

public class OperationLogLoader extends CacheLoaderAdapter<OperationLogCacheKey, Entity> {
    @Override
    public Entity select(CacheKey<?> key, Query query, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
        Entity data = template.select(OperationLogDef.Table.TABLE_OPERATION_LOG, OperationLogDef.Info.ID, query);
        if (data == null) {
            return EntityCacheX.DATA_NOT_FOUND;
        }
        return data;
    }

    @Override
    public int add(OperationLogCacheKey key, Entity value, ProtoSchema schema, DBTemplate template, Ref<Object> idRef, Object... args) throws Exception {
        return template.insert(OperationLogDef.Table.TABLE_OPERATION_LOG, value, idRef);
    }

    @Override
    public int delete(CacheKey<?> key, Where where, DBTemplate template, Object... args) throws Exception {
        return template.delete(OperationLogDef.Table.TABLE_OPERATION_LOG, where);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Table<Entity> query(CacheKey<?> key, Query query, ProtoSchema schema, DBTemplate template, Object[] args) throws Exception {
        return template.query(OperationLogDef.Table.TABLE_OPERATION_LOG, query);
    }

    public static class OperationLogCacheKey extends AbstractCacheKey<Long> {
        private static final String CACHE_KEY_PREFIX = "OperationLog-";

        public OperationLogCacheKey(long id) {
            super(id, CACHE_KEY_PREFIX);
        }
    }
}
