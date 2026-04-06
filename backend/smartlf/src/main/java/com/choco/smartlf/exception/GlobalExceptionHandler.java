package com.choco.smartlf.exception;

import com.choco.smartlf.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.servlet.http.HttpServletResponse;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获我们刚才自定义的 BusinessException
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务拦截：{}", e.getMessage());
        // 将自定义的错误码和信息包装成统一 Result 返回
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 专门捕获 @Validated 参数校验失败抛出的异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        // 把所有校验失败的字段信息收集起来，用换行符拼接
        String errorMsg = e.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("\n"));

        log.warn("请求参数校验失败：{}", errorMsg);

        // 400 是 HTTP 协议里标准的 Bad Request (请求参数错误) 状态码
        return Result.error(HttpServletResponse.SC_BAD_REQUEST, errorMsg);
    }

    /**
     * 兜底防线：捕获系统里所有意料之外的 Exception (比如空指针、SQL报错)
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleSystemException(Exception e) {
        log.error("系统内部异常：", e);
        return Result.error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务器开小差了，请联系管理员");
    }

    /**
     * 忽略浏览器自动请求 favicon.ico 找不到的报错
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFoundException(NoResourceFoundException e) {
       log.warn("静态资源未找到: {}", e.getResourcePath());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String parameterName = e.getParameterName();
        log.warn("请求参数缺失: {}", parameterName);
        return Result.error(HttpServletResponse.SC_BAD_REQUEST, "请求参数"+parameterName+"缺失");
    }
}