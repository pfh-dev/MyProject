package com.petadopt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注册请求参数（前端注册时提交的表单数据）。
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;   // 登录用户名（必填）

    @NotBlank(message = "密码不能为空")
    private String password;   // 登录密码（必填）

    private String nickname;   // 昵称（选填）
    private String phone;      // 手机号（选填）
    private String email;      // 邮箱（选填）
}
