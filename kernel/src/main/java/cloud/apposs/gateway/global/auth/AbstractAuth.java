package cloud.apposs.gateway.global.auth;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.util.Param;

public abstract class AbstractAuth implements Auth {
    protected final String id;

    protected final String type;

    protected final String name;

    protected final GatewayContext context;

    protected final Param configuration;

    public AbstractAuth(String id, String type, String name, GatewayContext context, Param configuration) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.context = context;
        this.configuration = configuration;
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
    public String toString() {
        return "Auth {" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
