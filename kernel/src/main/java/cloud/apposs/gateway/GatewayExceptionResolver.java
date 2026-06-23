package cloud.apposs.gateway;

import cloud.apposs.util.HttpStatus;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 网关异常统一捕捉，可自定义错误输出，进行服务降级
 */
public class GatewayExceptionResolver {
    public void resolveException(GatewayHttpRequest request, GatewayHttpResponse response, Throwable throwable) throws IOException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        response.setContentType("text/html; charset=utf-8");
        HttpStatus httpStatus = HttpStatus.HTTP_STATUS_500;
        if (throwable instanceof GatewayException) {
            httpStatus = ((GatewayException) throwable).getStatus();
        }
        response.setStatus(httpStatus.getCode());
        String message = throwable.getMessage() != null ? throwable.getMessage() : throwable.toString();
        response.write(httpStatus.getCode() + " " + httpStatus.getDescription() + ": " + message, true);
    }
}
