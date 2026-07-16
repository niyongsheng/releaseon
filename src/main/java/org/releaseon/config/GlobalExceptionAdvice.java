package org.releaseon.config;

import org.apache.shiro.authz.AuthorizationException;
import org.releaseon.utils.response.BaseResponse;
import org.releaseon.utils.response.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 * 统一处理各层异常，替代 Controller 中零散的 try-catch
 */
@ControllerAdvice
public class GlobalExceptionAdvice {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionAdvice.class);

    /**
     * 权限不足
     */
    @ExceptionHandler(AuthorizationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse handleAuthorization(AuthorizationException e) {
        log.warn("权限不足: {}", e.getMessage());
        return ResponseUtil.unauthz();
    }

    /**
     * 资源不存在（Spring Boot 默认 404）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public BaseResponse handleNotFound(NoResourceFoundException e) {
        return ResponseUtil.fail(404, "资源不存在");
    }

    /**
     * 参数错误
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse handleIllegalArgument(IllegalArgumentException e) {
        log.warn("参数错误: {}", e.getMessage());
        return ResponseUtil.badArgument();
    }

    /**
     * 通用兜底
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse handleException(Exception e) {
        log.error("系统内部错误", e);
        return ResponseUtil.serious();
    }
}
