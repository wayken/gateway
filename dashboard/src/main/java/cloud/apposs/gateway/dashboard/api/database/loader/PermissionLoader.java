package cloud.apposs.gateway.dashboard.api.database.loader;

import cloud.apposs.cachex.*;
import cloud.apposs.cachex.database.Entity;
import cloud.apposs.cachex.database.Query;
import cloud.apposs.cachex.database.Updater;
import cloud.apposs.cachex.database.Where;
import cloud.apposs.gateway.dashboard.api.database.app.PermissionDef;
import cloud.apposs.gateway.dashboard.api.database.loader.PermissionLoader.PermissionCacheKey;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Ref;
import cloud.apposs.util.Table;

public class PermissionLoader extends CacheLoaderAdapter<PermissionCacheKey, Entity> {
    @Override
    public Entity select(CacheKey<?> key, Query query, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
        Entity data = template.select(PermissionDef.Table.TABLE_PERMISSION, PermissionDef.Info.ID, query);
        if (data == null) {
            return EntityCacheX.DATA_NOT_FOUND;
        }
        return data;
    }

    @Override
    public int add(PermissionCacheKey key, Entity value, ProtoSchema schema, DBTemplate template, Ref<Object> idRef, Object... args) throws Exception {
        return template.insert(PermissionDef.Table.TABLE_PERMISSION, value, idRef);
    }

    @Override
    public int update(CacheKey<?> key, Updater updater, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
        return template.update(PermissionDef.Table.TABLE_PERMISSION, updater);
    }

    @Override
    public int delete(CacheKey<?> key, Where where, DBTemplate template, Object... args) throws Exception {
        return template.delete(PermissionDef.Table.TABLE_PERMISSION, where);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Table<Entity> query(CacheKey<?> key, Query query, ProtoSchema schema, DBTemplate template, Object[] args) throws Exception {
        return template.query(PermissionDef.Table.TABLE_PERMISSION, query);
    }

    public static class PermissionCacheKey extends AbstractCacheKey<Long> {
        private static final String CACHE_KEY_PREFIX = "Permission-";

        public PermissionCacheKey(long id) {
            super(id, CACHE_KEY_PREFIX);
        }
    }
}
