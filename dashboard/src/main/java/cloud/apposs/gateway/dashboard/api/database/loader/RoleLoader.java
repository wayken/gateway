package cloud.apposs.gateway.dashboard.api.database.loader;

import cloud.apposs.cachex.*;
import cloud.apposs.cachex.database.Entity;
import cloud.apposs.cachex.database.Query;
import cloud.apposs.cachex.database.Updater;
import cloud.apposs.cachex.database.Where;
import cloud.apposs.gateway.dashboard.api.database.app.RoleDef;
import cloud.apposs.gateway.dashboard.api.database.loader.RoleLoader.RoleCacheKey;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Ref;
import cloud.apposs.util.Table;

public class RoleLoader extends CacheLoaderAdapter<RoleCacheKey, Entity> {
    @Override
    public Entity select(CacheKey<?> key, Query query, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
        Entity data = template.select(RoleDef.Table.TABLE_ROLE, RoleDef.Info.ID, query);
        if (data == null) {
            return EntityCacheX.DATA_NOT_FOUND;
        }
        return data;
    }

    @Override
    public int add(RoleCacheKey key, Entity value, ProtoSchema schema, DBTemplate template, Ref<Object> idRef, Object... args) throws Exception {
        return template.insert(RoleDef.Table.TABLE_ROLE, value, idRef);
    }

    @Override
    public int update(CacheKey<?> key, Updater updater, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
        return template.update(RoleDef.Table.TABLE_ROLE, updater);
    }

    @Override
    public int delete(CacheKey<?> key, Where where, DBTemplate template, Object... args) throws Exception {
        return template.delete(RoleDef.Table.TABLE_ROLE, where);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Table<Entity> query(CacheKey<?> key, Query query, ProtoSchema schema, DBTemplate template, Object[] args) throws Exception {
        return template.query(RoleDef.Table.TABLE_ROLE, query);
    }

    public static class RoleCacheKey extends AbstractCacheKey<Long> {
        private static final String CACHE_KEY_PREFIX = "Role-";

        public RoleCacheKey(long id) {
            super(id, CACHE_KEY_PREFIX);
        }
    }
}
