package cloud.apposs.gateway.upstream;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.util.Param;

public abstract class AbstractUpstream implements Upstream {
    public static final String KEY_WEBSOCKET = "websocket";

    protected final String id;

    protected final String type;

    protected final String name;

    protected final GatewayContext context;

    protected final Param configuration;

    protected final boolean websocket;

    public AbstractUpstream(String id, String type, String name, GatewayContext context, Param configuration) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.context = context;
        this.configuration = configuration;
        this.websocket = configuration.getBoolean(KEY_WEBSOCKET, false);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isWebSocket() {
        return websocket;
    }

    @Override
    public String chooseWebSocketUrl(String path) {
        return null;
    }

    @Override
    public void shutdown() {
        // do nothing
    }

    @Override
    public String toString() {
        return "Upstream {" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
