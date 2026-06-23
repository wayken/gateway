package cloud.apposs.gateway.dashboard.api.database.loader;

import cloud.apposs.cachex.*;
import cloud.apposs.cachex.database.Entity;
import cloud.apposs.cachex.database.Query;
import cloud.apposs.cachex.database.Where;
import cloud.apposs.gateway.dashboard.api.database.app.SessionLogDef;
import cloud.apposs.gateway.dashboard.api.database.loader.SessionLogLoader.SessionLogCacheKey;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Ref;
import cloud.apposs.util.Table;

public class SessionLogLoader extends CacheLoaderAdapter<SessionLogCacheKey, Entity> {
    @Override
    public Entity select(CacheKey<?> key, Query query, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
        Entity data = template.select(SessionLogDef.Table.TABLE_SESSION_LOG, SessionLogDef.Info.ID, query);
        if (data == null) {
            return EntityCacheX.DATA_NOT_FOUND;
        }
        return data;
    }

    @Override
    public int add(SessionLogCacheKey key, Entity value, ProtoSchema schema, DBTemplate template, Ref<Object> idRef, Object... args) throws Exception {
        return template.insert(SessionLogDef.Table.TABLE_SESSION_LOG, value, idRef);
    }

    @Override
    public int delete(CacheKey<?> key, Where where, DBTemplate template, Object... args) throws Exception {
        return template.delete(SessionLogDef.Table.TABLE_SESSION_LOG, where);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Table<Entity> query(CacheKey<?> key, Query query, ProtoSchema schema, DBTemplate template, Object[] args) throws Exception {
        return template.query(SessionLogDef.Table.TABLE_SESSION_LOG, query);
    }

    public static class SessionLogCacheKey extends AbstractCacheKey<Long> {
        private static final String CACHE_KEY_PREFIX = "SessionLog-";

        public SessionLogCacheKey(long id) {
            super(id, CACHE_KEY_PREFIX);
        }
    }
}
