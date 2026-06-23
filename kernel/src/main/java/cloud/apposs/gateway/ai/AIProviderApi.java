package cloud.apposs.gateway.ai;

import cloud.apposs.okhttp.OkRequest;
import cloud.apposs.util.Param;

import java.util.Map;

/**
 * AI接口，目前支持：OpenAI、Gemini两个API类型接口
 */
public interface AIProviderApi {
    String OPENAI = "openai";
    String GEMINI = "gemini";

    /**
     * 构建请求体
     * @param provider   AI模型服务商
     * @param parameters 请求参数
     * @param headers    请求头
     */
    OkRequest build(AIProvider provider, Param parameters, Map<String, String> headers) throws Exception;
}
