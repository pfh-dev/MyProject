package com.petadopt.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户的信息载体（存入 ThreadLocal）。
 *
 * <p>登录拦截器解析 JWT 后，把用户信息封装成这个对象塞进 {@link UserContext}，
 * 之后同一请求里的任意代码都能通过它拿到"当前是谁在操作"。</p>
 *
 * <p>它本质上就是一把"临时的登录凭证快照"，只活在一次请求内，请求结束即销毁。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    /** 用户主键 id */
    private Long id;
    /** 用户名 */
    private String username;
    /** 角色标识，如 ADMIN / STAFF / VOLUNTEER / USER */
    private String role;
    /** 当前令牌的唯一 id（jti），用于登出时精确拉黑 */
    private String jti;
    /** 令牌过期时间（毫秒时间戳），用于判断会话还剩多久有效 */
    private long expiresAt;
}
