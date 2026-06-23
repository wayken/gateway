package cloud.apposs.gateway.ai;

import cloud.apposs.util.JsonUtil;
import cloud.apposs.util.Param;
import cloud.apposs.util.Table;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * AI服务商封装类，封装了AI服务商的配置信息
 */
public class AIProvider {
    private static final String RESOURCE_FILE = "providers.json";

    private final String id;

    private final String name;

    private final String type;

    private final boolean system;

    private final String url;

    private final Table<String> models;

    private final Table<Param> keys;

    public static final Map<String, AIProvider> AI_SYSTEM_PROVIDERS = new HashMap<>();
    static {
        InputStream resource = AIProvider.class.getClassLoader().getResourceAsStream(RESOURCE_FILE);
        Table<Param> providers = JsonUtil.parseJsonTable(resource);
        for (Param provider : providers) {
            String id = provider.getString(AIProviderConstant.KEY_ID);
            String name = provider.getString(AIProviderConstant.KEY_NAME);
            String type = provider.getString(AIProviderConstant.KEY_TYPE);
            boolean system = provider.getBoolean(AIProviderConstant.KEY_SYSTEM, false);
            String url = provider.getString(AIProviderConstant.KEY_URL);
            Table<String> models = provider.getTable(AIProviderConstant.KEY_MODELS);
            Table<Param> keys = provider.getTable(AIProviderConstant.KEY_KEYS);
            AI_SYSTEM_PROVIDERS.put(id, new AIProvider(id, name, type, system, url, models, keys));
        }
    }

    public AIProvider(String id, String name, String type, boolean system, String url, Table<String> models) {
        this(id, name, type, system, url, models, Table.builder());
    }

    public AIProvider(String id, String name, String type, boolean system, String url, Table<String> models, Table<Param> keys) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.system = system;
        this.url = url;
        this.models = models;
        this.keys = keys;
    }

    public static AIProvider build(String id, String name, String type, boolean system, String url, Table<String> models) {
        return new AIProvider(id, name, type, system, url, models);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isSystem() {
        return system;
    }

    public String getUrl() {
        return url;
    }

    public Table<String> getModels() {
        return models;
    }

    public Table<Param> getKeys() {
        return keys;
    }

    /**
     * 将源AI服务商和默认AI服务商进行合并
     * @param providers 原AI服务商列表
     */
    public static Map<String, AIProvider> mergeSystemProviders(Map<String, AIProvider> providers) {
        for (Map.Entry<String, AIProvider> entry : AI_SYSTEM_PROVIDERS.entrySet()) {
            String id = entry.getKey();
            AIProvider provider = entry.getValue();
            if (!providers.containsKey(id)) {
                providers.put(id, provider);
            }
        }
        return providers;
    }

    public static Table<Param> mergeSystemProviders(Table<Param> providers) {
        for (Map.Entry<String, AIProvider> entry : AI_SYSTEM_PROVIDERS.entrySet()) {
            String id = entry.getKey();
            AIProvider provider = entry.getValue();
            boolean found = false;
            for (Param data : providers) {
                if (data.getString("id").equals(id)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                continue;
            }
            Param param = Param.builder(AIProviderConstant.KEY_ID, provider.getId())
                    .setString(AIProviderConstant.KEY_NAME, provider.getName())
                    .setString(AIProviderConstant.KEY_TYPE, provider.getType())
                    .setBoolean(AIProviderConstant.KEY_SYSTEM, provider.isSystem())
                    .setString(AIProviderConstant.KEY_URL, provider.getUrl())
                    .setTable(AIProviderConstant.KEY_MODELS, provider.getModels())
                    .setTable(AIProviderConstant.KEY_KEYS, provider.getKeys());
            providers.add(param);
        }
        return providers;
    }

    public static AIProvider matchSystemProvider(String id) {
        return AI_SYSTEM_PROVIDERS.get(id);
    }

    @Override
    public String toString() {
        return "AIProvider{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", system=" + system +
                ", url='" + url + '\'' +
                ", models=" + models +
                '}';
    }

    public static final class Parameter {
        public static final String STREAM = "stream";
        public static final String MODEL = "model";
        public static final String MESSAGES = "messages";
        public static final String SYSTEM_PROMPT = "systemPrompt";
        public static final String USER_PROMPT = "userPrompt";
        public static final String TEMPERATURE = "temperature";
        public static final String TOP_P = "topP";
        public static final String MAX_TOKENS = "maxTokens";
    }
}
