package com.petadopt.common;

import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器：兜住整个系统抛出的异常，统一转成标准 JSON 响应。
 *
 * <p>作用：Controller / Service 里不必到处 try-catch，任何异常都会最终落到这里，
 * 转换成 {@link Result} 返回给前端，保证前端永远拿到统一的 {code, msg, data} 结构，
 * 而不是难看的 500 报错页。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常：把业务错误码和提示直接返回给前端。
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验失败异常（@Valid 校验不通过）。
     *
     * <p>当请求体字段不满足校验规则（如手机号格式错、必填项为空）时，
     * Spring 会抛出本异常，这里取出第一条错误提示返回给用户。</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        // 从所有校验失败的字段里，取第一条有意义的错误信息
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(StringUtils::hasLength)
                .findFirst()
                .orElse("参数校验失败");
        return Result.error(400, msg);
    }

    /**
     * 兜底异常处理：捕获其他所有未预期的异常，统一返回 500。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        // 打印堆栈，方便排查；生产环境可改成记录日志
        e.printStackTrace();
        return Result.error(500, StringUtils.hasLength(e.getMessage()) ? e.getMessage() : "系统异常");
    }
}
