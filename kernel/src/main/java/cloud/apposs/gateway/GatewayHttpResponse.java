package cloud.apposs.gateway;

import cloud.apposs.util.Cookie;
import cloud.apposs.util.SseEmitter;

import java.io.File;
import java.io.IOException;

public interface GatewayHttpResponse {
    /**
     * 获取响应状态码
     */
    int getStatus();

    /**
     * 添加响应头部数据
     */
    void putHeader(String key, String value);

    /**
     * 移除响应头部数据
     */
    void removeHeader(String key);

    /**
     * 设置响应状态码
     */
    void setStatus(int status);

    /**
     * 获取响应Content-Type
     * @return
     */
    String getContentType();

    /**
     * 设置响应Content-Type
     */
    void setContentType(String contentType);

    /**
     * 设置响应Cookie
     */
    void setCookie(Cookie cookie);

    /**
     * 响应字符串
     *
     * @param content 响应字符串
     * @param flush 是否主动触发发送事件
     */
    void write(String content, boolean flush) throws IOException;

    /**
     * 响应字节数据
     *
     * @param content 响应字节数据
     * @param flush 是否主动触发发送事件
     */
    void write(byte[] content, boolean flush) throws IOException;

    /**
     * 媒体文件响应输出，采用数据零拷贝输出到网络
     *
     * @param file 本地硬盘文件
     * @param flush 是否主动触发发送事件
     */
    void write(File file, boolean flush) throws IOException;

    /**
     * SSE数据流输出
     *
     * @param content SSE数据流
     * @param flush 是否主动触发发送事件
     */
    void write(SseEmitter content, boolean flush) throws IOException;

    /**
     * 响应数据立即发送
     */
    void flush() throws IOException;
}
