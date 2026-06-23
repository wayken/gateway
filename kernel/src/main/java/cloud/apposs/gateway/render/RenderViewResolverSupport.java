package cloud.apposs.gateway.render;

import cloud.apposs.gateway.render.resolver.CacheFileStreamRenderViewResolver;
import cloud.apposs.gateway.render.resolver.FileRenderViewResolver;
import cloud.apposs.gateway.render.resolver.SseEmitterRenderViewResolver;
import cloud.apposs.gateway.render.resolver.StringRenderViewResolver;
import cloud.apposs.util.CachedFileStream;
import cloud.apposs.util.SseEmitter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class RenderViewResolverSupport {
    private static final IRenderViewResolver DEFAULT_RENDER_VIEW_RESOLVER = new StringRenderViewResolver();
    private static final IRenderViewResolver FILE_RENDER_VIEW_RESOLVER = new FileRenderViewResolver();
    private static final IRenderViewResolver SSE_EMITTER_RENDER_VIEW_RESOLVER = new SseEmitterRenderViewResolver();
    private static final IRenderViewResolver CACHE_FILE_STREAM_RENDER_VIEW_RESOLVER = new CacheFileStreamRenderViewResolver();

    private static final Map<Class<?>, IRenderViewResolver> RENDER_VIEW_RESOLVER_MAPPING = new HashMap<>();
    static {
        RENDER_VIEW_RESOLVER_MAPPING.put(String.class, DEFAULT_RENDER_VIEW_RESOLVER);
        RENDER_VIEW_RESOLVER_MAPPING.put(File.class, FILE_RENDER_VIEW_RESOLVER);
        RENDER_VIEW_RESOLVER_MAPPING.put(SseEmitter.class, SSE_EMITTER_RENDER_VIEW_RESOLVER);
        RENDER_VIEW_RESOLVER_MAPPING.put(CachedFileStream.class, CACHE_FILE_STREAM_RENDER_VIEW_RESOLVER);
    }

    public static IRenderViewResolver getRenderViewResolver(Class<?> clazz) {
        IRenderViewResolver resolver = RENDER_VIEW_RESOLVER_MAPPING.get(clazz);
        return resolver == null ? DEFAULT_RENDER_VIEW_RESOLVER : resolver;
    }
}
