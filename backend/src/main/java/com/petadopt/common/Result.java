package com.petadopt.common;

import lombok.Data;

/**
 * 统一响应结构：所有接口返回给前端的数据都包成这个格式。
 *
 * <p>约定返回结构为 {@code {code, msg, data}}：</p>
 * <ul>
 *   <li>code —— 状态码，200 表示成功，其余表示各种错误；</li>
 *   <li>msg —— 提示信息，给用户看的文案；</li>
 *   <li>data —— 实际数据，成功时放业务数据，失败时为 null。</li>
 * </ul>
 *
 * <p>好处：前端只需解析这一种结构，不用为每个接口单独适配返回格式。</p>
 *
 * @param <T> data 的类型
 */
@Data
public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功响应（携带数据）。
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功响应（无数据）。
     */
    public static Result<Void> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 失败响应（默认错误码 500）。
     */
    public static Result<Void> error(String msg) {
        return new Result<>(500, msg, null);
    }

    /**
     * 失败响应（自定义错误码）。
     */
    public static Result<Void> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }
}
