package cloud.apposs.gateway.ai.api;

import cloud.apposs.gateway.ai.AIProvider;
import cloud.apposs.gateway.ai.AIProviderApi;
import cloud.apposs.gateway.ai.AIProviderConstant;
import cloud.apposs.okhttp.FormEntity;
import cloud.apposs.okhttp.OkRequest;
import cloud.apposs.util.Param;
import cloud.apposs.util.Table;

import java.util.Map;
import java.util.Objects;

public class OpenAIProviderApi implements AIProviderApi {
    @Override
    public OkRequest build(AIProvider provider, Param parameters, Map<String, String> headers) throws Exception {
        boolean stream = parameters.getBoolean(AIProvider.Parameter.STREAM, false);
        String model = parameters.getString(AIProvider.Parameter.MODEL);
        Table<Param> messages = parameters.getTable(AIProvider.Parameter.MESSAGES);
        String systemPrompt = parameters.getString(AIProvider.Parameter.SYSTEM_PROMPT);
        String userPrompt = parameters.getString(AIProvider.Parameter.USER_PROMPT);
        if (messages == null || messages.isEmpty()) {
            messages = Table.builder();
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                messages = messages.setParam(Param.builder("role", "system").setString("content", systemPrompt));
            }
            if (userPrompt != null && !userPrompt.isEmpty()) {
                messages = messages.setParam(Param.builder("role", "user").setString("content", userPrompt));
            }
        }
        double temperature = parameters.getDouble(AIProvider.Parameter.TEMPERATURE, 0.7);
        double topP = parameters.getDouble(AIProvider.Parameter.TOP_P, 0.9);
        FormEntity formEntity = FormEntity.builder(FormEntity.FORM_ENCTYPE_JSON)
                .add("stream", stream)
                .add("model", model)
                .add("messages", messages)
                .add("top_p", temperature)
                .add("temperature", topP);
        OkRequest request = OkRequest.builder().url(provider.getUrl()).sse(stream).post(formEntity);
        String key = handleKeyPickup(parameters.getTable(AIProviderConstant.KEY_KEYS));
        if (Objects.nonNull(key)) {
            request.header("Authorization", "Bearer " + key);
        }
        return request;
    }

    private String handleKeyPickup(Table<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        if (keys.size() == 1) {
            return keys.get(0);
        }
        // 随机获取一个key
        return keys.get(Math.abs(keys.hashCode()) % keys.size());
    }
}
