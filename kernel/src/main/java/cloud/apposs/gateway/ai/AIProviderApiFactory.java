package cloud.apposs.gateway.ai;

import cloud.apposs.gateway.ai.api.GeminiAIProviderApi;
import cloud.apposs.gateway.ai.api.OpenAIProviderApi;

import java.util.Objects;

public final class AIProviderApiFactory {
    public static AIProviderApi create(String provider) {
        if (Objects.equals(AIProviderApi.OPENAI, provider)) {
            return new OpenAIProviderApi();
        } else if (Objects.equals(AIProviderApi.GEMINI, provider)) {
            return new GeminiAIProviderApi();
        }
        throw new IllegalArgumentException("Unsupported AI Provider: " + provider);
    }
}
