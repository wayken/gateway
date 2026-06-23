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

/**
 * 基于Google Gemini的大模型调用API，参考
 * <pre>
 *     https://ai.google.dev/gemini-api/docs/text-generation?hl=zh-cn
 * </pre>
 */
public class GeminiAIProviderApi implements AIProviderApi {
    /**
     * 构建API请求，请求示例
     * <pre>
     *   curl -v -k "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent" \
     *   -H "x-goog-api-key: AIzaSyC8V6cr6reEPEzH7n30p1Y_XpAuL5ZD71c" \
     *   -H 'Content-Type: application/json' \
     *   -X POST \
     *   -d '{
     *     "system_instruction": {
     *       "parts": [
     *         {
     *           "text": "You are a cat. Your name is Neko."
     *         }
     *       ]
     *     },
     *     "contents": [
     *       {
     *         "role": "user",
     *         "parts": [
     *           {
     *             "text": "How does AI work?"
     *           }
     *         ]
     *       }
     *     ]
     *   }'
     * </pre>
     */
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
        Param geminiMessage = handleMessagesConvert(messages);
        FormEntity formEntity = FormEntity.builder(FormEntity.FORM_ENCTYPE_JSON)
                .add("system_instruction", geminiMessage.getParam("system_instruction"))
                .add("contents", geminiMessage.getTable("contents"));
        OkRequest request = null;
        if (stream) {
            request = OkRequest.builder().url(provider.getUrl() + "/models/" + model + "?alt=sse").sse(true).post(formEntity);
        } else {
            request = OkRequest.builder().url(provider.getUrl() + "/models/" + model).post(formEntity);
        }
        String key = handleKeyPickup(parameters.getTable(AIProviderConstant.KEY_KEYS));
        if (Objects.nonNull(key)) {
            request.header("x-goog-api-key", key);
        }
        return request;
    }

    private static final Param handleMessagesConvert(Table<Param> messages) {
        Param geminiMessage = Param.builder();
        for (Param message : messages) {
            String role = message.getString("role");
            if ("system".equals(role)) {
                Table<Param> parts = Table.builder();
                parts.add(Param.builder("text", message.getString("content")));
                geminiMessage.setParam("system_instruction", Param.builder("parts", parts));
            } else if ("user".equals(role)) {
                Param content = Param.builder();
                content.setString("role", "user");
                Table<Param> parts = Table.builder();
                parts.add(Param.builder("text", message.getString("content")));
                content.setTable("parts", parts);
                Table<Param> contents = geminiMessage.getTable("contents");
                if (contents == null) {
                    contents = Table.builder();
                    geminiMessage.setTable("contents", contents);
                }
                contents.add(content);
            } else if ("assistant".equals(role)) {
                Param content = Param.builder();
                content.setString("role", "model");
                Table<Param> parts = Table.builder();
                parts.add(Param.builder("text", message.getString("content")));
                content.setTable("parts", parts);
                Table<Param> contents = geminiMessage.getTable("contents");
                if (contents == null) {
                    contents = Table.builder();
                    geminiMessage.setTable("contents", contents);
                }
                contents.add(content);
            }
        }
        return geminiMessage;
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
