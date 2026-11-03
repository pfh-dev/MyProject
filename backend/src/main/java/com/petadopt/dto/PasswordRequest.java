package com.petadopt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码请求参数。
 */
@Data
public class PasswordRequest {

    @NotBlank(message = "原密码不能为空")
    private String oldPassword;  // 原密码

    @NotBlank(message = "新密码不能为空")
    private String newPassword;  // 新密码
}
