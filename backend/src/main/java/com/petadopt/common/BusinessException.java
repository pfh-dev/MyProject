package com.petadopt.common;

/**
 * 业务异常：用来表达"业务规则不满足"的预期错误。
 *
 * <p>例如"用户名已存在""密码错误""宠物已被领养"这类情况，不是程序 bug，
 * 而是业务上不允许。代码里主动抛出这个异常，再由全局异常处理器
 * {@link GlobalExceptionHandler} 统一转成友好提示返回给前端，
 * 避免 Service 层到处写返回码。</p>
 */
public class BusinessException extends RuntimeException {

    /** 业务错误码，便于前端区分具体错误类型 */
    private final int code;

    /**
     * 用默认错误码 500 构造业务异常。
     *
     * @param message 给用户看的错误提示
     */
    public BusinessException(String message) {
        this(500, message);
    }

    /**
     * 用自定义错误码构造业务异常。
     *
     * @param code    业务错误码
     * @param message 给用户看的错误提示
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取业务错误码。
     */
    public int getCode() {
        return code;
    }
}
