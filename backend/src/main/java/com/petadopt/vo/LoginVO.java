package com.petadopt.vo;

import com.petadopt.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录成功后的返回结果（token 凭证 + 用户信息）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private String token;  // 登录令牌，前端后续请求需携带它
    private User user;     // 用户信息（不含密码）
}
