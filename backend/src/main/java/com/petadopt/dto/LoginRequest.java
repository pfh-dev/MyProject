package com.petadopt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求参数（前端登录时提交的表单数据）。
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;   // 登录用户名

    @NotBlank(message = "密码不能为空")
    private String password;   // 登录密码
}
