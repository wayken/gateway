package cloud.apposs.gateway.dashboard.api.database.loader;

import cloud.apposs.cachex.*;
import cloud.apposs.cachex.database.Entity;
import cloud.apposs.cachex.database.Query;
import cloud.apposs.cachex.database.Updater;
import cloud.apposs.cachex.database.Where;
import cloud.apposs.gateway.dashboard.api.database.app.UserDef;
import cloud.apposs.gateway.dashboard.api.database.loader.UserLoader.UserCacheKey;
import cloud.apposs.protobuf.ProtoSchema;
import cloud.apposs.util.Ref;
import cloud.apposs.util.Table;

public class UserLoader extends CacheLoaderAdapter<UserCacheKey, Entity> {
    @Override
    public Entity select(CacheKey<?> key, Query query, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
        Entity data = template.select(UserDef.Table.TABLE_USER, UserDef.Info.ID, query);
        if (data == null) {
            return EntityCacheX.DATA_NOT_FOUND;
        }
        return data;
    }

    @Override
    public int add(UserCacheKey key, Entity value, ProtoSchema schema, DBTemplate template, Ref<Object> idRef, Object... args) throws Exception {
        return template.insert(UserDef.Table.TABLE_USER, value, idRef);
    }

    @Override
    public int update(CacheKey<?> key, Updater updater, ProtoSchema schema, DBTemplate template, Object... args) throws Exception {
        return template.update(UserDef.Table.TABLE_USER, updater);
    }

    @Override
    public int delete(CacheKey<?> key, Where where, DBTemplate template, Object... args) throws Exception {
        return template.delete(UserDef.Table.TABLE_USER, where);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Table<Entity> query(CacheKey<?> key, Query query, ProtoSchema schema, DBTemplate template, Object[] args) throws Exception {
        return template.query(UserDef.Table.TABLE_USER, query);
    }

    public static class UserCacheKey extends AbstractCacheKey<Long> {
        private static final String CACHE_KEY_PREFIX = "User-";

        public UserCacheKey(long id) {
            super(id, CACHE_KEY_PREFIX);
        }
    }
}
